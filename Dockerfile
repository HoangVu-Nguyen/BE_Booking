FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8


FROM amazoncorretto:21-alpine

WORKDIR /app

RUN apk add --no-cache ffmpeg

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]