package com.rasilon.ujetl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Executors ;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.apache.commons.beanutils.PropertyUtils; // Why does config need this?


/**
 * @author derryh
 *
 */
public class CopyingApp {
    static Logger log = LogManager.getLogger(CopyingApp.class);

    public static void main(String[] args) {
        CopyingAppCommandParser cli = new CopyingAppCommandParser(args);
        LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        String log4jConfigLocation = cli.getLog4jConfigFile();
        File file = new File(log4jConfigLocation);
        context.setConfigLocation(file.toURI());
        System.out.println("Config set from "+file.toURI());

        CopyingApp app = new CopyingApp(cli);
        try {
            app.run();
        } catch(Exception e) {
            log.error(String.format("%s - %s",app.getJobName(),e.toString()));
        }
    }

    CopyingAppCommandParser cli;
    String jobName;
    Integer blockSize = 100;



    public CopyingApp(CopyingAppCommandParser cli) {
        this.cli = cli;

        String   fname  = cli.getConfigFile();
        File f = new File(cli.getConfigFile());
        String[] job_name = fname.split("_");
        jobName = (f.getName().split("_"))[0];


    }

    public String getJobName() {
        return jobName;
    }

    public void run() {
        log.info(String.format("%s - Starting copying job",jobName));

        Connection sConn = null;
        Connection dConn = null;
        try {

            Configurations configs = new Configurations();

            Configuration config = configs.xml(cli.getConfigFile());

            String hardLimitSeconds = config.getString("hardLimitSeconds");
            if(hardLimitSeconds != null) {
                TimeLimiter hardLimit = new TimeLimiter(Integer.decode(hardLimitSeconds).intValue(),true);
                log.info(String.format("%s - Set a hard runtime limit of %s seconds",jobName,hardLimitSeconds));
                hardLimit.start();
            } else {
                log.info(String.format("%s - No runtime limit specified",jobName));
            }

            String blockSizeString = config.getString("blockSize");
            if(blockSizeString != null) {
                blockSize = new Integer(blockSizeString);
                log.info(String.format("%s - Set a block size of %s rows",jobName,blockSizeString));
            }


            sConn = getConnFor("source",config);
            dConn = getConnFor("dest",config);
            dConn.setAutoCommit(false);

            Integer nRowsToLog = null;
            try {
                nRowsToLog = new Integer(config.getString("nRowsToLog"));
                log.info(String.format("%s - Setting Row count interval to %s", jobName, nRowsToLog));
            } catch(Exception e) {
                nRowsToLog = new Integer(100); // If we don't have a new setting, use the old default
                log.info(String.format("%s - Setting Row count interval to default of 100 rows.",jobName));
            }

		  	Integer pollTimeout = null;
	  		try {
  				pollTimeout = new Integer(config.getString("pollTimeout"));
				log.info(String.format("%s - Setting Poll timeout to %s milliseconds", jobName, pollTimeout));
			} catch(Exception e) {
				pollTimeout = new Integer(1000); // If we don't have a new setting, use the old default
				log.info(String.format("%s - Setting poll timeout to default of 1 second.",jobName));
			}



            long startTime = System.nanoTime();

            log.info(String.format("%s - Jobs are:",jobName));
            Object prop = config.getProperty("jobs.job.name");
            if(prop instanceof Collection) {
                int numTabs = ((Collection<?>) prop).size();
                log.info(String.format("%s - Number of jobs: %s",jobName, new Integer(numTabs)));
                for(int i=0; i < numTabs; i++ ) {
                    String tabName = config.getString("jobs.job("+i+").name");
                    String tabKey = config.getString("jobs.job("+i+").key");
                    String tabSelect = config.getString("jobs.job("+i+").select");
                    String tabInsert = config.getString("jobs.job("+i+").insert");
                    Job j = new Job(sConn,dConn,tabName,jobName,tabKey,tabSelect,tabInsert,nRowsToLog,blockSize,pollTimeout);
                    j.start();
                    j.join();

                }
            } else if(prop instanceof String) {
                String tabName = config.getString("jobs.job.name");
                String tabKey = config.getString("jobs.job.key");
                String tabSelect = config.getString("jobs.job.select");
                String tabInsert = config.getString("jobs.job.insert");
                Job j = new Job(sConn,dConn,tabName,jobName,tabKey,tabSelect,tabInsert,nRowsToLog,blockSize,pollTimeout);
                j.start();
                j.join();
            } else {
                log.info(String.format("%s - Class is actually a %s",jobName, prop.getClass().getName()));
            }

            long endTime = System.nanoTime();
            logDuration("Job took", startTime, endTime);

        } catch (SQLException e) {
            SQLException x = e;
            do {
                log.error(String.format("%s - %s",jobName, x.toString()));
                x.printStackTrace();
                x = x.getNextException();
            } while(x != null && x != x.getNextException());
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            log.error(String.format("%s - %s",jobName, e.toString()));
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            log.error(String.format("%s - %s",jobName, e.toString()));
            throw new RuntimeException(e);
        }
    }

    private boolean excluded(String thing, String[] exclusions) {
        for (String exclusion : exclusions) {
            if (thing.equals(exclusion)) {
                return true;
            }
        }
        return false;
    }

    private void logDuration(String what, long startTime, long endTime) {
        long duration = endTime - startTime;

        log.info(String.format("%s - copying job completed in %s seconds",jobName, new Double(((double) duration) / 1000000000.0)) );

    }

    private Connection getConnFor(String connType, Configuration config) throws SQLException {
        Properties p = new Properties();
        p.setProperty("user",config.getString(connType + ".username"));
        p.setProperty("password",config.getString(connType + ".password"));
        Connection c =  DriverManager.getConnection(config.getString(connType + ".dsn"),p);
        c.setAutoCommit(false);

        String timeout = config.getString(connType + ".networkTimeout");
        if(timeout != null) {
            try {
                c.setNetworkTimeout(Executors.newFixedThreadPool(5), Integer.decode(timeout).intValue());
                log.info(String.format("%s - Set network timeout of %s on %s",jobName,timeout,connType));
            } catch(Exception e) {
                log.error(String.format("%s - Failed to set connection timeout: %s",jobName,e.toString()));
            }
        }

        return c;
    }
}
