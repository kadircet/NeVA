#include "user_orm.h"
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
  constexpr const char* const kInitialDatasetFile = "orm/user_orm_test.sql";

  using neva::backend::User;
  using neva::backend::LoginRequest;
  using neva::backend::orm::UserOrm;
  using neva::backend::orm::testing::MockOrmFixture;
  
  class UserOrmFixture : public MockOrmFixture {
    public:
    UserOrmFixture() : user_orm_(conn_pool_) {
      PrepareDatabase(kUserTableInitFile);
      PrepareDatabase(kInitialDatasetFile);
    }
    protected:
    UserOrm user_orm_;
  };

  TEST_F(UserOrmFixture, GetUserById) {
    const uint32_t kExistingUserId = 0;
    const std::string kExistingUserEmail= "test@gtest.com";
    User user;
    user_orm_.GetUserById(kExistingUserId, &user);
    //TODO: HAKAN insert user from code
    EXPECT_EQ(user.email(), kExistingUserEmail);
    EXPECT_EQ(user.user_id(), 1) << "Couldn't get existing user from db";
  }

  TEST_F(UserOrmFixture, GetUserByEmail) {
    const uint32_t kExistingUserId = 1;
    const std::string kExistingUserEmail= "test@gtest.com";
    User user;
    EXPECT_TRUE(user_orm_.GetUserByEmail(kExistingUserEmail, &user).ok()) << "Couldn't get existing user from db";
    EXPECT_EQ(user.user_id(), kExistingUserId);
  }

  TEST_F(UserOrmFixture, CheckWrongCredentials) {
    const uint32_t kExistingUserId = 1;
    const char* const kUserSalt = "1";
    const LoginRequest::AuthenticationType kAuthType = LoginRequest::DEFAULT;
    const char* const kEmail = "test@gtest.com";
    const char* const kPass = "AAAA";
    User user;
    user.set_password(kPass);
    std::string token = "";
    EXPECT_FALSE(user_orm_.CheckCredentials(kEmail, kPass, kAuthType, &token).ok()) << "Error while checking credentials";
    EXPECT_EQ(token, "") << "Token has changed.";
  }

  TEST_F(UserOrmFixture, InsertExistingUser) {
    const LoginRequest::AuthenticationType kAuthType = LoginRequest::DEFAULT;
    const char* const kEmail = "test@gtest.com";
    const char* const kPass = "AAAA";
    std::string token = "";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    EXPECT_FALSE(user_orm_.InsertUser(user, kAuthType, &token).ok()) << "Existing user inserted without problem.";
  }

  TEST_F(UserOrmFixture, InsertNonExistentUser) {
    const LoginRequest::AuthenticationType kAuthType = LoginRequest::DEFAULT;
    const char* const kEmail = "test@gtest.com";
    const char* const kPass = "BBBB";
    std::string token = "";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    EXPECT_TRUE(user_orm_.InsertUser(user, kAuthType, &token).ok()) << "Couldn't add non-existent user.";
    EXPECT_TRUE(user_orm_.CheckCredentials(kEmail, kPass, kAuthType, &token).ok()) << "Problem checking newly added user's credentials.";
    EXPECT_NE(token, "") << "Verification token after checking credentials is empty.";
  }

  TEST_F(UserOrmFixture, InsertAndChangeUser) {
    const LoginRequest::AuthenticationType kAuthType = LoginRequest::DEFAULT;
    const char* const kEmail = "test@gtest.com";
    const char* const kPass = "BBBB";
    const uint32_t kUserId = 2;
    std::string token = "";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    EXPECT_TRUE(user_orm_.InsertUser(user, kAuthType, &token).ok()) << "Couldn't add non-existent user.";
    user.mutable_date_of_birth()->set_seconds(12345678);
    user.set_name("new_test");
    user.set_gender(User::MALE);
    user.set_weight(80);
    user.set_photo("photo");
    EXPECT_TRUE(user_orm_.UpdateUserData(user.user_id(), user).ok());
  }


}