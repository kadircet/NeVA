#include <iostream>
#include <memory>
#include <string>

#include <grpc++/grpc++.h>
#include <mysql++.h>

#include "gflags/gflags.h"
#include "glog/logging.h"
#include "orm/proposition_orm.h"
#include "orm/suggestion_orm.h"
#include "orm/tag_orm.h"
#include "orm/user_history_orm.h"
#include "orm/user_orm.h"
#include "protos/backend.grpc.pb.h"
#include "social_media/facebook.h"
#include "util/error.h"
#include "util/file.h"
#include "util/hmac.h"

namespace neva {
namespace backend {
namespace {

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;
using orm::PropositionOrm;
using orm::SuggestionOrm;
using orm::TagOrm;
using orm::UserHistoryOrm;
using orm::UserOrm;

DEFINE_string(database_name, "", "Name of the database to connect to.");
DEFINE_string(database_server, "", "Domain of the database server.");
DEFINE_string(database_user, "", "Username to authenticate to database.");
DEFINE_string(database_password, "",
              "Password for the authentication of the user.");
DEFINE_string(server_uri, "",
              "URI for server to listen on, example: 0.0.0.0:50051");
DEFINE_string(ssl_key_path, "", "Private key file path for ssl certificate.");
DEFINE_string(ssl_cert_path, "", "File path for ssl certificate.");

class BackendServiceImpl final : public Backend::Service {
 public:
  Status Register(ServerContext* context, const RegisterRequest* request,
                  GenericReply* reply) override {
    VLOG(1) << "Received Register Request:" << request->user().email();
    std::string verification_token;
    // TODO(kadircet): Implement input sanity checking.

    const User user = request->user();
    const Status status =
        user_orm_->InsertUser(user, LoginRequest::DEFAULT, &verification_token);
    // TODO(kadircet): Implement sending of verification_token with email.
    return status;
  }

  Status Login(ServerContext* context, const LoginRequest* request,
               LoginReply* reply) override {
    const std::string email = request->email();
    const std::string password = request->password();
    VLOG(1) << "Received Login Request for: " << email;
    return user_orm_->CheckCredentials(email, password,
                                       request->authentication_type(),
                                       reply->mutable_token());
  }

  Status UpdateUser(ServerContext* context, const UpdateUserRequest* request,
                    GenericReply* reply) override {
    VLOG(1) << "Received UpdateUser:\n" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    const User user = request->user();
    return user_orm_->UpdateUserData(user_id, user);
  }

  Status GetUser(ServerContext* context, const GetUserRequest* request,
                 GetUserReply* reply) override {
    VLOG(1) << "Received GetUser:\n" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return user_orm_->GetUserData(user_id, reply->mutable_user());
  }

  Status SuggestionItemProposition(
      ServerContext* context, const SuggestionItemPropositionRequest* request,
      GenericReply* reply) override {
    VLOG(1) << "Received SuggestionItemProposition:\n"
            << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return proposition_orm_->InsertProposition(user_id, request->suggestion());
  }

  Status GetSuggestion(ServerContext* context,
                       const GetSuggestionRequest* request,
                       GetSuggestionReply* reply) override {
    VLOG(1) << "Received GetSuggestion for category: "
            << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    UserHistory user_history;
    user_history_orm_->FetchUserHistory(user_id, 0, &user_history);
    SuggestionList suggestion;
    RETURN_IF_ERROR(suggestion_orm_->GetSuggestion(
        user_history, request->suggestion_category(), reply->mutable_suggestion()));
    return Status::OK;
  }

  Status TagProposition(ServerContext* context,
                        const TagPropositionRequest* request,
                        GenericReply* reply) override {
    VLOG(1) << "Received TagProposition:" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return proposition_orm_->InsertProposition(user_id, request->tag());
  }

  Status TagValueProposition(ServerContext* context,
                             const TagValuePropositionRequest* request,
                             GenericReply* reply) override {
    VLOG(1) << "Received TagValueProposition:" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return proposition_orm_->InsertProposition(
        user_id, request->tag_id(), request->suggestee_id(), request->value());
  }

