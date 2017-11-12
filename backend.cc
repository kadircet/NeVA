#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>
#include "protos/backend.grpc.pb.h"

namespace neva {
namespace backend {
namespace {

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

class BackendServiceImpl final : public Backend::Service {
  Status Register(ServerContext* context, const RegisterRequest* request,
                  RegisterReply* reply) override {
    return Status(grpc::StatusCode::UNIMPLEMENTED, "Not implemented yet");
  }

  Status Login(ServerContext* context, const LoginRequest* request,
               LoginReply* reply) override {
    return Status(grpc::StatusCode::UNIMPLEMENTED, "Not implemented yet");
  }
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
