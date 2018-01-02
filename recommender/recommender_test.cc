#include "recommender.h"
#include <google/protobuf/text_format.h>
#include "glog/logging.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace recommender {
namespace {

using ::testing::get;
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

MATCHER(SuggestionEq, "") {
  return get<0>(arg).suggestee_id() == get<1>(arg).suggestee_id() &&
         get<0>(arg).name() == get<1>(arg).name();
}

TEST(GetMultipleSuggestions, SanityTest) {
  constexpr const char* kUserHistory = R"(
    history {
      suggestee_id: 1
    }
    history {
      suggestee_id: 1
    })";
  constexpr const char* kSuggestionList = R"(
    suggestion_list {
      suggestee_id: 1
      name: "test1"
    }
    suggestion_list {
      suggestee_id: 2
      name: "test2"
    }
  )";

  constexpr const uint32_t kExpectedSuggesteeCount = 2;

  UserHistory user_history;
  TextFormat::ParseFromString(kUserHistory, &user_history);
  SuggestionList all_suggestees;
  TextFormat::ParseFromString(kSuggestionList, &all_suggestees);

  const SuggestionList suggestion_list =
      GetMultipleSuggestions(user_history, all_suggestees);
  EXPECT_EQ(suggestion_list.suggestion_list_size(), kExpectedSuggesteeCount);
  EXPECT_THAT(
      suggestion_list.suggestion_list(),
      Contains(Pointwise(SuggestionEq(), all_suggestees.suggestion_list(0))));
  EXPECT_THAT(
      suggestion_list.suggestion_list(),
      Contains(Pointwise(SuggestionEq(), all_suggestees.suggestion_list(1))));
}

}  // namespace
}  // namespace recommender
}  // namespace backend
}  // namespace neva
