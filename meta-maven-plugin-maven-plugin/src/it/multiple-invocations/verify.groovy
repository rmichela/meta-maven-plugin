// Assert the plugin was generated correctly
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/InitializeMojo.java").exists()
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/HelloInitializeMojo.java").exists()
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_invocations_maven_plugin/GoodbyeInitializeMojo.java").exists()