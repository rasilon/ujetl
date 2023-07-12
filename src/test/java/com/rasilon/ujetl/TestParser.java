package com.rasilon.ujetl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class TestParser {

    @Test
    public void test001Parset() {
        try {
            String[] args = {
                "--config",
                "config_test_banana.xml"
            };
            CopyingAppCommandParser p = new CopyingAppCommandParser(args);

            assertEquals(p.getConfigFile(),"config_test_banana.xml");

        } catch(Exception e) {
            fail(e.toString());
        }
    }
}
