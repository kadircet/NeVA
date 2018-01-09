package(default_visibility = ["//visibility:public"])

cc_library(
    name = "facebook_validator",
    srcs = [
        "facebook.cc",
    ],
    hdrs = [
        "facebook.h",
    ],
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

cc_library(
    name = "json",
    srcs = ["json.hpp"],
    hdrs = ["json.hpp"],
    includes = ["."],
    visibility = ["//visibility:public"],
)

genrule(
    name = "json_header",
    srcs = ["@json//file"],
    outs = ["json.hpp"],
    cmd = "cp $(locations @json//file) $@",
)
