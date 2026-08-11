FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Shanghai

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
