FROM eclipse-temurin:22-jdk-alpine AS builder

# Copy repository respecting .dockerignore
COPY . /korap-sru

WORKDIR /korap-sru

RUN apk update && \
    apk add --no-cache git \
            curl \
            wget \
            maven

# Install tomcat
RUN wget https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.5/bin/apache-tomcat-11.0.5.tar.gz && \
    mkdir /opt/tomcat && \
    tar xvzf apache-tomcat-11.0.5.tar.gz \
        --strip-components 1 \
        --directory /opt/tomcat && \
    rm apache-tomcat-11.0.5.tar.gz

RUN git config --global user.email "korap+docker@ids-mannheim.de" && \
    git config --global user.name "Docker"

# Install KorAP-SRU
RUN mvn install

RUN rm -r /opt/tomcat/webapps/*

# RUN find target/KorapSRU-*.war -exec mv {} /opt/tomcat/webapps/ROOT.war ';'

RUN find target/KorapSRU-*.war -exec unzip {} -d /opt/tomcat/webapps/ROOT ';'

RUN addgroup -S korap && \
    adduser -S korap-sru -G korap && \
#    mkdir korap-sru && \
    chown -R korap-sru.korap /korap-sru && \
    chown -R korap-sru.korap /opt/tomcat

USER korap-sru

EXPOSE 8080

ENTRYPOINT [ "/opt/tomcat/bin/catalina.sh" ]

CMD [ "run" ]

LABEL description="Docker Image for KorAP-SRU, the Federated Content Search frontend fopr KorAP"
LABEL maintainer="korap@ids-mannheim.de"
LABEL repository="https://github.com/KorAP/KorapSRU"

# docker build -f Dockerfile -t korap/korap-sru:{nr} .