#ifndef _NEVA_ORM_SUGGESTIONS_H_
#define _NEVA_ORM_SUGGESTIONS_H_

#include <grpc++/impl/codegen/status.h>
#include <mysql++.h>
#include <cstdint>
#include <memory>
#include <vector>
#include "protos/suggestion.pb.h"

namespace neva {
namespace backend {
namespace orm {

class SuggestionOrm {
 public:
  // Initiates UserOrm class wih given mysql connection.
  SuggestionOrm(std::shared_ptr<mysqlpp::Connection> conn) : conn_(conn) {}

  grpc::Status GetSuggestees(
      const Suggestion::SuggestionCategory suggestion_category,
      const uint32_t start_index, SuggestionList* suggestion_list);

  grpc::Status GetSuggestion(
      const Suggestion::SuggestionCategory suggestion_category,
      Suggestion* suggestion);

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
