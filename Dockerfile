
FROM frolvlad/alpine-java

MAINTAINER "qyvlik <qyvlik@qq.com>"

VOLUME /tmp

WORKDIR /home/www

ADD target/*.jar /home/www/app.jar

RUN adduser -D -u 1000 www www \
    && chown www:www -R /home/www

EXPOSE 19102

USER www

ENV JAVA_OPTS=""

ENTRYPOINT exec java $JAVA_OPTS -jar /home/www/app.jar

