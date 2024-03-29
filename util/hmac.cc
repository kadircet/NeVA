#include "hmac.h"
#include "glog/logging.h"

#include <openssl/hmac.h>
#include <algorithm>
#include <functional>
#include <random>

namespace neva {
namespace backend {
namespace util {
namespace {

constexpr const size_t kQwordSizeInBits = 64;
constexpr const size_t kQwordSizeInBytes = 8;
constexpr const size_t kSHA512OutputSize = 512 / 8;
constexpr const int kMaxLength = 128;

}  // namespace

const std::string GenerateRandomKey(const unsigned seed, const int length) {
  CHECK_LE(length, kMaxLength) << "Key length cannot be more than "
                               << kMaxLength << " bytes. Length: " << length;
  CHECK_EQ(length % kQwordSizeInBytes, 0)
      << "Key length must be a multiple of " << kQwordSizeInBytes
      << ". Length: " << length;

  static std::independent_bits_engine<std::mt19937, kQwordSizeInBits, uint64_t>*
      random_qword =
          new std::independent_bits_engine<std::mt19937, kQwordSizeInBits,
                                           uint64_t>(seed);
  std::string random_bytes;
  random_bytes.resize(length);
  std::generate(random_bytes.begin(), random_bytes.end(),
                std::ref(*random_qword));

  return random_bytes;
}

const std::string GenerateRandomKeySecure(const int length) {
  CHECK_LE(length, kMaxLength) << "Key length cannot be more than "
                               << kMaxLength << " bytes. Length: " << length;
  CHECK_EQ(length % kQwordSizeInBytes, 0)
      << "Key length must be a multiple of " << kQwordSizeInBytes
      << ". Length: " << length;

  static std::independent_bits_engine<std::random_device, kQwordSizeInBits,
                                      uint64_t>* random_qword =
      new std::independent_bits_engine<std::random_device, kQwordSizeInBits,
                                       uint64_t>();
  std::string random_bytes;
  random_bytes.resize(length);
  std::generate(random_bytes.begin(), random_bytes.end(),
                std::ref(*random_qword));

  return random_bytes;
}

const std::string HMac(const std::string& key, const std::string& message) {
  std::string hmac;
  hmac.resize(kSHA512OutputSize);
  HMAC(EVP_sha512(), key.data(), key.size(),
       reinterpret_cast<const unsigned char*>(message.data()), message.size(),
       reinterpret_cast<unsigned char*>(&*hmac.begin()), nullptr);

  return hmac;
}

uint32_t GetRandom(const uint32_t n) {
  static std::default_random_engine* random_engine =
      new std::default_random_engine();
  uint64_t upper_limit = 1;
  while (upper_limit < n) upper_limit <<= 1;
  uint64_t result = (*random_engine)() % upper_limit;
  while (result >= n) result = (*random_engine)() % upper_limit;
  return result;
}

void InitializeRandom() {
  std::random_device random_device;
  GenerateRandomKey(random_device());
}

}  // namespace util
}  // namespace backend
}  // namespace neva
