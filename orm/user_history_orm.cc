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
    return Status(StatusCode::INTERNAL, query.error());
  }
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