  Status GetSuggestionItemList(ServerContext* context,
                               const GetSuggestionItemListRequest* request,
                               GetSuggestionItemListReply* reply) override {
    VLOG(1) << "Received GetSuggestionItemList:" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    uint32_t last_updated;
    suggestion_orm_->GetSuggestees(request->suggestion_category(),
                                   request->start_index(),
                                   reply->mutable_items(), &last_updated);
    reply->set_last_updated(last_updated);
    return Status::OK;
  }

  Status InformUserChoice(ServerContext* context,
                          const InformUserChoiceRequest* request,
                          InformUserChoiceReply* reply) override {
    VLOG(1) << "Received InformUserChoice:" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    int choice_id;
    RETURN_IF_ERROR(user_history_orm_->InsertChoice(user_id, request->choice(),
                                                    &choice_id));
    reply->set_choice_id(choice_id);

    return Status::OK;
  }

  Status FetchUserHistory(ServerContext* context,
                          const FetchUserHistoryRequest* request,
                          FetchUserHistoryReply* reply) override {
    VLOG(1) << "Received FetchUserHistory:" << request->DebugString();
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return user_history_orm_->FetchUserHistory(user_id, request->start_index(),
                                               reply->mutable_user_history());
  }

  Status CheckToken(ServerContext* context, const CheckTokenRequest* request,
                    GenericReply* reply) override {
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return Status::OK;
  }

  Status RecordFeedback(ServerContext* context,
                        const RecordFeedbackRequest* request,
                        GenericReply* reply) override {
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return user_history_orm_->RecordFeedback(user_id, request->user_feedback());
  }

  Status GetTags(ServerContext* context, const GetTagsRequest* request,
                 GetTagsReply* reply) override {
    int user_id;
    RETURN_IF_ERROR(user_orm_->CheckToken(request->token(), &user_id));
    return tag_orm_->GetTags(request->start_index(), reply->mutable_tag_list());
  }

  BackendServiceImpl() {
    conn_ = std::make_shared<mysqlpp::Connection>(false);
    conn_->set_option(new mysqlpp::ReconnectOption(true));
    conn_->connect(FLAGS_database_name.c_str(), FLAGS_database_server.c_str(),
                   FLAGS_database_user.c_str(),
                   FLAGS_database_password.c_str());
    CHECK(conn_->connected())
        << "Database connection failed." << conn_->error();

    mysqlpp::Query query = conn_->query("SET NAMES utf8;");
    query.execute();

    user_orm_ = std::unique_ptr<UserOrm>(new UserOrm(conn_));
    proposition_orm_ =
        std::unique_ptr<PropositionOrm>(new PropositionOrm(conn_));
    suggestion_orm_ = std::unique_ptr<SuggestionOrm>(new SuggestionOrm(conn_));
    user_history_orm_ =
        std::unique_ptr<UserHistoryOrm>(new UserHistoryOrm(conn_));
    tag_orm_ = std::unique_ptr<TagOrm>(new TagOrm(conn_));
  }

 private:
  std::shared_ptr<mysqlpp::Connection> conn_;
  std::unique_ptr<UserOrm> user_orm_;
  std::unique_ptr<PropositionOrm> proposition_orm_;
  std::unique_ptr<SuggestionOrm> suggestion_orm_;
  std::unique_ptr<UserHistoryOrm> user_history_orm_;
  std::unique_ptr<TagOrm> tag_orm_;
};

void RunServer() {
  util::InitializeRandom();

  std::string key;
  std::string cert;

  util::ReadFile(FLAGS_ssl_cert_path, &cert);
  util::ReadFile(FLAGS_ssl_key_path, &key);

  grpc::SslServerCredentialsOptions::PemKeyCertPair keycert = {key, cert};
  grpc::SslServerCredentialsOptions ssl_options;
  ssl_options.pem_key_cert_pairs.push_back(keycert);

  BackendServiceImpl service;

  ServerBuilder builder;
  builder.AddListeningPort(FLAGS_server_uri,
                           grpc::SslServerCredentials(ssl_options));
  builder.RegisterService(&service);

  std::unique_ptr<Server> server(builder.BuildAndStart());
  LOG(INFO) << "Server started on " << FLAGS_server_uri;

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
