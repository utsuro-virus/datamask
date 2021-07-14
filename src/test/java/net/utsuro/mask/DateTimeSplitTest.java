package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateTimeSplitTest extends DateTimeSplit {

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
    @DisplayName("変換先の型が2つ指定していない場合はエラー")
    void case3() throws Exception {
      try {
        execute("20120103", rule);
        fail("変換先の型が2つ指定されていないのに NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("2つの型変換先が指定されていません。", e.getMessage());
      }
      try {
        List<String> toClazz = new ArrayList<>();
        toClazz.add(String.class.getName());
        rule.setToClassNames(toClazz);
        execute("20120103", rule);
        fail("変換先の型が2つ指定されていないのに NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("2つの型変換先が指定されていません。", e.getMessage());
      }
    }

    @Test
    @DisplayName("日付と時刻の配列が返る")
    void case4() throws Exception {
      List<String> toClazz = new ArrayList<>();
      toClazz.add(Long.class.getName());
      toClazz.add(String.class.getName());
      rule.setToClassNames(toClazz);
      Object[] ret = (Object[]) execute("2021/01/03 04:38:59", rule);
      assertEquals(2, ret.length);
      assertEquals(Long.class, ret[0].getClass());
      assertEquals(String.class, ret[1].getClass());
      assertEquals(20210103L, ret[0]);
      assertEquals("04:38:59", ret[1]);
    }
  }

}
