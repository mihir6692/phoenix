package org.apache.phoenix.end2end;

import static org.apache.phoenix.query.BaseTest.setUpConfigForMiniCluster;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;
import org.apache.phoenix.jdbc.PhoenixDriver;
import org.apache.phoenix.mapreduce.util.ConnectionUtil;
import org.apache.phoenix.mapreduce.util.PhoenixConfigurationUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(NeedsOwnMiniClusterTest.class)
public class ConnectionUtilIT {

    private static HBaseTestingUtility hbaseTestUtil;
    private static Configuration conf;
  
    @BeforeClass
    public static void setUp() throws Exception {
        hbaseTestUtil = new HBaseTestingUtility();
        conf = hbaseTestUtil.getConfiguration();
        setUpConfigForMiniCluster(conf);
        conf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, "/hbase-test");
        hbaseTestUtil.startMiniCluster();
        Class.forName(PhoenixDriver.class.getName());
    }
    
	@Test
	public void testInputAndOutputConnections() throws SQLException {
		Connection inputConnection = ConnectionUtil.getInputConnection(conf);
		Statement stmt = inputConnection.createStatement();
		stmt.execute("create table t(a integer primary key,b varchar)");
		stmt.execute("upsert into t values(1,'foo')");
		inputConnection.commit();
		ResultSet rs = stmt.executeQuery("select count(*) from t");
		rs.next();
		assertEquals(1, rs.getInt(1));
		Connection outputConnection = ConnectionUtil.getOutputConnection(conf);
		stmt = outputConnection.createStatement();
		stmt.execute("create table t1(a integer primary key,b varchar)");
		stmt.execute("upsert into t1 values(1,'foo')");
		outputConnection.commit();
		rs = stmt.executeQuery("select count(*) from t1");
		rs.next();
		assertEquals(1, rs.getInt(1));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		try {
			PhoenixDriver.INSTANCE.close();
		} finally {
			try {
				DriverManager.deregisterDriver(PhoenixDriver.INSTANCE);
			} finally {
				hbaseTestUtil.shutdownMiniCluster();
			}
		}
	}
}
