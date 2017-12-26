#ifndef _NEVA_ORM_USERS_H_
#define _NEVA_ORM_USERS_H_

#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include "protos/backend.pb.h"
#include "protos/user.pb.h"

namespace neva {
namespace backend {
namespace orm {

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
  grpc::Status CheckCredentials(
      const std::string& email, const std::string& password,
      const LoginRequest::AuthenticationType authentication_type,
      std::string* session_token);

  // Inserts given user into database. On success puts verification token
  // generated for user into verification_token if it is not nullptr.
  // - email and password fields of the user must be filled.
  // - Everything except user_id, status, register_date and linked_accounts are
  //   optional. These mentioned fields are not used by this function.
  grpc::Status InsertUser(
      const User& user,
      const LoginRequest::AuthenticationType authentication_type,
      std::string* verification_token);

  // Updates optional profile data like gender, weight and date_of_birth of
  // given user, if user is already registered.
  grpc::Status UpdateUserData(const int user_id, const User& user);

  // Fetchs profile data of given user, if user is already registered.
  grpc::Status GetUserData(const int user_id, User* user);

  // Verifies a given authentication token and sets user_id to id of the user
  // bearing the token.
  grpc::Status CheckToken(const std::string& token, int* user_id);

 private:
  grpc::Status AddCredential(
      const uint32_t user_id, const std::string& salt, const User& user,
      const LoginRequest::AuthenticationType authentication_type,
      std::string* verification_token);
  grpc::Status UpdateVerificationToken(const uint32_t user_id,
                                       const std::string& token,
                                       const uint64_t expire);
  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
