#include "utils.h"

namespace neva {
namespace backend {
namespace orm {
namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

void GetTags(const mysqlpp::ScopedConnection& conn, Suggestion* suggestee) {
  mysqlpp::Query query = conn->query(
      "SELECT `tag_id` FROM `suggestee_tags` WHERE `suggestee_id`=%0");
  query.parse();
  const mysqlpp::StoreQueryResult res = query.store(suggestee->suggestee_id());
  for (const auto row : res) {
    Tag* tag = suggestee->add_tags();
    tag->set_id(row["tag_id"]);
  }
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
