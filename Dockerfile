# XNAT runtime image
# Built by .github/workflows/build-publish.yml.
# Expects the WAR at ./docker-context/xnat.war in the build context.
FROM tomcat:9-jdk21-temurin

# Remove the default ROOT app so the XNAT WAR can take its place.
RUN rm -rf /usr/local/tomcat/webapps/ROOT \
           /usr/local/tomcat/webapps/ROOT.war

COPY docker-context/xnat.war /usr/local/tomcat/webapps/ROOT.war

# XNAT writes archive/prearchive/cache/logs/plugins under XNAT_HOME.
# Mount a persistent volume at runtime: -v /host/xnathome:/data/xnat
ENV XNAT_HOME=/data/xnat
VOLUME ["/data/xnat"]

EXPOSE 8080

CMD ["catalina.sh", "run"]
