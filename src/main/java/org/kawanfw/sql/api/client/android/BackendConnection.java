/*
 * This file is part of AceQL
 * AceQL: Remote JDBC access over HTTP.                                     
 * Copyright (C) 2015,  KawanSoft SAS
 * (http://www.kawansoft.com). All rights reserved.  
 * AceQL is distributed in the hope that it will be useful,               
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * 
 * Any modifications to this file must keep this entire header
 * intact.                                                                          
 */
package org.kawanfw.sql.api.client.android;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//import org.kawanfw.sql.api.client.RemoteDriver;

/**
 * This example:
 * <ul>
 * <li>Inserts a Customer and an Order on a remote database.</li>
 * </li>Displays the inserted raws on the console with two SELECT executed on
 * the remote database.</li>
 * </ul>
 *
 * @author Nicolas de Pomereu
 */
public class BackendConnection {

    /**
     * The JDBC Connection to the remote AceQL Server
     */
    Connection connection = null;

    /**
     * Constructor
     *
     * @param connection the AwakeConnection to use for this session
     */
    public BackendConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * RemoteConnection Quick Start client example. Creates a Connection to a
     * remote database.
     *
     * @return the Connection to the remote database
     * @throws SQLException if a database access error occurs
     */

    public static Connection remoteConnectionBuilder(String url, String username, String password) throws SQLException {

        // Required for Android, optional for others environments:
        try {
            //ClassNotFoundException if AceQL client jar is not in the classpath
            Class.forName("org.kawanfw.sql.api.client.RemoteDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Attempt to establish a connection to the remote database:

        return DriverManager.getConnection(url, username, password);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public Connection getConnection() {
        return connection;
    }
}