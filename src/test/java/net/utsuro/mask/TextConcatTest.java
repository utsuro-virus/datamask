package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TextConcatTest extends TextConcat {

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
    @DisplayName("配列でない場合はそのまま返る")
    void case3() throws Exception {
      assertEquals("あいう", execute("あいう", rule));
    }

    @Test
    @DisplayName("配列を渡すと結合する")
    void case4() throws Exception {
      assertEquals("1abcx", execute(new String[] {"1", "abc", "x"}, rule));
    }

    @Test
    @DisplayName("セパレータを指定すると区切りありで結合する")
    void case5() throws Exception {
      rule.setSeparator(";");
      assertEquals("1;abc;x", execute(new String[] {"1", "abc", "x"}, rule));
    }

    @Test
    @DisplayName("変換ルールを渡すと指定書式等で変換したあと結合する")
    void case6() throws Exception {
      rule.setSeparator(";");
      rule.setDateTimeFormat("HHmmss");
      assertEquals("001234;045607;182900", execute(new LocalTime[] {
          LocalTime.parse("00:12:34"), LocalTime.parse("04:56:07"),
          LocalTime.parse("18:29:00")}, rule));
    }

  }

}
