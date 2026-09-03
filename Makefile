MVN ?= mvn

.PHONY: build run clean

build:
	$(MVN) -q package

run:
	$(MVN) -q compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
	java -XstartOnFirstThread -cp "target/classes:$$(cat target/classpath.txt)" com.humangl.Main

clean:
	$(MVN) clean
