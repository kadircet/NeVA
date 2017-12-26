#include "recommender.h"
#include <google/protobuf/text_format.h>
#include "glog/logging.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace recommender {
namespace {

using google::protobuf::TextFormat;

TEST(GetSuggestion, SanityTest) {
  constexpr const char* kUserHistory = R"(
    history {
      suggestee_id: 1
    }
    history {
      suggestee_id: 1
    }
    history {
      suggestee_id: 1
    }
    history {
      suggestee_id: 1
    })";
  constexpr const char* kSuggestionList = R"(
    suggestion_list {
      suggestee_id: 1
      name: "test"
    }
  )";
  constexpr const uint32_t kExpectedSuggesteeId = 1;
  constexpr const char* kExpectedSuggesteeName = "test";

  UserHistory user_history;
  TextFormat::ParseFromString(kUserHistory, &user_history);
  SuggestionList suggestion_list;
  TextFormat::ParseFromString(kSuggestionList, &suggestion_list);

  const SuggestionList suggested_list =
      GetSuggestion(user_history, suggestion_list);
  const Suggestion suggestion = suggested_list.suggestion_list(0);
  EXPECT_EQ(suggestion.suggestee_id(), kExpectedSuggesteeId);
  EXPECT_EQ(suggestion.name(), kExpectedSuggesteeName);
}

}  // namespace
}  // namespace recommender
}  // namespace backend
}  // namespace neva
