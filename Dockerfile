FROM openjdk:8-alpine
ADD ["build/libs/infrastructure-as-a-code2-0.0.1-SNAPSHOT.jar", "app.jar"]
EXPOSE 8080 8080
ARG spring_profiles=docker
ENV active_profiles=${spring_profiles}
RUN sh -c 'touch /app.jar'
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$active_profiles -jar /app.jar" ]