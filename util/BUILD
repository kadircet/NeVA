package(default_visibility = ["//visibility:public"])

cc_library(
    name = "hmac",
    srcs = ["hmac.cc"],
    hdrs = ["hmac.h"],
    linkopts = ["-lcrypto"],
    deps = ["@glog//:glog"],
)

cc_test(
    name = "hmac_test",
    srcs = ["hmac_test.cc"],
    deps = [
        ":hmac",
        "@googletest//:gtest_main",
    ],
)

cc_library(
    name = "time",
    srcs = ["time.cc"],
    hdrs = ["time.h"],
)

cc_library(
    name = "error",
    hdrs = ["error.h"],
)

cc_library(
    name = "file",
    srcs = ["file.cc"],
    hdrs = ["file.h"],
)

sh_binary(
    name = "kcov",
    srcs = ["kcov.sh"],
)
