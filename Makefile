MVN ?= mvn

.PHONY: run clean re

all: run

run: 
	$(MVN) -q compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
	java -XstartOnFirstThread -cp "target/classes:$$(cat target/classpath.txt)" com.humangl.Main

clean:
	$(MVN) clean -q

re:
	$(MVN) clean -q
	$(MVN) -q compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
	java -XstartOnFirstThread -cp "target/classes:$$(cat target/classpath.txt)" com.humangl.Main