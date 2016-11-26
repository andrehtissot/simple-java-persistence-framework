package com.andretissot.java.dao;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @author André Augusto Tissot
 */
public class ConnectionFactory {
    private static Connection connection = null;
    private static final ConnectionFactory instance = new ConnectionFactory();

    private ConnectionFactory() {}

    private Connection createConnection() throws Exception {
        Properties prop = new Properties();
        InputStream is = getClass().getResourceAsStream("database.properties");
        if (is == null)
            throw new Exception("File database.properties not found");
        prop.load(is);
        String dbDriver = prop.getProperty("db.driver");
        String dbUrl = prop.getProperty("db.url");
        String dbUser = prop.getProperty("db.user");
        String dbPwd = prop.getProperty("db.pwd");
        Class.forName(dbDriver);
        return DriverManager.getConnection(dbUrl, dbUser, dbPwd);
    }

    public static Connection getConnection() throws Exception {
        if (connection == null)
            return connection = instance.createConnection();
        return connection;
    }
}
