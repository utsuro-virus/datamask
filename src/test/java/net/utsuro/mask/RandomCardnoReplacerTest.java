package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class RandomCardnoReplacerTest extends RandomCardnoReplacer {

  @Nested
  @DisplayName("method: useDatabase")
  class UseDatabase {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("デフォルトではDBは使用しない")
    void case1() throws Exception {
      assertFalse(useDatabase(rule));
    }

    @Test
    @DisplayName("isUniqueValue指定時はDBは使用する")
    void case2() throws Exception {
      rule.setUniqueValue(true);
      assertTrue(useDatabase(rule));
    }

    @Test
    @DisplayName("isDeterministicReplace指定時はDBは使用する")
    void case3() throws Exception {
      rule.setDeterministicReplace(true);
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

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      assertEquals("あいう", replace("あいう", null));
    }

    @Test
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, replace(null, rule));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() throws Exception {
      assertEquals("", replace("", rule));
    }

    @Test
    @DisplayName("文字列以外はToString()して置換されて返る")
    void case4() throws Exception {
      String ret;
      ret = (String) execute(12345678901L, rule);
      assertNotEquals("12345678901", ret);
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case5() throws Exception {
      rule.setIgnoreValue("^0120-");
      assertEquals("0120-1234-5678", replace("0120-1234-5678", rule));
      assertEquals("0120-5678-1234", replace("0120-5678-1234", rule));
      assertNotEquals("012-0567-1234", replace("012-0567-1234", rule));
    }

    @Test
    @DisplayName("ランダム置換のカード番号が返る")
    void case10() throws Exception {
      String ret;
      rule.setUnmaksedLengthLeft(7); //最初の6桁は発行者識別番号(カード会社、ブランド)なので残す
      ret = replace("1234-5678-9012-3456", rule);
      assertTrue(ret.matches("^1234-56[0-9]{2}-[0-9]{4}-[0-9]{4}"));
      rule.setUnmaksedLengthLeft(7); //最初の6桁は発行者識別番号(カード会社、ブランド)なので残す
      ret = replace("1234-5678-9012", rule);
      assertTrue(ret.matches("^1234-56[0-9]{2}-[0-9]{4}"));
      rule.setUnmaksedLengthLeft(7); //最初の6桁は発行者識別番号(カード会社、ブランド)なので残す
      ret = replace("12-3456-7890-1234", rule);
      assertTrue(ret.matches("^12-3456-[0-9]{4}-[0-9]{4}"));
      rule.setUnmaksedLengthLeft(6); //最初の6桁は発行者識別番号(カード会社、ブランド)なので残す
      ret = replace("12345678901234", rule);
      assertTrue(ret.matches("^123456[0-9]{8}"));
      rule.setUnmaksedLengthLeft(-4); //末尾4桁だけ置換する
      ret = replace("12345678901234", rule);
      assertTrue(ret.matches("^1234567890[0-9]{4}"));
    }

    @Test
    @DisplayName("インスタンスメソッドでも基本的には処理は同じ")
    void case90() throws Exception {
      assertEquals("あいう", execute("あいう", null));
      assertEquals(null, execute(null, rule));
      assertTrue(((String) execute("1234-5678-9012-3456", rule)).matches("^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}"));
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

    @Test
    @DisplayName("一貫性テスト")
    void case1() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);

      setConnection(mockConn);
      rule.setDeterministicReplace(true);
      rule.setUniqueId("HOGE");

      // モックの設定: 1回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String ret1 = (String) execute("090-1234-5678", rule);

      // モックの設定: 2回目は値がある想定なのでレコードあり、さっき登録した値を返す
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getString("output_val")).thenReturn(ret1);

      String ret2 = (String) execute((Object) "090-1234-5678", rule);

      // モックの設定: 3回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String ret3 = (String) execute((Object) "0120-123-4567", rule);
      String ret4 = (String) execute((Object) null, rule);
      assertEquals(ret1, ret2, String.format("[%s]<>[%s]はNG", ret1, ret2));
      assertFalse(ret1.equals(ret3), String.format("[%s]=[%s]はNG", ret1, ret3));
      assertEquals(null, ret4);
    }

    @Test
    @DisplayName("ユニーク性テスト")
    void case2() throws Exception {
      // モックの設定
      when(mockConn.isClosed()).thenReturn(false);
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.execute()).thenReturn(true);

      // モックの設定： 1回生成した値で呼ばれたらtrueで返す
      List<String> retList = new ArrayList<>();
      DataMask converter = spy(new RandomCardnoReplacer());
      when(converter.isExistsInUniqueList(anyString(), anyString())).thenAnswer(new Answer<Boolean>() {
        public Boolean answer(InvocationOnMock invocation) {
          Object[] args = invocation.getArguments();
          return Boolean.valueOf(retList.indexOf(args[1]) >= 0);
        }
      });

      converter.setConnection(mockConn);
      rule.setUniqueValue(true);
      rule.setUniqueId("HOGE");
      int count = 10; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = (String) converter.execute("01", rule);
        int idx = retList.indexOf(ret);
        assertFalse(idx >= 0, String.format("[%d回目:%s]は%d回目ですでに生成されているのでNG", i, ret, idx));
        retList.add(ret);
      }
      assertEquals(10, retList.size());
      for (int i = 0; i < count; i++) {
        // ユニークなので一通り存在するはず
        String key = String.format("%d%s", i, MaskingUtil.getLuhnDigit(Integer.toString(i) + "X"));
        assertTrue(retList.indexOf(key) >= 0);
        // 1回は必ず登場する
        verify(converter, atLeast(1)).isExistsInUniqueList("HOGE", key);
        // 16回以上同じ値で呼ばれていたら偏りすぎ
        verify(converter, atMost(15)).isExistsInUniqueList("HOGE", key);
      }
    }

    @Test
    @DisplayName("一貫性NGテスト")
    void case3() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      // insert時にexecuteで重複エラーを発生させる
      doThrow(new SQLIntegrityConstraintViolationException()).when(mockPreparedStmnt).execute();
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.FALSE);

      try {
        setConnection(mockConn);
        rule.setDeterministicReplace(true);
        rule.setUniqueId("HOGE");
        String ret = (String) execute("090-1234-5678", rule);
        fail(String.format("%s が NGにならなかった", ret));
      } catch(SQLIntegrityConstraintViolationException e) {
        assertEquals("5回重複してユニークリストの登録に失敗しました。", e.getMessage());
      } finally {
        mockConn.close();
      }
    }

  }

}
