# General build RPM environment for CentOS 6.x
#
# VERSION       0.0.1

FROM centos:centos7
MAINTAINER Derry Hamilton <derryh@postcodelottery.co.uk>

# Install up-to-date epel rpm repository
RUN yum -y install epel-release

# Install java first, to get a sensible one.
RUN yum -y install java-1.8.0-openjdk-devel

# Install various packages to get compile environment
RUN yum -y groupinstall 'Development Tools'

# Install git command to access GitHub repository
RUN yum -y install git

# Install rpm-build to use rpmrebuild command
RUN yum -y install rpm-build

# Install yum-utils to use yumdownloader command
RUN yum -y install yum-utils

# Install rpmdevtools to use rpmdev-setuptree command
RUN yum -y install rpmdevtools

# Install rpmdevtools to use rpmdev-setuptree command
RUN yum -y install maven
RUN mkdir -p /root/rpmbuild/SOURCES
