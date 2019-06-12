# General build RPM environment for CentOS 6.x
#
# VERSION       0.0.1

FROM centos:centos7
MAINTAINER Derry Hamilton <derryh@rasilon.net>

# Install up-to-date epel rpm repository
RUN yum -y install epel-release

# Install java first, to get a sensible one.
RUN yum -y install java-1.8.0-openjdk-devel

COPY uJETL-2.*.x86_64.rpm /tmp/
COPY ujetl_entrypoint /
RUN rpm -i /tmp/uJETL-2.*.x86_64.rpm
ENTRYPOINT ["/ujetl_entrypoint"]

