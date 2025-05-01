.PHONY: clean package site archetype

clean:
	mvn clean

package:
	mvn package

site:
	mvn site -pl meta-maven-plugin-maven-plugin
	open meta-maven-plugin-maven-plugin/target/site/index.html

archetype:
	mvn install -pl meta-maven-plugin-maven-plugin-archetype
	mvn archetype:generate -DoutputDirectory=tmp -DarchetypeGroupId=meta-maven-plugin  -DarchetypeArtifactId=meta-maven-plugin-maven-plugin-archetype  -DarchetypeVersion=1.0-SNAPSHOT
