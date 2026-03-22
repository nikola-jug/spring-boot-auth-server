FROM eclipse-temurin:21-jre
WORKDIR /app
COPY spring-boot-auth-server/target/*.jar app.jar
COPY certs/rootCA.pem /tmp/rootCA.pem
RUN keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt \
    -alias mkcert-root -file /tmp/rootCA.pem && rm /tmp/rootCA.pem
ENTRYPOINT ["java", "-jar", "app.jar"]
