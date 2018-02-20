#include <iostream>

#include <cpr/cpr.h>
#include "glog/logging.h"
#include "json.hpp"
#include "protos/user.pb.h"

#include "facebook.h"

namespace neva {
namespace backend {
namespace FacebookValidator {

bool Validate(const std::string& user_id,
              const std::string& authentication_token) {
  auto r = cpr::Get(cpr::Url{"https://graph.facebook.com/me"},
                    cpr::Parameters{{"fields", "id"},
                                    {"access_token", authentication_token}});
  auto json = nlohmann::json::parse(r.text);
  auto iterator = json.find("id");
  if (iterator == json.end()) {
    VLOG(1) << "Something went wrong got response:\n" << r.text;
    return false;
  }
  const bool status = json["id"] == user_id;
  VLOG(1) << json["id"] << "==" << user_id;
  return status;
}

User FetchInfo(const std::string& user_id,
               const std::string& authentication_token) {
  // TODO(kadircet): Fetch detailed profile info.
  User user;
  user.set_email(user_id);
  // We'll use email address as facebook credential.
  user.set_password(user_id);
  return user;
}

}  // namespace FacebookValidator
}  // namespace backend
}  // namespace neva
