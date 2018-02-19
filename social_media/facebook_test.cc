#include "facebook.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace FacebookValidator {
namespace {

constexpr const char* const kEmail = "cbogrqqktr_1511716461@tfbnw.net";
constexpr const char* const kAuthTokenInvalid = "asdfasdfqwerasdf";
constexpr const char* const kAuthTokenValid =
    R"(EAACZAu8B33nYBAGFp8blpFnU8l7WrJkw5HsogZAZBNwB03QVrJ3Hxr7QwUdZBQiSfT6sajdWYniZBsSbD21qTKkReubAZCsEfwdxXEZAIWzLqYjKlH7kPmuZBNCrjF85FoOQIXNSWVRUnT4fQ7ZB1Oj1SXhmKbVoh354K3GDCskslO4BguaJU2YoHvzMwUQ9BHmee6FfrRptqWJgyJ1h87ZBceVZBENPe9CAJ2wK66pZBN6pjgZDZD)";

TEST(Validate, Valid) { EXPECT_TRUE(Validate(kEmail, kAuthTokenValid)); }

TEST(Validate, Invalid) { EXPECT_FALSE(Validate(kEmail, kAuthTokenInvalid)); }

}  // namespace
}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
