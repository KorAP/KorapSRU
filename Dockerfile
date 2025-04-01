# Requires Java 8 and Tomcat 9
FROM eclipse-temurin:8-jdk-alpine AS builder

# Copy repository respecting .dockerignore
COPY . /korap-sru

WORKDIR /korap-sru

RUN apk update && \
    apk add --no-cache git \
            curl \
            wget \
            maven && \
    mkdir /tomcat

RUN addgroup -S korap && \
    adduser -S korap-sru -G korap && \
    chown -R korap-sru.korap /korap-sru && \
    chown -R korap-sru.korap /tomcat

USER korap-sru

# Install tomcat
RUN wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.102/bin/apache-tomcat-9.0.102.tar.gz && \
    tar xvzf apache-tomcat-9.0.102.tar.gz \
        --strip-components 1 \
        --directory /tomcat && \
    rm apache-tomcat-9.0.102.tar.gz

RUN git config --global user.email "korap+docker@ids-mannheim.de" && \
    git config --global user.name "Docker"

# Install KorAP-SRU
RUN mvn install

RUN rm -r /tomcat/webapps/*

RUN find target/KorapSRU-*.war -exec unzip {} -d /tomcat/webapps/ROOT ';'

EXPOSE 8080

ENTRYPOINT [ "/tomcat/bin/catalina.sh" ]

CMD [ "run" ]

LABEL description="Docker Image for KorAP-SRU, the Federated Content Search frontend for KorAP"
LABEL maintainer="korap@ids-mannheim.de"
LABEL repository="https://github.com/KorAP/KorapSRU"

# docker build -f Dockerfile -t korap/korap-sru:{nr} .
# docker run --network host --name korap-sru --rm \
#            -v ${PWD}/WEB-INF/endpoint-description.xml:/tomcat/webapps/ROOT/WEB-INF/endpoint-description.xml \
#            -v ${PWD}/WEB-INF/sru-server-config.xml:/tomcat/webapps/ROOT/WEB-INF/sru-server-config.xml \
#            -v ${PWD}/WEB-INF/web.xml:/tomcat/webapps/ROOT/WEB-INF/web.xml \
#        korap/korap-sru:{nr}
