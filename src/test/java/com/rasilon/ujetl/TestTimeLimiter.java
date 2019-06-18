package com.rasilon.ujetl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class TestTimeLimiter {

    @Test
    public void test001Limiter() {
        try {
            TimeLimiter hardLimit = new TimeLimiter(1,false);
            hardLimit.start();

            Thread.sleep(10000);

            fail("Sleep wasn't interrupted by the limiter!");
        } catch(java.lang.InterruptedException e) {
            // Pass
        } catch(Exception e) {
            e.printStackTrace();
            fail("Unexpected exception.");
        }
    }
}
