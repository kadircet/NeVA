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
        "@glog//:glog",
    ],
)
