#include "orm/suggestion_orm.h"
#include "glog/logging.h"
#include "recommender/recommender.h"

namespace neva {
namespace backend {
namespace orm {

namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status SuggestionOrm::GetSuggestees(
    const Suggestion::SuggestionCategory suggestion_category,
    const uint32_t start_index, SuggestionList* suggestion_list) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "SELECT `id`, `name` FROM `suggestee` WHERE `category_id`=%0 AND "
      "`id`>%1");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(suggestion_category, start_index);
  for (const auto row : res) {
    Suggestion suggestion;
    suggestion.set_suggestee_id(row["id"]);
    suggestion.set_name(row["name"]);
    *suggestion_list->add_suggestion_list() = suggestion;
    VLOG(1) << suggestion.ShortDebugString() << " has been added to response.";
  }
  return Status::OK;
}

Status SuggestionOrm::GetSuggestion(
    const UserHistory& user_history,
    const Suggestion::SuggestionCategory suggestion_category,
    Suggestion* suggestion) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  SuggestionList suggestion_list;
  GetSuggestees(suggestion_category, 0, &suggestion_list);

  if (suggestion_list.suggestion_list_size() == 0) {
    VLOG(1) << "Requested a suggestion from an empty category: "
            << suggestion_category;
    return Status(StatusCode::INVALID_ARGUMENT,
                  "No items to suggest in that category.");
  }

  *suggestion = recommender::GetSuggestion(user_history, suggestion_list);
  VLOG(1) << "Returning:\n" << suggestion->ShortDebugString();
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
