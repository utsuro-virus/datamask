package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomNumGeneratorTest extends RandomNumGenerator {

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
      assertEquals(BigDecimal.valueOf(123), generate(BigDecimal.valueOf(123), null));
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
      assertEquals(BigDecimal.class, ret.getClass());
      ret = generate(BigDecimal.valueOf(123), rule);
      assertNotEquals(BigDecimal.valueOf(123), ret);
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case4() throws Exception {
      rule.setIgnoreValue(".+0$");
      rule.setMaxValue("123456");
      assertEquals(BigDecimal.valueOf(1230), execute(BigDecimal.valueOf(1230), rule));
      assertEquals(BigDecimal.valueOf(100), execute(BigDecimal.valueOf(100), rule));
      assertNotEquals(BigDecimal.valueOf(0), execute(BigDecimal.valueOf(0), rule));
      assertNotEquals(BigDecimal.valueOf(123), execute(BigDecimal.valueOf(123), rule));
    }

    @Test
    @DisplayName("最小値を指定した場合はそれ以上の値で生成")
    void case10() throws Exception {
      String exp = "10";
      rule.setMinValue(exp);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigDecimal ret = (BigDecimal) execute(BigDecimal.valueOf(1230), rule);
        assertTrue(new BigDecimal(exp).compareTo(ret) <= 0,
            String.format("%dは%s以上でないのでNG", ret.longValue(), exp));
        assertTrue(new BigDecimal(exp).scale() >= ret.scale(),
            String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(exp).scale()));
      }
    }

    @Test
    @DisplayName("最大値を指定した場合はそれ以下の値で生成")
    void case11() throws Exception {
      String exp = "10";
      rule.setMaxValue(exp);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigDecimal ret = (BigDecimal) execute(BigDecimal.valueOf(1230), rule);
        assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
            String.format("%dは0以上でないのでNG", ret.longValue()));
        assertTrue(new BigDecimal(exp).compareTo(ret) >= 0,
            String.format("%dは%s以下でないのでNG", ret.longValue(), exp));
      }
    }

    @Test
    @DisplayName("最小最大両方を指定した場合は範囲内の値で生成")
    void case12() throws Exception {
      String min = "12";
      String max = "234";
      rule.setMinValue(min);
      rule.setMaxValue(max);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigDecimal ret = (BigDecimal) execute(BigDecimal.valueOf(1230), rule);
        assertTrue(new BigDecimal(min).compareTo(ret) <= 0,
            String.format("%dは%s以上でないのでNG", ret.longValue(), min));
        assertTrue(new BigDecimal(max).compareTo(ret) >= 0,
            String.format("%dは%s以下でないのでNG", ret.longValue(), max));
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
      assertEquals(BigDecimal.class, ret.getClass());
      ret = execute(BigDecimal.valueOf(123), rule);
      assertNotEquals(BigDecimal.valueOf(123), ret);
    }

    @Test
    @DisplayName("BigDecimalに型変換できない場合はそのまま返る")
    void case4() throws Exception {
      assertEquals("あいう", execute("あいう", rule));
      assertNotEquals("-123456", execute("-123456", rule));
      int[] arr = new int[] {0, 1, 2};
      assertEquals(arr, execute(arr , rule));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(Long)")
    void case10() throws Exception {
      BigDecimal ret;
      String val;
      val = "123456";
      ret = (BigDecimal) execute(Long.valueOf(val), rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(Integer)")
    void case11() throws Exception {
      BigDecimal ret;
      String val;
      val = "123456";
      ret = (BigDecimal) execute(Integer.valueOf(val), rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(Float)")
    void case12() throws Exception {
      BigDecimal ret;
      String val;
      val = "123.456";
      ret = (BigDecimal) execute(Float.valueOf(val), rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(Double)")
    void case13() throws Exception {
      BigDecimal ret;
      String val;
      val = "123.456";
      ret = (BigDecimal) execute(Double.valueOf(val), rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(BigInteger)")
    void case14() throws Exception {
      BigDecimal ret;
      String val;
      val = "123";
      ret = (BigDecimal) execute(new BigInteger(val), rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(BigDecimal)")
    void case15() throws Exception {
      BigDecimal ret;
      String val;
      val = "123.04";
      ret = (BigDecimal) execute(new BigDecimal(val), rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

    @Test
    @DisplayName("BigDecimalに型変換できる場合は処理されて返る(String)")
    void case16() throws Exception {
      BigDecimal ret;
      String val;
      val = "-123.04";
      ret = (BigDecimal) execute(val, rule);
      assertTrue(BigDecimal.ZERO.compareTo(ret) <= 0,
          String.format("%dは0以上でないのでNG", ret.longValue()));
      assertTrue(new BigDecimal(val).scale() >= ret.scale(),
          String.format("%d桁は%d桁以下でないのでNG", ret.scale(), new BigDecimal(val).scale()));
    }

  }

}
