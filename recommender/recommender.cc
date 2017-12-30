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

  std::unordered_map<uint32_t, uint32_t> frequencies;
  std::unordered_map<uint32_t, const Suggestion*> id_to_suggestee;
  uint32_t max_freq = 0;
  std::unordered_set<uint32_t> max_freq_ids;
  for (const Suggestion& suggestion : suggestion_list.suggestion_list()) {
    const uint32_t suggestee_id = suggestion.suggestee_id();
    frequencies[suggestee_id] = 0;
    id_to_suggestee[suggestee_id] = &suggestion;
    max_freq_ids.insert(suggestee_id);
  }

  // Extract most frequent elements from user history.
  for (const Choice& choice : history.history()) {
    const uint32_t suggestee_id = choice.suggestee_id();
    auto it = frequencies.find(suggestee_id);
    if (it != frequencies.end()) {
      it->second++;
      if (it->second > max_freq) {
        max_freq = it->second;
        max_freq_ids.clear();
        max_freq_ids.insert(suggestee_id);
      } else if (it->second == max_freq) {
        max_freq_ids.insert(suggestee_id);
      }
    }
  }

  // Insert one random element for diversity.
  if (max_freq_ids.size() != suggestion_list_size) {
    uint32_t random_id = util::GetRandom(suggestion_list_size);
    uint32_t suggestee_id =
        suggestion_list.suggestion_list(random_id).suggestee_id();
    while (max_freq_ids.find(suggestee_id) != max_freq_ids.end()) {
      random_id++;
      if (random_id == suggestion_list_size) random_id = 0;
      suggestee_id = suggestion_list.suggestion_list(random_id).suggestee_id();
    }
    max_freq_ids.insert(suggestee_id);
  }

  // Pick one random element randomly from most frequent ones.
  const uint32_t element_idx = util::GetRandom(max_freq_ids.size());
  auto it = max_freq_ids.cbegin();
  for (uint32_t i = 0; i < element_idx; i++) it++;
  return *id_to_suggestee[*it];
}

SuggestionList GetMultipleSuggestions(const UserHistory& history,
                                      const SuggestionList& suggestion_list) {
  const size_t suggestion_list_size = suggestion_list.suggestion_list_size();
  CHECK(suggestion_list_size > 0) << "Empty suggestion list.";

  std::unordered_map<uint32_t, uint32_t> frequencies;
  std::unordered_map<uint32_t, const Suggestion*> id_to_suggestee;
  uint32_t max_freq = 0;
  std::unordered_set<uint32_t> max_freq_ids;
  for (const Suggestion& suggestion : suggestion_list.suggestion_list()) {
    const uint32_t suggestee_id = suggestion.suggestee_id();
    frequencies[suggestee_id] = 0;
    id_to_suggestee[suggestee_id] = &suggestion;
    max_freq_ids.insert(suggestee_id);
  }

  // Extract most frequent elements from user history.
  for (const Choice& choice : history.history()) {
    const uint32_t suggestee_id = choice.suggestee_id();
    auto it = frequencies.find(suggestee_id);
    if (it != frequencies.end()) {
      it->second++;
      if (it->second > max_freq) {
        max_freq = it->second;
        max_freq_ids.clear();
        max_freq_ids.insert(suggestee_id);
      } else if (it->second == max_freq) {
        max_freq_ids.insert(suggestee_id);
      }
    }
  }

  // Insert one random element for diversity.
  if (max_freq_ids.size() != suggestion_list_size) {
    uint32_t random_id = util::GetRandom(suggestion_list_size);
    uint32_t suggestee_id =
        suggestion_list.suggestion_list(random_id).suggestee_id();
    while (max_freq_ids.find(suggestee_id) != max_freq_ids.end()) {
      random_id++;
      if (random_id == suggestion_list_size) random_id = 0;
      suggestee_id = suggestion_list.suggestion_list(random_id).suggestee_id();
    }
    max_freq_ids.insert(suggestee_id);
  }

  // Return all most frequent elements
  SuggestionList suggested_list;
  for (const auto& it : max_freq_ids) {
    const Suggestion suggestion = *id_to_suggestee[it];
    VLOG(1) << suggestion.ShortDebugString() << " is current suggestion.";
    *suggested_list->add_suggestion_list() = suggestion;
  }

  return suggested_list;
}

}  // namespace recommender
}  // namespace backend
}  // namespace neva
