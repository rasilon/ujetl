package com.rasilon.ujetl;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;



public class TimeLimiter extends Thread {
    static Logger log = LogManager.getLogger(TimeLimiter.class);

    private long timeoutSeconds = 0;
    private Thread parentThread = Thread.currentThread();
    private boolean forceExit = false;

    public TimeLimiter(long timeoutSeconds,boolean forceExit) {
        this.timeoutSeconds = timeoutSeconds;
        this.forceExit = forceExit;
        setDaemon(true);
    }

    public TimeLimiter(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        setDaemon(true);
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        while(true) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                // Handled by the next test.  We really don't care here.
            }
            if(System.currentTimeMillis() > (startTime + (1000*timeoutSeconds))) {
                if(forceExit) {
                    log.error("Hard runtime limit hit.  Exiting now.");
                    System.exit(27);
                } else {
                    log.error("Interrupt runtime limit hit.  Watchdog thread sending and exiting.");
                    parentThread.interrupt();
                    return;
                }
            }
        }
    }

}
