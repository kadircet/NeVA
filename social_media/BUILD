package(default_visibility = ["//visibility:public"])

cc_library(
    name = "facebook_validator",
    srcs = ["facebook.cc"],
    hdrs = ["facebook.h"],
    deps = [
        "//:json",
        "//protos:user_proto",
        "@cpr//:cpr",
        "@glog//:glog",
    ],
)

cc_test(
    name = "facebook_validator_test",
    srcs = ["facebook_test.cc"],
    deps = [
        ":facebook_validator",
        "@googletest//:gtest_main",
    ],
)
