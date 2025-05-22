# Demo gRPC Maven Plugin

This is a demo project showcasing how the `meta-maven-plugin-maven-plugin` can be used to succinctly encapsulate
complex build logic in a Maven plugin. The example plugin shows a multi-stage gRPC code generation configuration
that lints the proto files, generates java code using the `protoc` complier, including multiple `protoc` plugins, 
and then generates protobuf documentation.