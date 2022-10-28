FROM amazoncorretto:11k
ENV TZ=Europe/Moscow
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]