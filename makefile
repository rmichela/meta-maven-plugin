.PHONY: clean package site archetype

clean:
	mvn clean

test:
	mvn verify

site:
	mvn clean site -pl meta-maven-plugin-maven-plugin
	open meta-maven-plugin-maven-plugin/target/site/index.html

site-publish:
	mvn clean site site:stage scm-publish:publish-scm -pl meta-maven-plugin-maven-plugin
	open https://rmichela.github.io/meta-maven-plugin/
