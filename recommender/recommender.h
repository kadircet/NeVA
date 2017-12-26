#ifndef _NEVA_ORM_USERS_H_
#define _NEVA_ORM_USERS_H_

#include "protos/suggestion.pb.h"
#include "protos/user_history.pb.h"

namespace neva {
namespace backend {
namespace recommender {

// Get suggestion according to the given history.
SuggestionList GetSuggestion(const UserHistory& history,
                             const SuggestionList& suggestion_list);
}  // namespace recommender
}  // namespace backend
}  // namespace neva

#endif
