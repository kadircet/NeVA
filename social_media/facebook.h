#ifndef _NEVA_SOCIAL_MEDIA_FACEBOOK_H_
#define _NEVA_SOCIAL_MEDIA_FACEBOOK_H_

#include <string>

namespace neva {
namespace backend {
namespace FacebookValidator {

bool Validate(const std::string& email,
              const std::string& authentication_token);
}
}  // namespace backend
}  // namespace neva

#endif
