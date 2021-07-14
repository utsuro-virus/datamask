package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NullToValueTest extends NullToValue {

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

    @Test
    @DisplayName("DBは使用しないのでnullが返る")
    void case1() throws Exception {
      assertEquals(null, getConnection());
    }

  }

  @Nested
  @DisplayName("method: setConnection")
  class SetConnection {

    @Mock
    Connection mockConn;

    @Test
    @DisplayName("DBは使用しないのでセットしてもnullが返る")
    void case1() throws Exception {
      setConnection(mockConn);
      assertEquals(null, getConnection());
    }

  }

  @Nested
  @DisplayName("method: execute")
  class Execute {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      assertEquals("あいう", execute("あいう", null));
    }

    @Test
    @DisplayName("null以外はそのまま返る(文字列)")
    void case2() throws Exception {
      assertEquals("hoge", execute("hoge", rule));
    }

    @Test
    @DisplayName("null以外はそのまま返る(文字列以外)")
    void case3() throws Exception {
      assertEquals(123L, execute(123L, rule));
    }

    @Test
    @DisplayName("変換先が未指定場合はエラー")
    void case4() throws Exception {
      try {
        rule.setToClassName(null);
        execute(null, rule);
        fail("変換先未指定 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("置換先型名が指定されていません。", e.getMessage());
      }
      try {
        rule.setToClassName("");
        execute(null, rule);
        fail("変換先未指定 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("置換先型名が指定されていません。", e.getMessage());
      }
    }

    @Test
    @DisplayName("nullなら固定値が返る")
    void case5() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.setFixedValue("hoge");
      assertEquals("hoge", execute(null, rule));

      rule.setToClassName(Long.class.getName());
      rule.setFixedValue("123");
      assertEquals(123L, execute(null, rule));
    }

    @Test
    @DisplayName("空文字でも固定値が返る")
    void case6() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.setFixedValue("hoge");
      assertEquals("hoge", execute("", rule));
    }

  }

}
