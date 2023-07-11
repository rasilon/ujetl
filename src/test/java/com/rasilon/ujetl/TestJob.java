package com.rasilon.ujetl;

import java.sql.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class TestJob {

    private static String jdbcURL = "jdbc:h2:mem:dbtest";
    @Test
    public void test002verifyH2Works() {
        try {
            Connection conn = DriverManager.getConnection(jdbcURL, "sa", "");
            conn.close();
        } catch(Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void testJob() {
        try (
                Connection src  = DriverManager.getConnection(jdbcURL, "sa", "");
                Connection dest = DriverManager.getConnection(jdbcURL, "sa", "");

            ) {
            src.createStatement().executeUpdate("CREATE TABLE src(id bigint not null primary key, dat varchar);");
            dest.createStatement().executeUpdate("CREATE TABLE dest(id bigint not null primary key, dat varchar);");
            PreparedStatement inserter = src.prepareStatement("INSERT INTO src(id,dat) VALUES(?,'banana')");
            for(int i=0; i<10000; i++) {
                inserter.setInt(1,i);
                inserter.executeUpdate();
            }

            Job j = new Job(
                src,
                dest,
                "jUnit Test Config",
                "jUnit Test Job",
                "SELECT -1 AS \"key\"",
                "SELECT id,dat FROM src WHERE id > ?",
                "INSERT INTO dest VALUES(?,?)",
                null,
                null,
                100,
                100,
                100,
                "select 'PID:'||session_id()",
                "select 'PID:'||session_id()"
            );
            j.start();
            j.join();
            // do stuff
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
