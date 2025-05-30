# The meta-maven-plugin-maven-plugin

**Have you ever wished Maven had a BOM for plugins? Well now it does!** The `meta-maven-plugin-maven-plugin` is a Maven
plugin for generating Maven meta-plugins.

_A what for what?_ Maven meta-plugins are Maven plugins that execute other maven plugins with a pre-configured plugin
configuration. Meta-plugins allow for sharing plugin configuration across multiple projects without the need for a
shared parent POM. Additionally, meta-plugins allow for the execution of multiple plugins with a single plugin import,
something not even a shared parent POM can do. If you find yourself copy/pasting the same hundred lines of plugin
configuration XML between POMs, you would benefit from using a meta-plugin.

Authoring Maven meta-plugins has traditionally been a manual affair. You had to implement a Maven mojo class and
explicitly invoke one or more maven plugins with code. The `meta-maven-plugin-maven-plugin` automates this process
by generating that code for you instead using the native Maven plugin configuration XML already present in your POMs.

## Documentation

:books: For examples and documentation, see the [plugin Maven site](https://rmichela.github.io/meta-maven-plugin/index.html)

## Releasing

1. Update pom.xml versions with a non-snapshot version.
2. Push changes to GitHub.
3. Create a new release on GitHub with the version number and tag.
4. When the release is created, GitHub Actions will automatically build and deploy the plugin to [Maven Central](https://central.sonatype.com/).
5. Update the version in pom.xml to the next snapshot version.
