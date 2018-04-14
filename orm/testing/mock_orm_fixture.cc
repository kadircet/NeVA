#include "mock_orm_fixture.h"
#include <fstream>
#include "glog/logging.h"
#include "orm/connectionpool.h"

namespace neva {
namespace backend {
namespace orm {
namespace testing {

namespace {
constexpr const char* const kTestDbName = "neva_test";
}

MockOrmFixture::MockOrmFixture() {
  conn_pool_ =
      std::make_shared<NevaConnectionPool>("", "localhost", "neva_test", "");
  mysqlpp::ScopedConnection conn(*conn_pool_);
  conn->drop_db(kTestDbName);
  CHECK(!conn->select_db(kTestDbName))
      << "Test databas already exists, last run might not be clean. Check "
         "Manually for artifacts and drop the database!";
  CHECK(conn->create_db(kTestDbName))
      << "Failed to create test database:" << conn->error();
  CHECK(conn->select_db(kTestDbName))
      << "Failed to select db:" << conn->error();
}

void MockOrmFixture::PrepareDatabase(const char* const db_init_file_name) {
  std::ifstream db_init_file(db_init_file_name);
  CHECK(db_init_file) << "Initialization file not found.";
  mysqlpp::ScopedConnection conn(*conn_pool_);
  CHECK(conn->set_option(new mysqlpp::MultiStatementsOption(true)))
      << "Failed to set option:" << conn->error();
  const std::string db_init_query{std::istreambuf_iterator<char>(db_init_file),
                                  std::istreambuf_iterator<char>()};
  mysqlpp::Query query = conn->query(db_init_query);
  CHECK(query.execute()) << conn->error() << db_init_query;
  while (query.more_results()) query.store_next();
}

MockOrmFixture::~MockOrmFixture() {
  mysqlpp::ScopedConnection conn(*conn_pool_);
  CHECK(conn->drop_db(kTestDbName)) << "Couldn't drop test database.\n"
                                    << conn->error();
}

}  // namespace testing
}  // namespace orm
}  // namespace backend
}  // namespace neva
