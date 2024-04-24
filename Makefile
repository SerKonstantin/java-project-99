.DEFAULT_GOAL := install

setup:
	./gradlew wrapper --gradle-version 8.5

clean:
	./gradlew clean

build:
	./gradlew clean build

install-dist:
	./gradlew clean installDist

install: setup install-dist

start:
	./build/install/java-project-99/bin/java-project-99

run:
	./gradlew run

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest

check-deps:
	./gradlew dependencyUpdates -Drevision=release

.PHONY: build
