#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>
#include <mysql++.h>

#include "gflags/gflags.h"
#include "glog/logging.h"
#include "orm/proposition_orm.h"
#include "orm/suggestion_orm.h"
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
using orm::PropositionOrm;
using orm::SuggestionOrm;
using orm::user::UserOrm;

constexpr const char* const kNevaDatabaseName = "neva";
constexpr const char* const kNevaDatabaseServer = "localhost";
constexpr const char* const kNevaDatabaseUser = "neva";
constexpr const char* const kNevaDatabasePassword = "";

class BackendServiceImpl final : public Backend::Service {
 public:
  Status Register(ServerContext* context, const RegisterRequest* request,
                  GenericReply* reply) override {
    VLOG(1) << "Received Register Request:" << request->user().email();
    std::string verification_token;
    // TODO(kadircet): Implement input sanity checking.

    const User user = request->user();
    const Status status = user_orm_->InsertUser(user, &verification_token);
    // TODO(kadircet): Implement sending of verification_token with email.
    return status;
  }

  Status Login(ServerContext* context, const LoginRequest* request,
               LoginReply* reply) override {
    const std::string email = request->email();
    const std::string password = request->password();
    VLOG(1) << "Received Login Request for: " << email;
    if (request->authentication_type() == LoginRequest::FACEBOOK) {
      if (!FacebookValidator::Validate(email, password)) {
        return Status(grpc::StatusCode::INVALID_ARGUMENT,
                      "Authentication token cannot be validated.");
      }
      // TODO(kadircet): Fetch relevant user info from facebook if logging in
      // for the first time and perform register.
      User user;
      user.set_email(email);
      user.set_password(password);
      std::string verification_token;
      user_orm_->InsertUser(user, &verification_token);
    }
    return user_orm_->CheckCredentials(email, password, reply->mutable_token());
  }

  Status SuggestionItemProposition(
      ServerContext* context, const SuggestionItemPropositionRequest* request,
      GenericReply* reply) override {
    VLOG(1) << "Received SuggestionItemProposition:\n"
            << request->DebugString();

    int user_id;
    const Status status = user_orm_->CheckToken(request->token(), &user_id);
    if (!status.ok()) {
      return status;
    }
    return proposition_orm_->InsertProposition(user_id, request->suggestion());
  }

  Status GetSuggestion(ServerContext* context,
                       const GetSuggestionRequest* request,
                       GetSuggestionReply* reply) override {
    VLOG(1) << "Received GetSuggestion for category: "
            << request->DebugString();
    int user_id;
    {
      const Status status = user_orm_->CheckToken(request->token(), &user_id);
      if (!status.ok()) {
        return status;
      }
    }
    {
      Suggestion suggestion;
      const Status status = suggestion_orm_->GetSuggestion(
          request->suggestion_category(), &suggestion);
      if (!status.ok()) {
        return status;
      }
      reply->set_name(suggestion.name());
    }
    return Status::OK;
  }

  Status TagProposition(ServerContext* context,
                        const TagPropositionRequest* request,
                        GenericReply* reply) override {
    VLOG(1) << "Received TagProposition:" << request->DebugString();
    int user_id;
    const Status status = user_orm_->CheckToken(request->token(), &user_id);
    if (!status.ok()) {
      return status;
    }
    return proposition_orm_->InsertProposition(user_id, request->tag());
  }

  Status TagValueProposition(ServerContext* context,
                             const TagValuePropositionRequest* request,
                             GenericReply* reply) override {
    VLOG(1) << "Received TagValueProposition:" << request->DebugString();
    int user_id;
    const Status status = user_orm_->CheckToken(request->token(), &user_id);
    if (!status.ok()) {
      return status;
    }
    return proposition_orm_->InsertProposition(
        user_id, request->tag_id(), request->suggestee_id(), request->value());
  }

  Status GetSuggestionItemList(ServerContext* context,
                               const GetSuggestionItemListRequest* request,
                               GetSuggestionItemListReply* reply) override {
    VLOG(1) << "Received GetSuggestionItemList:" << request->DebugString();
    int user_id;
    const Status status = user_orm_->CheckToken(request->token(), &user_id);
    if (!status.ok()) {
      return status;
    }
    std::vector<Suggestion> suggestees;
    suggestion_orm_->GetSuggestees(request->suggestion_category(),
                                   request->start_index(), &suggestees);
    for (const Suggestion& suggestion : suggestees) {
      *reply->add_items() = suggestion;
    }
    return Status::OK;
  }

  Status InformUserChoice(ServerContext* context,
                          const InformUserChoiceRequest* request,
                          GenericReply* reply) override {
    VLOG(1) << "Received InformUserChoice:" << request->DebugString();
    int user_id;
    const Status status = user_orm_->CheckToken(request->token(), &user_id);
    if (!status.ok()) {
      return status;
    }
    return Status(grpc::StatusCode::UNIMPLEMENTED, "Not implemented yet.");
  }

  BackendServiceImpl() {
    conn_ = std::make_shared<mysqlpp::Connection>(false);
    conn_->set_option(new mysqlpp::ReconnectOption(true));
    conn_->connect(kNevaDatabaseName, kNevaDatabaseServer, kNevaDatabaseUser,
                   kNevaDatabasePassword);
    CHECK(conn_->connected()) << "Database connection failed.";

    mysqlpp::Query query = conn_->query("SET NAMES utf8;");
    query.execute();

    user_orm_ = std::unique_ptr<UserOrm>(new UserOrm(conn_));
    proposition_orm_ =
        std::unique_ptr<PropositionOrm>(new PropositionOrm(conn_));
    suggestion_orm_ = std::unique_ptr<SuggestionOrm>(new SuggestionOrm(conn_));
  }

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
  std::unique_ptr<UserOrm> user_orm_;
  std::unique_ptr<PropositionOrm> proposition_orm_;
  std::unique_ptr<SuggestionOrm> suggestion_orm_;
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
  gflags::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);
  neva::backend::RunServer();
  return 0;
}
