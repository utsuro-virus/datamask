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
class TextSubstrTest extends TextSubstr {

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
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, execute(null, rule));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() throws Exception {
      assertEquals("", execute("", rule));
    }

    @Test
    @DisplayName("開始インデックスから終了インデックス-1までの文字が返る")
    void case4() throws Exception {
      rule.setBeginIndex(1);
      rule.setEndIndex(3);
      assertEquals("23", execute("123456", rule));
    }

    @Test
    @DisplayName("終了インデックス超過は末尾までの文字が返る")
    void case5() throws Exception {
      rule.setBeginIndex(1);
      rule.setEndIndex(10);
      assertEquals("23456", execute("123456", rule));
    }

    @Test
    @DisplayName("終了インデックス省略時は末尾までの文字が返る")
    void case6() throws Exception {
      rule.setBeginIndex(2);
      assertEquals("3456", execute("123456", rule));
    }

    @Test
    @DisplayName("開始インデックスに負数指定時は末尾からの文字が返る")
    void case7() throws Exception {
      rule.setBeginIndex(-2);
      assertEquals("56", execute("123456", rule));
    }

  }

}
