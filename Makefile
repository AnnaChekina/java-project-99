setup:
	gradle wrapper --gradle-version 8.7

clean:
	./gradlew clean

build:
	./gradlew clean build

run:
	./gradlew bootRun

install:
	./gradlew instinstallDist

run-dist:
	./build/install/java-project-99/bin/java-project-99

generate-migrations:
	./gradlew generateMigrations

checkstyle:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

.PHONY: build