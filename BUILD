licenses(["notice"])

package(default_visibility = ["//visibility:public"])

cc_binary(
    name = "neva_backend",
    srcs = ["backend.cc"],
    deps = [
        "//orm:proposition_orm",
        "//orm:suggestion_orm",
        "//orm:tag_orm",
        "//orm:user_history_orm",
        "//orm:user_orm",
        "//protos:backend_proto",
        "//social_media:facebook_validator",
        "//util:error",
        "//util:file",
        "@com_github_gflags_gflags//:gflags",
        "@glog//:glog",
    ],
)
