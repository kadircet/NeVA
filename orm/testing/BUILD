package(default_visibility = ["//visibility:public"])

cc_library(
    name = "mock_orm_fixture",
    srcs = ["mock_orm_fixture.cc"],
    hdrs = ["mock_orm_fixture.h"],
    deps = [
        "//orm:connectionpool",
        "//social_media:facebook_validator",
        "@glog//:glog",
        "@googletest//:gtest_main",
    ],
)
