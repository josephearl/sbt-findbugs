ACTIVATOR=/opt/typesafe/activator/activator
GIT_BRANCH=$(shell git rev-parse --symbolic-full-name --abbrev-ref HEAD)

build:
	$(ACTIVATOR) clean test scripted

publish:
ifeq ($(GIT_BRANCH), develop)
	$(ACTIVATOR) publish
else
	@echo "Not develop, on $(GIT_BRANCH) so nothing to publish"
endif

all: build publish

.PHONY: build publish all
