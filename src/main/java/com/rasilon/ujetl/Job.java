package com.rasilon.ujetl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;



import org.apache.logging.log4j.Logger;


/**
 * @author derryh
 *
 */
public class Job extends Thread {
    static Logger log = org.apache.logging.log4j.LogManager.getLogger(Job.class);

    Connection sConn;
    Connection dConn;
    String name;
    String jobName;
    String key;
    String select;
    String insert;
    String preTarget;
    String postTarget;
    Integer nRowsToLog;
    Integer blockSize;
    Integer pollTimeout;

    BlockingQueue<List<String>> resultBuffer;
    AtomicBoolean producerLive;
    AtomicBoolean threadsExit = new AtomicBoolean(false);;


    public Job(Connection sConn,Connection dConn,String name,String jobName,String key,String select,String insert,String preTarget,String postTarget,Integer nRowsToLog,Integer blockSize,Integer pollTimeout) {
        this.sConn = sConn;
        this.dConn = dConn;
        this.name = name;
        this.jobName = jobName;
        this.key = key;
        this.select = select;
        this.insert = insert;
        this.preTarget = preTarget;
        this.postTarget = postTarget;
        this.nRowsToLog = nRowsToLog;
        this.blockSize = blockSize;
        this.pollTimeout = pollTimeout;

        resultBuffer = new ArrayBlockingQueue<List<String>>( 3 * blockSize);
        producerLive = new AtomicBoolean(true);
        this.setName(String.format("%s-%s-Manager",jobName,name));
    }

    int arraySum(int[] arr) {
        int sum = 0;
        for(int i : arr) {
            sum += i;
        }
        return sum;
    }




    private class Producer extends Thread {
        ResultSet src;
        BlockingQueue q;
        public Producer(ResultSet src,BlockingQueue q) {
            this.src = src;
            this.q = q;
            this.setName(String.format("%s-%s-Producer",jobName,name));
        }
        public void run() {
            try {
                long rowsInserted = 0;
                long rowsAttempted = 0;
                long stamp = System.nanoTime();
                long nstamp;
                int columnCount = src.getMetaData().getColumnCount();
                log.debug("Running select.");
                while(src.next()) {
                    List<String> row = new ArrayList(columnCount);

                    for(int i=1; i<=columnCount; i++) {
                        row.add(src.getString(i));
                    }
                    while(!q.offer(row,1000,java.util.concurrent.TimeUnit.MILLISECONDS)) {
                        if(threadsExit.get()) {
                            log.error("Producer thread asked to exit.");
                            return;
                        }
                        log.trace("Producer queue full.");
                    }
                    rowsAttempted++;
                    if(rowsAttempted % nRowsToLog == 0) {
                        log.info(String.format("%s - Queued %s rows for %s so far",jobName,rowsAttempted,name));
                    }
                }
                producerLive.set(false);
                log.info(String.format("%s - Queued a total of %s rows for %s",jobName,rowsAttempted,name));
            } catch(Exception e) {
                producerLive.set(false); // Signal we've exited.
                threadsExit.set(true); // Signal we've exited.
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private class Consumer extends Thread {
        PreparedStatement insertStatement;
        BlockingQueue<List<String>> q;

        public Consumer(PreparedStatement insertStatement,BlockingQueue q) {
            this.insertStatement = insertStatement;
            this.q = q;
            this.setName(String.format("%s-%s-Consumer",jobName,name));
        }
        public void run() {
            try {
                long rowsAttempted = 0;
                long rowsInserted = 0;

                while(true) {
                    List<String> row = q.poll(pollTimeout,java.util.concurrent.TimeUnit.MILLISECONDS);
                    if(row == null && producerLive.get() == false) {
                        row = q.poll(1,java.util.concurrent.TimeUnit.MILLISECONDS);
                    }
                    if(row == null && producerLive.get() == false) {
                        rowsInserted += arraySum(insertStatement.executeBatch());
                        dConn.commit();
                        log.info(String.format("%s - Inserted  a total of %s of %s notified rows into %s",jobName,rowsInserted,rowsAttempted,name));
                        return;
                    }
                    if(threadsExit.get()) {
                        log.error("Consumer thread asked to exit.");
                        return;
                    }
                    if(row == null) {
                        log.warn("Queue empty.");
                        continue;
                    }

                    for(int i=0; i<row.size(); i++) {
                        insertStatement.setString(i+1,row.get(i));
                    }
                    insertStatement.addBatch();

                    rowsAttempted++;
                    if(rowsAttempted % nRowsToLog == 0) {
                        rowsInserted += arraySum(insertStatement.executeBatch());
                        dConn.commit();
                        log.info(String.format("%s - Inserted %s of %s notified rows into %s",
                            jobName,
                            rowsInserted,
                            rowsAttempted,
                            name));
                    }
                }
            } catch(Exception e) {
                threadsExit.set(true); // Signal we've exited.
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    // Outer run
    public void run() {
        try {
            ResultSet rs;

            log.info(String.format("%s - Processing table: %s",jobName,name));
            if(preTarget != null){
                log.info("Trying to execute preTarget SQL");
                PreparedStatement s = dConn.prepareStatement(preTarget);
                s.executeUpdate();
                s.close();
                dConn.commit();
            }else{
                log.info("No preTarget; skipping.");
            }

            log.debug("Trying to execute: "+key);
            PreparedStatement keyStatement = dConn.prepareStatement(key);
            rs = keyStatement.executeQuery();
            rs.next();
            String keyVal = rs.getString("key");
            log.info(String.format("%s - Our start key is %s",jobName,keyVal));

            log.debug("Trying to execute: "+select);
            PreparedStatement selectStatement = sConn.prepareStatement(select);
            selectStatement.setFetchSize(blockSize);
            selectStatement.setString(1,keyVal);

            log.debug("Trying to prepare: "+insert);
            PreparedStatement insertStatement = dConn.prepareStatement(insert);

            log.debug("About to execute select.");
            rs = selectStatement.executeQuery();

            Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    threadsExit.set(true);
                    log.error("Job exiting: Caught exception from subthread: " + ex);
                    System.exit(1);
                }
            };
            Thread p = new Producer(rs,resultBuffer);
            p.setUncaughtExceptionHandler(h);
            p.start();

            Thread c = new Consumer(insertStatement,resultBuffer);
            c.setUncaughtExceptionHandler(h);
            c.start();

            p.join();
            c.join();

            if(postTarget != null){
                log.info("Trying to execute postTarget SQL");
                PreparedStatement s = dConn.prepareStatement(postTarget);
                s.executeUpdate();
                s.close();
                dConn.commit();
            }else{
                log.info("No postTarget; skipping.");
            }


        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
