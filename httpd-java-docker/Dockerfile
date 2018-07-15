FROM openjdk:8
RUN apt-get update && apt-get install -y apache2 less vim jq curl zip
RUN a2enmod rewrite proxy_connect proxy proxy_http proxy_http2
CMD ["/usr/sbin/apachectl","-DFOREGROUND"]