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

  const Suggestion suggestion = GetSuggestion(user_history, suggestion_list);
  EXPECT_EQ(suggestion.suggestee_id(), kExpectedSuggesteeId);
  EXPECT_EQ(suggestion.name(), kExpectedSuggesteeName);
}

}  // namespace
}  // namespace recommender
}  // namespace backend
}  // namespace neva
