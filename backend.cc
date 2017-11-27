#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>
#include <mysql++.h>

#include "glog/logging.h"
#include "orm/user_orm.h"
#include "protos/backend.grpc.pb.h"
#include "social_media/facebook.h"

namespace neva {
namespace backend {
namespace {

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;
using orm::user::UserOrm;

constexpr const char* const kNevaDatabaseName = "neva";
constexpr const char* const kNevaDatabaseServer = "localhost";
constexpr const char* const kNevaDatabaseUser = "neva";
constexpr const char* const kNevaDatabasePassword = "";

class BackendServiceImpl final : public Backend::Service {
 public:
  Status Register(ServerContext* context, const RegisterRequest* request,
                  GenericReply* reply) override {
    std::string verification_token;
    // TODO(kadircet): Implement input sanity checking.

    const User user = request->user();
    if (request->authentication_type() == RegisterRequest::FACEBOOK) {
      if (!FacebookValidator::Validate(user.email(), user.password())) {
        return Status(grpc::StatusCode::INVALID_ARGUMENT,
                      "Authentication token cannot be validated.");
      }
    }
    const Status status = user_orm_->InsertUser(user, &verification_token);
    // TODO(kadircet): Implement sending of verification_token with email.
    return status;
  }

  Status Login(ServerContext* context, const LoginRequest* request,
               LoginReply* reply) override {
    return user_orm_->CheckCredentials(request->email(), request->password(),
                                       reply->mutable_token());
  }

  Status SuggestionItemProposition(
      ServerContext* context, const SuggestionItemPropositionRequest* request,
      GenericReply* reply) override {
    int user_id;
    const Status status = user_orm_->CheckToken(request->token, &user_id);
    if (!status.Ok()) {
      return status;
    }
    return Status(grpc::StatusCode::UNIMPLEMENTED, "Not implemented yet");
  }

  BackendServiceImpl() {
    conn_ = std::make_shared<mysqlpp::Connection>(false);
    conn_->connect(kNevaDatabaseName, kNevaDatabaseServer, kNevaDatabaseUser,
                   kNevaDatabasePassword);
    CHECK(conn_->connected()) << "Database connection failed.";

    user_orm_ = std::unique_ptr<UserOrm>(new UserOrm(conn_));
  }

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
  std::unique_ptr<UserOrm> user_orm_;
};

void RunServer() {
  std::string server_address("0.0.0.0:50051");
  BackendServiceImpl service;

  ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
  builder.RegisterService(&service);

  std::unique_ptr<Server> server(builder.BuildAndStart());
  std::cout << "Server started on " << server_address << std::endl;

  server->Wait();
}

}  // namespace
}  // namespace backend
}  // namespace neva

int main(int argc, char** argv) {
  neva::backend::RunServer();
  return 0;
}
