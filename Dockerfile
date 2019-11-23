FROM openjdk:8-jdk-alpine
RUN apk add --no-cache bash
VOLUME /tmp
RUN mkdir -p ~/specifications
ARG API_SPECIFICATION
COPY ${API_SPECIFICATION} /root/specifications
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]