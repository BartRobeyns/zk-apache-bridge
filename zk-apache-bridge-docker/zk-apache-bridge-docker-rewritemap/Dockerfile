FROM ecdevops.eu/httpd-java-docker:1.0.0-SNAPSHOT

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

RUN mkdir /etc/apache2/maps/
COPY src/main/docker/resources/apimap.txt /etc/apache2/maps/
COPY src/main/docker/resources/000-default.conf /etc/apache2/sites-available/

COPY src/main/docker/resources/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

CMD ["/entrypoint.sh"]
