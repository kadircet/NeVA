#ifndef _NEVA_SOCIAL_MEDIA_FACEBOOK_H_
#define _NEVA_SOCIAL_MEDIA_FACEBOOK_H_

#include <string>

namespace neva {
namespace backend {
namespace FacebookValidator {

// Checks whether a given authentication token is associated with the specified
// email address using Facebook Graph API.
bool Validate(const std::string& email,
              const std::string& authentication_token);

}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva

#endif
