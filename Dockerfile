FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /opt/jar
COPY wsmonitor.jar /opt/jar

CMD ["java", "-jar", "/opt/jar/wsmonitor.jar"]