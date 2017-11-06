#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>
#include "protos/backend.grpc.pb.h"

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

using backend::Backend;
using backend::RegisterReply;
using backend::RegisterRequest;

class BackendServiceImpl final : public Backend::Service {
  Status Register(ServerContext* context, const RegisterRequest* request,
                  RegisterReply* reply) override {
    reply->set_message("Not implemented yet");
    return Status::OK;
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

int main(int argc, char** argv) {
  RunServer();
  return 0;
}
