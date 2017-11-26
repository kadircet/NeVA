#include "facebook.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace FacebookValidator {
namespace {

constexpr const char* const kEmail = "cbogrqqktr_1511716461@tfbnw.net";
constexpr const char* const kAuthTokenInvalid = "asdfasdfqwerasdf";
constexpr const char* const kAuthTokenValid =
    R"(EAACZAu8B33nYBAAfb1hxBLTCuaKwEM2ZC6ZAM7rZCC4SpyjDPRWeZC4kPDdRkoJwjST2LP5HTSmZCRUFEOxorN1J5tXf3jZBqMMDPFtuEepuayUCGo5jX4ggpk0KC6w3eAZAoKxk1U3UHWNRO16DaFHLZBrdNeBiMcIEctJnFznL26C5OWdbT6SCtYGZA7CZCFGjIfqElHRB1fVInzSZCZBCGkNPIIb9sAzMzsC46lBQX439HkAZDZD)";

TEST(Validate, Valid) { EXPECT_TRUE(Validate(kEmail, kAuthTokenValid)); }

TEST(Validate, Invalid) { EXPECT_FALSE(Validate(kEmail, kAuthTokenInvalid)); }

}  // namespace
}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
