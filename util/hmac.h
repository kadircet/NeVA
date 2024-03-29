#ifndef _NEVA_UTIL_HMAC_H_
#define _NEVA_UTIL_HMAC_H_

#include <string>

namespace neva {
namespace backend {
namespace util {

// Generates a random key of given length. Initiates rng with the given seed.
// Please note that, this function is not designed to be cryptograpihacally
// secure. Because its main purpose is to be used for generating the key to
// HMac. Which doesn't need to be secure but only unique.
const std::string GenerateRandomKey(const unsigned seed = 5489u,
                                    const int length = 128);

// Signs the message using the given key and returns the signature. Uses SHA512.
const std::string HMac(const std::string& key, const std::string& message);

// Returns a random integer in range [0,n).
uint32_t GetRandom(const uint32_t n);

// Initializes seed of random number generator.
void InitializeRandom();

// Generates a random key of given length.
// Please note that, this function is designed to be cryptograpihacally secure.
// Therefore it is a lot slower when entropy is low compared to insecure
// version. Make sure you use it whenever security is a real concern.
const std::string GenerateRandomKeySecure(const int length = 128);

}  // namespace util
}  // namespace backend
}  // namespace neva

#endif
