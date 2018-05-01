#include "recommender.h"
#include <google/protobuf/text_format.h>
#include "glog/logging.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "mysql_cache_fetcher.h"
#include "orm/testing/mock_orm_fixture.h"

namespace neva {
namespace backend {
namespace recommender {
namespace {

using google::protobuf::TextFormat;
using neva::backend::orm::testing::MockOrmFixture;

constexpr const uint32_t kUserId = 1;
constexpr const int32_t kLeastSuggestionSize = 10;

class RecommenderFixture : public MockOrmFixture {
 public:
  RecommenderFixture() : cache_fetcher_(new MySQLCacheFetcher(conn_pool_)) {}

 protected:
  std::unique_ptr<CacheFetcer> cache_fetcher_;
};

TEST_F(RecommenderFixture, SanityTest) {
  constexpr const char* kSuggestionList = R"(
    suggestion_list {
      suggestee_id: 1
    }
    suggestion_list {
      suggestee_id: 2
    }
    suggestion_list {
      suggestee_id: 3
    }
    suggestion_list {
      suggestee_id: 4
    }
    suggestion_list {
      suggestee_id: 5
    }
    suggestion_list {
      suggestee_id: 6
    }
    suggestion_list {
      suggestee_id: 7
    }
    suggestion_list {
      suggestee_id: 8
    }
    suggestion_list {
      suggestee_id: 9
    }
    suggestion_list {
      suggestee_id: 10
    }
    suggestion_list {
      suggestee_id: 11
    }
    suggestion_list {
      suggestee_id: 12
    }
    suggestion_list {
      suggestee_id: 13
    }
    suggestion_list {
      suggestee_id: 14
    }
    suggestion_list {
      suggestee_id: 15
    }
    suggestion_list {
      suggestee_id: 16
    }
    suggestion_list {
      suggestee_id: 17
    }
  )";
  SuggestionList suggestion_list;
  TextFormat::ParseFromString(kSuggestionList, &suggestion_list);

  const SuggestionList recommendation =
      GetMultipleSuggestions(kUserId, suggestion_list, cache_fetcher_.get());

  // Check for minimum list size.
  EXPECT_GE(recommendation.suggestion_list_size(), kLeastSuggestionSize);

  // Make sure all elements are unique.
  std::unordered_set<uint32_t> suggestee_ids;
  for (const Suggestion& suggestion : recommendation.suggestion_list()) {
    EXPECT_EQ(suggestee_ids.find(suggestion.suggestee_id()),
              suggestee_ids.end());
    suggestee_ids.insert(suggestion.suggestee_id());
  }
}

}  // namespace
}  // namespace recommender
}  // namespace backend
}  // namespace neva
