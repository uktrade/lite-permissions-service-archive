FROM java:8

ENV JAR_FILE lite-permissions-service-1.3.jar
ENV CONFIG_FILE /conf/permissions-service-config.yaml

WORKDIR /opt/lite-permissions-service

COPY build/libs/$JAR_FILE /opt/lite-permissions-service

CMD java "-jar" $JAR_FILE "server" $CONFIG_FILE
