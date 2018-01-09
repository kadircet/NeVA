#ifndef _NEVA_ORM_CONNECTIONPOOL_H_
#define _NEVA_ORM_CONNECTIONPOOL_H_

#include <mysql++.h>
#include <mutex>
#include <string>

constexpr const unsigned int kMaxIdleTime = 3;

namespace neva {
namespace backend {
namespace orm {

class NevaConnectionPool : public mysqlpp::ConnectionPool {
 public:
  NevaConnectionPool(const std::string& database_name,
                     const std::string& database_server,
                     const std::string& database_user,
                     const std::string& database_password)
      : database_name_(database_name),
        database_server_(database_server),
        database_user_(database_user),
        database_password_(database_password),
        conns_in_use_(0) {}
  ~NevaConnectionPool() { clear(); }

  mysqlpp::Connection* grab();
  void release(const mysqlpp::Connection* conn);

 protected:
  mysqlpp::Connection* create();
  void destroy(mysqlpp::Connection* conn);
  unsigned int max_idle_time() { return kMaxIdleTime; }

 private:
  const std::string database_name_;
  const std::string database_server_;
  const std::string database_user_;
  const std::string database_password_;

  std::mutex conn_count_mutex_;
  uint32_t conns_in_use_;
};

}  // namespace orm
}  // namespace backend
}  // namespace neva

#endif
