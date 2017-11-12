#include "orm/user_orm.h"
#include "glog/logging.h"

namespace neva {
namespace backend {
namespace orm {
namespace user {

User UserOrm::GetUserById(const uint32_t user_id) {
  CHECK(conn_.get() != nullptr) << "Connection was null";
  mysqlpp::Query query = conn_->query("SELECT * FROM `users` WHERE `id`=:%0");
  query.parse();

  mysqlpp::StoreQueryResult res = query.store(user_id);
  CHECK(!res.empty()) << "User not found";
  CHECK_EQ(res.num_rows(), 1) << "Returned more than one user.";

  User user;
  user.set_user_id(user_id);
  user.set_email(res[0]["email"]);
  user.set_name(res[0]["name"]);
  user.set_gender(
      static_cast<User::Gender>(static_cast<int>(res[0]["gender"])));
  user.set_weight(res[0]["weight"]);
  return user;
}

}  // namespace user
}  // namespace orm
}  // namespace backend
}  // namespace neva
