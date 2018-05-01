#include "user_orm.h"
#include "glog/logging.h"
#include "social_media/facebook.h"
#include "util/error.h"
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
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query =
      conn->query("SELECT `email`, `status` FROM `user` WHERE `id`=:%0");
  query.parse();

  const mysqlpp::StoreQueryResult res = query.store(user_id);
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
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query =
      conn->query("SELECT `id`, `status` FROM `user` WHERE `email`=:%0q");
  query.parse();

  const mysqlpp::StoreQueryResult res = query.store(email);
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
  mysqlpp::ScopedConnection conn(*conn_pool_);

  const bool is_facebook = authentication_type == LoginRequest::FACEBOOK;

  if (is_facebook && !FacebookValidator::Validate(email, password)) {
    return Status(StatusCode::UNKNOWN, "Wrong credentials.");
  }

  mysqlpp::Query query = conn->query(
      "SELECT `id`, `salt`, `status` FROM `user` WHERE `email`=%0q");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(email);
  if (res.empty()) {
    if (!is_facebook) {
      LOG(INFO) << email << " doesn't exists in database.";
      return Status(StatusCode::UNKNOWN, "Wrong credentials.");
    }

    LOG(INFO) << email << " first login using facebook.";
    const User user = FacebookValidator::FetchInfo(email, password);
    InsertUser(user, authentication_type, nullptr);
    res = query.store(email);
  }
  // TODO(kadircet): Implement Status::ACTIVE check after sending verification
  // emails.
  // TODO(kadircet): In future deduce user_id from credentials table instead.
  const int user_id = res[0]["id"];
  if (!is_facebook) {
    const mysqlpp::String sql_salt = res[0]["salt"];

    query.reset();
    query
        << "SELECT `credential` FROM `user_credentials` WHERE `user_id`=%0 AND "
           "`type`=%1";
    query.parse();
    res = query.store(user_id, authentication_type);
    if (res.empty()) {
      VLOG(1) << "No credential of type: " << authentication_type
              << " for user: " << email;
      return Status(StatusCode::UNKNOWN, "Wrong credentials.");
    }
    const mysqlpp::String sql_password = res[0]["credential"];
    const std::string hash(sql_password.data(), sql_password.size());
    const std::string salt(sql_salt.data(), sql_salt.size());
    if (hash != util::HMac(salt, password)) {
      VLOG(1) << email << " tried to authenticate with wrong credential.";
      return Status(StatusCode::INVALID_ARGUMENT, "Wrong credentials.");
    }
  }

  query.reset();
  query << "SELECT `user_id` FROM `user_session` WHERE `token`=%0q";
  query.parse();

  do {
    *session_token = util::GenerateRandomKeySecure();
    res = query.store(*session_token);
  } while (!res.empty());
  query.reset();

  // TODO(kadircet): Implement token expiration.
  query << "INSERT INTO `user_session` (`user_id`, `token`, `expire`) VALUES "
           "(%0, %1q, %2)";
  query.parse();

  if (!query.execute(user_id, *session_token, 0)) {
    VLOG(1) << "Query failed with:" << query.error();
    return Status(StatusCode::INTERNAL, "Internal server error.");
  }

  VLOG(1) << email << " has been authenticated successfully.";
  return Status::OK;
}

Status UserOrm::AddCredential(
    const uint32_t user_id, const std::string& salt, const User& user,
    const LoginRequest::AuthenticationType authentication_type,
    std::string* verification_token) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  const std::string hmac = util::HMac(salt, user.password());
  mysqlpp::Query query = conn->query(
      "INSERT INTO `user_credentials` (`user_id`, `credential`, `type`) "
      "VALUES (%0, %1q, %2)");
  query.parse();
  if (!query.execute(user_id, hmac, authentication_type)) {
    return Status(StatusCode::INTERNAL, query.error());
  }
  return Status::OK;
}

Status UserOrm::UpdateVerificationToken(const uint32_t user_id,
                                        const std::string& token,
                                        const uint64_t expire) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "INSERT INTO `user_verification` (`id`, `token`, `expire`) VALUES "
      "(%0, %1q, %2)");
  query.parse();
  if (!query.execute(user_id, token, expire)) {
    return Status(StatusCode::INTERNAL, query.error());
  }
  return Status::OK;
}

