FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk maven && \
    apt-get clean

WORKDIR /app

COPY src ./src
COPY pom.xml .
COPY test1.go test2.go test3.go ./

RUN mvn clean compile

# Запуск
CMD ["java", "-cp", "target/classes", "com.golexer.Main"]