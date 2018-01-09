#include "orm/tag_orm.h"
#include "glog/logging.h"

namespace neva {
namespace backend {
namespace orm {
namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

Status TagOrm::GetTags(const uint32_t start_index,
                       ::google::protobuf::RepeatedPtrField<Tag>* tag_list) {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  mysqlpp::Query query =
      conn->query("SELECT `id`, `key` FROM `tag` WHERE `id`>%0");
  query.parse();

  const mysqlpp::StoreQueryResult res = query.store(start_index);
  if (!res) {
    return Status(StatusCode::INTERNAL, query.error());
  }
  for (const auto& row : res) {
    Tag* tag = tag_list->Add();
    tag->set_id(row["id"]);
    tag->set_name(row["key"]);
    VLOG(1) << tag->ShortDebugString() << " has been added to response.";
  }
  return Status::OK;
}

}  // namespace orm
}  // namespace backend
}  // namespace neva
