#include <iostream>

#include <cpr/cpr.h>
#include <json.hpp>
#include "glog/logging.h"

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
  auto iterator = json.find("email");
  if (iterator == json.end()) {
    VLOG(1) << "Something went wrong got response:\n" << r.text;
    return false;
  }
  const bool status = json["email"] == email;
  VLOG(1) << json["email"] << "==" << email;
  return status;
}

}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
