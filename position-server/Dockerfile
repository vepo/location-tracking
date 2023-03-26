FROM openjdk:20-jdk-slim
FROM maven:alpine
WORKDIR /app
COPY . /app
RUN mvn -v
RUN mvn clean install -DskipTests
EXPOSE 8080
LABEL maintainer=victor.perticarrari@gmail.com”
ADD ./target/position-server.jar /developments/
ENTRYPOINT ["java","-jar","/developments/position-server.jar"]