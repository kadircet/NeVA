#ifndef _NEVA_BACKEND_RECOMMENDER_CACHE_FETCHER_H_
#define _NEVA_BACKEND_RECOMMENDER_CACHE_FETCHER_H_

#include <grpc++/impl/codegen/status.h>
#include <cstdint>
#include "protos/suggestion.pb.h"

namespace neva {
namespace backend {
namespace recommender {

class CacheFetcer {
 public:
  // Fills in sugggestion_list with *only* suggestee_id's. Wrapper should fill
  // in additional information.
  virtual grpc::Status GetCachedRecommendations(
      const uint32_t user_id, SuggestionList* suggestion_list) const = 0;
};

}  // namespace recommender
}  // namespace backend
}  // namespace neva

#endif
