FROM openjdk:8-jdk
MAINTAINER <n3integration@gmail.com>

EXPOSE 4567

VOLUME /data

COPY build/libs/*.jar /app/service.jar

CMD ["java", "-jar", "/app/service.jar"]