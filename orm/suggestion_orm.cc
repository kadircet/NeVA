#include "orm/suggestion_orm.h"
#include "glog/logging.h"

namespace neva {
namespace backend {
namespace orm {

namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status SuggestionOrm::GetSuggestees(
    const Suggestion::SuggestionCategory suggestion_category,
    const uint32_t start_index, std::vector<Suggestion>* suggestees) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "SELECT `name` FROM `suggestee` WHERE `category_id`=%0 AND `id`>%1");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(suggestion_category, start_index);
  for (const auto row : res) {
    Suggestion suggestion;
    suggestion.set_name(row["name"]);
    suggestees->push_back(suggestion);
  }
  return Status::OK;
}

Status SuggestionOrm::GetSuggestion(
    const Suggestion::SuggestionCategory suggestion_category,
    Suggestion* suggestion) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "SELECT `name` FROM `suggestee` WHERE `category_id`=%0 ORDER BY RAND() "
      "LIMIT 1");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(suggestion_category);
  if (res.empty()) {
    return Status(StatusCode::INVALID_ARGUMENT,
                  "No items to suggest in that category.");
  }
  suggestion->set_name(res[0]["name"]);
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
