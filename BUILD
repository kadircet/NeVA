licenses(["notice"])

package(default_visibility = ["//visibility:public"])

cc_binary(
    name = "neva_backend",
    srcs = ["backend.cc"],
    deps = [
        "//orm:connectionpool",
        "//orm:proposition_orm",
        "//orm:suggestion_orm",
        "//orm:tag_orm",
        "//orm:user_history_orm",
        "//orm:user_orm",
        "//protos:backend_proto",
        "//social_media:facebook_validator",
        "//util:error",
        "//util:file",
        "//util:hmac",
        "@com_github_gflags_gflags//:gflags",
        "@glog//:glog",
    ],
)

cc_library(
    name = "json",
    srcs = ["json.hpp"],
    hdrs = ["json.hpp"],
    visibility = ["//visibility:public"],
)

genrule(
    name = "json_header",
    srcs = ["@json//file"],
    outs = ["json.hpp"],
    cmd = "cp $(locations @json//file) $@",
)
