Summary: Java app to facilitate moving data between databases.
Name: uJETL
Version: 2.2.3
Release: 1
Group: Applications/Database
License: All rights reserved.
Source: uJETL-%{version}.tar.gz
URL:  https://github.com/rasilon/ujetl.git
Distribution: derryh
Vendor: derryh
Packager: Derry Hamilton <derryh@rasilon.net>
#BuildRoot: .

%description
A very small ETL app

%prep
%setup

%build
#mvn -Dmaven.test.skip=true clean package
true

%install
mkdir -p $RPM_BUILD_ROOT/usr/share/ujetl/lib $RPM_BUILD_ROOT/etc/ujetl $RPM_BUILD_ROOT/usr/bin
cp target/CopyingApp-*-jar-with-dependencies.jar $RPM_BUILD_ROOT/usr/share/ujetl/lib/CopyingApp.jar
cp install_extra/run_copying_job $RPM_BUILD_ROOT/usr/bin
cp install_extra/copying_defaults_log4j.xml $RPM_BUILD_ROOT/etc/ujetl

%files
/usr/share/ujetl/lib/CopyingApp.jar
/usr/bin/run_copying_job
/etc/ujetl/copying_defaults_log4j.xml
