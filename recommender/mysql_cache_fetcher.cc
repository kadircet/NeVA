#include "mysql_cache_fetcher.h"
#include "glog/logging.h"

namespace neva {
namespace backend {
namespace recommender {

namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status MySQLCacheFetcher::GetCachedRecommendations(
    const uint32_t user_id, SuggestionList* suggestion_list) const {
  CHECK(suggestion_list) << "suggestion_list shouldn't be nullptr";
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query = conn->query(
      "SELECT `suggestee_id` FROM `recommender_cache` WHERE `user_id` = %0");
  query.parse();
  const mysqlpp::StoreQueryResult res = query.store(user_id);
  if (!res) {
    LOG(WARNING) << "Something went wrong while executing query:\n"
                 << query.str(user_id) << "\n"
                 << query.info();
    return Status(StatusCode::INTERNAL, "Internal server error.");
  }
  for (const mysqlpp::Row& row : res) {
    Suggestion suggestion;
    suggestion.set_suggestee_id(row["suggestee_id"]);
    *suggestion_list->add_suggestion_list() = suggestion;
  }
  return Status::OK;
}

}  // namespace recommender
}  // namespace backend
}  // namespace neva
