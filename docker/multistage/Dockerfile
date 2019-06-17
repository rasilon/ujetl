FROM centos:centos7 as builder
RUN yum -y install epel-release java-1.8.0-openjdk-devel
RUN yum -y groupinstall 'Development Tools'
RUN yum -y install git maven
RUN git clone https://github.com/rasilon/ujetl.git
RUN cd ujetl && mvn package




FROM openjdk:8-alpine as runner
LABEL maintainer="Derry Hamilton <derryh@rasilon.net>"

RUN apk update && apk upgrade && apk add bash

RUN mkdir -p /usr/share/ujetl/lib/ /var/ujetl /etc/ujetl

COPY --from=builder  /ujetl/target/CopyingApp-2.*-jar-with-dependencies.jar /usr/share/ujetl/lib/CopyingApp.jar
COPY --from=builder  /ujetl/install_extra/copying_defaults_log4j.xml /etc/ujetl/
COPY ujetl_entrypoint /
CMD ["/ujetl_entrypoint"]


FROM runner as tester
COPY TEST_config_live.xml /var/ujetl/
COPY wait_for_postgres /
RUN apk add postgresql-client


FROM runner as deploy
# Convice docker cloud to build the deploy image
