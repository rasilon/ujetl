FROM ubuntu:22.04 as builder
RUN apt-get update && apt-get -y upgrade
RUN apt-get -y install openjdk-19-jdk-headless maven git
RUN git clone --single-branch --branch main https://github.com/rasilon/ujetl.git
RUN cd ujetl && mvn -e package

FROM openjdk:11 as runner
LABEL maintainer="Derry Hamilton <derryh@rasilon.net>"

RUN apt update && apt upgrade -y && apt install -y bash

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
