#ifndef _NEVA_ORM_PROPOSITION_H_
#define _NEVA_ORM_PROPOSITION_H_

#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include "protos/suggestion.pb.h"

namespace neva {
namespace backend {
namespace orm {

class PropositionOrm {
 public:
  // Initiates PropositionOrm class wih given mysql connection.
  PropositionOrm(std::shared_ptr<mysqlpp::Connection> conn) : conn_(conn) {}

  // Inserts given suggestion item proposition to database.
  grpc::Status InsertProposition(const int user_id,
                                 const Suggestion& suggestion);

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
