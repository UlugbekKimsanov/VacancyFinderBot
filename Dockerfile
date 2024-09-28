FROM openjdk:17
EXPOSE 8080
ARG JAR_FILE=target/Vacancy_Finder-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} Vacancy_Finder
ENTRYPOINT ["java","-jar", "Vacancy_Finder"]