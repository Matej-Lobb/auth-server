FROM sgrio/java:jdk_11_centos
ARG APP_DIR=/opt/auth-server
RUN mkdir -p ${APP_DIR}
COPY target/auth-server*.jar ${APP_DIR}/auth-server.jar
EXPOSE 9090
WORKDIR ${APP_DIR}
CMD java $JAVA_OPTIONS -Dserver.port=9090 -jar auth-server.jar -debug
