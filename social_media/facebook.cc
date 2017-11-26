#include <iostream>

#include <cpr/cpr.h>
#include <json.hpp>

#include "facebook.h"

namespace neva {
namespace backend {
namespace FacebookValidator {

bool Validate(const std::string& email,
              const std::string& authentication_token) {
  auto r = cpr::Get(cpr::Url{"https://graph.facebook.com/me"},
                    cpr::Parameters{{"fields", "email"},
                                    {"access_token", authentication_token}});
  auto json = nlohmann::json::parse(r.text);
  return json["email"] == email;
}

}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
