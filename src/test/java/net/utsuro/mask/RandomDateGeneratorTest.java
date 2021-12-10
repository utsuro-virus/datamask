package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomDateGeneratorTest extends RandomDateGenerator {

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
  @DisplayName("method: generate")
  class Generate {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      assertEquals(LocalDateTime.parse("2021-01-17T18:55:01"),
          generate(LocalDateTime.parse("2021-01-17T18:55:01"), null));
    }

    @Test
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, generate(null, rule));
    }

    @Test
    @DisplayName("null置換ありなら処理されて返る")
    void case3() throws Exception {
      rule.setNullReplace(true);
      Object ret = generate(null, rule);
      assertNotEquals(null, ret);
      assertEquals(LocalDateTime.class, ret.getClass());
      ret = generate(LocalDateTime.parse("2021-01-17T18:55:01"), rule);
      assertNotEquals(LocalDateTime.parse("2021-01-17T18:55:01"), ret);
      rule.setTermFrom("-10D");
      rule.setTermTo("10D");
      ret = generate(null, rule);
      assertNotEquals(null, ret);
      assertEquals(LocalDateTime.class, ret.getClass());
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case4() throws Exception {
      rule.setIgnoreValue("^202[0-9]");
      assertEquals(LocalDateTime.parse("2020-01-17T18:55:01"), execute(LocalDateTime.parse("2020-01-17T18:55:01"), rule));
      assertEquals(LocalDateTime.parse("2021-01-17T18:55:01"), execute(LocalDateTime.parse("2021-01-17T18:55:01"), rule));
      assertNotEquals(LocalDateTime.parse("1989-01-17T18:55:01"), execute(LocalDateTime.parse("1989-01-17T18:55:01"), rule));
      assertNotEquals(LocalDateTime.parse("2019-01-17T18:55:01"), execute(LocalDateTime.parse("2019-01-17T18:55:01"), rule));
    }

    @Test
    @DisplayName("最小値を指定した場合はそれ以上の値で生成")
    void case10() throws Exception {
      LocalDateTime min = LocalDateTime.parse("1907-01-01T00:00:00");
      rule.setMinDate(min);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(min.compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, min));
      }
    }

    @Test
    @DisplayName("最大値を指定した場合はそれ以下の値で生成")
    void case11() throws Exception {
      LocalDateTime max = LocalDateTime.parse("1921-12-31T00:00:00");
      rule.setMaxDate(max);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(max.compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, max));
      }
    }

    @Test
    @DisplayName("最小最大両方を指定した場合は範囲内の値で生成")
    void case12() throws Exception {
      LocalDateTime min = LocalDateTime.parse("1907-01-01T00:00:00");
      LocalDateTime max = LocalDateTime.parse("1921-12-31T00:00:00");
      rule.setMinDate(min);
      rule.setMaxDate(max);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(min.compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, min));
        assertTrue(max.compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, max));
      }
    }

    @Test
    @DisplayName("期間FROMを指定した場合はそれ以上の値で生成")
    void case20() throws Exception {
      LocalDateTime min = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusMonths(-10);
      rule.setTermFrom("-10M");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(min.compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, min));
      }
    }

    @Test
    @DisplayName("期間TOを指定した場合はそれ以下の値で生成")
    void case21() throws Exception {
      LocalDateTime max = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusYears(10);
      rule.setTermTo("10Y");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(max.compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, max));
      }
    }

    @Test
    @DisplayName("期間FROM/TO両方を指定した場合は範囲内の値で生成")
    void case22() throws Exception {
      // 日数指定
      LocalDateTime min = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(-10);
      LocalDateTime max = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(10);
      rule.setTermFrom("-10D");
      rule.setTermTo("10D");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(min.compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, min));
        assertTrue(max.plusDays(10).compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, max));
      }
      // 月数指定
      min = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusMonths(-20);
      max = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusMonths(-10);
      rule.setTermFrom("-20M");
      rule.setTermTo("-10M");
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(min.compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, min));
        assertTrue(max.plusDays(10).compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, max));
      }
      // 年数指定
      min = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusYears(10);
      max = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusYears(20);
      rule.setTermFrom("10Y");
      rule.setTermTo("20Y");
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(min.compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, min));
        assertTrue(max.plusDays(10).compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, max));
      }
    }

    @Test
    @DisplayName("期間指定なしは4桁年の範囲内の値で生成")
    void case23() throws Exception {
      rule.setTermFrom("");
      rule.setTermTo("");
      int count = 10; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(LocalDateTime.parse("0000-01-01T00:00:00").compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, LocalDateTime.now().plusDays(-10)));
        assertTrue(LocalDateTime.parse("9999-12-31T23:59:59").compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, LocalDateTime.now().plusDays(10)));
      }
      rule.setTermFrom(null);
      rule.setTermTo(null);
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(LocalDateTime.parse("0000-01-01T00:00:00").compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, LocalDateTime.now().plusDays(-10)));
        assertTrue(LocalDateTime.parse("9999-12-31T23:59:59").compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, LocalDateTime.now().plusDays(10)));
      }
      rule.setTermFrom("hoge");
      rule.setTermTo("fuga");
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = (LocalDateTime) execute(LocalDateTime.now(), rule);
        assertTrue(LocalDateTime.parse("0000-01-01T00:00:00").compareTo(ret) <= 0,
            String.format("%sは%s以上でないのでNG", ret, LocalDateTime.now().plusDays(-10)));
        assertTrue(LocalDateTime.parse("9999-12-31T23:59:59").compareTo(ret) >= 0,
            String.format("%sは%s以下でないのでNG", ret, LocalDateTime.now().plusDays(10)));
      }
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
    @DisplayName("null置換ありなら処理されて返る")
    void case3() throws Exception {
      rule.setNullReplace(true);
      Object ret = execute(null, rule);
      assertNotEquals(null, ret);
      assertEquals(LocalDateTime.class, ret.getClass());
    }

    @Test
    @DisplayName("LocalDateTimeに型変換できない場合はそのまま返る")
    void case4() throws Exception {
      assertEquals("あいう", execute("あいう", rule));
      assertNotEquals("2021/01/17", execute("2021/01/17", rule));
      int[] arr = new int[] {0, 1, 2};
      assertEquals(arr, execute(arr , rule));
      assertEquals(19309999, execute(19309999, rule));
    }

    @Test
    @DisplayName("LocalDateTimeに型変換できない場合も置換指定ありなら生成されて返る")
    void case5() throws Exception {
      rule.setInvalidDateReplace(true);
      assertNotEquals("あいう", execute("あいう", rule));
      assertNotEquals("2021/01/17", execute("2021/01/17", rule));
      int[] arr = new int[] {0, 1, 2};
      assertNotEquals(arr, execute(arr , rule));
      assertNotEquals(19309999, execute(19309999, rule));
    }


    @Test
    @DisplayName("LocalDateTimeに型変換できる場合は処理されて返る")
    void case10() throws Exception {
      LocalDateTime ret;
      String val;
      val = "20210117";
      ret = (LocalDateTime) execute(Long.valueOf(val), rule);
      assertNotEquals(val, ret);
      val = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).toString();
      ret = (LocalDateTime) execute(val, rule);
      assertNotEquals(val, ret);
      ret = (LocalDateTime) execute(LocalDateTime.parse(val), rule);
      assertNotEquals(val, ret);
    }

  }

}
