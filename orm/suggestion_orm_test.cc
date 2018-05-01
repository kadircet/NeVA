#include "suggestion_orm.h"
#include <memory>
#include "connectionpool.h"
#include "glog/logging.h"
#include "gmock/gmock.h"
#include "google/protobuf/text_format.h"
#include "gtest/gtest.h"
#include "protos/suggestion.pb.h"
#include "testing/mock_orm_fixture.h"

namespace {

constexpr const char* const kUserTableInitFile = "tables/user.sql";
constexpr const char* const kSuggestionTableInitFile = "tables/suggestions.sql";
constexpr const char* const kRecommenderCacheTableInitFile = "tables/recommender.sql";
constexpr const char* const kInitialDataSetFile = "orm/suggestion_orm_test.sql";
constexpr const uint32_t kExistingId = 1;
constexpr const uint32_t kNonExistingId = 2;

using neva::backend::Suggestion;
using neva::backend::SuggestionList;
using neva::backend::orm::SuggestionOrm;
using neva::backend::orm::testing::MockOrmFixture;

class SuggestionOrmFixture : public MockOrmFixture {
 public:
 SuggestionOrmFixture():suggestion_orm_(conn_pool_, cache_fetcher_) {
   PrepareDatabase(kUserTableInitFile);
   PrepareDatabase(kSuggestionTableInitFile);
   PrepareDatabase(kRecommenderCacheTableInitFile);
   PrepareDatabase(kInitialDataSetFile);
 }
 protected:
 SuggestionOrm suggestion_orm_;
};

TEST_F(SuggestionOrmFixture, GetSuggestees) {
  const SuggestionCategory kCategory = Suggestion::MEAL;
  const int kStartIndex = 0;
  int lastUpdated = 0;
  SuggestionList suggestion_list;

  grpc::Status result = suggestion_orm_.GetSuggestees(kCategory, kStartIndex, &suggestion_list, &lastUpdated);
  EXPECT_TRUE(result.ok()) << result.error_message();
  EXPECT_NE(suggestion_list.suggestion_list_size(), 0) << "Suggestion list size is 0.";

}

TEST_F(SuggestionOrmFixture, MultipleSuggestion) {
  const SuggestionCategory kCategory = Suggestion::MEAL;
  const int kUserId = 1;
  SuggestionList suggestion_list;

  grpc::Status result = suggestion_orm_.GetMultipleSuggestions(kUserId, kCategory, suggestion_list);
  EXPECT_TRUE(result.ok()) << result.error_message();
  EXPECT_EQ(suggestion_list.suggestion_list_size(), 10) << "Suggestion list size is not 10.";
}