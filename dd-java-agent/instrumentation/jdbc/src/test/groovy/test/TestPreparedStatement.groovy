package test

import java.sql.Array
import java.sql.Blob
import java.sql.Clob
import java.sql.Connection
import java.sql.Date
import java.sql.NClob
import java.sql.ParameterMetaData
import java.sql.PreparedStatement
import java.sql.Ref
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLWarning
import java.sql.SQLXML
import java.sql.Time
import java.sql.Timestamp

class TestPreparedStatement implements PreparedStatement {
  private Connection connection
  public String sql

  TestPreparedStatement(Connection connection, String sql = null) {
    this.connection = connection
    this.sql = sql
  }

  @Override
  ResultSet executeQuery(String sql) throws SQLException {
    this.sql = sql
    return null
  }

  @Override
  int executeUpdate(String sql) throws SQLException {
    this.sql = sql
    return 0
  }

  @Override
  void close() throws SQLException {
  }

  @Override
  int getMaxFieldSize() throws SQLException {
    return 0
  }

  @Override
  void setMaxFieldSize(int max) throws SQLException {
  }

  @Override
  int getMaxRows() throws SQLException {
    return 0
  }

  @Override
  void setMaxRows(int max) throws SQLException {}

  @Override
  void setEscapeProcessing(boolean enable) throws SQLException {}

  @Override
  int getQueryTimeout() throws SQLException {
    return 0
  }

  @Override
  void setQueryTimeout(int seconds) throws SQLException {
  }

  @Override
  void cancel() throws SQLException {
  }

  @Override
  SQLWarning getWarnings() throws SQLException {
    return null
  }

  @Override
  void clearWarnings() throws SQLException {
  }

  @Override
  void setCursorName(String name) throws SQLException {
  }

  @Override
  boolean execute(String sql) throws SQLException {
    this.sql = sql
    return false
  }

  @Override
  ResultSet getResultSet() throws SQLException {
    return null
  }

  @Override
  int getUpdateCount() throws SQLException {
    return 0
  }

  @Override
  boolean getMoreResults() throws SQLException {
    return false
  }

  @Override
  void setFetchDirection(int direction) throws SQLException {
  }

  @Override
  int getFetchDirection() throws SQLException {
    return 0
  }

  @Override
  void setFetchSize(int rows) throws SQLException {
  }

  @Override
  int getFetchSize() throws SQLException {
    return 0
  }

  @Override
  int getResultSetConcurrency() throws SQLException {
    return 0
  }

  @Override
  int getResultSetType() throws SQLException {
    return 0
  }

  @Override
  void addBatch(String sql) throws SQLException {
  }

  @Override
  void clearBatch() throws SQLException {
  }

  @Override
  int[] executeBatch() throws SQLException {
    return new int[0]
  }

  @Override
  Connection getConnection() throws SQLException {
    return connection
  }

  @Override
  boolean getMoreResults(int current) throws SQLException {
    return false
  }

  @Override
  ResultSet getGeneratedKeys() throws SQLException {
    return null
  }

  @Override
  int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    this.sql = sql
    return 0
  }

  @Override
  int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    this.sql = sql
    return 0
  }

  @Override
  int executeUpdate(String sql, String[] columnNames) throws SQLException {
    this.sql = sql
    return 0
  }

  @Override
  boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    this.sql = sql
    return false
  }

  @Override
  boolean execute(String sql, int[] columnIndexes) throws SQLException {
    this.sql = sql
    return false
  }

  @Override
  boolean execute(String sql, String[] columnNames) throws SQLException {
    this.sql = sql
    return false
  }

  @Override
  int getResultSetHoldability() throws SQLException {
    return 0
  }

  @Override
  boolean isClosed() throws SQLException {
    return false
  }

  @Override
  void setPoolable(boolean poolable) throws SQLException {}

  @Override
  boolean isPoolable() throws SQLException {
    return false
  }

  @Override
  void closeOnCompletion() throws SQLException {}

  @Override
  boolean isCloseOnCompletion() throws SQLException {
    return false
  }

  @Override
  ResultSet executeQuery() throws SQLException {
    return null
  }

  @Override
  int executeUpdate() throws SQLException {
    return 0
  }

  @Override
  void setNull(int parameterIndex, int sqlType) throws SQLException {}

  @Override
  void setBoolean(int parameterIndex, boolean x) throws SQLException {}

  @Override
  void setByte(int parameterIndex, byte x) throws SQLException {}

  @Override
  void setShort(int parameterIndex, short x) throws SQLException {}

  @Override
  void setInt(int parameterIndex, int x) throws SQLException {}

  @Override
  void setLong(int parameterIndex, long x) throws SQLException {}

  @Override
  void setFloat(int parameterIndex, float x) throws SQLException {}

  @Override
  void setDouble(int parameterIndex, double x) throws SQLException {}

  @Override
  void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {}

  @Override
  void setString(int parameterIndex, String x) throws SQLException {}

  @Override
  void setBytes(int parameterIndex, byte[] x) throws SQLException {}

  @Override
  void setDate(int parameterIndex, Date x) throws SQLException {}

  @Override
  void setTime(int parameterIndex, Time x) throws SQLException {}

  @Override
  void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {}

  @Override
  void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {}

  @Override
  void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {}

  @Override
  void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {}

  @Override
  void clearParameters() throws SQLException {}

  @Override
  void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {}

  @Override
  void setObject(int parameterIndex, Object x) throws SQLException {}

  @Override
  boolean execute() throws SQLException {
    return false
  }

  @Override
  void addBatch() throws SQLException {}

  @Override
  void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {}

  @Override
  void setRef(int parameterIndex, Ref x) throws SQLException {}

  @Override
  void setBlob(int parameterIndex, Blob x) throws SQLException {}

  @Override
  void setClob(int parameterIndex, Clob x) throws SQLException {}

  @Override
  void setArray(int parameterIndex, Array x) throws SQLException {}

  @Override
  ResultSetMetaData getMetaData() throws SQLException {
    return null
  }

  @Override
  void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {}

  @Override
  void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {}

  @Override
  void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {}

  @Override
  void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {}

  @Override
  void setURL(int parameterIndex, URL x) throws SQLException {}

  @Override
  ParameterMetaData getParameterMetaData() throws SQLException {
    return null
  }

  @Override
  void setRowId(int parameterIndex, RowId x) throws SQLException {}

  @Override
  void setNString(int parameterIndex, String value) throws SQLException {}

  @Override
  void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {}

  @Override
  void setNClob(int parameterIndex, NClob value) throws SQLException {}

  @Override
  void setClob(int parameterIndex, Reader reader, long length) throws SQLException {}

  @Override
  void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {}

  @Override
  void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {}

  @Override
  void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {}

  @Override
  void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {}

  @Override
  void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {}

  @Override
  void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {}

  @Override
  void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {}

  @Override
  void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {}

  @Override
  void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {}

  @Override
  void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {}

  @Override
  void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {}

  @Override
  void setClob(int parameterIndex, Reader reader) throws SQLException {}

  @Override
  void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {}

  @Override
  void setNClob(int parameterIndex, Reader reader) throws SQLException {}

  @Override
  <T> T unwrap(Class<T> iface) throws SQLException {
    return null
  }

  @Override
  boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false
  }
}
