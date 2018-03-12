#include "recommender.h"
#include "glog/logging.h"
#include "util/hmac.h"

#include <unordered_map>

namespace neva {
namespace backend {
namespace recommender {

Suggestion GetSuggestion(const UserHistory& history,
                         const SuggestionList& suggestion_list) {
  const size_t suggestion_list_size = suggestion_list.suggestion_list_size();
  CHECK(suggestion_list_size > 0) << "Empty suggestion list.";

  SuggestionList suggested_list =
      GetMultipleSuggestions(history.user_id(), suggestion_list, nullptr);

  // Pick one random element randomly from most frequent ones.
  const uint32_t element_idx =
      util::GetRandom(suggested_list.suggestion_list_size());
  CHECK(element_idx < suggestion_list_size && element_idx >= 0)
      << "element_idx: " << element_idx << " out of scope.";
  return suggested_list.suggestion_list(element_idx);
}

SuggestionList GetMultipleSuggestions(const uint32_t user_id,
                                      const SuggestionList& suggestion_list,
                                      const CacheFetcer* cache_fetcher) {
  VLOG(1) << "Entering GetMultipleSuggestions";
  const size_t suggestion_list_size = suggestion_list.suggestion_list_size();
  CHECK(suggestion_list_size > 0) << "Empty suggestion list.";

  SuggestionList suggested_list;
  cache_fetcher->GetCachedRecommendations(user_id, &suggested_list);

  std::unordered_map<uint32_t, const Suggestion*> id_to_suggestee;
  for (const Suggestion& suggestion : suggestion_list.suggestion_list()) {
    const uint32_t suggestee_id = suggestion.suggestee_id();
    id_to_suggestee[suggestee_id] = &suggestion;
  }

  // TODO(kadircet): Insert different elements for diversity.
  for (Suggestion& suggestion : *suggested_list.mutable_suggestion_list()) {
    suggestion = *id_to_suggestee[suggestion.suggestee_id()];
    VLOG(1) << suggestion.ShortDebugString() << " has been added to list.";
  }
  VLOG(1) << "Exiting GetMultipleSuggestions";

  return suggested_list;
}

}  // namespace recommender
}  // namespace backend
}  // namespace neva
