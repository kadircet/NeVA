#ifndef _NEVA_UTIL_TIME_H_
#define _NEVA_UTIL_TIME_H_

#include <cstdint>

namespace neva {
namespace backend {
namespace util {

// Returns unix timestamp in seconds.
uint64_t GetTimestamp();

}  // namespace util
}  // namespace backend
}  // namespace neva

#endif
