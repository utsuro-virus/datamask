package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShuffleTextReplacerTest extends ShuffleTextReplacer {

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
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case4() throws Exception {
      rule.setIgnoreValue("ab.+f.$");
      assertEquals("aBcdefG", execute("aBcdefG", rule));
      assertNotEquals("aBcdef", execute("aBcdef", rule));
    }

    @Test
    @DisplayName("シャッフルする")
    void case10() throws Exception {
      int count = 1000;
      Map<String, Integer> retCount = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String ret = (String) execute("あいう", rule);
        if (!retCount.containsKey(ret)) {
          retCount.put(ret, 0);
        }
        retCount.put(ret, retCount.get(ret) + 1);
        assertEquals(3, ret.length());
        assertTrue(ret.indexOf("あ") >= 0);
        assertTrue(ret.indexOf("い") >= 0);
        assertTrue(ret.indexOf("う") >= 0);
      }
      // 組み合わせ数
      assertEquals(3 * 2 * 1, retCount.size());
      // 片寄りチェック
      for (String key : retCount.keySet()) {
        assertTrue(retCount.get(key) <= count * 0.3);
      }
    }

  }

}
