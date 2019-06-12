FROM centos:centos7 as builder
RUN yum -y install epel-release java-1.8.0-openjdk-devel
RUN yum -y groupinstall 'Development Tools'
RUN yum -y install git maven
RUN git clone https://github.com/rasilon/ujetl.git
RUN cd ujetl && mvn package




#FROM centos:centos7
FROM openjdk:8-alpine
MAINTAINER Derry Hamilton <derryh@rasilon.net>

#RUN yum -y install epel-release
#RUN yum -y install java-1.8.0-openjdk-devel

RUN mkdir -p /usr/share/ujetl/lib/ /var/ujetl /etc/ujetl

COPY --from=builder  /ujetl/target/CopyingApp-2.*-jar-with-dependencies.jar /usr/share/ujetl/lib/CopyingApp.jar
COPY --from=builder  /ujetl/install_extra/copying_defaults_log4j.xml /etc/ujetl/
COPY docker/multistage/ujetl_entrypoint /
ENTRYPOINT ["/ujetl_entrypoint"]

