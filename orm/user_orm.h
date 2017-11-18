#ifndef _NEVA_ORM_USERS_H_
#define _NEVA_ORM_USERS_H_

#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include "protos/user.pb.h"

namespace neva {
namespace backend {
namespace orm {
namespace user {

class UserOrm {
 public:
  // Initiates UserOrm class wih given mysql connection.
  UserOrm(std::shared_ptr<mysqlpp::Connection> conn) : conn_(conn) {}

  // Fills in the given user object's id, email and status fields, where user_id
  // specifies which user to be fetched. Returns Status::OK on success.
  grpc::Status GetUserById(const uint32_t user_id, User* user);

  // Fills in the given user object's id, email and status fields, where email
  // specifies which user to be fetched. Returns Status::OK on success.
  grpc::Status GetUserByEmail(const std::string& email, User* user);

  // Checks whether credentials given in the user matches with the ones in
  // database. email and password fields of the user object must be filled.
  grpc::Status CheckCredentials(const User& user);

 private:
  // Returns salt used for the user specified by the email parameter.
  const std::string GetSaltByEmail(const std::string& email);

  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace user
}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
