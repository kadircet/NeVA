#include "user_history_orm.h"
#include "glog/logging.h"

namespace neva {
namespace backend {
namespace orm {

namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status UserHistoryOrm::InsertChoice(const uint32_t user_id,
                                    const Choice& choice) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "INSERT INTO `user_choice_history` (`user_id`, `suggestee_id`, "
      "`timestamp`, `latitude`, `longitude`) VALUES (%0, %1, %2, %3, %4)");
  query.parse();
  if (!query.execute(user_id, choice.suggestee_id(),
                     choice.timestamp().seconds(), choice.latitude(),
                     choice.longitude())) {
    return Status(StatusCode::INTERNAL, "Internal server error.");
  }

  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
