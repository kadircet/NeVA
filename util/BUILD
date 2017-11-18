cc_library(
    name = "hmac",
    srcs = ["hmac.cc"],
    hdrs = ["hmac.h"],
    copts = [
        "-I/usr/include/openssl/hmac.h",
        "-lcrypto",
    ],
    deps = ["@glog//:glog"],
)
