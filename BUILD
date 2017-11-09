licenses(["notice"])

package(default_visibility = ["//visibility:public"])

cc_binary(
    name = "hello-world-run",
    srcs = ["hello-world-run.cc"],
    deps = [":hello-world"],
)

cc_library(
    name = "hello-world",
    srcs = ["hello-world.cc"],
    hdrs = ["hello-world.h"],
)

cc_test(
    name = "hello-world-test",
    srcs = ["hello-world-test.cc"],
    deps = [
        ":hello-world",
        "@googletest//:gtest_main",
    ],
)

cc_binary(
    name = "neva_backend",
    srcs = ["backend.cc"],
    deps = ["//protos:backend_proto"],
)
