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

constexpr const char* kExpectedHmac =
    "\x9B\xA1\xF6"
    "3e\xA6\xCA\xF6nF4\x8F"
    "C\xCD\xEF\x95`\x15\xBE\xA9\x97\xAD\xEB\x6\xE6\x90\a\xEE?"
    "\xF5\x17\xDF\x10\xFC^\xB8`\xDA=C\xB8,*"
    "\x4\f\x93\x11\x19\xD2\xDF\xC6\xD0\x8E%7B):\x86\x8C\xC2\xD8 "
    "\x15";
constexpr const size_t kExpectedHMacOutputSize = 512 / 8;

TEST(GenerateRandomKey, SanityTest) {
  const std::string random_key = GenerateRandomKey(0, kExpectedRandomKeySize);

  EXPECT_EQ(random_key.size(), kExpectedRandomKeySize);
  EXPECT_EQ(random_key, kExpectedRandomKey);
}

TEST(HMac, SanityTest) {
  const std::string key = "test";
  const std::string message = "test";
  const std::string hmac = HMac(key, message);

  EXPECT_EQ(hmac.size(), kExpectedHMacOutputSize);
  EXPECT_EQ(hmac, kExpectedHmac);
}

}  // namespace
}  // namespace util
}  // namespace backend
}  // namespace neva
