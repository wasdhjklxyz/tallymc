JAR   = $(wildcard build/libs/TallyMC-*.jar)
PAPER ?= $(PAPER_JAR)
RUN   := run
PORT  ?= 12345
MEM   ?= 2G

.PHONY: build lock clean version server-init run deploy

build:
	./gradlew build

lock:
	./gradlew dependencies --write-locks

clean:
	./gradlew clean

version:
	@./gradlew -q properties | grep '^version:' | awk '{print $$2}'

server-init:
	mkdir -p $(RUN)/plugins
	echo "eula=true" > $(RUN)/eula.txt
	printf 'server-port=$(PORT)\nonline-mode=false\nmotd=TallyMC dev\nmax-players=5\nview-distance=8\nspawn-protection=0\n' \
		> $(RUN)/server.properties

run: build server-init
	cp $(JAR) $(RUN)/plugins/
	cd $(RUN) && java -Xmx$(MEM) -jar $(PAPER) --nogui

deploy: build
	cp $(JAR) $(RUN)/plugins/
	@echo "jar copied - run /reload confirm or restart the server"
