// Assert the plugin was generated correctly
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/meta_maven_plugin/test_case_multiple_invocations_maven_plugin/InitializeMojo.java").exists()
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/meta_maven_plugin/test_case_multiple_invocations_maven_plugin/HelloInitializeMojo.java").exists()
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/meta_maven_plugin/test_case_multiple_invocations_maven_plugin/GoodbyeInitializeMojo.java").exists()

// Assert the plugin executed correctly
assert new File(basedir, "/build.log").text.contains("Use multiple plugin <executions> instead.")