# L'image de base reelle est imposee par le BuildConfig (dockerStrategy.from -> openshift/ubi8-openjdk-21-runtime).
# Java 21 obligatoire : java.version=21 dans craft-parent.
FROM ubi8-openjdk-21-runtime

COPY app.jar /deployments/app.jar

EXPOSE 8080
USER 185

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/deployments/app.jar"]
