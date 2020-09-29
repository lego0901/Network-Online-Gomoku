JAVA_COMPILE=javac
JAVA_RUN=java
#JAVA_VERSION=$(shell java -version | grep :

JAVA_SRC=$(wildcard src/*.java src/*/*.java)
JAVA_BIN=$(JAVA_SRC:src/%.java=bin/%.class)

MANIFEST=manifest.mf


all: compile server client

help:
	@echo make help: print this message
	@echo make compile: compile all .java files in src to bin
	@echo make server: make an executable server.jar file
	@echo make client: make an executable client.jar file
	@echo make all: compile and make executable server.jar and client.jar

compile: $(JAVA_BIN)

bin/%.class: src/%.java
	$(JAVA_COMPILE) -d ./bin $<

server: compile
	@echo "Manifest-Version: 1.0" > $(MANIFEST)
	@echo "Main-Class: server.Server" >> $(MANIFEST)
	@echo "" >> $(MANIFEST)
	jar cfm server.jar $(MANIFEST) -C bin/ .
	@rm $(MANIFEST)

client: compile
	@echo "Manifest-Version: 1.0" > $(MANIFEST)
	@echo "Main-Class: client.Client" >> $(MANIFEST)
	@echo "" >> $(MANIFEST)
	jar cfm client.jar $(MANIFEST) -C bin/ .
	@rm $(MANIFEST)

clean:
	rm -rf bin
	rm server.jar client.jar

