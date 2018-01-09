#include "orm/connectionpool.h"

#include <chrono>
#include <thread>

#include "glog/logging.h"

namespace neva {
namespace backend {
namespace orm {

namespace {
constexpr const uint32_t kMaxConnectionCount = 128;
constexpr const uint32_t kSleepDuration = 250;
}  // namespace

mysqlpp::Connection* NevaConnectionPool::grab() {
  // No need to lock the mutex while reading, because conns_in_use is primitive
  // and updated in one cycle. So we can't misread.
  while (conns_in_use_ > kMaxConnectionCount) {
    std::this_thread::sleep_for(std::chrono::milliseconds(kSleepDuration));
  }

  std::unique_lock<std::mutex> lock(conn_count_mutex_);
  ++conns_in_use_;
  mysqlpp::Connection* conn = mysqlpp::ConnectionPool::grab();
  CHECK(conn->ping()) << "SQL server connection faded away.";
  return conn;
}

void NevaConnectionPool::release(const mysqlpp::Connection* conn) {
  mysqlpp::ConnectionPool::release(conn);
  std::unique_lock<std::mutex> lock(conn_count_mutex_);
  --conns_in_use_;
}

mysqlpp::Connection* NevaConnectionPool::create() {
  mysqlpp::Connection* conn = new mysqlpp::Connection(false);
  conn->set_option(new mysqlpp::ReconnectOption(true));
  conn->connect(database_name_.c_str(), database_server_.c_str(),
                database_user_.c_str(), database_password_.c_str());
  CHECK(conn->connected()) << "Database connection failed." << conn->error();
  conn->query("SET NAMES utf8;").execute();
  return conn;
}

void NevaConnectionPool::destroy(mysqlpp::Connection* conn) { delete conn; }

}  // namespace orm
}  // namespace backend
}  // namespace neva
