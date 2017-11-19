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
        "//orm:user_orm",
        "//protos:backend_proto",
        "@glog//:glog",
    ],
)
