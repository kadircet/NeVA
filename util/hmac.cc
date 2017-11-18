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

constexpr const size_t kQwordSize = 64;
constexpr const size_t kSHA512OutputSize = 512 / 8;

}  // namespace

const std::string GenerateRandomKey(const int length = 128) {
  CHECK_LE(length, 128) << "Key length cannot be more than 128 bits. Length: "
                        << length;
  CHECK_EQ(length % kQwordSize, 0) << "Key length must be a multiple of "
                                   << kQwordSize << ". Length: " << length;

  std::independent_bits_engine<std::mt19937, kQwordSize, uint64_t> random_qword;
  std::string random_bytes;
  const size_t number_of_bytes = length / kQwordSize;

  random_bytes.reserve(number_of_bytes);
  std::generate(random_bytes.begin(), random_bytes.begin() + number_of_bytes,
                std::ref(random_qword));

  return random_bytes;
}

const std::string HMac(const std::string& key, const std::string& message) {
  std::string hmac;
  hmac.reserve(kSHA512OutputSize);
  HMAC(EVP_sha512(), key.data(), key.size(),
       reinterpret_cast<const unsigned char*>(message.data()), message.size(),
       reinterpret_cast<unsigned char*>(&*hmac.begin()), nullptr);

  return hmac;
}

}  // namespace util
}  // namespace backend
}  // namespace neva
