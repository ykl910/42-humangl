MVN ?= mvn

.PHONY: build run clean

build:
	$(MVN) -q package

run:
	$(MVN) -q compile exec:java

clean:
	$(MVN) clean
