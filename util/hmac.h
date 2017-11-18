#ifndef _NEVA_UTIL_HMAC_H_
#define _NEVA_UTIL_HMAC_H_

#include <string>

namespace neva {
namespace backend {
namespace util {

// Generates a random key of given length.
// Please note that, this function is not designed to be cryptograpihacally
// secure. Because its main purpose is to be used for generating the key to
// HMac. Which doesn't need to be secure but only unique.
const std::string GenerateRandomKey(const int length);

// Signs the message using the given key and returns the signature. Uses SHA512.
const std::string HMac(const std::string& key, const std::string& message);

}  // namespace util
}  // namespace backend
}  // namespace neva

#endif
