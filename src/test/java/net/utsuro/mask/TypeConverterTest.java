package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.utsuro.mask.MaskingUtil.CharType;

@ExtendWith(MockitoExtension.class)
class TypeConverterTest extends TypeConverter {

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
  @DisplayName("method: isDateTimeClass")
  class IsDateTimeClass {

    @Test
    @DisplayName("日付系のクラスの場合Trueが返る")
    void case1() throws Exception {
      assertTrue(isDateTimeClass(java.util.Date.class));
      assertTrue(isDateTimeClass(java.sql.Date.class));
      assertTrue(isDateTimeClass(java.sql.Time.class));
      assertTrue(isDateTimeClass(java.sql.Timestamp.class));
      assertTrue(isDateTimeClass(java.time.LocalDate.class));
      assertTrue(isDateTimeClass(java.time.LocalDateTime.class));
      assertTrue(isDateTimeClass(java.time.LocalTime.class));
    }

    @Test
    @DisplayName("日付系のクラス以外の場合Falseが返る")
    void case2() throws Exception {
      assertFalse(isDateTimeClass(String.class));
      assertFalse(isDateTimeClass(int.class));
      assertFalse(isDateTimeClass(Long.class));
      assertFalse(isDateTimeClass(BigDecimal.class));
      assertFalse(isDateTimeClass(Object.class));
    }

  }

  @Nested
  @DisplayName("method: isTimeClass")
  class IsTimeClass {

    @Test
    @DisplayName("時刻系のクラスの場合Trueが返る")
    void case1() throws Exception {
      assertTrue(isTimeClass(java.sql.Time.class));
      assertTrue(isTimeClass(java.time.LocalTime.class));
    }

    @Test
    @DisplayName("時刻系のクラス以外の場合Falseが返る")
    void case2() throws Exception {
      assertFalse(isTimeClass(java.sql.Date.class));
      assertFalse(isTimeClass(java.util.Date.class));
      assertFalse(isTimeClass(java.sql.Timestamp.class));
      assertFalse(isTimeClass(LocalDate.class));
      assertFalse(isTimeClass(LocalDateTime.class));
      assertFalse(isTimeClass(String.class));
      assertFalse(isTimeClass(int.class));
      assertFalse(isTimeClass(Long.class));
      assertFalse(isTimeClass(BigDecimal.class));
      assertFalse(isTimeClass(Object.class));
    }

  }

  @Nested
  @DisplayName("method: isNumClass")
  class IsNumClass {

    @Test
    @DisplayName("数値系のクラスの場合Trueが返る")
    void case1() throws Exception {
      assertTrue(isNumClass(Long.class));
      assertTrue(isNumClass(Integer.class));
      assertTrue(isNumClass(BigInteger.class));
      assertTrue(isNumClass(BigDecimal.class));
    }

    @Test
    @DisplayName("数値系のクラス以外の場合Falseが返る")
    void case2() throws Exception {
      assertFalse(isNumClass(String.class));
      assertFalse(isNumClass(int.class));
      assertFalse(isNumClass(java.sql.Date.class));
      assertFalse(isNumClass(java.sql.Timestamp.class));
      assertFalse(isNumClass(Object.class));
    }

  }

  @Nested
  @DisplayName("method: toDateTime")
  class ToDateTime {

    @Test
    @DisplayName("nullの場合nullが返る")
    void case1() throws Exception {
      assertEquals(null, toDateTime(null, java.sql.Date.class));
    }

    @Test
    @DisplayName("空文字の場合空文字が返る")
    void case2() throws Exception {
      assertEquals("", toDateTime("", java.sql.Date.class));
    }

