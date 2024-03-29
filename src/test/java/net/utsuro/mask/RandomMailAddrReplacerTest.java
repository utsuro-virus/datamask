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
import java.time.LocalDate;
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
class RandomMailAddrReplacerTest extends RandomMailAddrReplacer {

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
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case4() throws Exception {
      rule.setIgnoreValue("@example.com$");
      assertEquals("hoge@example.com", replace("hoge@example.com", rule));
      assertNotEquals("hoge@example.co.jp", replace("hoge@example.co.jp", rule));
      assertEquals("fuga@example.com", replace("fuga@example.com", rule));
    }

    @Test
    @DisplayName("生成しない文字を指定")
    void case5() throws Exception {
      int count = 500; //試行回数
      rule.setRandomNoGenChar("[a-f]");
      for (int i = 0; i < count; i++) {
        String ret = replace("a@example.com", rule);
        assertTrue(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$"));
        assertFalse(ret.matches("[a-f]"));
      }
    }

    @Test
    @DisplayName("文字列以外はそのまま返る")
    void case6() throws Exception {
      LocalDate val = LocalDate.now();
      assertEquals(val, execute(val, rule));
    }

    @Test
    @DisplayName("ランダム生成メールアドレス")
    void case10() throws Exception {
      String ret;
      String val;
      val = "hoge-fuga@example.com";
      ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertEquals(val.length(), ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$"), ret);
      val = "fugafuga@example.co.jp";
      ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertEquals(val.length(), ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$"));
    }

    @Test
    @DisplayName("強制置換あり時、トップレベルドメインより元の値の長さが短い場合はユーザー名は1文字")
    void case11() throws Exception {
      String val = "x";
      rule.setInvalidMailAddressReplace(true);
      String ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertTrue(val.length() < ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$"), ret);
    }

    @Test
    @DisplayName("強制置換あり時、ドメイン名が選択されたトップレベルドメインより短い場合はドメイン名は1文字")
    void case12() throws Exception {
      String val = "xyyy@a";
      rule.setInvalidMailAddressReplace(true);
      String ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertTrue(val.length() < ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@[a-zA-Z0-9]\\.([a-zA-Z0-9]+\\.)*[a-zA-Z]{2,}$"), ret);
    }

    @Test
    @DisplayName("強制置換なし時、メールアドレス形式でない場合はただのランダム文字列置換")
    void case13() throws Exception {
      String val = "x";
      String ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertEquals(val.length(), ret.length());
      assertFalse(ret.matches("^[a-zA-Z0-9_+-]@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$"), ret);
    }

    @Test
    @DisplayName("強制置換なし時、メールアドレス形式でない場合はただのランダム文字列置換")
    void case14() throws Exception {
      String val = "090-1234-5678.";
      String ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertEquals(val.length(), ret.length());
      System.out.println(ret);
      assertFalse(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@[a-zA-Z0-9]\\.([a-zA-Z0-9]+\\.)*[a-zA-Z]{2,}$"), ret);
    }

    @Test
    @DisplayName("ドメイン部分は指定してランダム生成メールアドレス")
    void case20() throws Exception {
      String ret;
      String val;
      rule.setDomainReplacement("example.com");
      val = "hoge@hoghoghogee.com";
      ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertEquals(val.length(), ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@example\\.com$"));
      val = "fugafuga@example.co.jp";
      ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertEquals(val.length(), ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@example\\.com$"));
    }

    @Test
    @DisplayName("指定ドメインより元の値の長さが短い場合はユーザー名は1文字")
    void case21() throws Exception {
      rule.setDomainReplacement("@example.net");
      String val = "x@a.com";
      String ret = replace(val, rule);
      assertNotEquals(val, ret);
      assertTrue(val.length() < ret.length());
      assertTrue(ret.matches("^[a-zA-Z0-9_+-]@example\\.net$"), String.format("%sはNG", ret));
    }

    @Test
    @DisplayName("インスタンスメソッドでも基本的には処理は同じ")
    void case90() throws Exception {
      assertEquals("あいう", execute("あいう", null));
      assertEquals(null, execute(null, rule));
      assertTrue(((String) execute("hoge@example.com", rule))
          .matches("^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}$"));
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

      String ret1 = (String) execute("hoge@example.com", rule);

      // モックの設定: 2回目は値がある想定なのでレコードあり、さっき登録した値を返す
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getString("output_val")).thenReturn(ret1);

      String ret2 = (String) execute((Object) "hoge@example.com", rule);

      // モックの設定: 3回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String ret3 = (String) execute((Object) "fuga@example.com", rule);
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
      DataMask converter = spy(new RandomMailAddrReplacer());
      when(converter.isExistsInUniqueList(anyString(), anyString())).thenAnswer(new Answer<Boolean>() {
        public Boolean answer(InvocationOnMock invocation) {
          Object[] args = invocation.getArguments();
          return Boolean.valueOf(retList.indexOf(args[1]) >= 0);
        }
      });

      converter.setConnection(mockConn);
      rule.setUniqueValue(true);
      rule.setUniqueId("HOGE");
      int count = 26; //試行回数
      rule.setDomainReplacement("@example.com");
      for (int i = 0; i < count; i++) {
        String ret = (String) converter.execute("h@example.com", rule);
        int idx = retList.indexOf(ret);
        assertFalse(idx >= 0, String.format("[%d回目:%s]は%d回目ですでに生成されているのでNG", i, ret, idx));
        retList.add(ret);
      }
      assertEquals(26, retList.size());
      String[] exp = new String[] {"a", "b", "c", "d", "e", "f", "g",
          "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
          "v", "w", "x", "y", "z"};
      for (int i = 0; i < count; i++) {
        // ユニークなので一通り存在するはず
        String key = exp[i].concat("@example.com");
        assertTrue(retList.indexOf(key) >= 0);
        // 1回は必ず登場する
        verify(converter, atLeast(1)).isExistsInUniqueList("HOGE", key);
        // 20回以上同じ値で呼ばれていたら偏りすぎ
        verify(converter, atMost(20)).isExistsInUniqueList("HOGE", key);
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
        String ret = (String) execute("hogefuga@example.com", rule);
        fail(String.format("%s が NGにならなかった", ret));
      } catch(SQLIntegrityConstraintViolationException e) {
        assertEquals("5回重複してユニークリストの登録に失敗しました。", e.getMessage());
      } finally {
        mockConn.close();
      }
    }

  }

}
