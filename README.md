# ujetl (Micro Java ETL)
Originally written in the days of trying to do something, with no budget, I wrote this out of necessity.  I subsequently got permission to open-source it so long as there were no references to the company in it.  So this is the cleaned up version, with a few additional features added and the things that turned out to be pointless removed.

It's probably the smallest functional ETL application with decent performance.  Since I only use it on Postgres nowadays, it only officially supports Postgres at the moment.  But in the near past it's worked pulling data from "several commercial databases" that don't like being named in benchmarks etc. and if you have the JDBC jars in your classpath then it should just work.

For an example config file, please see [TEST_config_live.xml](https://github.com/rasilon/ujetl/blob/master/src/test/resources/TEST_config_live.xml)

To run the dockerised integration tests, use `build_util/run_docker_tests` in this repo.  

A runnable docker image is available at [rasilon/ujetl](https://cloud.docker.com/repository/docker/rasilon/ujetl).  This expects config files copied into, or mounted into `/var/ujetl/`.  RPMs can be built using `build_util/build_rpms_in_docker`.  As the name suggests, you need docker for that.
