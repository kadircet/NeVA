#ifndef _NEVA_ORM_TESTING_MOCK_ORM_FIXTURE_H_
#define _NEVA_ORM_TESTING_MOCK_ORM_FIXTURE_H_

#include <memory>
#include "gtest/gtest.h"
#include "orm/connectionpool.h"

namespace neva {
namespace backend {
namespace orm {
namespace testing {

class MockOrmFixture : public ::testing::Test {
 protected:
  MockOrmFixture();

  void PrepareDatabase(const char* const db_init_file_name);
  ~MockOrmFixture();

  std::shared_ptr<NevaConnectionPool> conn_pool_;
};

}  // namespace testing
}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
