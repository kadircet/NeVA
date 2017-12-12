#include <iostream>

#include <cpr/cpr.h>
#include <json.hpp>
#include "glog/logging.h"
#include "protos/user.pb.h"

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

User FetchInfo(const std::string& email,
               const std::string& authentication_token) {
  // TODO(kadircet): Fetch detailed profile info.
  User user;
  user.set_email(email);
  // We'll use email address as facebook credential.
  user.set_password(email);
  return user;
}

}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
