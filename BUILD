licenses(["notice"])

package(default_visibility = ["//visibility:public"])

cc_binary(
    name = "neva_backend",
    srcs = ["backend.cc"],
    copts = [
        "-I/usr/include/mysql++/",
        "-I/usr/include/mysql",
    ],
    linkopts = ["-lmysqlpp"],
    deps = [
        "//orm:proposition_orm",
        "//orm:suggestion_orm",
        "//orm:user_orm",
        "//protos:backend_proto",
        "//social_media:facebook_validator",
        "@com_github_gflags_gflags//:gflags",
        "@glog//:glog",
    ],
)
