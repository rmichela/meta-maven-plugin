.PHONY: clean package site archetype

clean:
	mvn clean

package:
	mvn package

site:
	mvn site -pl meta-maven-plugin-maven-plugin
	open meta-maven-plugin-maven-plugin/target/site/index.html

site-publish:
	mvn site site:stage scm-publish:publish-scm -pl meta-maven-plugin-maven-plugin
