FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jdk-jammy
# set the working directory inside the container
WORKDIR /app

# copy the built jar file into the container
COPY --from=build /app/target/*.jar app.jar

#Expose the default spring boot port
EXPOSE 8080

# RUn the application
ENTRYPOINT [ "java", "-jar", "app.jar" ]