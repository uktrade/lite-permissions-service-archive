FROM openjdk:8-jre

ARG NEXUS_BASE_URL=http://nexus.mgmt.licensing.service.trade.gov.uk.test/repository
ARG NEXUS_REPO=lite-builds
ARG BUILD_VERSION

ENV PROJECT_NAME permissions-service
ENV JAR_FILE $PROJECT_NAME-$BUILD_VERSION.jar
ENV CONFIG_FILE /conf/$PROJECT_NAME-config.yaml
ENV SOAP_CONNECT_TIMEOUT 20000
ENV SOAP_READ_TIMEOUT 60000

LABEL uk.gov.bis.lite.version=$BUILD_VERSION

WORKDIR /opt/$PROJECT_NAME

ADD $NEXUS_BASE_URL/$NEXUS_REPO/uk/gov/bis/lite/$PROJECT_NAME/$BUILD_VERSION/$JAR_FILE $JAR_FILE
RUN chmod 0644 $JAR_FILE

EXPOSE 8080

CMD java \
-Djava.security.egd=file:/dev/./urandom \
-Dsun.rmi.transport.proxy.connectTimeout=$SOAP_CONNECT_TIMEOUT \
-Dsun.net.client.defaultConnectTimeout=$SOAP_CONNECT_TIMEOUT \
-Dsun.net.client.defaultReadTimeout=$SOAP_READ_TIMEOUT \
-jar $JAR_FILE server $CONFIG_FILE
