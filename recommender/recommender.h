#ifndef _NEVA_BACKEND_RECOMMENDER_RECOMMENDER_H_
#define _NEVA_BACKEND_RECOMMENDER_RECOMMENDER_H_

#include "cache_fetcher.h"
#include "protos/suggestion.pb.h"
#include "protos/user_history.pb.h"

namespace neva {
namespace backend {
namespace recommender {

// Get suggestion according to the given history.
Suggestion GetSuggestion(const UserHistory& history,
                         const SuggestionList& suggestion_list);

// Get suggestions for the given user.
SuggestionList GetMultipleSuggestions(const uint32_t user_id,
                                      const SuggestionList& suggestion_list,
                                      const CacheFetcer* cache_fetcher);

}  // namespace recommender
}  // namespace backend
}  // namespace neva

#endif
