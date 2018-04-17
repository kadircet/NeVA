#include "tag_orm.h"
#include <memory>
#include "connectionpool.h"
#include "glog/logging.h"
#include "gmock/gmock.h"
#include "google/protobuf/text_format.h"
#include "gtest/gtest.h"
#include "testing/mock_orm_fixture.h"
#include "protos/suggestion.pb.h"


namespace {

  constexpr const char* const kTagTableInitFile = "tables/suggestions.sql";
  constexpr const char* const kInitialDataSetFile = "orm/tag_orm_test.sql";

  using grpc::Status;
  using grpc::StatusCode;
  using neva::backend::Tag;
  using neva::backend::orm::TagOrm;
  using neva::backend::orm::testing::MockOrmFixture;

  class TagOrmFixture: public MockOrmFixture {
    public:
      TagOrmFixture(): tag_orm_(conn_pool_) {
        PrepareDatabase(kTagTableInitFile);
        PrepareDatabase(kInitialDataSetFile);
      }
    protected:
      TagOrm tag_orm_;
  };

  TEST_F(TagOrmFixture, GetExistingTags) {
    const uint32_t kStartIndex = 0;
    const size_t kResultSize = 1;
    ::google::protobuf::RepeatedPtrField<Tag> tag_list;
    EXPECT_TRUE(tag_orm_.GetTags(kStartIndex, &tag_list).ok()) << "GetTags failed to get tags.";
    EXPECT_EQ(tag_list.size(), kResultSize) << "Count of tags got is wrong.";
  }
}