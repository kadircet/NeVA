package(default_visibility = ["//visibility:public"])

cc_library(
    name = "user_orm",
    srcs = ["user_orm.cc"],
    hdrs = ["user_orm.h"],
    copts = [
        "-I/usr/include/mysql++/",
        "-I/usr/include/mysql",
    ],
    linkopts = ["-lmysqlpp"],
    deps = [
        "//protos:user_proto",
        "//util:hmac",
        "//util:time",
        "@glog//:glog",
    ],
)

cc_library(
    name = "proposition_orm",
    srcs = ["proposition_orm.cc"],
    hdrs = ["proposition_orm.h"],
    copts = [
        "-I/usr/include/mysql++/",
        "-I/usr/include/mysql",
    ],
    linkopts = ["-lmysqlpp"],
    deps = [
        "//protos:suggestion_proto",
        "@glog//:glog",
    ],
)

cc_library(
    name = "suggestion_orm",
    srcs = ["suggestion_orm.cc"],
    hdrs = ["suggestion_orm.h"],
    copts = [
        "-I/usr/include/mysql++/",
        "-I/usr/include/mysql",
    ],
    linkopts = ["-lmysqlpp"],
    deps = [
        "//protos:suggestion_proto",
        "@glog//:glog",
    ],
)

cc_library(
    name = "user_history_orm",
    srcs = ["user_history_orm.cc"],
    hdrs = ["user_history_orm.h"],
    copts = [
        "-I/usr/include/mysql++/",
        "-I/usr/include/mysql",
    ],
    linkopts = ["-lmysqlpp"],
    deps = [
        "//protos:user_history_proto",
        "//protos:user_proto",
        "@glog//:glog",
    ],
)
