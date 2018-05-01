#include "user_history_orm.h"
#include <memory>
#include "connectionpool.h"
#include "glog/logging.h"
#include "gmock/gmock.h"
#include "google/protobuf/text_format.h"
#include "gtest/gtest.h"
#include "protos/user_history.pb.h"
#include "testing/mock_orm_fixture.h"

namespace {

constexpr const char* const kUserTableInitFile = "tables/user.sql";
constexpr const char* const kSuggestionTableInitFile = "tables/suggestions.sql";
constexpr const char* const kHistoryTablesInitFile = "tables/user_history.sql";
constexpr const char* const kInitialDataSetFile = "orm/user_history_orm_test.sql";

using neva::backend::Choice;
using neva::backend::UserFeedback;
using neva::backend::UserHistory;
using neva::backend::orm::UserHistoryOrm;
using neva::backend::orm::testing::MockOrmFixture;

class UserHistoryOrmFixture : public MockOrmFixture {
 public:
  UserHistoryOrmFixture():user_history_orm_(conn_pool_) {
    PrepareDatabase(kUserTableInitFile);
    PrepareDatabase(kSuggestionTableInitFile);
    PrepareDatabase(kHistoryTablesInitFile);
    PrepareDatabase(kInitialDataSetFile);
  }
 protected:
  UserHistoryOrm user_history_orm_;
};

TEST_F(UserHistoryOrmFixture, InsertUserHistoryExistingSuggestee) {
  neva::backend::util::Timestamp timestamp;
  timestamp.set_seconds(41414141);
  const double latitude = 39.89306792, longitude = 32.78321898;
  const int kSuggesteeId = 1;
  const int kUserId = 1;
  int choice_id;
  Choice choice;
  *choice.mutable_timestamp() = timestamp;
  choice.set_latitude(latitude);
  choice.set_longitude(longitude);
  choice.set_suggestee_id(kSuggesteeId);

  grpc::Status result = user_history_orm_.InsertChoice(kUserId, choice, &choice_id);
  EXPECT_TRUE(result.ok()) << "Cannot add choice with existing user and suggestee" << result.error_message();

}

TEST_F(UserHistoryOrmFixture, InsertUserHistoryNonExistingSuggestee) {
  neva::backend::util::Timestamp timestamp;
  timestamp.set_seconds(41414141);
  const double latitude = 39.89306792, longitude = 32.78321898;
  const int kSuggesteeId = 99;
  const int kUserId = 1;
  int choice_id;
  Choice choice;
  *choice.mutable_timestamp() = timestamp;
  choice.set_latitude(latitude);
  choice.set_longitude(longitude);
  choice.set_suggestee_id(kSuggesteeId);

  grpc::Status result = user_history_orm_.InsertChoice(kUserId, choice, &choice_id);
  EXPECT_FALSE(result.ok()) << "Cannot add choice with existing user and suggestee" << result.error_message();
}

TEST_F(UserHistoryOrmFixture, RecordFeedbackValid) {
  const double latitude = 39.89306792, longitude = 32.78321898;
  const int kSuggesteeId = 1;
  const int kUserId = 1;
  const int choice_id = 1;
  neva::backend::util::Timestamp timestamp;
  timestamp.set_seconds(41414141);
  
  Choice choice;
  *choice.mutable_timestamp() = timestamp;
  choice.set_latitude(latitude);
  choice.set_longitude(longitude);
  choice.set_suggestee_id(kSuggesteeId);
  choice.set_choice_id(choice_id);

  UserFeedback user_feedback;
  user_feedback.set_user_id(1);
  *user_feedback.mutable_choice() = choice;
  user_feedback.set_feedback(UserFeedback::DISLIKE);

  grpc::Status result = user_history_orm_.RecordFeedback(kUserId, user_feedback);
  EXPECT_TRUE(result.ok()) << "Cannot record feedback." << result.error_message();
}

TEST_F(UserHistoryOrmFixture, RecordFeedbackInvalid) {
  const double latitude = 39.89306792, longitude = 32.78321898;
  const int kSuggesteeId = 99;
  const int kUserId = 1;
  const int choice_id = 1;
  neva::backend::util::Timestamp timestamp;
  timestamp.set_seconds(41414141);
  
  Choice choice;
  *choice.mutable_timestamp() = timestamp;
  choice.set_latitude(latitude);
  choice.set_longitude(longitude);
  choice.set_suggestee_id(kSuggesteeId);
  choice.set_choice_id(choice_id);

  UserFeedback user_feedback;
  user_feedback.set_user_id(1);
  *user_feedback.mutable_choice() = choice;
  user_feedback.set_feedback(UserFeedback::DISLIKE);

  grpc::Status result = user_history_orm_.RecordFeedback(kUserId, user_feedback);
  EXPECT_FALSE(result.ok()) << "Cannot record feedback." << result.error_message();
}

TEST_F(UserHistoryOrmFixture, FetchUserHistory) {
  UserHistory user_history;
  const int kUserId = 1;
  const int kStartIdx = 0;
  grpc::Status result = user_history_orm_.FetchUserHistory(kUserId, kStartIdx, &user_history);
  EXPECT_TRUE(result.ok()) << result.error_message();
  EXPECT_NE(user_history.history_size(), 0) << "History size is 0";
}

}