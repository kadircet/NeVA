#include "proposition_orm.h"
#include <memory>
#include "connectionpool.h"
#include "glog/logging.h"
#include "gmock/gmock.h"
#include "google/protobuf/text_format.h"
#include "gtest/gtest.h"
#include "protos/suggestion.pb.h"
#include "testing/mock_orm_fixture.h"

namespace {

constexpr const char* const kUserTableInitFile = "tables/user_history.sql";

using neva::backend::Suggestion;
using neva::backend::orm::PropositionOrm;
using neva::backend::orm::testing::MockOrmFixture;

class UserHistoryOrmFixture : public MockOrmFixture {
 public: