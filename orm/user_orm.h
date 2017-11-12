#ifndef _NEVA_ORM_USERS_H_
#define _NEVA_ORM_USERS_H_

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
  UserOrm(std::shared_ptr<mysqlpp::Connection> conn) : conn_(conn) {}
  User GetUserById(const uint32_t user_id);

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace user
}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
