FROM openjdk:8-alpine
RUN mkdir /app
ADD target/scala-2.12/skills-1.0.jar /app/
EXPOSE 8080
CMD java -jar /app/skills-1.0.jar
