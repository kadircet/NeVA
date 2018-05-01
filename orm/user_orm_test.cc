#include "user_orm.h"
#include <memory>
#include "connectionpool.h"
#include "glog/logging.h"
#include "gmock/gmock.h"
#include "google/protobuf/text_format.h"
#include "gtest/gtest.h"
#include "protos/suggestion.pb.h"
#include "testing/mock_orm_fixture.h"
#include "util/error.h"
#include "util/hmac.h"
#include "util/time.h"

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
    int InsertUserToDatabase(const User& user, const LoginRequest::AuthenticationType authentication_type) {
      mysqlpp::ScopedConnection conn(*conn_pool_);
      mysqlpp::Query query = conn->query("INSERT INTO `user` (`email`, `status`, `salt`) "
           "VALUES (%0q, %1, %2q)");
      query.parse();
      const std::string salt = neva::backend::util::GenerateRandomKey();
      const int user_id = query.execute(user.email(), User::INACTIVE, salt).insert_id();
      const std::string hmac = neva::backend::util::HMac(salt, user.password());
      query.reset();
      query << "INSERT INTO `user_credentials` (`user_id`, `credential`, `type`) "
      "VALUES (%0, %1q, %2)";
      query.parse();
      query.execute(user_id, hmac, authentication_type);
      query.reset();
      query << "INSERT INTO `user_info` (`id`, `register_date`) VALUES (%0, %1)";
      query.parse();
      query.execute(user_id, 31313131);
      return user_id;
      
    }
    UserOrm user_orm_;
  };

  TEST_F(UserOrmFixture, GetUserById) {
    const uint32_t kExistingUserId = 1;
    const std::string kExistingUserEmail= "user_1@neva.com";
    User user;
    user_orm_.GetUserById(kExistingUserId, &user);
    EXPECT_EQ(user.email(), kExistingUserEmail);
    EXPECT_EQ(user.user_id(), 1) << "Couldn't get existing user from db";
  }

  TEST_F(UserOrmFixture, GetUserByEmail) {
    const uint32_t kExistingUserId = 1;
    const std::string kExistingUserEmail= "user_1@neva.com";
    User user;
    EXPECT_TRUE(user_orm_.GetUserByEmail(kExistingUserEmail, &user).ok()) << "Couldn't get existing user from db";
    EXPECT_EQ(user.user_id(), kExistingUserId);
  }

  TEST_F(UserOrmFixture, InsertNewUser) {
    const std::string kUserEmail = "insert_test@neva.com";
    const std::string kUserPass = "pass";
    User user;
    user.set_email(kUserEmail);
    user.set_password(kUserPass);
    grpc::Status result = user_orm_.InsertUser(user, LoginRequest::DEFAULT, nullptr);
    EXPECT_TRUE(result.ok()) << "Couldn't insert new user " << result.error_message() << result.error_code();
  }

  TEST_F(UserOrmFixture, InsertExistingUser) {
    const std::string kEmail = "insert_test_existing@neva.com";
    const std::string kPass = "password";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    const int user_id = InsertUserToDatabase(user, LoginRequest::DEFAULT);
    grpc::Status result = user_orm_.InsertUser(user, LoginRequest::DEFAULT, nullptr);
    EXPECT_FALSE(result.ok());
  }

  TEST_F(UserOrmFixture, LinkExistingUser) {
    const std::string kEmail = "link_test@neva.com";
    const std::string kPass = "password";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    const int user_id = InsertUserToDatabase(user, LoginRequest::DEFAULT);
    grpc::Status result = user_orm_.InsertUser(user, LoginRequest::FACEBOOK, nullptr);
    EXPECT_TRUE(result.ok()) << "Linking user failed";
  }

  TEST_F(UserOrmFixture, CheckCredentialsCorrect) {
    const std::string kEmail = "credential_check@neva.com";
    const std::string kPass = "password";
    std::string session_token = "";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    InsertUserToDatabase(user, LoginRequest::DEFAULT);
    grpc::Status result = user_orm_.CheckCredentials(kEmail, kPass, LoginRequest::DEFAULT, &session_token);
    EXPECT_TRUE(result.ok()) << "Couldn't authenticate with correct credentials";
    EXPECT_STRCASENE(session_token.c_str(), "Empty session token");
  }

  TEST_F(UserOrmFixture, CheckCredentialsWrong) {
    const std::string kEmail = "credential_check_wrong@neva.com";
    const std::string kPassCorrect = "password1";
    const std::string kPassWrong = "password2";
    std::string session_token = "";
    User user;
    user.set_email(kEmail);
    user.set_password(kPassCorrect);
    InsertUserToDatabase(user, LoginRequest::DEFAULT);
    grpc::Status result = user_orm_.CheckCredentials(kEmail, kPassWrong, LoginRequest::DEFAULT, &session_token);
    EXPECT_FALSE(result.ok()) << "Authenticated with wrong credentials";
    EXPECT_STRCASEEQ(session_token.c_str(), "");
  }

  TEST_F(UserOrmFixture, CheckCredentialsNonExistent) {
    const std::string kEmail = "credential_check_noexist@neva.com";
    const std::string kPass = "password1";
    std::string session_token = "";
    grpc::Status result = user_orm_.CheckCredentials(kEmail, kPass, LoginRequest::DEFAULT, &session_token);
    EXPECT_FALSE(result.ok()) << "Authenticated with non existing user";
    EXPECT_STRCASEEQ(session_token.c_str(), "");
  }

  TEST_F(UserOrmFixture, UpdateUserDataAndGet){
    const std::string kEmail = "user_data@neva.com";
    const std::string kPass = "password";
    User user;
    user.set_email(kEmail);
    user.set_password(kPass);
    const int user_id = InsertUserToDatabase(user, LoginRequest::DEFAULT);
    const int birth_time = 31313131;
    const std::string username = "testusername";
    const User::Gender gender = User::MALE;
    const float weight = 55.55f;
    const std::string photo_url = "photourl://photo.jpg";
    neva::backend::util::Timestamp date_of_birth;
    date_of_birth.set_seconds(birth_time);

    *user.mutable_date_of_birth() = date_of_birth;
    user.set_name(username);
    user.set_gender(gender);
    user.set_weight(weight);
    user.set_photo(photo_url);

    grpc::Status result = user_orm_.UpdateUserData(user_id, user);
    EXPECT_TRUE(result.ok()) << "Couldn't update user data" << user_id << result.error_message();

    User empty_user;
    result = user_orm_.GetUserData(user_id, &empty_user);
    EXPECT_TRUE(result.ok()) << "Couldn't get user data"<< result.error_message();
    EXPECT_EQ(user.date_of_birth().seconds(), birth_time) << "Date of birth is wrong.";
    EXPECT_STREQ(user.name().c_str(), username.c_str())<< "Username is wrong.";
    EXPECT_EQ(user.gender(), gender)<< "Gender is wrong.";
    EXPECT_FLOAT_EQ(user.weight(), weight)<< "Weight is wrong.";
    EXPECT_EQ(user.photo(), photo_url.c_str())<< "Photo is wrong.";
  }
}