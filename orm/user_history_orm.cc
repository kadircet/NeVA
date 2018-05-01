#include "user_history_orm.h"
#include "glog/logging.h"
#include "orm/utils.h"
#include "util/hmac.h"
namespace neva {
namespace backend {
namespace orm {

namespace {

using grpc::Status;
using grpc::StatusCode;
constexpr uint32_t kMaximumItemNumber = 30;
}  // namespace

Status UserHistoryOrm::InsertChoice(const uint32_t user_id,
                                    const Choice& choice, int* choice_id) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "INSERT INTO `user_choice_history` (`user_id`, `suggestee_id`, "
      "`timestamp`, `latitude`, `longitude`) VALUES (%0, %1, %2, %3, %4)");
  query.parse();
  const mysqlpp::SimpleResult res = query.execute(
      user_id, choice.suggestee_id(), choice.timestamp().seconds(),
      choice.latitude(), choice.longitude());
  if (!res) {
    VLOG(1) << "Something went wrong while inserting:\n"
            << choice.DebugString() << "\nInfo:" << query.info();
    return Status(StatusCode::INTERNAL, "Internal server error.");
  }
  *choice_id = res.insert_id();
  VLOG(1) << choice.DebugString()
          << " inserted succesfully with id: " << *choice_id;
  return Status::OK;
}

Status UserHistoryOrm::FetchUserHistory(const uint32_t user_id,
                                        const uint32_t start_idx,
                                        UserHistory* user_history) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "SELECT `id`, `suggestee_id`, `timestamp`, `latitude`, `longitude` FROM "
      "`user_choice_history` WHERE `user_id`=%0 AND `id`>%1");
  query.parse();

  user_history->set_user_id(user_id);
  const mysqlpp::StoreQueryResult res = query.store(user_id, start_idx);
  for (const mysqlpp::Row& row : res) {
    Choice* const choice = user_history->add_history();
    choice->set_choice_id(row["id"]);
    choice->set_suggestee_id(row["suggestee_id"]);
    util::Timestamp timestamp;
    timestamp.set_seconds(row["timestamp"]);
    *choice->mutable_timestamp() = timestamp;
    choice->set_latitude(row["latitude"]);
    choice->set_longitude(row["longitude"]);
  }
  VLOG(1) << "Returned user history: " << user_history->DebugString();
  return Status::OK;
}

Status UserHistoryOrm::RecordFeedback(const uint32_t user_id,
                                      const UserFeedback& user_feedback) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "INSERT INTO `user_recommendation_feedback` (`user_id`, `suggestee_id`, "
      "`last_choice_id`, `timestamp`, `latitude`, `longitude`, `feedback`) "
      "VALUES (%0, %1, %2, %3, %4, %5, %6)");
  query.parse();

  const Choice choice = user_feedback.choice();
  if (!query.execute(user_id, choice.suggestee_id(), choice.choice_id(),
                     choice.timestamp().seconds(), choice.latitude(),
                     choice.longitude(), user_feedback.feedback())) {
    VLOG(1) << "Query failed with:" << query.error();
    return Status(StatusCode::INTERNAL, query.error());
  }
  return Status::OK;
}

