#ifndef _NEVA_BACKEND_RECOMMENDER_MYSQL_CACHE_FETCHER_H_
#define _NEVA_BACKEND_RECOMMENDER_MYSQL_CACHE_FETCHER_H_

#include <memory>
#include "cache_fetcher.h"
#include "orm/connectionpool.h"

namespace neva {
namespace backend {
namespace recommender {

class MySQLCacheFetcher : public CacheFetcer {
 public:
  MySQLCacheFetcher(std::shared_ptr<orm::NevaConnectionPool> conn_pool)
      : conn_pool_(conn_pool) {}

  grpc::Status GetCachedRecommendations(const uint32_t user_id,
                                        SuggestionList* suggestion_list) const;

 private:
  std::shared_ptr<orm::NevaConnectionPool> conn_pool_;
};

}  // namespace recommender
}  // namespace backend
}  // namespace neva

#endif
