package(default_visibility = ["//visibility:public"])

cc_library(
    name = "hmac",
    srcs = ["hmac.cc"],
    hdrs = ["hmac.h"],
    copts = [
        "-I/usr/include/openssl/",
    ],
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
