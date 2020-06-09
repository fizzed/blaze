/*
 * Copyright 2015 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.blaze.mysql;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.UriMixin;
import com.fizzed.blaze.util.MutableUri;
import com.fizzed.blaze.util.ObjectHelper;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlConnect extends Action<MysqlConnect.Result,MysqlSession> implements UriMixin<MysqlConnect> {
    static private final Logger log = LoggerFactory.getLogger(MysqlConnect.class);

    // jdbc:mysql://localhost:3309/${ebean.datasource.name}?serverTimezone=UTC&useSSL=false&characterEncoding=UTF-8
    private MutableUri uri;
    
    public MysqlConnect(Context context) {
        this(context, new MutableUri("mysql:/"));
    }
    
    public MysqlConnect(Context context, MutableUri uri) {
        super(context);
        this.uri = uri;
    }
    
    @Override
    public MutableUri getUri() {
        return this.uri;
    }
    
    public MysqlConnect databaseName(String name) {
        this.uri.path(name);
        return this;
    }

    static public class Result extends com.fizzed.blaze.core.Result<MysqlConnect,MysqlSession,Result> {
        
        Result(MysqlConnect action, MysqlSession value) {
            super(action, value);
        }
        
    }
    
    @Override
    protected Result doRun() throws BlazeException {
        ObjectHelper.requireNonNull(uri, "uri cannot be null");
        ObjectHelper.requireNonNull(uri.getScheme(), "uri scheme is required for mysql (e.g. mysql://localhost)");
        ObjectHelper.requireNonNull(uri.getHost(), "uri host is required for mysql");
  
        try {
            // build a jdbc url....
            MutableUri jdbcUrl = new MutableUri("jdbc:mysql://" + this.uri.getHost());
            
            jdbcUrl.scheme("jdbc:mysql");
            jdbcUrl.host(this.uri.getHost());
            
            if (this.uri.getPort() != null && this.uri.getPort() != 3306) {
                jdbcUrl.port(this.uri.getPort());
            }
            
            if (this.uri.getPath() != null) {
                jdbcUrl.path(this.uri.getPath());
            }
            
            if (this.uri.getUsername() != null) {
                jdbcUrl.query("user", this.uri.getUsername());
            }
            
            
            MutableUri redactedJdbcUrl = new MutableUri(jdbcUrl);
            
            
            if (this.uri.getPassword() != null) {
                jdbcUrl.query("password", this.uri.getPassword());
                redactedJdbcUrl.query("password", "*redacted-pw*");
            }
            
            final long start = System.currentTimeMillis();
            log.info("Connecting to {}", redactedJdbcUrl);
            
            final Connection connection = DriverManager.getConnection(jdbcUrl.toString());

            final DatabaseMetaData meta = connection.getMetaData();
            
            final String name = meta.getDatabaseProductName();
            final String version = meta.getDatabaseProductVersion();
            
            final MysqlInfo info = new MysqlInfo(name, version);
            
            log.info("Connected to {} ({} v{}) (in {} ms)", redactedJdbcUrl, name, version, (System.currentTimeMillis() - start));
            
            return new Result(this, new MysqlSession(this.context, this.uri.toImmutableUri(), redactedJdbcUrl.toImmutableUri(), connection, info));
            
        }
        catch (SQLException e) {
            throw new BlazeException(e.getMessage(), e);
            // handle any errors
//            System.out.println("SQLException: " + ex.getMessage());
//            System.out.println("SQLState: " + ex.getSQLState());
//            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
    
}
