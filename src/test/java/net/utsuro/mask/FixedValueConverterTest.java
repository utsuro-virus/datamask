package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FixedValueConverterTest extends FixedValueConverter {

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
    @DisplayName("変換先が未指定場合はエラー")
    void case3() throws Exception {
      try {
        rule.setToClassName(null);
        execute("アイウ", rule);
        fail("変換先未指定 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("置換先型名が指定されていません。", e.getMessage());
      }
      try {
        rule.setToClassName("");
        execute("アイウ", rule);
        fail("変換先未指定 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("置換先型名が指定されていません。", e.getMessage());
      }
    }

    @Test
    @DisplayName("null置換ありなら固定値が返る")
    void case4() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.setNullReplace(true);
      rule.setFixedValue("hoge");
      assertEquals("hoge", execute(null, rule));
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case5() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.setIgnoreValue("ab.+f.$");
      rule.setFixedValue("hoge");
      assertEquals("aBcdefG", execute("aBcdefG", rule));
      assertEquals("hoge", execute("aBcdefGA", rule));
      assertEquals("hoge", execute("a", rule));
      assertEquals("hoge", execute("あいうえ", rule));
    }

    @Test
    @DisplayName("%sysdateでシステム日付をセット")
    void case6() throws Exception {
      rule.setToClassName(LocalDate.class.getName());
      rule.setFixedValue("%sysdate");
      assertEquals(LocalDate.now(), execute("aBcdefG", rule));
    }

    @Test
    @DisplayName("%systimestampでシステム日時をセット")
    void case7() throws Exception {
      rule.setToClassName(LocalDateTime.class.getName());
      rule.setFixedValue("%systimestamp");
      assertEquals(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), execute("aBcdefG", rule));
    }

  }

}
