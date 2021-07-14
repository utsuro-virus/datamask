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
class MaskedTextReplacerTest extends MaskedTextReplacer {

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
      assertEquals("あいう", replace("あいう", null));
    }

    @Test
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, replace(null, rule));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() throws Exception {
      assertEquals("", replace("", rule));
    }

    @Test
    @DisplayName("マスク文字(半角)が指定されなかった場合はそのまま返る")
    void case4() throws Exception {
      rule.setReplacementHalfChar(null);
      assertEquals("aBcdefG", replace("aBcdefG", rule));
      rule.setReplacementHalfChar("");
      assertEquals("aBcdefG", replace("aBcdefG", rule));
    }

    @Test
    @DisplayName("マスク文字(半角数字)が指定されなかった場合は半角文字でマスク、それすら指定なしならそのまま返る")
    void case5() throws Exception {
      rule.setReplacementHalfNum(null);
      assertEquals("XXXX", replace("1234", rule));
      rule.setReplacementHalfNum("");
      assertEquals("XXXX", replace("1234", rule));
      rule.setReplacementHalfNum(null);
      rule.setReplacementHalfChar(null);
      assertEquals("1234", replace("1234", rule));
      rule.setReplacementHalfNum("");
      rule.setReplacementHalfChar("");
      assertEquals("1234", replace("1234", rule));
    }

    @Test
    @DisplayName("マスク文字(全角)が指定されなかった場合はそのまま返る")
    void case6() throws Exception {
      rule.setReplacementWideChar(null);
      assertEquals("あいう", replace("あいう", rule));
      rule.setReplacementWideChar("");
      assertEquals("あいう", replace("あいう", rule));
    }

    @Test
    @DisplayName("マスク文字(全角数字)が指定されなかった場合は全角文字でマスク、それすら指定なしならそのまま返る")
    void case7() throws Exception {
      rule.setReplacementWideNum(null);
      assertEquals("○○○", replace("あいう", rule));
      rule.setReplacementWideNum("");
      assertEquals("○○○", replace("あいう", rule));
      rule.setReplacementWideNum(null);
      rule.setReplacementWideChar(null);
      assertEquals("あいう", replace("あいう", rule));
      rule.setReplacementWideNum("");
      rule.setReplacementWideChar("");
      assertEquals("あいう", replace("あいう", rule));
    }

    @Test
    @DisplayName("全角文字を渡した場合には○でマスクされて返る")
    void case10() throws Exception {
      assertEquals("○○○", replace("あいう", rule));
    }

    @Test
    @DisplayName("全角数字を渡した場合には９でマスクされて返る")
    void case11() throws Exception {
      assertEquals("○○９９９○", replace("あい１２３う", rule));
    }

    @Test
    @DisplayName("半角文字を渡した場合にはXでマスクされて返る")
    void case12() throws Exception {
      assertEquals("XXX", replace("aBc", rule));
    }

    @Test
    @DisplayName("半角数字を渡した場合には9でマスクされて返る")
    void case13() throws Exception {
      assertEquals("XX999X", replace("aB123c", rule));
    }

    @Test
    @DisplayName("開始位置を指定した場合は左からn文字除外してマスクする")
    void case14() throws Exception {
      rule.setUnmaksedLengthLeft(2);
      assertEquals("aBXXXXX", replace("aBcdefG", rule));
      assertEquals("aBXXXXXX", replace("aBcdefGA", rule));
    }

    @Test
    @DisplayName("終了位置を指定した場合は右からn文字除外してマスクする")
    void case15() throws Exception {
      rule.setUnmaksedLengthRight(2);
      assertEquals("XXXXXfG", replace("aBcdefG", rule));
    }

    @Test
    @DisplayName("両方指定した場合は中央n文字マスクする")
    void case16() throws Exception {
      rule.setUnmaksedLengthLeft(2);
      rule.setUnmaksedLengthRight(2);
      assertEquals("aBXXXfG", replace("aBcdefG", rule));
    }

    @Test
    @DisplayName("開始位置にマイナスを指定した場合は右からからn文字マスクする")
    void case17() throws Exception {
      rule.setUnmaksedLengthLeft(-2);
      assertEquals("aBcdeXX", replace("aBcdefG", rule));
      assertEquals("aBcdefXX", replace("aBcdefGA", rule));
    }

    @Test
    @DisplayName("除外パターンを指定した場合はその文字はマスクされずに返る")
    void case18() throws Exception {
      rule.setUnmaksedChar("[-－]");
      assertEquals("XXXX-9999-9999", replace("abcd-4567-8901", rule));
      assertEquals("○○○○○○○○○○９－９９－９９", replace("東京都千代田区外神田３－１６－１８", rule));
    }

    @Test
    @DisplayName("空白はマスクされずに返るがホワイトスペースマスクも指定するとマスクされる")
    void case19() throws Exception {
      assertEquals(" XXXX 9999　 9999 ", replace(" abcd 4567　 8901 ", rule));
      rule.useWhiteSpaceMask(true);
      assertEquals("XXXXXX9999○X9999X", replace(" abcd 4567　 8901 ", rule));
    }

    @Test
    @DisplayName("奇数文字目の文字マスクする")
    void case20() throws Exception {
      rule.useOddCharMask(true);
      assertEquals("XBXdXfX", replace("aBcdefG", rule));
      assertEquals("XBXdXfXA", replace("aBcdefGA", rule));
      assertEquals("X", replace("a", rule));
      assertEquals("○い○え", replace("あいうえ", rule));
    }

    @Test
    @DisplayName("偶数文字目の文字マスクする")
    void case21() throws Exception {
      rule.useEvenCharMask(true);
      assertEquals("aXcXeXG", replace("aBcdefG", rule));
      assertEquals("aXcXeXGX", replace("aBcdefGA", rule));
      assertEquals("a", replace("a", rule));
      assertEquals("あ○う○", replace("あいうえ", rule));
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case22() throws Exception {
      rule.setIgnoreValue("ab.+f.$");
      assertEquals("aBcdefG", replace("aBcdefG", rule));
      assertEquals("XXXXXXXX", replace("aBcdefGA", rule));
      assertEquals("X", replace("a", rule));
      assertEquals("○○○○", replace("あいうえ", rule));
    }

    @Test
    @DisplayName("インスタンスメソッドでも処理は同じ")
    void case90() throws Exception {
      assertEquals("あいう", execute("あいう", null));
      assertEquals(null, execute(null, rule));
      assertEquals("○○○", execute("あいう", rule));
      assertEquals("999999", execute(123456, rule));
    }

  }

}
