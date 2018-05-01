#include "recommender.h"
#include "glog/logging.h"
#include "util/hmac.h"

#include <unordered_map>
#include <unordered_set>

namespace neva {
namespace backend {
namespace recommender {

namespace {

constexpr const int32_t kLeastSuggestionSize = 10;
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

  std::unordered_set<uint32_t> suggested_ids;
  for (Suggestion& suggestion : *suggested_list.mutable_suggestion_list()) {
    const uint32_t suggestee_id = suggestion.suggestee_id();
    suggestion = *id_to_suggestee[suggestee_id];
    suggested_ids.insert(suggestee_id);
    VLOG(1) << suggestion.ShortDebugString() << " has been added to list.";
  }

  int32_t elements_to_insert = kLeastSuggestionSize - suggested_ids.size();
  while (elements_to_insert > 0) {
    const uint32_t random_id = util::GetRandom(suggestion_list_size);
    const uint32_t suggestee_id =
        suggestion_list.suggestion_list(random_id).suggestee_id();
    if (suggested_ids.find(suggestee_id) != suggested_ids.end()) continue;
    suggested_ids.insert(suggestee_id);
    *suggested_list.add_suggestion_list() = *id_to_suggestee[suggestee_id];
    elements_to_insert--;
  }
  VLOG(1) << "Exiting GetMultipleSuggestions";

  return suggested_list;
}

}  // namespace recommender
}  // namespace backend
}  // namespace neva
