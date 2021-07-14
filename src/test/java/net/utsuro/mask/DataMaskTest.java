package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataMaskTest implements DataMask {

  private Connection conn;

  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {
    return null;
  }

  @Override
  public Connection getConnection() {
    return conn;
  }

  @Override
  public void setConnection(Connection conn) {
    this.conn = conn;
  }

  @Nested
  @DisplayName("method: useDatabase")
  class UseDatabase {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("DBは使用しない")
    void case1() throws Exception {
      assertFalse(useDatabase(rule));
    }

  }

  @Nested
  @DisplayName("method: getConnection")
  class GetConnection {

    @Mock
    Connection mockConn;

    @Test
    @DisplayName("デフォルトではDBは使用しないのでnullが返る")
    void case1() throws Exception {
      assertEquals(null, getConnection());
    }

    @Test
    @DisplayName("セットすればセットしたものが返る")
    void case2() throws Exception {
      setConnection(mockConn);
      assertEquals(mockConn, getConnection());
    }

  }

  @Nested
  @DisplayName("method: setConnection")
  class SetConnection {

    @Mock
    Connection mockConn;

    @Test
    @DisplayName("セットすればセットしたものが返る")
    void case1() throws Exception {
      setConnection(mockConn);
      assertEquals(mockConn, getConnection());
    }

  }

  @Nested
  @DisplayName("method: getRegisteredUniqueVal")
  class GetRegisteredUniqueVal {

    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockConn.isClosed()).thenReturn(true, false, false, false);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(false, true);
      when(mockResultSet.getString(anyString())).thenReturn("fuga");
    }

    @Test
    @DisplayName("DBにデータがある場合はその値を返す")
    void case1() throws Exception {
      // コネクションがnull
      assertEquals(null, getRegisteredUniqueVal("hoge", "id2"));
      // DB未接続時
      setConnection(mockConn);
      assertEquals(null, getRegisteredUniqueVal("hoge", "id2"));
      // nullを渡したとき
      assertEquals(null, getRegisteredUniqueVal("hoge", null));
      // rs.next()でfalse
      assertEquals(null, getRegisteredUniqueVal("hoge", "id1"));
      // rs.next()でtrue
      assertEquals("fuga", getRegisteredUniqueVal("hoge", "id2"));
    }

  }

  @Nested
  @DisplayName("method: isExistsInUniqueList")
  class IsExistsInUniqueList {

    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
      // モックの設定
      when(mockConn.isClosed()).thenReturn(true, false, false, false, false);
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(false, true, true);
      when(mockResultSet.getLong(anyString())).thenReturn(1L, 0L);
    }

    @Test
    @DisplayName("DBにデータがある場合はtrue、データがなければfalseを返す")
    void case1() throws Exception {
      // コネクションがnull
      assertFalse(isExistsInUniqueList("hoge", "id2"));
      // DB未接続時
      setConnection(mockConn);
      assertFalse(isExistsInUniqueList("hoge", "id2"));
      // nullを渡したとき
      assertFalse(isExistsInUniqueList("hoge", null));
      // rs.next()でfalse
      assertFalse(isExistsInUniqueList("hoge", "id1"));
      // rs.next()でtrue かつ 件数1件
      assertTrue(isExistsInUniqueList("hoge", "id2"));
      // rs.next()でtrue かつ 件数0件
      assertFalse(isExistsInUniqueList("hoge", "id2"));
    }

  }

  @Nested
  @DisplayName("method: addUniqueList")
  class AddUniqueList {

    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
      // モックの設定
      when(mockConn.isClosed()).thenReturn(true, false, false, false, false, false);
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.execute()).thenReturn(true);
    }

    @Test
    @DisplayName("ユニークリストへの登録")
    void case1() throws Exception {
      // コネクションがnull
      assertTrue(addUniqueList("hoge", "id2", "hoge"));
      // DB未接続時
      setConnection(mockConn);
      assertTrue(addUniqueList("hoge", "id2", "fuga"));
      // nullを渡したとき
      assertTrue(addUniqueList("hoge", null, null));
      assertTrue(addUniqueList("hoge", null, "hoge"));
      assertTrue(addUniqueList("hoge", "id3", null));
      // 正常な場合
      assertTrue(addUniqueList("hoge", "id2", "fuga"));
      verify(mockPreparedStmnt, times(1)).setString(1, "hoge");
      verify(mockPreparedStmnt, times(1)).setString(2, "id2");
      verify(mockPreparedStmnt, times(1)).setString(3, "fuga");
    }

  }

}
