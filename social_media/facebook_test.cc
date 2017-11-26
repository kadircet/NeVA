#include "facebook.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace FacebookValidator {
namespace {

constexpr const char* const kEmail = "cbogrqqktr_1511716461@tfbnw.net";
constexpr const char* const kAuthTokenInvalid = "asdfasdfqwerasdf";
constexpr const char* const kAuthTokenValid =
    R"(EAACZAu8B33nYBAHyARqh8V6tQEF84eRMGC5UNwXEJM79aicwfFcnZCfSZBugZAZBxZCcGZCpHEZBQIlCJinFfzoSjUpY8EvZAtWcHwpQBxftzUDxQO2VKehD5mV83lNwSiAKr0uYEBGYkiZC7cJvFql0K2gGdTlh8atpUTzeUotEhbjVgyYeYCwpWZA)";

TEST(Validate, Valid) { EXPECT_TRUE(Validate(kEmail, kAuthTokenValid)); }

TEST(Validate, Invalid) { EXPECT_FALSE(Validate(kEmail, kAuthTokenInvalid)); }

}  // namespace
}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
