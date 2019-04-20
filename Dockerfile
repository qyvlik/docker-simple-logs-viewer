
FROM maven:alpine as builder
COPY pom.xml pom.xml
COPY src/ src/
VOLUME /var/maven/.m2
RUN mvn -DskipTests clean package

FROM frolvlad/alpine-java

MAINTAINER "qyvlik <qyvlik@qq.com>"

VOLUME /tmp

WORKDIR /home/www

COPY --from=builder target/*.jar app.jar

EXPOSE 19102

ENV JAVA_OPTS=""

ENTRYPOINT exec java $JAVA_OPTS -jar /home/www/app.jar