Status UserOrm::InsertUser(
    const User& user,
    const LoginRequest::AuthenticationType authentication_type,
    std::string* verification_token) {
  mysqlpp::ScopedConnection conn(*conn_pool_);

  const std::string email = user.email();

  mysqlpp::Query query =
      conn->query("SELECT `id`, `salt` FROM `user` WHERE `email`=%0q");
  query.parse();
  const mysqlpp::StoreQueryResult res = query.store(email);
  if (!res.empty()) {
    VLOG(1) << email << " already exist trying to link new credentials.";
    const uint32_t user_id = res[0]["id"];
    const mysqlpp::String sql_salt = res[0]["salt"];
    const std::string salt(sql_salt.data(), sql_salt.size());
    return AddCredential(user_id, salt, user, authentication_type,
                         verification_token);
  }
  query.reset();
  query << "INSERT INTO `user` (`email`, `status`, `salt`) "
           "VALUES (%0q, %1, %2q)";
  query.parse();
  const std::string salt = util::GenerateRandomKey();
  const int user_id = query.execute(email, User::INACTIVE, salt).insert_id();
  query.reset();

  RETURN_IF_ERROR(AddCredential(user_id, salt, user, authentication_type,
                                verification_token));

  if (verification_token != nullptr) {
    *verification_token = util::GenerateRandomKeySecure();
    RETURN_IF_ERROR(UpdateVerificationToken(
        user_id, util::HMac(salt, *verification_token),
        util::GetTimestamp() + kVerficationTokenExpireTime));
  }

  query << "INSERT INTO `user_info` (`id`, `register_date`) "
           "VALUES (%0, %1)";
  query.parse();
  if (!query.execute(user_id, util::GetTimestamp())) {
    return Status(StatusCode::INTERNAL, query.error());
  }

  RETURN_IF_ERROR(UpdateUserData(user_id, user));
  VLOG(1) << email << " has been successfully registered.";

  return Status::OK;
}

Status UserOrm::UpdateUserData(const int user_id, const User& user) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query();

  if (user.has_date_of_birth()) {
    query << "UPDATE `user_info` "
             "SET `date_of_birth`=%0 "
             "WHERE `id`=%1";
    query.parse();
    query.execute(user.date_of_birth().seconds(), user_id);
    query.reset();
  }
  if (!user.name().empty()) {
    query << "UPDATE `user_info` "
             "SET `name`=%0q "
             "WHERE `id`=%1";
    query.parse();
    query.execute(user.name(), user_id);
    query.reset();
  }
  if (user.gender() != 0) {
    query << "UPDATE `user_info` "
             "SET `gender`=%0 "
             "WHERE `id`=%1";
    query.parse();
    query.execute(user.gender(), user_id);
    query.reset();
  }
  if (user.weight() != 0) {
    query << "UPDATE `user_info` "
             "SET `weight`=%0 "
             "WHERE `id`=%1";
    query.parse();
    query.execute(user.weight(), user_id);
    query.reset();
  }
  if (!user.photo().empty()) {
    query << "UPDATE `user_info` "
             "SET `photo`=%0q "
             "WHERE `id`=%1";
    query.parse();
    query.execute(user.photo(), user_id);
    query.reset();
  }

  VLOG(1) << "User data for user with id: " << user_id
          << " has been successfully updated.";
  return Status::OK;
}

Status UserOrm::GetUserData(const int user_id, User* user) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "SELECT `date_of_birth`, `name`, `gender`, `weight`, `photo`, "
      "`register_date` FROM `user_info` WHERE `id`=%0");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(user_id);
  if (res.empty()) {
    VLOG(1) << "User data for user with id: " << user_id
            << " doesn't exists in database.";
    return Status(StatusCode::UNKNOWN, "User data not found.");
  }

  if (res[0]["date_of_birth"] != mysqlpp::null) {
    util::Timestamp timestamp;
    timestamp.set_seconds(res[0]["date_of_birth"]);
    *user->mutable_date_of_birth() = timestamp;
  }

  if (res[0]["name"] != mysqlpp::null) {
    user->set_name(res[0]["name"]);
  }

  if (res[0]["gender"] != mysqlpp::null) {
    user->set_gender(
        static_cast<User::Gender>(static_cast<int>(res[0]["gender"])));
  }

  if (res[0]["weight"] != mysqlpp::null) {
    user->set_weight(res[0]["weight"]);
  }

  if (res[0]["photo"] != mysqlpp::null) {
    user->set_photo(res[0]["photo"]);
  }

  if (res[0]["register_date"] != mysqlpp::null) {
    util::Timestamp timestamp;
    timestamp.set_seconds(res[0]["register_date"]);
    *user->mutable_register_date() = timestamp;
  }

  return Status::OK;
}

Status UserOrm::CheckToken(const std::string& token, int* user_id) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query();
  query << "SELECT `user_id`, `expire` FROM `user_session` WHERE `token`=%0q";
  query.parse();

  const mysqlpp::StoreQueryResult res = query.store(token);
  if (!res || res.empty()) {
    VLOG(1) << "Session token deosn't exists." << query.error();
    return Status(StatusCode::INVALID_ARGUMENT,
                  "No such session token exists.");
  }
  // TODO(kadircet): Implement session expire check.
  *user_id = res[0]["user_id"];
  VLOG(1) << "Session token belongs to user with id: " << *user_id;

  return Status::OK;
}

Status UserOrm::UserNeedsUpdate(const uint32_t user_id) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query();
  query << "INSERT INTO `user_needs_update` (`user_id`, `needs_update`) VALUES "
           "(%0, 1) ON DUPLICATE UPDATE `needs_update`=1";
  query.parse();

  if (!query.execute(user_id)) {
    LOG(WARNING) << "User needs update failed on query:" << query.str(user_id)
                 << "\nWith error:" << query.error();
    return Status(StatusCode::INTERNAL, query.error());
  }
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
