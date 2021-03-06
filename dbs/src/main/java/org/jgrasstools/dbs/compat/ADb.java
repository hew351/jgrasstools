/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.dbs.compat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jgrasstools.dbs.spatialite.ForeignKey;
import org.jgrasstools.dbs.spatialite.QueryResult;

/**
 * Abstract non spatial db class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public abstract class ADb implements AutoCloseable {

    protected IJGTConnection mConn = null;

    protected String mDbPath;

    public boolean mPrintInfos = true;

    /**
     * Open the connection to a database.
     * 
     * <b>Make sure the connection object is created here.</b>
     * 
     * @param dbPath
     *            the database path. If <code>null</code>, an in-memory db is
     *            created.
     * @return <code>true</code> if the database did already exist.
     * @throws Exception
     */
    public abstract boolean open( String dbPath ) throws Exception;

    /**
     * @return the path to the database.
     */
    public String getDatabasePath() {
        return mDbPath;
    }

    /**
     * Toggle autocommit mode.
     * 
     * @param enable
     *            if <code>true</code>, autocommit is enabled if not already
     *            enabled. Vice versa if <code>false</code>.
     * @throws SQLException
     */
    public void enableAutocommit( boolean enable ) throws Exception {
        boolean autoCommitEnabled = mConn.getAutoCommit();
        if (enable && !autoCommitEnabled) {
            // do enable if not already enabled
            mConn.setAutoCommit(true);
        } else if (!enable && autoCommitEnabled) {
            // disable if not already disabled
            mConn.setAutoCommit(false);
        }
    }

    /**
     * Get database infos.
     * 
     * @return the string array of [sqlite_version, spatialite_version,
     *         spatialite_target_cpu]
     * @throws SQLException
     */
    public String[] getDbInfo() throws Exception {
        // checking SQLite and SpatiaLite version + target CPU
        String sql = "SELECT sqlite_version()";
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            String[] info = new String[1];
            while( rs.next() ) {
                // read the result set
                info[0] = rs.getString(1);
            }
            return info;
        }
    }

    /**
     * Create a new table.
     * 
     * @param tableName
     *            the table name.
     * @param fieldData
     *            the data for each the field (ex. id INTEGER NOT NULL PRIMARY
     *            KEY).
     * @throws SQLException
     */
    public void createTable( String tableName, String... fieldData ) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(tableName).append("(");
        for( int i = 0; i < fieldData.length; i++ ) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(fieldData[i]);
        }
        sb.append(")");

        try (IJGTStatement stmt = mConn.createStatement()) {
            stmt.execute(sb.toString());
        }
    }

    /**
     * Create an single column index.
     * 
     * @param tableName
     *            the table.
     * @param column
     *            the column.
     * @param isUnique
     *            if <code>true</code>, a unique index will be created.
     * @throws Exception
     */
    public void createIndex( String tableName, String column, boolean isUnique ) throws Exception {
        String sql = getIndexSql(tableName, column, isUnique);
        try (IJGTStatement stmt = mConn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.contains("index") && message.contains("already exists")) {
                logWarn(message);
            } else {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the sql to create an index.
     * 
     * @param tableName
     *            the table.
     * @param column
     *            the column.
     * @param isUnique
     *            if <code>true</code>, a unique index will be created.
     * @return the index sql.
     */
    public String getIndexSql( String tableName, String column, boolean isUnique ) {
        String unique = "UNIQUE ";
        if (!isUnique) {
            unique = "";
        }
        String indexName = tableName + "__" + column + "_idx";
        String sql = "CREATE " + unique + "INDEX " + indexName + " on " + tableName + "(" + column + ");";
        return sql;
    }

    /**
     * Get the list of available tables.
     * 
     * @param doOrder
     *            if <code>true</code>, the names are ordered.
     * @return the list of names.
     * @throws Exception
     */
    public List<String> getTables( boolean doOrder ) throws Exception {
        List<String> tableNames = new ArrayList<String>();
        String orderBy = " ORDER BY name";
        if (!doOrder) {
            orderBy = "";
        }
        String sql = "SELECT name FROM sqlite_master WHERE type='table' or type='view'" + orderBy;
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            while( rs.next() ) {
                String tabelName = rs.getString(1);
                tableNames.add(tabelName);
            }
            return tableNames;
        }
    }

    /**
     * Checks if the table is available.
     * 
     * @param tableName
     *            the name of the table.
     * @return <code>true</code> if the table exists.
     * @throws Exception
     */
    public boolean hasTable( String tableName ) throws Exception {
        String sql = "SELECT name FROM sqlite_master WHERE type='table'";
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            while( rs.next() ) {
                String name = rs.getString(1);
                if (name.equals(tableName)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Get the column [name, type, pk] values of a table.
     * 
     * @param tableName
     *            the table to check.
     * @return the list of column [name, type, pk].
     * @throws SQLException
     */
    public List<String[]> getTableColumns( String tableName ) throws Exception {
        String sql;
        if (tableName.indexOf('.') != -1) {
            // it is an attached database
            String[] split = tableName.split("\\.");
            String dbName = split[0];
            String tmpTableName = split[1];
            sql = "PRAGMA " + dbName + ".table_info(" + tmpTableName + ")";
        } else {
            sql = "PRAGMA table_info(" + tableName + ")";
        }

        List<String[]> columnNames = new ArrayList<String[]>();
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            IJGTResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            int nameIndex = -1;
            int typeIndex = -1;
            int pkIndex = -1;
            for( int i = 1; i <= columnCount; i++ ) {
                String columnName = rsmd.getColumnName(i);
                if (columnName.equals("name")) {
                    nameIndex = i;
                } else if (columnName.equals("type")) {
                    typeIndex = i;
                } else if (columnName.equals("pk")) {
                    pkIndex = i;
                }
            }

            while( rs.next() ) {
                String name = rs.getString(nameIndex);
                String type = rs.getString(typeIndex);
                String pk = "0";
                if (pkIndex > 0)
                    pk = rs.getString(pkIndex);
                columnNames.add(new String[]{name, type, pk});
            }
            return columnNames;
        }
    }

    /**
     * Get the foreign keys from a table.
     * 
     * @param tableName
     *            the table to check on.
     * @return the list of keys.
     * @throws Exception
     */
    public List<ForeignKey> getForeignKeys( String tableName ) throws Exception {
        String sql = null;
        if (tableName.indexOf('.') != -1) {
            // it is an attached database
            String[] split = tableName.split("\\.");
            String dbName = split[0];
            String tmpTableName = split[1];
            sql = "PRAGMA " + dbName + ".foreign_key_list(" + tmpTableName + ")";
        } else {
            sql = "PRAGMA foreign_key_list(" + tableName + ")";
        }

        List<ForeignKey> fKeys = new ArrayList<ForeignKey>();
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            IJGTResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            int fromIndex = -1;
            int toIndex = -1;
            int toTableIndex = -1;
            for( int i = 1; i <= columnCount; i++ ) {
                String columnName = rsmd.getColumnName(i);
                if (columnName.equals("from")) {
                    fromIndex = i;
                } else if (columnName.equals("to")) {
                    toIndex = i;
                } else if (columnName.equals("table")) {
                    toTableIndex = i;
                }
            }
            while( rs.next() ) {
                ForeignKey fKey = new ForeignKey();
                Object fromObj = rs.getObject(fromIndex);
                Object toObj = rs.getObject(toIndex);
                Object toTableObj = rs.getObject(toTableIndex);
                if (fromObj != null && toObj != null && toTableObj != null) {
                    fKey.from = fromObj.toString();
                    fKey.to = toObj.toString();
                    fKey.table = toTableObj.toString();
                } else {
                    continue;
                }
                fKeys.add(fKey);
            }
            return fKeys;
        }
    }

    /**
     * Get the record count of a table.
     * 
     * @param tableName
     *            the name of the table.
     * @return the record count or -1.
     * @throws Exception
     */
    public long getCount( String tableName ) throws Exception {
        String sql = "select count(*) from " + tableName;
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            while( rs.next() ) {
                long count = rs.getLong(1);
                return count;
            }
            return -1;
        }
    }

    /**
     * Execute a query from raw sql.
     * 
     * @param sql
     *            the sql to run.
     * @param limit
     *            a limit, ignored if < 1
     * @return the resulting records.
     * @throws Exception
     */
    public QueryResult getTableRecordsMapFromRawSql( String sql, int limit ) throws Exception {
        QueryResult queryResult = new QueryResult();
        try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
            IJGTResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for( int i = 1; i <= columnCount; i++ ) {
                String columnName = rsmd.getColumnName(i);
                queryResult.names.add(columnName);
                String columnTypeName = rsmd.getColumnTypeName(i);
                queryResult.types.add(columnTypeName);
            }
            int count = 0;
            while( rs.next() ) {
                Object[] rec = new Object[columnCount];
                for( int j = 1; j <= columnCount; j++ ) {
                    Object object = rs.getObject(j);
                    rec[j - 1] = object;
                }
                queryResult.data.add(rec);
                if (limit > 0 && ++count > (limit - 1)) {
                    break;
                }
            }
            return queryResult;
        }
    }

    /**
     * Execute a query from raw sql and put the result in a csv file.
     * 
     * @param sql
     *            the sql to run.
     * @param csvFile
     *            the output file.
     * @param doHeader
     *            if <code>true</code>, the header is written.
     * @param separator
     *            the separator (if null, ";" is used).
     * @throws Exception
     */
    public void runRawSqlToCsv( String sql, File csvFile, boolean doHeader, String separator ) throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            try (IJGTStatement stmt = mConn.createStatement(); IJGTResultSet rs = stmt.executeQuery(sql)) {
                IJGTResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for( int i = 1; i <= columnCount; i++ ) {
                    if (i > 1) {
                        bw.write(separator);
                    }
                    String columnName = rsmd.getColumnName(i);
                    bw.write(columnName);
                }
                bw.write("\n");
                while( rs.next() ) {
                    for( int j = 1; j <= columnCount; j++ ) {
                        if (j > 1) {
                            bw.write(separator);
                        }
                        Object object = rs.getObject(j);
                        if (object != null) {
                            bw.write(object.toString());
                        } else {
                            bw.write("");
                        }
                    }
                    bw.write("\n");
                }
            }
        }
    }

    /**
     * Execute an update, insert or delete by sql.
     * 
     * @param sql
     *            the sql to run.
     * @return the result code of the update.
     * @throws Exception
     */
    public int executeInsertUpdateDeleteSql( String sql ) throws Exception {
        try (IJGTStatement stmt = mConn.createStatement()) {
            int executeUpdate = stmt.executeUpdate(sql);
            return executeUpdate;
        }
    }

    public int executeInsertUpdateDeletePreparedSql( String sql, Object[] objects ) throws Exception {
        try (IJGTPreparedStatement stmt = mConn.prepareStatement(sql)) {
            for( int i = 0; i < objects.length; i++ ) {
                if (objects[i] instanceof Boolean) {
                    stmt.setBoolean(i + 1, (boolean) objects[i]);
                } else if (objects[i] instanceof byte[]) {
                    stmt.setBytes(i + 1, (byte[]) objects[i]);
                } else if (objects[i] instanceof Double) {
                    stmt.setDouble(i + 1, (double) objects[i]);
                } else if (objects[i] instanceof Float) {
                    stmt.setFloat(i + 1, (float) objects[i]);
                } else if (objects[i] instanceof Integer) {
                    stmt.setInt(i + 1, (int) objects[i]);
                } else if (objects[i] instanceof Long) {
                    stmt.setLong(i + 1, (long) objects[i]);
                } else if (objects[i] instanceof Short) {
                    stmt.setShort(i + 1, (short) objects[i]);
                } else if (objects[i] instanceof String) {
                    stmt.setString(i + 1, (String) objects[i]);
                } else {
                    stmt.setString(i + 1, objects[i].toString());
                }
            }
            int executeUpdate = stmt.executeUpdate();
            return executeUpdate;
        }
    }

    /**
     * @return the connection to the database.
     */
    public IJGTConnection getConnection() {
        return mConn;
    }

    public void close() throws Exception {
        if (mConn != null) {
            mConn.setAutoCommit(false);
            mConn.commit();
            mConn.close();
        }
    }

    /**
     * Escape sql.
     * 
     * @param sql
     *            the sql code to escape.
     * @return the escaped sql.
     */
    public static String escapeSql( String sql ) {
        // ' --> ''
        sql = sql.replaceAll("'", "''");
        // " --> ""
        sql = sql.replaceAll("\"", "\"\"");
        // \ --> (remove backslashes)
        sql = sql.replaceAll("\\\\", "");
        return sql;
    }

    /**
     * Composes the formatter for unix timstamps in queries.
     * 
     * <p>
     * The default format is: <b>2015-06-11 03:14:51</b>, as given by pattern:
     * <b>%Y-%m-%d %H:%M:%S</b>.
     * </p>
     * 
     * @param columnName
     *            the timestamp column in the db.
     * @param datePattern
     *            the datepattern.
     * @return the query piece.
     */
    public static String getTimestampQuery( String columnName, String datePattern ) {
        if (datePattern == null)
            datePattern = "%Y-%m-%d %H:%M:%S";
        String sql = "strftime('" + datePattern + "', " + columnName + " / 1000, 'unixepoch')";
        return sql;
    }

    protected abstract void logWarn( String message );

    protected abstract void logInfo( String message );

    protected abstract void logDebug( String message );

}