    @Test
    @DisplayName("変換元が配列の場合はエラー")
    void case3() throws Exception {
      try {
        String[] arr = new String[] {"1", "2", "3"};
        toDateTime(arr, java.sql.Date.class);
        fail("配列 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付型変換 String[] → Date は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("変換元が非対応オブジェクトの場合はエラー")
    void case4() throws Exception {
      try {
        StringBuilder val = new StringBuilder("2021-01-03");
        toDateTime(val, java.sql.Date.class);
        fail("非対応オブジェクト が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付型変換 StringBuilder → Date は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("変換先が非対応クラスの場合はエラー")
    void case5() throws Exception {
      try {
        toDateTime("2021-01-03", String.class);
        fail("非対応オブジェクト が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付型変換 String → String は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("書式が解析できない場合はエラー")
    void case6() throws Exception {
      MaskingRule rule = new MaskingRule();
      rule.setDateTimeFormat("hoge");
      try {
        toDateTime("2021-01-03", LocalDate.class, rule);
        fail("非対応書式 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertTrue(e.getMessage().startsWith("Unknown pattern"));
      }
    }

    @Test
    @DisplayName("指定書式で解析できない場合はエラー")
    void case7() throws Exception {
      MaskingRule rule = new MaskingRule();
      rule.setDateTimeFormat("mmyydd");
      try {
        toDateTime("2021-01-03", LocalDate.class, rule);
        fail("非対応書式 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("'2021-01-03'が現在の日付書式で解析できません。", e.getMessage());
      }
    }

    @Test
    @DisplayName("LocalDateTimeへ変換")
    void case10() throws Exception {
      Object ret;
      LocalDateTime exp = LocalDateTime.parse("2021-01-03T00:00:00");

      ret = toDateTime(exp, LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(20210103, LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(20210103L, LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(BigInteger.valueOf(20210103), LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(BigDecimal.valueOf(20210103), LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(LocalDate.parse("2021-01-03"), LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-03"), LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(java.sql.Date.valueOf("2021-01-03"), LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(java.sql.Timestamp.valueOf("2021-01-03 00:00:00"), LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("20210103", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021-01-03", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021.01.03", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/1/3", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021-01-03 00:00", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 00:00:00", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021.01.03 00:00:00", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021年1月3日 0:0:0", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021-01-03T00:00:00", LocalDateTime.class);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

    }

    @Test
    @DisplayName("LocalDateへ変換")
    void case11() throws Exception {
      Object ret;
      LocalDate exp = LocalDate.parse("2021-01-03");

      ret = toDateTime(exp, LocalDate.class);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 00:00:00", LocalDate.class);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(2021, LocalDate.class);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(LocalDate.parse("2021-01-01"), ret);

      ret = toDateTime(202101, LocalDate.class);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(LocalDate.parse("2021-01-01"), ret);

      ret = toDateTime("2021/01", LocalDate.class);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(LocalDate.parse("2021-01-01"), ret);

    }

    @Test
    @DisplayName("java.sql.Dateへ変換")
    void case12() throws Exception {
      Object ret;
      java.sql.Date exp = java.sql.Date.valueOf("2021-01-03");

      ret = toDateTime(exp, java.sql.Date.class);
      assertEquals(java.sql.Date.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 00:00:00", java.sql.Date.class);
      assertEquals(java.sql.Date.class, ret.getClass());
      assertEquals(exp, ret);
    }

    @Test
    @DisplayName("java.sql.Timeへ変換")
    void case13() throws Exception {
      Object ret;
      java.sql.Time exp = java.sql.Time.valueOf("23:42:59");

      ret = toDateTime(exp, java.sql.Time.class);
      assertEquals(java.sql.Time.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 23:42:59", java.sql.Time.class);
      assertEquals(java.sql.Time.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("23:42:59", java.sql.Time.class);
      assertEquals(java.sql.Time.class, ret.getClass());
      assertEquals(exp, ret);
    }

    @Test
    @DisplayName("java.sql.Timestampへ変換")
    void case14() throws Exception {
      Object ret;
      java.sql.Timestamp exp = java.sql.Timestamp.valueOf("2021-01-03 00:00:00");

      ret = toDateTime(exp, java.sql.Timestamp.class);
      assertEquals(java.sql.Timestamp.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 00:00:00", java.sql.Timestamp.class);
      assertEquals(java.sql.Timestamp.class, ret.getClass());
      assertEquals(exp, ret);
    }

    @Test
    @DisplayName("java.util.Dateへ変換")
    void case15() throws Exception {
      Object ret;
      java.util.Date exp = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-03");

      ret = toDateTime(exp, java.util.Date.class);
      assertEquals(java.util.Date.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 00:00:00", java.util.Date.class);
      assertEquals(java.util.Date.class, ret.getClass());
      assertEquals(exp, ret);
    }

    @Test
    @DisplayName("LocalTimeへ変換")
    void case16() throws Exception {
      Object ret;
      LocalTime exp = LocalTime.parse("20:42:59");

      ret = toDateTime(exp, LocalTime.class);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(204259L, LocalTime.class);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(204259, LocalTime.class);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2021/01/03 20:42:59", LocalTime.class);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(4259, LocalTime.class);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(LocalTime.parse("00:42:59"), ret);

      ret = toDateTime("20:42", LocalTime.class);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(LocalTime.parse("20:42:00"), ret);
    }

    @Test
    @DisplayName("LocalDateTimeへ変換(書式指定あり)")
    void case17() throws Exception {
      MaskingRule rule = new MaskingRule();
      Object ret;
      LocalDateTime exp = LocalDateTime.parse("2021-01-01T00:00:00");

      rule.setDateTimeFormat(null);
      ret = toDateTime(20210101000000L, LocalDateTime.class, rule);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("");
      ret = toDateTime(20210101, LocalDateTime.class, rule);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("yyyyMMddHHmmss");
      ret = toDateTime(20210101000000L, LocalDateTime.class, rule);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("20210101000000", LocalDateTime.class, rule);
      assertEquals(LocalDateTime.class, ret.getClass());
      assertEquals(exp, ret);

    }

    @Test
    @DisplayName("LocalDateへ変換(書式指定あり)")
    void case18() throws Exception {
      MaskingRule rule = new MaskingRule();
      Object ret;
      LocalDate exp = LocalDate.parse("2021-01-03");

      rule.setDateTimeFormat(null);
      ret = toDateTime("2021-01-03", LocalDate.class, rule);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("");
      ret = toDateTime("2021-01-03", LocalDate.class, rule);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("yyyyMMdd");
      ret = toDateTime(20210103L, LocalDate.class, rule);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("20210103", LocalDate.class, rule);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("yyyyMM");
      ret = toDateTime(202101L, LocalDate.class, rule);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(LocalDate.parse("2021-01-01"), ret);

      rule.setDateTimeFormat("GGGGy年M月d日");
      ret = toDateTime("令和3年01月03日", LocalDate.class, rule);
      assertEquals(LocalDate.class, ret.getClass());
      assertEquals(exp, ret);
    }

    @Test
    @DisplayName("LocalTimeへ変換(書式指定あり)")
    void case19() throws Exception {
      MaskingRule rule = new MaskingRule();
      Object ret;
      LocalTime exp = LocalTime.parse("20:42:00");

      rule.setDateTimeFormat(null);
      ret = toDateTime(204200L, LocalTime.class, rule);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("");
      ret = toDateTime(204200L, LocalTime.class, rule);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      rule.setDateTimeFormat("HHmm");
      ret = toDateTime(2042L, LocalTime.class, rule);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime(2042, LocalTime.class, rule);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);

      ret = toDateTime("2042", LocalTime.class, rule);
      assertEquals(LocalTime.class, ret.getClass());
      assertEquals(exp, ret);
    }

  }

  @Nested
  @DisplayName("method: fromDateTime")
  class FromDateTime {

    @Test
    @DisplayName("nullの場合nullが返る")
    void case1() throws Exception {
      assertEquals(null, fromDateTime(null, java.sql.Date.class));
    }

    @Test
    @DisplayName("変換元が配列の場合はエラー")
    void case2() throws Exception {
      try {
        String[] arr = new String[] {"1", "2", "3"};
        fromDateTime(arr, java.sql.Date.class);
        fail("配列 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付型変換 String[] → Date は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("変換元が非対応オブジェクトの場合はエラー")
    void case3() throws Exception {
      try {
        StringBuilder val = new StringBuilder("2021-01-03");
        fromDateTime(val, java.sql.Date.class);
        fail("非対応オブジェクト が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付型変換 StringBuilder → Date は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("変換先が非対応クラスの場合はエラー")
    void case4() throws Exception {
      try {
        fromDateTime(LocalDate.parse("2021-01-03"), StringBuilder.class);
        fail("非対応オブジェクト が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("日付型変換 LocalDate → StringBuilder は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("LocalDateTimeから変換")
    void case10() throws Exception {
      Object ret;
      LocalDateTime val = LocalDateTime.parse("2021-01-03T20:31:49");

      ret = fromDateTime(val, Long.class);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103203149L, ret);

      ret = fromDateTime(val, Integer.class);
      assertEquals(Integer.class, ret.getClass());
      assertEquals(20210103, ret);

      ret = fromDateTime(val, BigInteger.class);
      assertEquals(BigInteger.class, ret.getClass());
      assertEquals(BigInteger.valueOf(20210103203149L), ret);

      ret = fromDateTime(val, BigDecimal.class);
      assertEquals(BigDecimal.class, ret.getClass());
      assertEquals(BigDecimal.valueOf(20210103203149L), ret);

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("2021/01/03 20:31:49", ret);

    }

    @Test
    @DisplayName("LocalDateから変換")
    void case11() throws Exception {
      Object ret;
      LocalDate val = LocalDate.parse("2021-01-03");

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("2021/01/03", ret);

    }

    @Test
    @DisplayName("java.sql.Dateから変換")
    void case12() throws Exception {
      Object ret;
      java.sql.Date val = java.sql.Date.valueOf("2021-01-03");

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("2021/01/03", ret);

    }

    @Test
    @DisplayName("java.sql.Timeから変換")
    void case13() throws Exception {
      Object ret;
      java.sql.Time val = java.sql.Time.valueOf("23:42:59");

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("23:42:59", ret);

    }

    @Test
    @DisplayName("java.sql.Timestampから変換")
    void case14() throws Exception {
      Object ret;
      java.sql.Timestamp val = java.sql.Timestamp.valueOf("2021-01-03 20:31:49");

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("2021/01/03 20:31:49", ret);

    }

    @Test
    @DisplayName("java.util.Dateから変換")
    void case15() throws Exception {
      Object ret;
      java.util.Date val = new SimpleDateFormat("yyyy-MM-dd").parse("2021-01-03");

      ret = fromDateTime(val, Long.class);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103L, ret);

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("2021/01/03", ret);

    }

    @Test
    @DisplayName("LocalTimeから変換")
    void case16() throws Exception {
      Object ret;
      LocalTime val = LocalTime.parse("23:42:59");

      ret = fromDateTime(val, Long.class);
      assertEquals(Long.class, ret.getClass());
      assertEquals(234259L, ret);

      ret = fromDateTime(val, String.class);
      assertEquals(String.class, ret.getClass());
      assertEquals("23:42:59", ret);

    }

    @Test
    @DisplayName("LocalDateTimeから変換(書式指定あり)")
    void case17() throws Exception {
      MaskingRule rule = new MaskingRule();
      Object ret;
      LocalDateTime val = LocalDateTime.parse("2021-01-03T20:31:49");

      rule.setDateTimeFormat(null);
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103203149L, ret);

      rule.setDateTimeFormat("");
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103203149L, ret);

      rule.setDateTimeFormat("yyyyMMdd");
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103L, ret);

      ret = fromDateTime(val, Integer.class, rule);
      assertEquals(Integer.class, ret.getClass());
      assertEquals(20210103, ret);

      ret = fromDateTime(val, BigInteger.class, rule);
      assertEquals(BigInteger.class, ret.getClass());
      assertEquals(BigInteger.valueOf(20210103L), ret);

      ret = fromDateTime(val, BigDecimal.class, rule);
      assertEquals(BigDecimal.class, ret.getClass());
      assertEquals(BigDecimal.valueOf(20210103L), ret);

      ret = fromDateTime(val, String.class, rule);
      assertEquals(String.class, ret.getClass());
      assertEquals("20210103", ret);

    }

    @Test
    @DisplayName("LocalDateから変換(書式指定あり)")
    void case18() throws Exception {
      MaskingRule rule = new MaskingRule();
      Object ret;
      LocalDate val = LocalDate.parse("2021-01-03");

      rule.setDateTimeFormat(null);
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103L, ret);

      rule.setDateTimeFormat("");
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(20210103L, ret);

      rule.setDateTimeFormat("yyyyMM");
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(202101L, ret);

      ret = fromDateTime(val, Integer.class, rule);
      assertEquals(Integer.class, ret.getClass());
      assertEquals(202101, ret);

      ret = fromDateTime(val, BigInteger.class, rule);
      assertEquals(BigInteger.class, ret.getClass());
      assertEquals(BigInteger.valueOf(202101L), ret);

      ret = fromDateTime(val, BigDecimal.class, rule);
      assertEquals(BigDecimal.class, ret.getClass());
      assertEquals(BigDecimal.valueOf(202101L), ret);

      ret = fromDateTime(val, String.class, rule);
      assertEquals(String.class, ret.getClass());
      assertEquals("202101", ret);

      rule.setDateTimeFormat("GGGGy年M月d日");
      ret = fromDateTime(val, String.class, rule);
      assertEquals(String.class, ret.getClass());
      assertEquals("令和3年1月3日", ret);

      rule.setDateTimeFormat("GGGGGy.MM.dd");
      ret = fromDateTime(val, String.class, rule);
      assertEquals(String.class, ret.getClass());
      assertEquals("R3.01.03", ret);
    }

    @Test
    @DisplayName("LocalTimeから変換(書式指定あり)")
    void case19() throws Exception {
      MaskingRule rule = new MaskingRule();
      Object ret;
      LocalTime val = LocalTime.parse("20:31:49");

      rule.setDateTimeFormat(null);
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(203149L, ret);

      rule.setDateTimeFormat("");
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(203149L, ret);

      rule.setDateTimeFormat("HHmm");
      ret = fromDateTime(val, Long.class, rule);
      assertEquals(Long.class, ret.getClass());
      assertEquals(2031L, ret);

      ret = fromDateTime(val, Integer.class, rule);
      assertEquals(Integer.class, ret.getClass());
      assertEquals(2031, ret);

      ret = fromDateTime(val, BigInteger.class, rule);
      assertEquals(BigInteger.class, ret.getClass());
      assertEquals(BigInteger.valueOf(2031L), ret);

      ret = fromDateTime(val, BigDecimal.class, rule);
      assertEquals(BigDecimal.class, ret.getClass());
      assertEquals(BigDecimal.valueOf(2031L), ret);

      ret = fromDateTime(val, String.class, rule);
      assertEquals(String.class, ret.getClass());
      assertEquals("2031", ret);

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
      rule.setToClassName(String.class.getName());
      assertEquals("", execute("", rule));
    }

    @Test
    @DisplayName("変換先が未指定場合はエラー")
    void case4() throws Exception {
      try {
        rule.setToClassName(null);
        execute("アイウ", rule);
        fail("変換先未指定 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("型変換先が指定されていません。", e.getMessage());
      }
      try {
        rule.setToClassName("");
        execute("アイウ", rule);
        fail("変換先未指定 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("型変換先が指定されていません。", e.getMessage());
      }
    }

    @Test
    @DisplayName("変換先が存在しないクラス名の場合はエラー")
    void case5() throws Exception {
      try {
        rule.setToClassName("String");
        execute("アイウ", rule);
        fail("変換先未存在 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("型変換先の指定 String に問題があります。", e.getMessage());
      }
    }

    @Test
    @DisplayName("変換が未対応の場合はエラー")
    void case6() throws Exception {
      try {
        rule.setToClassName(Integer.class.getName());
        execute(new StringBuilder("アイウ"), rule);
        fail("変換非対応 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("型変換 StringBuilder → Integer は非対応。", e.getMessage());
      }
      try {
        rule.setToClassName(Integer.class.getName());
        execute(Boolean.valueOf(false), rule);
        fail("変換非対応 が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("型変換 Boolean → Integer は非対応。", e.getMessage());
      }
    }

    @Test
    @DisplayName("StringからStringで文字種変換(ひらがな)")
    void case10() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.useHiragana(true);
      assertEquals("あいう", execute("アイウ", rule));
      assertEquals("あいうぱぴこ", execute("ｱｲｳﾊﾟﾋﾟｺ", rule));
      assertEquals("あいう", execute("あいう", rule));
    }

    @Test
    @DisplayName("StringからStringで文字種変換(全角カナ)")
    void case11() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.useWideKana(true);
      assertEquals("アイウ", execute("ｱｲｳ", rule));
      assertEquals("アイウ", execute("あいう", rule));
      assertEquals("アイウ", execute("アイウ", rule));
    }

    @Test
    @DisplayName("StringからStringで文字種変換(半角カナ)")
    void case12() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.useHalfKana(true);
      assertEquals("ｱｲｳ", execute("あいう", rule));
      assertEquals("ｱｲｳ", execute("アイウ", rule));
      assertEquals("ｱｲｳ", execute("ｱｲｳ", rule));
    }

    @Test
    @DisplayName("StringからStringで文字種変換(英大文字)")
    void case13() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.useUpperCase(true);
      assertEquals("ABC", execute("abc", rule));
      assertEquals("ABC", execute("ABC", rule));
      assertEquals("あいう", execute("あいう", rule));
    }

    @Test
    @DisplayName("StringからStringで文字種変換(英小文字)")
    void case14() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.useLowerCase(true);
      assertEquals("abc", execute("ABC", rule));
      assertEquals("abc", execute("abc", rule));
      assertEquals("あいう", execute("あいう", rule));
    }

    @Test
    @DisplayName("StringからStringで文字種変換(かな小文字)")
    void case15() throws Exception {
      rule.setToClassName(String.class.getName());
      rule.useUpperCaseKana(true);
      assertEquals("あいう", execute("ぁぃぅ", rule));
      assertEquals("アイウ", execute("ァィゥ", rule));
      assertEquals("ｱｲｳ", execute("ｧｨｩ", rule));
    }

    @Test
    @DisplayName("変換前後が同じ型の場合はそのまま返る")
    void case20() throws Exception {
      rule.setToClassName(Long.class.getName());
      assertEquals(123L, execute(123L, rule));
      assertEquals(-123L, execute(-123L, rule));
    }

    @Test
    @DisplayName("日付への変換")
    void case21() throws Exception {
      rule.setToClassName(LocalDate.class.getName());
      assertEquals(LocalDate.parse("2021-01-03"), execute(20210103L, rule));
    }

    @Test
    @DisplayName("日付からの変換")
    void case22() throws Exception {
      rule.setToClassName(String.class.getName());
      assertEquals("2021/01/03", execute(LocalDate.parse("2021-01-03"), rule));
    }

    @Test
    @DisplayName("Stringへの変換")
    void case23() throws Exception {
      rule.setToClassName(String.class.getName());
      assertEquals("123456", execute(123456L, rule));
    }

    @Test
    @DisplayName("コピーコンストラクタでの変換")
    void case24() throws Exception {
      rule.setToClassName(StringBuilder.class.getName());
      assertTrue(new StringBuilder("abc").compareTo((StringBuilder) execute("abc", rule)) == 0);
    }

    @Test
    @DisplayName("valueOfでの変換")
    void case25() throws Exception {
      rule.setToClassName(BigInteger.class.getName());
      assertEquals(new BigInteger("123456"), execute(123456L, rule));
      rule.setToClassName(CharType.class.getName());
      assertEquals(CharType.ALL, execute("ALL", rule));
    }

  }

}
