# ===== build stage =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /src

# кеш зависимостей
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline

# исходники
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests package

# ===== runtime =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# безопасный пользователь
RUN addgroup --system app && adduser --system --ingroup app app

# каталоги, которые использует приложение
RUN mkdir -p /app/logs /app/attachments

# основной JAR и РЕСУРСЫ (из корня проекта)
COPY --from=build /src/target/*.jar app.jar
COPY resources /app/resources

# права пользователю приложения
RUN chown -R app:app /app

# опции JVM и путь вложений
ENV JAVA_OPTS=""
ENV APP_ATTACHMENTS_DIR=/app/attachments

EXPOSE 8080
VOLUME ["/app/attachments"]

USER app
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dfile.encoding=UTF-8 -Duser.timezone=UTC -Dapp.attachments.dir=$APP_ATTACHMENTS_DIR -jar app.jar"]
