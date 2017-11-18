#include "hmac.h"
#include "gtest/gtest.h"

namespace neva {
namespace backend {
namespace util {
namespace {

constexpr const char* kExpectedRandomKey =
    "/\xC0\xFBg\xD3\xF2W\xD8\x8C\xC1'\xAEQ\x19H\x94\xD0\xC5O\xC0"
    "c\xB1\x1D\x93\xA7\xC1\xB9 "
    "\xCA\x97\xFEr\x1C\x80\xA4\x85\xE8\x11\x84*\x1F\x1\xE7"
    "9f\vR\x80"
    "c\x8C\xAA\xCB\x6/"
    "\xF4\xCC\xB4N\x94\xBA\xCFu0E\xA3_^q$0\x83*p\x95\x7F\x8A+\x7F\xBBy>"
    "\xDE\xC3\xAE\x94"
    "2\xE:$Vh\x2P\xB6&\xAEs\xBCM\x18\x2^kpH_\x9A\xF8"
    "C=`\xC3\x8BVyK\x10\x9Dn\xD0yuSh\xE4\xFB"
    "F\x1F";
constexpr const size_t kExpectedRandomKeySize = 128;

TEST(GenerateRandomKey, SanityTest) {
  const std::string random_key = GenerateRandomKey(0, kExpectedRandomKeySize);
  EXPECT_EQ(random_key.size(), kExpectedRandomKeySize);
  EXPECT_EQ(random_key, kExpectedRandomKey);
}

TEST(HMac, SanityTest) {}

}  // namespace
}  // namespace util
}  // namespace backend
}  // namespace neva
