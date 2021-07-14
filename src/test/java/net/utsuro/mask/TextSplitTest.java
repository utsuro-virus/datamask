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
class TextSplitTest extends TextSplit {

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
    @DisplayName("セパレータで区切られた配列が返る")
    void case4() throws Exception {
      rule.setSeparator(",");
      String[] ret = (String[]) execute("12,34,56", rule);
      assertEquals(3, ret.length);
      assertEquals("12", ret[0]);
      assertEquals("34", ret[1]);
      assertEquals("56", ret[2]);
    }

    @Test
    @DisplayName("セパレータで区切られた配列が返る(空文字含む)")
    void case5() throws Exception {
      rule.setSeparator(";");
      String[] ret = (String[]) execute("12;;34,56;", rule);
      assertEquals(4, ret.length);
      assertEquals("12", ret[0]);
      assertEquals("", ret[1]);
      assertEquals("34,56", ret[2]);
      assertEquals("", ret[3]);
    }

    @Test
    @DisplayName("セパレータがnullの場合は空文字扱いになって1文字ずつ分割した配列が返る")
    void case6() throws Exception {
      rule.setSeparator(null);
      String[] ret = (String[]) execute("123", rule);
      assertEquals(4, ret.length);
      assertEquals("1", ret[0]);
      assertEquals("2", ret[1]);
      assertEquals("3", ret[2]);
    }

  }

}
