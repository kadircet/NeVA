#include "user_orm.h"
#include "glog/logging.h"
#include "util/hmac.h"

namespace neva {
namespace backend {
namespace orm {
namespace user {

namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status UserOrm::GetUserById(const uint32_t user_id, User* user) {
  if (conn_.get() == nullptr) {
    return Status(StatusCode::UNKNOWN, "Connection was null.");
  }
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
  if (conn_.get() == nullptr) {
    return Status(StatusCode::UNKNOWN, "Connection was null.");
  }
  mysqlpp::Query query =
      conn_->query("SELECT `id`, `status` FROM `user` WHERE `email`=:%0");
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

Status UserOrm::CheckCredentials(const User& user) {
  if (conn_.get() == nullptr) {
    return Status(StatusCode::UNKNOWN, "Connection was null.");
  }
  mysqlpp::Query query = conn_->query(
      "SELECT `id`, `salt`, `status`, `password` FROM `user` WHERE "
      "`email`=:%0");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(user.email());
  if (res.empty()) return Status(StatusCode::UNKNOWN, "Wrong credentials.");
  if (res.num_rows() != 1) {
    return Status(StatusCode::UNKNOWN,
                  "More than one user matches this email.");
  }

  if (res[0]["password"] ==
      util::HMac(static_cast<const std::string>(res[0]["salt"]),
                 user.password())) {
    return Status::OK;
  }

  return Status(StatusCode::INVALID_ARGUMENT, "Wrong credentials.");
}

}  // namespace user
}  // namespace orm
}  // namespace backend
}  // namespace neva
