#include "facebook.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace FacebookValidator {
namespace {

constexpr const char* const kEmail = "10156039002563534";
constexpr const char* const kAuthTokenInvalid = "asdfasdfqwerasdf";
constexpr const char* const kAuthTokenValid =
    R"(EAACZAu8B33nYBAIPugzwpxa39utKEi4kpWAqYv6UZAZAXiH1qpC7BMCU7kWFa3quXtda3XO8kulxzgketefLfN1Ku9fSxN5XMxR2EKw931oWY1sVIiGKkBJLwMwHy6052sdWwP6ZB6NuJvlFMF2qvYcN2S440LNVKYrtawGSyZCYZAmcyWtZBjk)";

TEST(Validate, Valid) { EXPECT_TRUE(Validate(kEmail, kAuthTokenValid)); }

TEST(Validate, Invalid) { EXPECT_FALSE(Validate(kEmail, kAuthTokenInvalid)); }

}  // namespace
}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
