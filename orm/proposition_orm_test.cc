#include "proposition_orm.h"
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
constexpr const char* const kPropositionTableInitFile =
    "tables/user_based_suggestion.sql";
constexpr const char* const kInitialDataSetFile =
    "orm/proposition_orm_test.sql";
constexpr const uint32_t kExistingId = 1;
constexpr const uint32_t kNonExistingId = 2;

using neva::backend::Suggestion;
using neva::backend::orm::PropositionOrm;
using neva::backend::orm::testing::MockOrmFixture;

class PropositionOrmFixture : public MockOrmFixture {
 public:
  PropositionOrmFixture() : proposition_orm_(conn_pool_) {
    PrepareDatabase(kUserTableInitFile);
    PrepareDatabase(kSuggestionTableInitFile);
    PrepareDatabase(kPropositionTableInitFile);
    PrepareDatabase(kInitialDataSetFile);
  }

 protected:
  Suggestion GetItemProposition(const uint32_t user_id,
                                const std::string& name) {
    mysqlpp::ScopedConnection conn(*conn_pool_);
    mysqlpp::Query query = conn->query(
        "SELECT * FROM `item_suggestion` WHERE `user_id` = %0 AND `suggestion` "
        "= %1q");
    query.parse();
    mysqlpp::StoreQueryResult res = query.store(user_id, name);
    EXPECT_GT(res.num_rows(), 0);

    const auto row = res.front();
    Suggestion suggestion;
    suggestion.set_suggestion_category(
        static_cast<Suggestion::SuggestionCategory>(
            static_cast<uint32_t>(row["category_id"])));
    suggestion.set_name(row["suggestion"]);
    return suggestion;
  }

  void CheckTagPropositionExists(const uint32_t user_id,
                                 const std::string& tag) {
    mysqlpp::ScopedConnection conn(*conn_pool_);
    mysqlpp::Query query = conn->query(
        "SELECT * FROM `tag_suggestion` WHERE `user_id` = %0 AND `tag` = %1q");
    query.parse();
    mysqlpp::StoreQueryResult res = query.store(user_id, tag);
    EXPECT_GT(res.num_rows(), 0);
  }

  void CheckTvsPropositionExists(const uint32_t user_id, const uint32_t tag_id,
                                 const uint32_t suggestee_id,
                                 const std::string& value) {
    mysqlpp::ScopedConnection conn(*conn_pool_);
    mysqlpp::Query query = conn->query(
        "SELECT `value` FROM `tag_value_suggestion` WHERE `user_id` = %0 AND "
        "`tag_id` = %1 AND `suggestee_id` = %2");
    query.parse();
    mysqlpp::StoreQueryResult res = query.store(user_id, tag_id, suggestee_id);
    EXPECT_GT(res.num_rows(), 0);
    EXPECT_EQ(res.back()["value"], value);
  }

  PropositionOrm proposition_orm_;
};

TEST_F(PropositionOrmFixture, InsertSuggestion) {
  constexpr const char* const kSuggestion = R"(
      suggestion_category: MEAL
      name: "Test"
    )";
  Suggestion suggestion;
  CHECK(google::protobuf::TextFormat::ParseFromString(kSuggestion, &suggestion))
      << "Failed to parse proto definition.";
  EXPECT_TRUE(proposition_orm_.InsertProposition(kExistingId, suggestion).ok());
  EXPECT_EQ(
      suggestion.suggestion_category(),
      GetItemProposition(kExistingId, suggestion.name()).suggestion_category());
}

TEST_F(PropositionOrmFixture, InsertSuggestionNonExistingUser) {
  constexpr const char* const kSuggestion = R"(
      suggestion_category: MEAL
      name: "Test"
    )";
  Suggestion suggestion;
  CHECK(google::protobuf::TextFormat::ParseFromString(kSuggestion, &suggestion))
      << "Failed to parse proto definition.";
  EXPECT_FALSE(
      proposition_orm_.InsertProposition(kNonExistingId, suggestion).ok());
}

TEST_F(PropositionOrmFixture, InsertSuggestionNonExistingCategory) {
  constexpr const char* const kSuggestion = R"(
      name: "Test"
    )";
  Suggestion suggestion;
  CHECK(google::protobuf::TextFormat::ParseFromString(kSuggestion, &suggestion))
      << "Failed to parse proto definition.";
  EXPECT_FALSE(
      proposition_orm_.InsertProposition(kNonExistingId, suggestion).ok());
}

TEST_F(PropositionOrmFixture, InsertTag) {
  constexpr const char* const kTag = "test_tag";
  EXPECT_TRUE(proposition_orm_.InsertProposition(kExistingId, kTag).ok());
  CheckTagPropositionExists(kExistingId, kTag);
}

TEST_F(PropositionOrmFixture, InsertTagNonExistingUser) {
  constexpr const char* const kTag = "test_tag";
  EXPECT_FALSE(proposition_orm_.InsertProposition(kNonExistingId, kTag).ok());
}

TEST_F(PropositionOrmFixture, InsertTvs) {
  constexpr const char* const kValue = "test_value";
  EXPECT_TRUE(
      proposition_orm_
          .InsertProposition(kExistingId, kExistingId, kExistingId, kValue)
          .ok());
  CheckTvsPropositionExists(kExistingId, kExistingId, kExistingId, kValue);
  EXPECT_TRUE(proposition_orm_
                  .InsertProposition(kExistingId, kExistingId, kExistingId, "")
                  .ok());
  CheckTvsPropositionExists(kExistingId, kExistingId, kExistingId, "");

  EXPECT_FALSE(
      proposition_orm_
          .InsertProposition(kNonExistingId, kExistingId, kExistingId, kValue)
          .ok());
  EXPECT_FALSE(
      proposition_orm_
          .InsertProposition(kExistingId, kNonExistingId, kExistingId, kValue)
          .ok());
  EXPECT_FALSE(
      proposition_orm_
          .InsertProposition(kExistingId, kExistingId, kNonExistingId, kValue)
          .ok());
}

}  // namespace
