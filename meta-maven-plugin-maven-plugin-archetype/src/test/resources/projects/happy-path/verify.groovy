// Assert the archetype executed correctly
assert new File(basedir, "/project/hello-world-meta-maven-plugin/pom.xml")
        .text.contains("<goalPrefix>helloworld</goalPrefix>")
assert new File(basedir, "/project/hello-world-meta-maven-plugin/src/site/markdown/index.md.vm")
        .text.contains("# hello-world-meta-maven-plugin")
