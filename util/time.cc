#include "time.h"

#include <chrono>

namespace neva {
namespace backend {
namespace util {

uint64_t GetTimestamp() {
  return std::chrono::system_clock::now().time_since_epoch().count();
}

}  // namespace util
}  // namespace backend
}  // namespace neva
