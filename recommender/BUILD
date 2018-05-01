package(default_visibility = ["//visibility:public"])

cc_library(
    name = "recommender",
    srcs = ["recommender.cc"],
    hdrs = ["recommender.h"],
    deps = [
        ":mysql_cache_fetcher",
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
        ":mysql_cache_fetcher",
        ":recommender",
        "//orm/testing:mock_orm_fixture",
        "@com_google_protobuf//:protobuf",
        "@googletest//:gtest_main",
    ],
)

cc_library(
    name = "mysql_cache_fetcher",
    srcs = ["mysql_cache_fetcher.cc"],
    hdrs = [
        "cache_fetcher.h",
        "mysql_cache_fetcher.h",
    ],
    deps = [
        "//orm:connectionpool",
        "//protos:suggestion_proto",
        "@glog//:glog",
    ],
)
