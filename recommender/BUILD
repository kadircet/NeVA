package(default_visibility = ["//visibility:public"])

cc_library(
    name = "recommender",
    srcs = ["recommender.cc"],
    hdrs = ["recommender.h"],
    deps = [
        "//protos:suggestion_proto",
        "//protos:user_history_proto",
        "//util:hmac",
        "@glog//:glog",
    ],
)

cc_test(
    name = "recommender_test",
    srcs = ["recommender_test.cc"],
    deps = [
        ":recommender",
        "@com_google_protobuf//:protobuf",
        "@googletest//:gtest_main",
    ],
)
