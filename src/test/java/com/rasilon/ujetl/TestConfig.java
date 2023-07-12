package com.rasilon.ujetl;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import org.apache.commons.beanutils.PropertyUtils; // Why does config need this?

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * @author derryh
 *
 */
public class TestConfig {

    @Test
    public void test001VerifyArrayOfDrivers() {
        try {
            Configurations configs = new Configurations();
            Configuration config = configs.xml("TEST_config_live.xml");
            String[] drivers = config.get(String[].class, "drivers.driver");
            int ndrivers =drivers.length;
            if(ndrivers != 3){
                fail("Expected 3 drivers, but found "+ndrivers);
            }
        } catch(Exception e) {
            fail(e.toString());
        }
    }

}
