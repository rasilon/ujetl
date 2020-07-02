FROM centos:centos8 as builder
RUN yum -y install epel-release java-11-openjdk-devel
RUN yum -y groupinstall 'Development Tools'
RUN yum -y install git maven
RUN alternatives --set javac /usr/lib/jvm/java-11-openjdk-11.0.7.10-1.el8_1.x86_64/bin/javac
RUN alternatives --set java /usr/lib/jvm/java-11-openjdk-11.0.7.10-1.el8_1.x86_64/bin/java
RUN git clone --single-branch --branch master https://github.com/rasilon/ujetl.git
RUN cd ujetl && JAVA_HOME=/usr/lib/jvm/java-11 mvn -e package




FROM openjdk:11 as runner
LABEL maintainer="Derry Hamilton <derryh@rasilon.net>"

RUN apt update && apt upgrade && apt install bash

RUN mkdir -p /usr/share/ujetl/lib/ /var/ujetl /etc/ujetl

COPY --from=builder  /ujetl/target/CopyingApp-2.*-jar-with-dependencies.jar /usr/share/ujetl/lib/CopyingApp.jar
COPY --from=builder  /ujetl/install_extra/copying_defaults_log4j.xml /etc/ujetl/
COPY ujetl_entrypoint /
CMD ["/ujetl_entrypoint"]


FROM runner as tester
COPY TEST_config_live.xml /var/ujetl/
COPY wait_for_postgres /
RUN apt-get  install -y postgresql-client


FROM runner as deploy
# Convice docker cloud to build the deploy image
