// Assert the plugin was generated correctly
assert new File(basedir, "/target/generated-sources/meta-maven-plugin/io/github/rmichela/test_case_multiple_embedded_invocations_maven_plugin/InitializeMojo.java").exists()

// Assert the plugin executed correctly
assert new File(basedir, "/build.log").text.contains("Embedded plugins must be unique but found duplicate declaration of plugin com.github.ekryd.echo-maven-plugin:echo-maven-plugin")
