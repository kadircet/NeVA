package(default_visibility = ["//visibility:public"])

cc_library(
    name = "user_orm",
    srcs = ["user_orm.cc"],
    hdrs = ["user_orm.h"],
    deps = [
        ":connectionpool",
        "//protos:backend_proto",
        "//protos:user_proto",
        "//social_media:facebook_validator",
        "//util:error",
        "//util:hmac",
        "//util:time",
        "@glog//:glog",
    ],
)

cc_library(
    name = "proposition_orm",
    srcs = ["proposition_orm.cc"],
    hdrs = ["proposition_orm.h"],
    deps = [
        ":connectionpool",
        "//protos:suggestion_proto",
        "@glog//:glog",
    ],
)

cc_library(
    name = "suggestion_orm",
    srcs = ["suggestion_orm.cc"],
    hdrs = ["suggestion_orm.h"],
    deps = [
        ":connectionpool",
        "//protos:suggestion_proto",
        "//protos:user_history_proto",
        "//recommender",
        "@glog//:glog",
    ],
)

cc_library(
    name = "user_history_orm",
    srcs = ["user_history_orm.cc"],
    hdrs = ["user_history_orm.h"],
    deps = [
        ":connectionpool",
        "//protos:user_history_proto",
        "//protos:user_proto",
        "@glog//:glog",
    ],
)

cc_library(
    name = "tag_orm",
    srcs = ["tag_orm.cc"],
    hdrs = ["tag_orm.h"],
    deps = [
        ":connectionpool",
        "//protos:suggestion_proto",
        "@glog//:glog",
    ],
)

cc_library(
    name = "connectionpool",
    srcs = ["connectionpool.cc"],
    hdrs = ["connectionpool.h"],
    deps = [
        "@glog//:glog",
    ],
)

cc_test(
    name = "proposition_orm_test",
    srcs = ["proposition_orm_test.cc"],
    data = [
        ":proposition_orm_test.sql",
        "//tables:suggestions.sql",
        "//tables:user.sql",
        "//tables:user_based_suggestion.sql",
    ],
    deps = [
        ":proposition_orm",
        "//orm/testing:mock_orm_fixture",
        "//protos:suggestion_proto",
    ],
)

exports_files(glob(["*_test.sql"]))
