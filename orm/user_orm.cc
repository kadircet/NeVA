#include "user_orm.h"
#include "glog/logging.h"
#include "util/hmac.h"
#include "util/time.h"

namespace neva {
namespace backend {
namespace orm {
namespace user {

namespace {

using grpc::Status;
using grpc::StatusCode;

// 24 hours in seconds.
constexpr const uint64_t kVerficationTokenExpireTime = 24 * 60 * 60;

}  // namespace

Status UserOrm::GetUserById(const uint32_t user_id, User* user) {
  conn_->ping();

  mysqlpp::Query query =
      conn_->query("SELECT `email`, `status` FROM `user` WHERE `id`=:%0");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(user_id);
  if (res.empty()) return Status(StatusCode::UNKNOWN, "User not found.");
  if (res.num_rows() != 1) {
    return Status(StatusCode::UNKNOWN, "More than one user matches this id.");
  }

  user->set_user_id(user_id);
  user->set_email(res[0]["email"]);
  user->set_status(
      static_cast<User::Status>(static_cast<int>(res[0]["status"])));
  return Status::OK;
}

Status UserOrm::GetUserByEmail(const std::string& email, User* user) {
  conn_->ping();

  mysqlpp::Query query =
      conn_->query("SELECT `id`, `status` FROM `user` WHERE `email`=:%0q");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(email);
  if (res.empty()) return Status(StatusCode::UNKNOWN, "User not found.");
  if (res.num_rows() != 1) {
    return Status(StatusCode::UNKNOWN,
                  "More than one user matches this email.");
  }

  user->set_user_id(res[0]["id"]);
  user->set_email(email);
  user->set_status(
      static_cast<User::Status>(static_cast<int>(res[0]["status"])));
  return Status::OK;
}

Status UserOrm::CheckCredentials(const User& user, std::string* session_token) {
  return CheckCredentials(user.email(), user.password(), session_token);
}

Status UserOrm::CheckCredentials(const std::string& email,
                                 const std::string& password,
                                 std::string* session_token) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "SELECT `id`, `salt`, `status`, `password` FROM `user` WHERE "
      "`email`=%0q");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(email);
  if (res.empty()) return Status(StatusCode::UNKNOWN, "Wrong credentials.");
  if (res.num_rows() != 1) {
    return Status(StatusCode::UNKNOWN,
                  "More than one user matches this email.");
  }

  // TODO(kadircet): Implement Status::ACTIVE check after sending verification
  // emails.
  if (res[0]["password"] ==
      util::HMac(static_cast<const std::string>(res[0]["salt"]), password)) {
    query.reset();
    *session_token = util::GenerateRandomKey();
    query << "INSERT INTO `user_session` (`id`, `token`, `expire`) VALUES "
             "(%0, %1q, %2) ON DUPLICATE KEY UPDATE `token`=%1q, `expire`=%2";
    query.parse();
    // TODO(kadircet): Implement token expiration.
    query.execute(res[0]["id"], *session_token, 0);
    return Status::OK;
  }

  return Status(StatusCode::INVALID_ARGUMENT, "Wrong credentials.");
}

Status UserOrm::InsertUser(const User& user, std::string* verification_token) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query();

  {
    query << "SELECT `id` FROM `user` WHERE `email`=%0q";
    query.parse();
    const mysqlpp::StoreQueryResult res = query.store(user.email());
    if (!res.empty()) {
      return Status(StatusCode::INVALID_ARGUMENT,
                    "This mail address has already been registered.");
    }
    query.reset();
  }

  {
    query << "INSERT INTO `user` (`email`, `password`, `status`, `salt`) "
             "VALUES (%0q, %1q, %2, %3q)";
    query.parse();
    const std::string salt = util::GenerateRandomKey();
    const std::string hmac = util::HMac(salt, user.password());
    const int user_id =
        query.execute(user.email(), hmac, User::INACTIVE, salt).insert_id();
    query.reset();

    *verification_token = util::GenerateRandomKey();
    query << "INSERT INTO `user_verification` (`id`, `token`, `expire`) VALUES "
             "(%0, %1q, %2)";
    query.execute(user_id, util::HMac(salt, *verification_token),
                  util::GetTimestamp() + kVerficationTokenExpireTime);
    query.reset();

    // TODO(kadircet): Insert remaining fields into user_info.
  }

  return Status::OK;
}

Status UserOrm::CheckToken(const std::string& token, int* user_id) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query();
  {
    query << "SELECT `id`, `expire` FROM `user_session` WHERE `token`=%0q";
    query.parse();

    const mysqlpp::StoreQueryResult res = query.store(token);
    if (res.empty()) {
      return Status(StatusCode::INVALID_ARGUMENT,
                    "No such session token exists.");
    }
    // TODO(kadircet): Implement session expire check.
    *user_id = res[0]["id"];

    return Status::OK;
  }
}

}  // namespace user
}  // namespace orm
}  // namespace backend
}  // namespace neva
