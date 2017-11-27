package(default_visibility = ["//visibility:public"])

load("@org_pubref_rules_protobuf//cpp:rules.bzl", "cc_proto_library")

cc_proto_library(
    name = "util_proto",
    protos = ["util.proto"],
)

cc_proto_library(
    name = "user_proto",
    proto_deps = [":util_proto"],
    protos = ["user.proto"],
)

cc_proto_library(
    name = "backend_proto",
    proto_deps = [
        ":user_proto",
        ":util_proto",
    ],
    protos = ["backend.proto"],
    with_grpc = True,
)

cc_proto_library(
    name = "suggestion_proto",
    protos = ["suggestion.proto"],
)
