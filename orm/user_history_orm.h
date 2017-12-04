#ifndef _NEVA_ORM_USER_HISTORY_H_
#define _NEVA_ORM_USER_HISTORY_H_

#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include "protos/user_history.pb.h"

namespace neva {
namespace backend {
namespace orm {

class UserHistoryOrm {
 public:
  // Initiates UserHistoryOrm class wih given mysql connection.
  UserHistoryOrm(std::shared_ptr<mysqlpp::Connection> conn) : conn_(conn) {}

  // Inserts one choice associated with user_id into database.
  grpc::Status InsertChoice(const uint32_t user_id, const Choice& choice);

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
