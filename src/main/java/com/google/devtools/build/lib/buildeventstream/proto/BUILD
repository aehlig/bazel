package(default_visibility = ["//src:__subpackages__"])

filegroup(
    name = "srcs",
    srcs = glob(["**"]),
)

proto_library(
    name = "build_event_stream_proto",
    srcs = ["build_event_stream.proto"],
)

java_proto_library(
    name = "build_event_stream_java_proto",
    deps = [":build_event_stream_proto"],
)

filegroup(
    name = "dist_jars",
    srcs = [], # Fixme: src jar of the build_event_stream java proto library
)
