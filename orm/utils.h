#ifndef _NEVA_ORM_UTILS_H_
#define _NEVA_ORM_UTILS_H_
#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include "protos/suggestion.pb.h"
namespace neva {
namespace backend {
namespace orm {
  
  using grpc::Status;
  using grpc::StatusCode;
  
  // Fetches tags associated with given suggestee and stores them into it.
  // Assumes suggestee->suggestee_id is set.
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
#endif