Status UserHistoryOrm::FetchColdStartCompletionStatus(const uint32_t user_id,
                                                      bool* completion_status) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "SELECT `status` FROM "
      "`user_coldstart_status` WHERE `user_id`=%0");
  query.parse();
  const mysqlpp::StoreQueryResult res = query.store(user_id);
  if (res.empty()) {
    query.reset();
    query << "INSERT INTO `user_coldstart_status` (`user_id`, `status`) VALUES "
             "(%0, %1)";
    query.parse();
    if (!query.execute(user_id, false)) {
      VLOG(1) << "Query failed with:" << query.error();
      return Status(StatusCode::INTERNAL, "Internal server error.");
    }
    *completion_status = false;
    return Status::OK;
  } else if (res.num_rows() != 1) {
    return Status(StatusCode::UNKNOWN,
                  "More than one user matches this user_id.");
  }

  *completion_status = res[0]["status"];
  return Status::OK;
}
Status UserHistoryOrm::FetchColdStartItemList(
    const uint32_t user_id,
    const Suggestion::SuggestionCategory coldstart_item_category,
    SuggestionList* coldstart_item_list) {
  mysqlpp::ScopedConnection conn(*conn_pool_);

  SuggestionList all_available_items;
  {
    mysqlpp::Query query = conn->query(
        "SELECT `id`, `name` FROM `suggestee` WHERE "
        "`category_id`=%0 AND `id` NOT IN("
        " SELECT `feedback_id` FROM `user_coldstart_history` WHERE "
        "`user_id`=%1)");
    query.parse();

    const mysqlpp::StoreQueryResult res =
        query.store(coldstart_item_category, user_id);
    for (const auto row : res) {
      Suggestion suggestion;
      suggestion.set_suggestee_id(row["id"]);
      suggestion.set_name(row["name"]);
      GetTags(conn, &suggestion);
      *all_available_items.add_suggestion_list() = suggestion;
    }
  }

  if (all_available_items.suggestion_list_size() == 0) {
    VLOG(1) << "Requested a coldstart_item_list from an empty category: "
            << coldstart_item_category;
    return Status(StatusCode::INVALID_ARGUMENT,
                  "No items to suggest in that category.");
  }

  uint32_t recorded_items_count = 0;
  {
    mysqlpp::Query query = conn->query(
        "SELECT `feedback_id` FROM `user_coldstart_history` WHERE "
        "`user_id`=%0");
    query.parse();

    const mysqlpp::StoreQueryResult res = query.store(user_id);
    recorded_items_count = res.num_rows();
  }

  uint32_t elements_to_insert = kMaximumItemNumber - recorded_items_count;
  std::unordered_set<uint32_t> added_ids;
  const size_t all_available_items_size =
      all_available_items.suggestion_list_size();
  while (elements_to_insert > 0) {
    const uint32_t random_id = util::GetRandom(all_available_items_size);
    const uint32_t suggestee_id =
        all_available_items.suggestion_list(random_id).suggestee_id();
    if (added_ids.find(suggestee_id) != added_ids.end()) continue;
    *coldstart_item_list->add_suggestion_list() =
        all_available_items.suggestion_list(random_id);
    added_ids.insert(suggestee_id);
    elements_to_insert--;
  }
  return Status::OK;
}

Status UserHistoryOrm::RecordColdStartItem(
    const uint32_t user_id, const Suggestion* coldstart_item,
    const UserFeedback::Feedback feedback) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "INSERT INTO `user_coldstart_history` (`user_id`, `feedback_id`, "
      "`feedback`) VALUES (%0, %1, %2)");
  query.parse();

  if (!query.execute(user_id, coldstart_item->suggestee_id(), feedback)) {
    VLOG(1) << "Query failed with:" << query.error();
    return Status(StatusCode::INTERNAL, query.error());
  }

  bool completed_coldstart;

  {
    mysqlpp::Query query = conn->query(
        "SELECT `feedback_id` FROM `user_coldstart_history` WHERE "
        "`user_id`=%0");
    query.parse();

    const mysqlpp::StoreQueryResult res = query.store(user_id);
    completed_coldstart = res.num_rows() == kMaximumItemNumber;
  }

  if (completed_coldstart) {
    mysqlpp::Query query = conn->query(
        "SELECT `status` FROM "
        "`user_coldstart_status` WHERE `user_id`=%0");
    query.parse();
    const mysqlpp::StoreQueryResult res = query.store(user_id);
    if (res.empty()) {
      query.reset();
      query
          << "INSERT INTO `user_coldstart_status` (`user_id`, `status`) VALUES "
             "(%0, %1)";
      query.parse();
      if (!query.execute(user_id, true)) {
        VLOG(1) << "Query failed with:" << query.error();
        return Status(StatusCode::INTERNAL, "Internal server error.");
      }
    } else {
      query.reset();
      query << "UPDATE `user_coldstart_status` SET `status` = %0 WHERE "
               "`user_id` = %1";
      query.parse();
      if (!query.execute(true, user_id)) {
        VLOG(1) << "Query failed with:" << query.error();
        return Status(StatusCode::INTERNAL, "Internal server error.");
      }
    }
  }
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
