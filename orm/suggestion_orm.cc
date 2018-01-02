#include "orm/suggestion_orm.h"
#include "glog/logging.h"
#include "recommender/recommender.h"

namespace neva {
namespace backend {
namespace orm {

namespace {

using grpc::Status;
using grpc::StatusCode;

// Fetches tags associated with given suggestee and stores them into it.
// Assumes suggestee->suggestee_id is set.
void GetTags(mysqlpp::Connection* conn, Suggestion* suggestee) {
  mysqlpp::Query query = conn->query(
      "SELECT `tag_id` FROM `suggestee_tags` WHERE `suggestee_id`=%0");
  query.parse();
  const mysqlpp::StoreQueryResult res = query.store(suggestee->suggestee_id());
  for (const auto row : res) {
    Tag* tag = suggestee->add_tags();
    tag->set_id(row["tag_id"]);
  }
}

}  // namespace

Status SuggestionOrm::GetSuggestees(
    const Suggestion::SuggestionCategory suggestion_category,
    const uint32_t start_index, SuggestionList* suggestion_list,
    uint32_t* last_updated) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  mysqlpp::Query query = conn_->query(
      "SELECT `id`, `name`, `last_updated` FROM `suggestee` WHERE "
      "`category_id`=%0 AND `last_updated`>%1");
  query.parse();

  const mysqlpp::StoreQueryResult res =
      query.store(suggestion_category, start_index);
  uint32_t max_updated = start_index;
  for (const auto row : res) {
    Suggestion suggestion;
    suggestion.set_suggestee_id(row["id"]);
    suggestion.set_name(row["name"]);
    max_updated =
        std::max(max_updated, static_cast<uint32_t>(row["last_updated"]));
    GetTags(conn_.get(), &suggestion);
    *suggestion_list->add_suggestion_list() = suggestion;
    VLOG(1) << suggestion.ShortDebugString() << " has been added to response.";
  }
  if (last_updated != nullptr) *last_updated = max_updated;
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

Status SuggestionOrm::GetMultipleSuggestions(
    const UserHistory& user_history,
    const Suggestion::SuggestionCategory suggestion_category,
    SuggestionList* suggestion_list) {
  if (!conn_->ping()) {
    return Status(StatusCode::UNKNOWN, "SQL server connection faded away.");
  }

  SuggestionList all_suggestees;
  GetSuggestees(suggestion_category, 0, &all_suggestees);

  if (all_suggestees.suggestion_list_size() == 0) {
    VLOG(1) << "Requested a suggestion from an empty category: "
            << suggestion_category;
    return Status(StatusCode::INVALID_ARGUMENT,
                  "No items to suggest in that category.");
  }

  *suggestion_list = recommender::GetMultipleSuggestions(user_history, all_suggestees);
  VLOG(1) << "Returning:\n" << suggestion_list->ShortDebugString();
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
