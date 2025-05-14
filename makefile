.PHONY: clean package site archetype help

help:		# Print this help message
	@echo "Available targets:"
	@awk -F':|#' '/^[a-zA-Z0-9_-]+:/{printf "  %-15s %s\n", $$1, ($$3 ? $$3 : "")}' $(MAKEFILE_LIST)

clean:      # Remove build artifacts
	mvn clean

test:       # Run tests
	mvn verify

site:       # Build and open project site
	mvn clean site -pl meta-maven-plugin-maven-plugin
	open meta-maven-plugin-maven-plugin/target/site/index.html

site-publish: # Publish project site to GitHub Pages
	mvn clean site site:stage scm-publish:publish-scm -pl meta-maven-plugin-maven-plugin
	open https://rmichela.github.io/meta-maven-plugin/

