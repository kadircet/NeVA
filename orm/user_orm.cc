#include "user_orm.h"
#include "glog/logging.h"
#include "social_media/facebook.h"
#include "util/hmac.h"
#include "util/time.h"

namespace neva {
namespace backend {
namespace orm {

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

Status UserOrm::CheckCredentials(
    const std::string& email, const std::string& password,
    const LoginRequest::AuthenticationType authentication_type,
    std::string* session_token) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  const bool is_facebook = authentication_type == LoginRequest::FACEBOOK;

  if (is_facebook && !FacebookValidator::Validate(email, password)) {
    return Status(StatusCode::UNKNOWN, "Wrong credentials.");
  }

  mysqlpp::Query query = conn_->query(
      "SELECT `id`, `salt`, `status` FROM `user` WHERE `email`=%0q");
  query.parse();

  const mysqlpp::StoreQueryResult res = query.store(email);
  if (res.empty()) {
    if (is_facebook) {
      VLOG(1) << email << " first login using facebook.";
      const User user = FacebookValidator::FetchInfo(email, password);
      InsertUser(user, authentication_type, nullptr);
    } else {
      VLOG(1) << email << " doesn't exists in database.";
      return Status(StatusCode::UNKNOWN, "Wrong credentials.");
    }
  }

  // TODO(kadircet): Implement Status::ACTIVE check after sending verification
  // emails.
  // TODO(kadircet): In feature deduce user_id from credentials table instead.
  const int user_id = res[0]["id"];
  const mysqlpp::String sql_salt = res[0]["salt"];
  query.reset();
  query << "SELECT `credential` FROM `user_credentials` WHERE `id`=%0 AND "
           "`type`=%1";
  query.parse();
  const mysqlpp::StoreQueryResult res_credential =
      query.store(user_id, authentication_type);
  const mysqlpp::String sql_password = res[0]["credential"];
  const std::string hash(sql_password.data(), sql_password.size());
  const std::string salt(sql_salt.data(), sql_salt.size());
  if (is_facebook || hash == util::HMac(salt, password)) {
    query.reset();
    *session_token = util::GenerateRandomKey();
    query << "INSERT INTO `user_session` (`id`, `token`, `expire`) VALUES "
             "(%0, %1q, %2) ON DUPLICATE KEY UPDATE `token`=%1q, `expire`=%2";
    query.parse();
    // TODO(kadircet): Implement token expiration.
    query.execute(user_id, *session_token, 0);
    VLOG(1) << email << " has been authenticated successfully.";
    return Status::OK;
  }
  VLOG(1) << email << " tried to authenticate with wrong credential.";

  return Status(StatusCode::INVALID_ARGUMENT, "Wrong credentials.");
}  // namespace orm

Status UserOrm::InsertUser(
    const User& user,
    const LoginRequest::AuthenticationType authentication_type,
    std::string* verification_token) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  const std::string email = user.email();

  mysqlpp::Query query = conn_->query();

  {
    query << "SELECT `id` FROM `user` WHERE `email`=%0q";
    query.parse();
    const mysqlpp::StoreQueryResult res = query.store(email);
    if (!res.empty()) {
      VLOG(1) << email << " already exists in the database.";
      return Status(StatusCode::INVALID_ARGUMENT,
                    "This mail address has already been registered.");
    }
    query.reset();
  }

  {
    query << "INSERT INTO `user` (`email` `status`, `salt`) "
             "VALUES (%0q, %1, %2q)";
    query.parse();
    const std::string salt = util::GenerateRandomKey();
    const int user_id = query.execute(email, User::INACTIVE, salt).insert_id();
    query.reset();

    const std::string hmac = util::HMac(salt, user.password());
    query << "INSERT INTO `user_credentials` (`user_id`, `credential`, `type`) "
             "VALUES (%0, %1q, %2)";
    query.parse();
    query.execute(user_id, hmac, authentication_type);
    query.reset();

    if (verification_token != nullptr) {
      *verification_token = util::GenerateRandomKey();
      query
          << "INSERT INTO `user_verification` (`id`, `token`, `expire`) VALUES "
             "(%0, %1q, %2)";
      query.execute(user_id, util::HMac(salt, *verification_token),
                    util::GetTimestamp() + kVerficationTokenExpireTime);
      query.reset();
    }

    // TODO(kadircet): Insert remaining fields into user_info.
  }
  VLOG(1) << email << " has been successfully registered.";

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
      VLOG(1) << "Session token deosn't exists.";
      return Status(StatusCode::INVALID_ARGUMENT,
                    "No such session token exists.");
    }
    // TODO(kadircet): Implement session expire check.
    *user_id = res[0]["id"];
    VLOG(1) << "Session token belongs to user with id: " << *user_id;

    return Status::OK;
  }
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
