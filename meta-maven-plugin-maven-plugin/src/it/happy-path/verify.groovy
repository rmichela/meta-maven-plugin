// Assert the plugin was generated correctly
assert new File(basedir, "/plugin/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_happy_path_maven_plugin/CompileMojo.java").exists()

// Assert the plugin executed correctly
assert new File(basedir, "/build.log").text.contains("Bonjour Value")