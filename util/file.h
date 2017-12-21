#ifndef _NEVA_UTIL_FILE_H_
#define _NEVA_UTIL_FILE_H_

#include <string>

namespace neva {
namespace backend {
namespace util {

// Opens and reads the file with path file_name into buffer.
void ReadFile(const std::string& file_name, std::string* buffer);

}  // namespace util
}  // namespace backend
}  // namespace neva

#endif
