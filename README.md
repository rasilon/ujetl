# ujetl (Micro Java ETL)
Originally written in the days of trying to do something, with no budget, I wrote this out of necessity.  I subsequently got permission to open-source it so long as there were no references to the company in it.  So this is the cleaned up version, with a few additional features added and the things that turned out to be pointless removed.

It's probably the smallest functional ETL application with decent performance.  Since I only use it on Postgres nowadays, it only officially supports Postgres at the moment.  But in the near past it's worked pulling data from "several commercial databases" that don't like being named in benchmarks etc. and if you have the JDBC jars in your classpath then it should just work.
