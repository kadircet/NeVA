#include "facebook.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace FacebookValidator {
namespace {

constexpr const char* const kEmail = "cbogrqqktr_1511716461@tfbnw.net";
constexpr const char* const kAuthTokenInvalid = "asdfasdfqwerasdf";
constexpr const char* const kAuthTokenValid =
    R"(EAACZAu8B33nYBACVHdiJShqSGmVnJZASFWE0A5DFiQlZCsEATShIY6eJHJYeaEbe3X2HN7vBeiSrs9AVW6YuZCgrfLFTBaDNlVdnVmChYSZCMiuurZBz9XUPOhOsBP2yhUCiD1apZCMcg0jYeWj90DjVNeSi0XWRUg9KxR1Bd6M4gZDZD)";

TEST(Validate, Valid) { EXPECT_TRUE(Validate(kEmail, kAuthTokenValid)); }

TEST(Validate, Invalid) { EXPECT_FALSE(Validate(kEmail, kAuthTokenInvalid)); }

}  // namespace
}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
