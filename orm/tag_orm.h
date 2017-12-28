#ifndef _NEVA_ORM_TAGS_H_
#define _NEVA_ORM_TAGS_H_

#include <google/protobuf/repeated_field.h>
#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include <vector>
#include "protos/suggestion.pb.h"

namespace neva {
namespace backend {
namespace orm {

class TagOrm {
 public:
  // Initiates TagOrm class wih given mysql connection.
  TagOrm(std::shared_ptr<mysqlpp::Connection> conn) : conn_(conn) {}

  // Returns tags in the database.
  grpc::Status GetTags(const uint32_t start_index,
                       ::google::protobuf::RepeatedPtrField<Tag>* tag_list);

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
