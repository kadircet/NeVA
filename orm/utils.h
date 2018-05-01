#ifndef _NEVA_ORM_UTILS_H_
#define _NEVA_ORM_UTILS_H_
#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include "protos/suggestion.pb.h"
namespace neva {
namespace backend {
namespace orm {
namespace {

using grpc::Status;
using grpc::StatusCode;

}  // namespace

// Fetches tags associated with given suggestee and stores them into it.
// Assumes suggestee->suggestee_id is set.
void GetTags(const mysqlpp::ScopedConnection& conn, Suggestion* suggestee);

}  // namespace orm
}  // namespace backend
}  // namespace neva
#endif
