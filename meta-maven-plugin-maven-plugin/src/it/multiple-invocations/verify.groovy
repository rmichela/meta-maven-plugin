// Assert the plugin was generated correctly
assert new File(basedir, "/plugin/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/InitializeMojo.java").exists()
assert new File(basedir, "/plugin/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/HelloInitializeMojo.java").exists()
assert new File(basedir, "/plugin/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/HelloAbstractMetaPluginMojo.java")
        .text.contains("Hello World")
assert new File(basedir, "/plugin/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/GoodbyeInitializeMojo.java").exists()
assert new File(basedir, "/plugin/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/GoodbyeAbstractMetaPluginMojo.java")
        .text.contains("Goodbye World")

// Assert the plugin executed correctly
assert new File(basedir, "/build.log").text.contains("Hello DEFAULT World")
assert new File(basedir, "/build.log").text.contains("Hello World")
assert new File(basedir, "/build.log").text.contains("Goodbye World")