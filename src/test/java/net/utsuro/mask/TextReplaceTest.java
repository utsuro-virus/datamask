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
class TextReplaceTest extends TextReplace {

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
    @DisplayName("正規表現未指定はそのまま返る")
    void case3() throws Exception {
      rule.setTextReplaceRegex("");
      assertEquals("あいう", execute("あいう", rule));
      rule.setTextReplaceRegex(null);
      assertEquals("あいう", execute("あいう", rule));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case4() throws Exception {
      rule.setTextReplaceRegex("a");
      assertEquals("", execute("", rule));
    }

    @Test
    @DisplayName("文字列置換する")
    void case10() throws Exception {
      rule.setTextReplaceRegex("いあ");
      rule.setTextReplacement("イ");
      assertEquals("イイい", execute("いあいあい", rule));
    }

    @Test
    @DisplayName("置換文字列nullは空文字扱いで指定文字の除去をする")
    void case11() throws Exception {
      rule.setTextReplaceRegex("いあ");
      rule.setTextReplacement(null);
      assertEquals("い", execute("いあいあい", rule));
    }

  }

}
