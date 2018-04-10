#include "user_orm.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace orm {
namespace {

using grpc::Status;
using grpc::StatusCode;

  TEST(GetUserById, ExistingUser) {
    int kUserId = 1;
    int kStatus = 1;
    string kUserEmail = "hkn@test.com";
    User db_user;

    UserOrm user_orm;
    Status result = user_orm.GetUserById(kUserId, &db_user);
    EXPECT_EQ(result, Status::OK);
    EXPECT_EQ(user.id, kUserId);
    EXPECT_EQ(user.status, kStatus);
    EXPECT_EQ(user.email, kUserEmail);

  }

  TEST(GetUserById, NonExistentUser) {
    int kUserId = -1;
    User db_user;
    UserOrm user_orm;
    Status result = user_orm.GetUserById(kUserId, &db_user);
    EXPECT_EQ(result.error_code(), StatusCode::UNKNOWN);
  }

  TEST(GetUserByEmail, ExistingUser) {
    int kUserId = 1;
    int kStatus = 1;
    string kUserEmail = "hkn@test.com";
    User db_user;

    UserOrm user_orm;
    Status result = user_orm.GetUserById(kUserEmail, &db_user);
    EXPECT_EQ(result, Status::OK);
    EXPECT_EQ(user.id, kUserId);
    EXPECT_EQ(user.status, kStatus);
    EXPECT_EQ(user.email, kUserEmail);

  }

  TEST(GetUserByEmail, NonExistentUser) {
    string kUserEmail = "thisisnotevenavalidemail";
    User db_user;
    UserOrm user_orm;
    Status result = user_orm.GetUserById(kUserEmail, &db_user);
    EXPECT_EQ(result.error_code(), StatusCode::UNKNOWN);
  }
}
}
}
}