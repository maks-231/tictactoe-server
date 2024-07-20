FROM adoptopenjdk/openjdk11:alpine

VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} /tmp
WORKDIR /tmp

EXPOSE 5000
EXPOSE 5001

CMD ["java","-jar","/tmp/tictactoe-server-1.0-SNAPSHOT.jar"]