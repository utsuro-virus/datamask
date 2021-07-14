package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateTimeConcatTest extends DateTimeConcat {

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
    @DisplayName("要素数2の配列で無い場合はエラー")
    void case3() throws Exception {
      try {
        execute("20120103", rule);
        fail("配列でない値 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付と時刻からなる要素数2の配列が必要です。", e.getMessage());
      }
      try {
        execute(new String[] {"20120103"}, rule);
        fail("要素数2以外の配列 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付と時刻からなる要素数2の配列が必要です。", e.getMessage());
      }
    }

    @Test
    @DisplayName("配列を渡すと結合する")
    void case4() throws Exception {
      rule.setToClassName(LocalDateTime.class.getName());
      assertEquals(LocalDateTime.parse("2021-01-03T12:34:59"), execute(new String[] {"20210103", "123459"}, rule));
      assertEquals(LocalDateTime.parse("2021-01-03T02:34:59"), execute(new Integer[] {20210103, 23459}, rule));
    }
  }
}
