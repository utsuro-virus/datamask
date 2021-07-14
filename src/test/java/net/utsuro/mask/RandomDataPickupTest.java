package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomDataPickupTest extends RandomDataPickup {

  @Nested
  @DisplayName("method: useDatabase")
  class UseDatabase {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("常にDBは使用する")
    void case1() throws Exception {
      assertTrue(useDatabase(rule));
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
  @DisplayName("method: execute")
  class Execute {

    MaskingRule rule = new MaskingRule();
    @Mock
    Connection mockConn;

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      rule.setSelectListTableName("hoge");
      assertEquals("あいう", execute("あいう", null));
    }

    @Test
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      rule.setSelectListTableName("hoge");
      assertEquals(null, execute(null, rule));
    }

    @Test
    @DisplayName("ピックアップ元が指定されていない場合はエラー")
    void case3() throws Exception {
      try {
        execute("hoge", rule);
        fail("ピックアップ元未指定がNGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("データ選択リストの対象テーブル名 selectListTableName が指定されていません。", e.getMessage());
      }
      try {
        rule.setSelectListTableName("hoge");
        execute("hoge", rule);
        fail("ピックアップ元未指定がNGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("データ選択リストの対象カラム名 selectListColName が指定されていません。", e.getMessage());
      }
    }

  }

  @Nested
  @DisplayName("method: execute with Database")
  class ExecuteWithDatabase {

    MaskingRule rule = new MaskingRule();
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
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      // コネクションをセット
      setConnection(mockConn);
    }

    @Test
    @DisplayName("SQL文の組み立て検証")
    void case1() throws Exception {
      // モックの設定
      when(mockResultSet.next()).thenReturn(Boolean.TRUE);
      when(mockResultSet.getLong("maxseq")).thenReturn(123L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      when(mockResultSet.getObject(anyString())).thenReturn("fuga");

      rule.setSelectListTableName("hoge");
      rule.setSelectListSeqNoColName("idx");
      rule.setSelectListColName("val");
      execute("テスト", rule);

      verify(mockConn, times(1)).prepareStatement("SELECT MAX(idx) AS maxseq FROM hoge");
      verify(mockConn, times(1)).prepareStatement("SELECT val FROM hoge WHERE idx = ?");
    }

    @Test
    @DisplayName("null置換ありなら処理されて返る")
    void case2() throws Exception {
      // モックの設定
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong("maxseq")).thenReturn(123L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());

      rule.setSelectListTableName("hoge");
      rule.setSelectListColName("val");
      assertEquals(null, execute(" ", rule));
      assertEquals(null, execute("a", rule));
      assertEquals(null, execute(12345L, rule));
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case3() throws Exception {
      // モックの設定
      when(mockResultSet.next()).thenReturn(Boolean.TRUE);
      when(mockResultSet.getLong("maxseq")).thenReturn(123L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      when(mockResultSet.getObject(anyString())).thenReturn("fuga");

      rule.setIgnoreValue("^ $");
      rule.setSelectListTableName("hoge");
      rule.setSelectListColName("val");
      assertEquals(" ", execute(" ", rule));
      assertNotEquals("a", execute("a", rule));
      rule.setNullReplace(true);
      assertEquals("fuga", execute(null, rule));
    }

    @Test
    @DisplayName("データ無しの場合nullが返る(件数ゼロ)")
    void case4() throws Exception {
      // モックの設定
      when(mockResultSet.next()).thenReturn(Boolean.FALSE);

      rule.setSelectListTableName("hoge");
      rule.setSelectListColName("val");
      assertEquals(null, execute(" ", rule));
      assertEquals(null, execute("a", rule));
      assertEquals(null, execute(12345L, rule));
    }

    @Test
    @DisplayName("データ無しの場合nullが返る(対象IDのデータ無し)")
    void case5() throws Exception {
      // モックの設定
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong("maxseq")).thenReturn(123L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());

      rule.setSelectListTableName("hoge");
      rule.setSelectListColName("val");
      assertEquals(null, execute(" ", rule));
      assertEquals(null, execute("a", rule));
      assertEquals(null, execute(12345L, rule));
    }

    @Test
    @DisplayName("ランダムIDで取得したデータが返る")
    void case10() throws Exception {
      List<String> retList = new ArrayList<>();
      retList.add("0000");
      retList.add("イチ");
      retList.add("に");
      retList.add("ｻﾝ");
      retList.add("4444");
      retList.add("555");
      retList.add(" 6");
      retList.add("77777");
      retList.add("あいう8");
      retList.add("hoge9");
      // モックの設定
      when(mockResultSet.next()).thenReturn(Boolean.TRUE);
      when(mockResultSet.getLong("maxseq")).thenReturn(9L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      when(mockResultSet.getObject(anyString()))
          .thenReturn(retList.get(0))
          .thenReturn(retList.get(1))
          .thenReturn(retList.get(2))
          .thenReturn(retList.get(3))
          .thenReturn(retList.get(4))
          .thenReturn(retList.get(5))
          .thenReturn(retList.get(6))
          .thenReturn(retList.get(7))
          .thenReturn(retList.get(8))
          .thenReturn(retList.get(9));
      rule.setSelectListTableName("hoge");
      rule.setSelectListColName("val");

      int count = 10; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = (String) execute(" ", rule);
        assertEquals(retList.get(i), ret);
      }

      for (int i = 0; i < count; i++) {
        // 50%同じ値で呼ばれていたら偏りすぎ
        verify(mockPreparedStmnt, atMost((int) ((double) count * 0.5))).setLong(1, (long) i);
      }

    }

  }

}
