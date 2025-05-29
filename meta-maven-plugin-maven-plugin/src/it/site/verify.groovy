assert new File(basedir, "/target/site/initialize-mojo.html").text.contains("An example meta-maven plugin.")
assert new File(basedir, "/target/site/initialize-mojo.html").text.contains("Prints a message during the Initialize phase.")
assert new File(basedir, "/target/site/initialize-mojo.html").text.contains("It can be anything, but it is recommended to use a simple string.")
assert new File(basedir, "/target/site/initialize-mojo.html").text.contains("com.github.ekryd.echo-maven-plugin")