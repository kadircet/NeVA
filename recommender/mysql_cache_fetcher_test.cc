#include "mysql_cache_fetcher.h"
#include <google/protobuf/text_format.h>
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "orm/testing/mock_orm_fixture.h"
#include "protos/suggestion.pb.h"

namespace {

using google::protobuf::TextFormat;
using neva::backend::SuggestionList;
using neva::backend::orm::testing::MockOrmFixture;
using neva::backend::recommender::MySQLCacheFetcher;
using ::testing::get;
using ::testing::Pointwise;

constexpr const char* const kUserTableInitFile = "tables/user.sql";
constexpr const char* const kSuggestionTableInitFile = "tables/suggestions.sql";
constexpr const char* const kRecommenderTableInitFile =
    "tables/recommender.sql";
constexpr const char* const kInitialDataSetFile =
    "recommender/mysql_cache_fetcher_test.sql";

constexpr const uint32_t kUserId = 1;

class MySQLCacheFetcherFixture : public MockOrmFixture {
 public:
  MySQLCacheFetcherFixture() : mysql_cache_fetcher_(conn_pool_) {
    PrepareDatabase(kUserTableInitFile);
    PrepareDatabase(kSuggestionTableInitFile);
    PrepareDatabase(kRecommenderTableInitFile);
    PrepareDatabase(kInitialDataSetFile);
  }

 protected:
  MySQLCacheFetcher mysql_cache_fetcher_;
};

MATCHER(SuggesteeEq, "Check if two suggestees have same id.") {
  return get<0>(arg).suggestee_id() == get<1>(arg).suggestee_id();
}

TEST_F(MySQLCacheFetcherFixture, SanityTest) {
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
  )";
  SuggestionList suggestion_list;
  TextFormat::ParseFromString(kSuggestionList, &suggestion_list);

  SuggestionList cached_list;
  const auto status =
      mysql_cache_fetcher_.GetCachedRecommendations(kUserId, &cached_list);

  EXPECT_TRUE(status.ok()) << status.error_message();
  EXPECT_EQ(cached_list.suggestion_list_size(),
            suggestion_list.suggestion_list_size());
  EXPECT_THAT(cached_list.suggestion_list(),
              Pointwise(SuggesteeEq(), suggestion_list.suggestion_list()));
}

}  // namespace
