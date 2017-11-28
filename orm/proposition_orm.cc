#include "proposition_orm.h"

namespace neva {
namespace backend {
namespace orm {
namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status PropositionOrm::InsertProposition(const int user_id,
                                         const Suggestion& suggestion) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "INSERT INTO `item_suggestion` (`user_id`, `category_id`, `suggestion`) "
      "VALUES (%0, %1, %2q)");
  query.parse();

  const mysqlpp::SimpleResult result = query.execute(
      user_id, suggestion.suggestion_category(), suggestion.name());
  if (result) return Status::OK;
  return Status(StatusCode::UNKNOWN, "Internal error.");
}

Status PropositionOrm::InsertProposition(const int user_id,
                                         const std::string& tag) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "INSERT INTO `tag_suggestion` (`user_id`, `tag`) "
      "VALUES (%0, %1q)");
  query.parse();

  const mysqlpp::SimpleResult result = query.execute(user_id, tag);
  if (result) return Status::OK;
  return Status(StatusCode::UNKNOWN, "Internal error.");
}

Status PropositionOrm::InsertProposition(const int user_id, const int tag_id,
                                         const int suggestee_id,
                                         const std::string& value) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "INSERT INTO `tag_suggestion` (`user_id`, `tag_id`, `suggestee_id`, "
      "`value`) VALUES (%0, %1, %2, %3q)");
  query.parse();

  const mysqlpp::SimpleResult result =
      query.execute(user_id, tag_id, suggestee_id, value);
  if (result) return Status::OK;
  return Status(StatusCode::UNKNOWN, "Internal error.");
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
