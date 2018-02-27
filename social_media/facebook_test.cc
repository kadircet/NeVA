#include "facebook.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace FacebookValidator {
namespace {

constexpr const char* const kEmail = "10156039002563534";
constexpr const char* const kAuthTokenInvalid = "asdfasdfqwerasdf";
constexpr const char* const kAuthTokenValid =
    R"(EAACZAu8B33nYBAL5YYjoDWqRCXoCpbeyEayoEU6xiUqfX289YqMkdns9p73ZAsoSFouS2jOZAoSGBrXNi2dqJddYaZAN9ymiYEM2bEZCZCB7pZAJyvOIYN9X1ZACKul8zNhrYZAaPSfkdZBBAE8jGMRZBaakNVUxttGg6sZD)";

TEST(Validate, Valid) { EXPECT_TRUE(Validate(kEmail, kAuthTokenValid)); }

TEST(Validate, Invalid) { EXPECT_FALSE(Validate(kEmail, kAuthTokenInvalid)); }

}  // namespace
}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
