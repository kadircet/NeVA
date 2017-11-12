http_archive(
    name = "com_github_gflags_gflags",
    strip_prefix = "gflags-2.2.1",
    url = "https://github.com/gflags/gflags/archive/v2.2.1.tar.gz",
)

new_http_archive(
    name = "glog",
    build_file = "glog.BUILD",
    strip_prefix = "glog-0.3.5",
    url = "https://github.com/google/glog/archive/v0.3.5.tar.gz",
)

new_http_archive(
    name = "googletest",
    build_file = "gunit.BUILD",
    strip_prefix = "googletest-release-1.8.0",
    url = "https://github.com/google/googletest/archive/release-1.8.0.tar.gz",
)

http_archive(
    name = "org_pubref_rules_protobuf",
    strip_prefix = "rules_protobuf-0.8.1",
    url = "https://github.com/pubref/rules_protobuf/archive/v0.8.1.tar.gz",
)

load("@org_pubref_rules_protobuf//cpp:rules.bzl", "cpp_proto_repositories")

cpp_proto_repositories()
