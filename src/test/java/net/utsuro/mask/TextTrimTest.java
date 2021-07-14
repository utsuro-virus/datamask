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
class TextTrimTest extends TextTrim {

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
      assertEquals("あいう", trim("あいう", null));
    }

    @Test
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, trim(null, rule));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() throws Exception {
      assertEquals("", trim("", rule));
    }

    @Test
    @DisplayName("前後の全半角スペースはTrimされる")
    void case4() throws Exception {
      assertEquals("あ い　う", trim(" 　 　あ い　う　  ", rule));
    }

    @Test
    @DisplayName("全角スペースは残す場合、半角スペースのみTrimされる")
    void case5() throws Exception {
      rule.setKeepWideSpaceTrim(true);
      assertEquals("　 　あ い　う　", trim(" 　 　あ い　う　  ", rule));
    }

    @Test
    @DisplayName("インスタンスメソッドでも処理は同じ")
    void case6() throws Exception {
      assertEquals("あ い　う", execute(" 　 　あ い　う　  ", rule));
      rule.setKeepWideSpaceTrim(true);
      assertEquals("　 　あ い　う　", execute(" 　 　あ い　う　  ", rule));
    }

    @Test
    @DisplayName("指定した場合はLTrimされる")
    void case7() throws Exception {
      rule.useLTrim(true);
      rule.setKeepWideSpaceTrim(true);
      assertEquals("　 　あ い　う　  ", trim(" 　 　あ い　う　  ", rule));
      rule.setKeepWideSpaceTrim(false);
      assertEquals("あ い　う　  ", trim(" 　 　あ い　う　  ", rule));
    }

    @Test
    @DisplayName("指定した場合はRTrimされる")
    void case8() throws Exception {
      rule.useRTrim(true);
      rule.setKeepWideSpaceTrim(true);
      assertEquals(" 　 　あ い　う　", trim(" 　 　あ い　う　  ", rule));
      rule.setKeepWideSpaceTrim(false);
      assertEquals(" 　 　あ い　う", trim(" 　 　あ い　う　  ", rule));
    }

  }

}
