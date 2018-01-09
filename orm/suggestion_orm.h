#ifndef _NEVA_ORM_SUGGESTIONS_H_
#define _NEVA_ORM_SUGGESTIONS_H_

#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include <vector>
#include "orm/connectionpool.h"
#include "protos/suggestion.pb.h"
#include "protos/user_history.pb.h"

namespace neva {
namespace backend {
namespace orm {

class SuggestionOrm {
 public:
  // Initiates SuggestionOrm class wih given mysql connection.
  SuggestionOrm(std::shared_ptr<NevaConnectionPool> conn_pool)
      : conn_pool_(conn_pool) {}

  grpc::Status GetSuggestees(
      const Suggestion::SuggestionCategory suggestion_category,
      const uint32_t start_index, SuggestionList* suggestion_list,
      uint32_t* last_updated = nullptr);

  grpc::Status GetSuggestion(
      const UserHistory& user_history,
      const Suggestion::SuggestionCategory suggestion_category,
      Suggestion* suggestion);

  grpc::Status GetMultipleSuggestions(
      const UserHistory& user_history,
      const Suggestion::SuggestionCategory suggestion_category,
      SuggestionList* suggestion_list);

 private:
  std::shared_ptr<NevaConnectionPool> conn_pool_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
