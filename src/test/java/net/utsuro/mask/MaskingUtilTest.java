package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MaskingUtilTest extends MaskingUtil {

  @Nested
  @DisplayName("enum: CharType")
  class EnumCharType {

    @Test
    @DisplayName("getCode()でコード値取得")
    void case1() {
      CharType val;
      val = CharType.ALL;
      assertEquals(99, val.getCode());
    }

    @Test
    @DisplayName("getName()で名称取得")
    void case2() {
      CharType val;
      val = CharType.ALL;
      assertEquals("すべて", val.getName());
    }

    @Test
    @DisplayName("getReqByte()で必要容量取得")
    void case3() {
      assertEquals(1, CharType.HALF.getReqByte());
      assertEquals(2, CharType.WIDE.getReqByte());
    }

    @Test
    @DisplayName("getTypeByString()で文字からEnum取得")
    void case4() {
      String s;
      CharType type;
      s = null;
      type = CharType.getTypeByString(s);
      assertEquals(CharType.UNKNOWN, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.UNKNOWN, type));
      s = "";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.UNKNOWN, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.UNKNOWN, type));
      s = "0";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.NUMBER, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.NUMBER, type));
      s = "a";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.LOWER_ALPHA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.LOWER_ALPHA, type));
      s = "A";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.UPPER_ALPHA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.UPPER_ALPHA, type));
      s = ".";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.SPECIAL, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.SPECIAL, type));
      s = "ａ";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.WIDE_LOWER_ALPHA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.WIDE_LOWER_ALPHA, type));
      s = "Ｚ";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.WIDE_UPPER_ALPHA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.WIDE_UPPER_ALPHA, type));
      s = "８";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.WIDE_NUMBER, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.WIDE_NUMBER, type));
      s = "あ";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.HIRAGANA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.HIRAGANA, type));
      s = "ン";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.WIDE_KANA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.WIDE_KANA, type));
      s = "ﾌ";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.HALF_KANA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.HALF_KANA, type));
      s = "漢字";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.KANJI, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.KANJI, type));
      s = "（";
      type = CharType.getTypeByString(s);
      assertEquals(CharType.WIDE_SPECIAL, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.WIDE_SPECIAL, type));
      s = "ー"; //長音記号はカタカナ
      type = CharType.getTypeByString(s);
      assertEquals(CharType.WIDE_KANA, type, String.format("[%s]は%sのはずなのに%sになった", s, CharType.WIDE_KANA, type));
    }

  }

  @Nested
  @DisplayName("method: isWideChar")
  class IsWideChar {

    @Test
    @DisplayName("全角文字を渡した場合にはtrueが返る")
    void case1() {
      assertTrue(isWideChar("あ"));
    }

    @Test
    @DisplayName("半角文字を渡した場合にはfalseが返る")
    void case2() {
      assertFalse(isWideChar("A"));
    }

    @Test
    @DisplayName("前半角混在はtrueが返る")
    void case3() {
      assertTrue(isWideChar("Aあ"));
    }

    @Test
    @DisplayName("nullはfalseが返る")
    void case4() {
      assertFalse(isWideChar(null));
    }

    @Test
    @DisplayName("空文字はfalseが返る")
    void case5() {
      assertFalse(isWideChar(""));
    }

  }

  @Nested
  @DisplayName("method: charTypeNormalize")
  class CharTypeNormalize {

    @Test
    @DisplayName("ALL")
    void case1() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.ALL));
      assertTrue(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれていない", CharType.LOWER_ALPHA));
      assertTrue(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれていない", CharType.UPPER_ALPHA));
      assertTrue(ret.contains(CharType.NUMBER), String.format("%sが含まれていない", CharType.NUMBER));
      assertTrue(ret.contains(CharType.HALF_KANA), String.format("%sが含まれていない", CharType.HALF_KANA));
      assertTrue(ret.contains(CharType.SPECIAL), String.format("%sが含まれていない", CharType.SPECIAL));
      assertTrue(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_LOWER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_UPPER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれていない", CharType.WIDE_NUMBER));
      assertTrue(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれていない", CharType.WIDE_KANA));
      assertTrue(ret.contains(CharType.HIRAGANA), String.format("%sが含まれていない", CharType.HIRAGANA));
      assertTrue(ret.contains(CharType.KANJI), String.format("%sが含まれていない", CharType.KANJI));
      assertTrue(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれていない", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("HALF")
    void case2() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.HALF));
      assertTrue(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれていない", CharType.LOWER_ALPHA));
      assertTrue(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれていない", CharType.UPPER_ALPHA));
      assertTrue(ret.contains(CharType.NUMBER), String.format("%sが含まれていない", CharType.NUMBER));
      assertTrue(ret.contains(CharType.HALF_KANA), String.format("%sが含まれていない", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertFalse(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれている", CharType.WIDE_LOWER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれている", CharType.WIDE_UPPER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれている", CharType.WIDE_NUMBER));
      assertFalse(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれている", CharType.WIDE_KANA));
      assertFalse(ret.contains(CharType.HIRAGANA), String.format("%sが含まれている", CharType.HIRAGANA));
      assertFalse(ret.contains(CharType.KANJI), String.format("%sが含まれている", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("HALF_WITH_SPECIAL")
    void case3() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.HALF_WITH_SPECIAL));
      assertTrue(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれていない", CharType.LOWER_ALPHA));
      assertTrue(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれていない", CharType.UPPER_ALPHA));
      assertTrue(ret.contains(CharType.NUMBER), String.format("%sが含まれていない", CharType.NUMBER));
      assertTrue(ret.contains(CharType.HALF_KANA), String.format("%sが含まれていない", CharType.HALF_KANA));
      assertTrue(ret.contains(CharType.SPECIAL), String.format("%sが含まれていない", CharType.SPECIAL));
      assertFalse(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれている", CharType.WIDE_LOWER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれている", CharType.WIDE_UPPER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれている", CharType.WIDE_NUMBER));
      assertFalse(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれている", CharType.WIDE_KANA));
      assertFalse(ret.contains(CharType.HIRAGANA), String.format("%sが含まれている", CharType.HIRAGANA));
      assertFalse(ret.contains(CharType.KANJI), String.format("%sが含まれている", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("WIDE")
    void case4() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.WIDE));
      assertFalse(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれている", CharType.LOWER_ALPHA));
      assertFalse(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれている", CharType.UPPER_ALPHA));
      assertFalse(ret.contains(CharType.NUMBER), String.format("%sが含まれている", CharType.NUMBER));
      assertFalse(ret.contains(CharType.HALF_KANA), String.format("%sが含まれている", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertTrue(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_LOWER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_UPPER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれていない", CharType.WIDE_NUMBER));
      assertTrue(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれていない", CharType.WIDE_KANA));
      assertTrue(ret.contains(CharType.HIRAGANA), String.format("%sが含まれていない", CharType.HIRAGANA));
      assertTrue(ret.contains(CharType.KANJI), String.format("%sが含まれていない", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("WIDE_WITH_SPECIAL")
    void case5() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.WIDE_WITH_SPECIAL));
      assertFalse(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれている", CharType.LOWER_ALPHA));
      assertFalse(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれている", CharType.UPPER_ALPHA));
      assertFalse(ret.contains(CharType.NUMBER), String.format("%sが含まれている", CharType.NUMBER));
      assertFalse(ret.contains(CharType.HALF_KANA), String.format("%sが含まれている", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertTrue(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_LOWER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_UPPER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれていない", CharType.WIDE_NUMBER));
      assertTrue(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれていない", CharType.WIDE_KANA));
      assertTrue(ret.contains(CharType.HIRAGANA), String.format("%sが含まれていない", CharType.HIRAGANA));
      assertTrue(ret.contains(CharType.KANJI), String.format("%sが含まれていない", CharType.KANJI));
      assertTrue(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれてない", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("ALPHA")
    void case6() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.ALPHA));
      assertTrue(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれていない", CharType.LOWER_ALPHA));
      assertTrue(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれていない", CharType.UPPER_ALPHA));
      assertFalse(ret.contains(CharType.NUMBER), String.format("%sが含まれている", CharType.NUMBER));
      assertFalse(ret.contains(CharType.HALF_KANA), String.format("%sが含まれている", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertFalse(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれている", CharType.WIDE_LOWER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれている", CharType.WIDE_UPPER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれている", CharType.WIDE_NUMBER));
      assertFalse(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれている", CharType.WIDE_KANA));
      assertFalse(ret.contains(CharType.HIRAGANA), String.format("%sが含まれている", CharType.HIRAGANA));
      assertFalse(ret.contains(CharType.KANJI), String.format("%sが含まれている", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("ALPHANUM")
    void case7() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.ALPHANUM));
      assertTrue(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれていない", CharType.LOWER_ALPHA));
      assertTrue(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれていない", CharType.UPPER_ALPHA));
      assertTrue(ret.contains(CharType.NUMBER), String.format("%sが含まれていない", CharType.NUMBER));
      assertFalse(ret.contains(CharType.HALF_KANA), String.format("%sが含まれている", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertFalse(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれている", CharType.WIDE_LOWER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれている", CharType.WIDE_UPPER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれている", CharType.WIDE_NUMBER));
      assertFalse(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれている", CharType.WIDE_KANA));
      assertFalse(ret.contains(CharType.HIRAGANA), String.format("%sが含まれている", CharType.HIRAGANA));
      assertFalse(ret.contains(CharType.KANJI), String.format("%sが含まれている", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("WIDE_ALPHA")
    void case8() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.WIDE_ALPHA));
      assertFalse(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれている", CharType.LOWER_ALPHA));
      assertFalse(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれている", CharType.UPPER_ALPHA));
      assertFalse(ret.contains(CharType.NUMBER), String.format("%sが含まれている", CharType.NUMBER));
      assertFalse(ret.contains(CharType.HALF_KANA), String.format("%sが含まれている", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertTrue(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_LOWER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_UPPER_ALPHA));
      assertFalse(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれている", CharType.WIDE_NUMBER));
      assertFalse(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれている", CharType.WIDE_KANA));
      assertFalse(ret.contains(CharType.HIRAGANA), String.format("%sが含まれている", CharType.HIRAGANA));
      assertFalse(ret.contains(CharType.KANJI), String.format("%sが含まれている", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("WIDE_ALPHANUM")
    void case9() {
      EnumSet<CharType> ret = charTypeNormalize(EnumSet.of(CharType.WIDE_ALPHANUM));
      assertFalse(ret.contains(CharType.LOWER_ALPHA), String.format("%sが含まれている", CharType.LOWER_ALPHA));
      assertFalse(ret.contains(CharType.UPPER_ALPHA), String.format("%sが含まれている", CharType.UPPER_ALPHA));
      assertFalse(ret.contains(CharType.NUMBER), String.format("%sが含まれている", CharType.NUMBER));
      assertFalse(ret.contains(CharType.HALF_KANA), String.format("%sが含まれている", CharType.HALF_KANA));
      assertFalse(ret.contains(CharType.SPECIAL), String.format("%sが含まれている", CharType.SPECIAL));
      assertTrue(ret.contains(CharType.WIDE_LOWER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_LOWER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_UPPER_ALPHA), String.format("%sが含まれていない", CharType.WIDE_UPPER_ALPHA));
      assertTrue(ret.contains(CharType.WIDE_NUMBER), String.format("%sが含まれていない", CharType.WIDE_NUMBER));
      assertFalse(ret.contains(CharType.WIDE_KANA), String.format("%sが含まれている", CharType.WIDE_KANA));
      assertFalse(ret.contains(CharType.HIRAGANA), String.format("%sが含まれている", CharType.HIRAGANA));
      assertFalse(ret.contains(CharType.KANJI), String.format("%sが含まれている", CharType.KANJI));
      assertFalse(ret.contains(CharType.WIDE_SPECIAL), String.format("%sが含まれている", CharType.WIDE_SPECIAL));
    }

  }

  @Nested
  @DisplayName("method: getRandomString")
  class GetRandomString {

    @Test
    @DisplayName("英小文字指定で指定した長さの英小文字が返る")
    void case1() {
      int len = 10;
      CharType charType = CharType.LOWER_ALPHA;
      String ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[a-z]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("英小文字指定でランダムに返る")
    void case2() {
      String target = "abcdefghijklmnopqrstuvwxyz";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomString(1, CharType.LOWER_ALPHA);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomString() [%s] 試行回数: %d ---", CharType.LOWER_ALPHA, count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

    @Test
    @DisplayName("英大文字指定で指定した長さの英大文字が返る")
    void case3() {
      int len = 10;
      CharType charType = CharType.UPPER_ALPHA;
      String ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[A-Z]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("英大文字指定でランダムに返る")
    void case4() {
      String target = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomString(1, CharType.UPPER_ALPHA);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomString() [%s] 試行回数: %d ---", CharType.UPPER_ALPHA, count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

    @Test
    @DisplayName("数字指定で指定した長さの数字が返る")
    void case5() {
      int len = 10;
      CharType charType = CharType.NUMBER;
      String ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[0-9]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("数字指定でランダムに返る")
    void case6() {
      String target = "0123456789";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomString(1, CharType.NUMBER);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomString() [%s] 試行回数: %d ---", CharType.NUMBER, count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

    @Test
    @DisplayName("半角カナ指定で指定した長さの半角カナが返る")
    void case7() {
      int len = 20;
      int count = 100; // 試行回数
      for (int i = 0; i < count; i++) {
        CharType charType = CharType.HALF_KANA;
        String ret = getRandomString(len, charType);
        assertTrue(ret.matches("^[\\uFF61-\\uFF9F]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
        assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
      }
    }

    @Test
    @DisplayName("半角カナ指定でランダムに返る")
    void case8() {
      String[] target = {
          "ｱ", "ｲ", "ｳ", "ｴ", "ｵ",
          "ｶ", "ｷ", "ｸ", "ｹ", "ｺ",
          "ｻ", "ｼ", "ｽ", "ｾ", "ｿ",
          "ﾀ", "ﾁ", "ﾂ", "ﾃ", "ﾄ",
          "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ",
          "ﾊ", "ﾋ", "ﾌ", "ﾍ", "ﾎ",
          "ﾏ", "ﾐ", "ﾑ", "ﾒ", "ﾓ",
          "ﾔ", "ﾕ", "ﾖ",
          "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ",
          "ﾜ", "ｦ", //"ﾝ", 1文字ずつだとﾝは登場しない
          "ｶﾞ", "ｷﾞ", "ｸﾞ", "ｹﾞ", "ｺﾞ",
          "ｻﾞ", "ｼﾞ", "ｽﾞ", "ｾﾞ", "ｿﾞ",
          "ﾀﾞ", "ﾁﾞ", "ﾂﾞ", "ﾃﾞ", "ﾄﾞ",
          "ﾊﾞ", "ﾋﾞ", "ﾌﾞ", "ﾍﾞ", "ﾎﾞ",
          "ｳﾞ",
          "ﾊﾟ", "ﾋﾟ", "ﾌﾟ", "ﾍﾟ", "ﾎﾟ"//,
          //"ｧ", "ｨ", "ｩ", "ｪ", "ｫ", //1文字ずつだとカナ小文字は登場しない
          //"ｯ", "ｬ", "ｭ", "ｮ" //1文字ずつだとカナ小文字は登場しない
      };
      int count = target.length * 1000; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomString(2, CharType.HALF_KANA);
        if (s.matches("^[^ﾞﾟ]+$")) {
          s = s.substring(0, 1);
        }
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomString() [%s] 試行回数: %d ---", CharType.HALF_KANA, count));
      System.out.println(ret);

      String[] val = target;

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 3割登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < count * 0.3, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

    @Test
    @DisplayName("記号指定で指定した長さの記号が返る")
    void case9() {
      int len = 10;
      CharType charType = CharType.SPECIAL;
      String ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[!-~]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("記号指定でランダムに返る")
    void case10() {
      String target = "!\"#$%&'()*+-./:;<=>?@[\\\\]^_`{|}~";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomString(1, CharType.SPECIAL);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomString() [%s] 試行回数: %d ---", CharType.SPECIAL, count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

    @Test
    @DisplayName("全角記号指定で指定した長さの全角記号が返る")
    void case11() {
      int len = 10;
      CharType charType = CharType.WIDE_SPECIAL;
      String ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
    }

    @Test
    @DisplayName("全角記号指定でランダムに返る")
    void case12() {
      String target = "！”＃＄％＆’（）＊＋－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣";
      int count = target.length() * 100; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomString(2, CharType.WIDE_SPECIAL);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomString() [%s] 試行回数: %d ---", CharType.WIDE_SPECIAL, count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30%登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < count * 0.3, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

    @Test
    @DisplayName("捨て仮名や「ん」は先頭に登場しない")
    void case13() {
      int count = 3000; //試行回数
      for (int i = 0; i < count; i++) {
        String s = getRandomString(2, CharType.HIRAGANA);
        assertFalse(s.matches("[ぁぃぅぇぉっゃゅょゎん]"));
      }
    }

    @Test
    @DisplayName("捨て仮名や「ン」は先頭に登場しない")
    void case14() {
      int count = 3000; //試行回数
      for (int i = 0; i < count; i++) {
        String s = getRandomString(2, CharType.WIDE_KANA);
        assertFalse(s.matches("[ァィゥェォッャュョヮン]"));
      }
    }

    @Test
    @DisplayName("捨て仮名や「ﾝ」は先頭に登場しない")
    void case15() {
      int count = 3000; //試行回数
      for (int i = 0; i < count; i++) {
        String s = getRandomString(2, CharType.WIDE_KANA);
        assertFalse(s.matches("[ｧｨｩｪｫｯｬｭｮﾝ]"));
      }
    }

    @Test
    @DisplayName("「ゃゅょ」はありえない位置に登場しない")
    void case16() {
      int count = 3000; //試行回数
      for (int i = 0; i < count; i++) {
        String s = getRandomString(4, CharType.HIRAGANA);
        assertFalse(s.matches("[^きしちにひみりぎじぢびぴ][ゃゅょ]"));
      }
    }

    @Test
    @DisplayName("「ャュョ」はありえない位置に登場しない")
    void case17() {
      int count = 3000; //試行回数
      for (int i = 0; i < count; i++) {
        String s = getRandomString(4, CharType.WIDE_KANA);
        assertFalse(s.matches("[^キシチニヒミリギジヂビピ][ャュョ]"));
      }
    }

    @Test
    @DisplayName("「ｬｭｮ」はありえない位置に登場しない")
    void case18() {
      int count = 3000; //試行回数
      for (int i = 0; i < count; i++) {
        String s = getRandomString(4, CharType.WIDE_KANA);
        assertFalse(s.matches("[^ｷｼﾁﾆﾋﾐﾘ][ｬｭｮ]"));
      }
    }

    @Test
    @DisplayName("全角系は指定文字種と長さで生成された文字列が返る")
    void case20() {
      String ret;
      int len;
      CharType charType;
      // 全角英小文字
      len = 10;
      charType = CharType.WIDE_LOWER_ALPHA;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[ａ-ｚ]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
      // 全角英大文字
      len = 10;
      charType = CharType.WIDE_UPPER_ALPHA;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[Ａ-Ｚ]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
      // 全角数字
      len = 10;
      charType = CharType.WIDE_NUMBER;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[０-９]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
      // 全角カナ
      len = 10;
      charType = CharType.WIDE_KANA;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[ァ-ヴ]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
      // ひらがな
      len = 10;
      charType = CharType.HIRAGANA;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[ぁ-ん]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
      // 漢字
      len = 10;
      charType = CharType.KANJI;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^([々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
    }

    @Test
    @DisplayName("ALPHAで複合指定文字列が返る")
    void case30() {
      String ret;
      int len;
      CharType charType;
      // 英小文字＋英大文字
      len = 100;
      charType = CharType.ALPHA;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[a-zA-Z]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertFalse(ret.matches("^[^a-z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.LOWER_ALPHA));
      assertFalse(ret.matches("^[^A-Z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.UPPER_ALPHA));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("ALPHANUMで複合指定文字列が返る")
    void case31() {
      String ret;
      int len;
      CharType charType;
      // 英数
      len = 100;
      charType = CharType.ALPHANUM;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[0-9a-zA-Z]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertFalse(ret.matches("^[^0-9]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.NUMBER));
      assertFalse(ret.matches("^[^a-z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.LOWER_ALPHA));
      assertFalse(ret.matches("^[^A-Z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.UPPER_ALPHA));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("HALFで複合指定文字列が返る")
    void case32() {
      String ret;
      int len;
      CharType charType;
      // 半角文字(記号以外)
      len = 100;
      charType = CharType.HALF;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[0-9a-zA-Z\\uFF61-\\uFF9F]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertFalse(ret.matches("^[^0-9]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.NUMBER));
      assertFalse(ret.matches("^[^a-z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.LOWER_ALPHA));
      assertFalse(ret.matches("^[^A-Z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.UPPER_ALPHA));
      assertFalse(ret.matches("^[\\uFF61-\\uFF9F]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.HALF_KANA));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("HALF_WITH_SPECIALで複合指定文字列が返る")
    void case33() {
      String ret;
      int len;
      CharType charType;
      // 半角文字
      len = 100;
      charType = CharType.HALF_WITH_SPECIAL;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[0-9a-zA-Z\\uFF61-\\uFF9F!-~]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertFalse(ret.matches("^[^0-9]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.NUMBER));
      assertFalse(ret.matches("^[^a-z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.LOWER_ALPHA));
      assertFalse(ret.matches("^[^A-Z]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.UPPER_ALPHA));
      assertFalse(ret.matches("^[\\uFF61-\\uFF9F]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.HALF_KANA));
      assertFalse(ret.matches("^[!-~]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.SPECIAL));
      assertTrue(ret.length() == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length()));
    }

    @Test
    @DisplayName("WIDE_ALPHAで複合指定文字列が返る")
    void case34() {
      String ret;
      int len;
      CharType charType;
      // 全角英小文字＋全角英大文字
      len = 100;
      charType = CharType.WIDE_ALPHA;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[ａ-ｚＡ-Ｚ]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertFalse(ret.matches("^[^ａ-ｚ]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.WIDE_LOWER_ALPHA));
      assertFalse(ret.matches("^[^Ａ-Ｚ]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.WIDE_UPPER_ALPHA));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
    }

    @Test
    @DisplayName("WIDE_ALPHANUMで複合指定文字列が返る")
    void case35() {
      String ret;
      int len;
      CharType charType;
      // 全角英数
      len = 100;
      charType = CharType.WIDE_ALPHANUM;
      ret = getRandomString(len, charType);
      assertTrue(ret.matches("^[０-９ａ-ｚＡ-Ｚ]+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
      assertFalse(ret.matches("^[^０-９]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.WIDE_NUMBER));
      assertFalse(ret.matches("^[^ａ-ｚ]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.WIDE_LOWER_ALPHA));
      assertFalse(ret.matches("^[^Ａ-Ｚ]+$"), String.format("[%s]は[%s]が1文字も含まれないのでNG", ret, CharType.WIDE_UPPER_ALPHA));
      assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
    }

    @Test
    @DisplayName("WIDEで複合指定文字列が返る")
    void case36() {
      int len = 1000;
      int count = 200;
      Map<CharType, Integer> matchCount = new HashMap<>();
      matchCount.put(CharType.WIDE_NUMBER, 0);
      matchCount.put(CharType.WIDE_LOWER_ALPHA, 0);
      matchCount.put(CharType.WIDE_UPPER_ALPHA, 0);
      matchCount.put(CharType.WIDE_KANA, 0);
      matchCount.put(CharType.HIRAGANA, 0);
      matchCount.put(CharType.KANJI, 0);
      CharType charType = CharType.WIDE;
      // 全角文字(記号を除く)
      for (int i = 0; i < count; i++) {
        String ret = getRandomString(len, charType);
        assertTrue(ret.matches("^([０-９ａ-ｚＡ-Ｚぁ-んァ-ヴ]|[々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
        assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
        if (ret.matches("^(.*)[０-９]+(.*)$")) {
          matchCount.put(CharType.WIDE_NUMBER, matchCount.get(CharType.WIDE_NUMBER) + 1);
        }
        if (ret.matches("^(.*)[ａ-ｚ]+(.*)$")) {
          matchCount.put(CharType.WIDE_LOWER_ALPHA, matchCount.get(CharType.WIDE_LOWER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[Ａ-Ｚ]+(.*)$")) {
          matchCount.put(CharType.WIDE_UPPER_ALPHA, matchCount.get(CharType.WIDE_UPPER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[ァ-ヴ]+(.*)$")) {
          matchCount.put(CharType.WIDE_KANA, matchCount.get(CharType.WIDE_KANA) + 1);
        }
        if (ret.matches("^(.*)[ぁ-ん]+(.*)$")) {
          matchCount.put(CharType.HIRAGANA, matchCount.get(CharType.HIRAGANA) + 1);
        }
        if (ret.matches("^(.*)([々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+(.*)$")) {
          matchCount.put(CharType.KANJI, matchCount.get(CharType.KANJI) + 1);
        }
      }
      assertTrue(matchCount.get(CharType.WIDE_NUMBER) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_NUMBER));
      assertTrue(matchCount.get(CharType.WIDE_LOWER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_LOWER_ALPHA));
      assertTrue(matchCount.get(CharType.WIDE_UPPER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_UPPER_ALPHA));
      assertTrue(matchCount.get(CharType.WIDE_KANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_KANA));
      assertTrue(matchCount.get(CharType.HIRAGANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.HIRAGANA));
      assertTrue(matchCount.get(CharType.KANJI) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.KANJI));

    }

    @Test
    @DisplayName("WIDE_WITH_SPECIALで複合指定文字列が返る")
    void case37() {
      int len = 1000;
      int count = 200;
      Map<CharType, Integer> matchCount = new HashMap<>();
      matchCount.put(CharType.WIDE_NUMBER, 0);
      matchCount.put(CharType.WIDE_LOWER_ALPHA, 0);
      matchCount.put(CharType.WIDE_UPPER_ALPHA, 0);
      matchCount.put(CharType.WIDE_KANA, 0);
      matchCount.put(CharType.HIRAGANA, 0);
      matchCount.put(CharType.KANJI, 0);
      matchCount.put(CharType.WIDE_SPECIAL, 0);
      CharType charType = CharType.WIDE_WITH_SPECIAL;
      // 全角文字
      for (int i = 0; i < count; i++) {
        String ret = getRandomString(len, charType);
        assertTrue(ret.matches("^([０-９ａ-ｚＡ-Ｚぁ-んァ-ヴ！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣]|[々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+$"), String.format("[%s]は[%s]以外が含まれるのでNG", ret, charType));
        assertTrue(ret.length() * 2 == len, String.format("[%d]は指定した長さ以外なのでNG", ret.length() * 2));
        if (ret.matches("^(.*)[０-９]+(.*)$")) {
          matchCount.put(CharType.WIDE_NUMBER, matchCount.get(CharType.WIDE_NUMBER) + 1);
        }
        if (ret.matches("^(.*)[ａ-ｚ]+(.*)$")) {
          matchCount.put(CharType.WIDE_LOWER_ALPHA, matchCount.get(CharType.WIDE_LOWER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[Ａ-Ｚ]+(.*)$")) {
          matchCount.put(CharType.WIDE_UPPER_ALPHA, matchCount.get(CharType.WIDE_UPPER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[ァ-ヴ]+(.*)$")) {
          matchCount.put(CharType.WIDE_KANA, matchCount.get(CharType.WIDE_KANA) + 1);
        }
        if (ret.matches("^(.*)[ぁ-ん]+(.*)$")) {
          matchCount.put(CharType.HIRAGANA, matchCount.get(CharType.HIRAGANA) + 1);
        }
        if (ret.matches("^(.*)([々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+(.*)$")) {
          matchCount.put(CharType.KANJI, matchCount.get(CharType.KANJI) + 1);
        }
        if (ret.matches("^(.*)[！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣]+(.*)$")) {
          matchCount.put(CharType.WIDE_SPECIAL, matchCount.get(CharType.WIDE_SPECIAL) + 1);
        }

      }
      assertTrue(matchCount.get(CharType.WIDE_NUMBER) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_NUMBER));
      assertTrue(matchCount.get(CharType.WIDE_LOWER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_LOWER_ALPHA));
      assertTrue(matchCount.get(CharType.WIDE_UPPER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_UPPER_ALPHA));
      assertTrue(matchCount.get(CharType.WIDE_KANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_KANA));
      assertTrue(matchCount.get(CharType.HIRAGANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.HIRAGANA));
      assertTrue(matchCount.get(CharType.KANJI) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.KANJI));
      assertTrue(matchCount.get(CharType.WIDE_SPECIAL) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("ALLで複合指定文字列が返る")
    void case38() {
      int len = 1000;
      int count = 200;
      Map<CharType, Integer> matchCount = new HashMap<>();
      matchCount.put(CharType.NUMBER, 0);
      matchCount.put(CharType.LOWER_ALPHA, 0);
      matchCount.put(CharType.UPPER_ALPHA, 0);
      matchCount.put(CharType.HALF_KANA, 0);
      matchCount.put(CharType.SPECIAL, 0);
      matchCount.put(CharType.WIDE_NUMBER, 0);
      matchCount.put(CharType.WIDE_LOWER_ALPHA, 0);
      matchCount.put(CharType.WIDE_UPPER_ALPHA, 0);
      matchCount.put(CharType.WIDE_KANA, 0);
      matchCount.put(CharType.HIRAGANA, 0);
      matchCount.put(CharType.KANJI, 0);
      matchCount.put(CharType.WIDE_SPECIAL, 0);
      CharType charType = CharType.ALL;
      // 全角文字
      for (int i = 0; i < count; i++) {
        String ret = getRandomString(len, charType);
        if (ret.matches("^(.*)[0-9]+(.*)$")) {
          matchCount.put(CharType.NUMBER, matchCount.get(CharType.NUMBER) + 1);
        }
        if (ret.matches("^(.*)[a-z]+(.*)$")) {
          matchCount.put(CharType.LOWER_ALPHA, matchCount.get(CharType.LOWER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[A-Z]+(.*)$")) {
          matchCount.put(CharType.UPPER_ALPHA, matchCount.get(CharType.UPPER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[\\uFF61-\\uFF9F]+(.*)$")) {
          matchCount.put(CharType.HALF_KANA, matchCount.get(CharType.HALF_KANA) + 1);
        }
        if (ret.matches("^(.*)[!-~]+(.*)$")) {
          matchCount.put(CharType.SPECIAL, matchCount.get(CharType.SPECIAL) + 1);
        }
        if (ret.matches("^(.*)[０-９]+(.*)$")) {
          matchCount.put(CharType.WIDE_NUMBER, matchCount.get(CharType.WIDE_NUMBER) + 1);
        }
        if (ret.matches("^(.*)[ａ-ｚ]+(.*)$")) {
          matchCount.put(CharType.WIDE_LOWER_ALPHA, matchCount.get(CharType.WIDE_LOWER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[Ａ-Ｚ]+(.*)$")) {
          matchCount.put(CharType.WIDE_UPPER_ALPHA, matchCount.get(CharType.WIDE_UPPER_ALPHA) + 1);
        }
        if (ret.matches("^(.*)[ァ-ヴ]+(.*)$")) {
          matchCount.put(CharType.WIDE_KANA, matchCount.get(CharType.WIDE_KANA) + 1);
        }
        if (ret.matches("^(.*)[ぁ-ん]+(.*)$")) {
          matchCount.put(CharType.HIRAGANA, matchCount.get(CharType.HIRAGANA) + 1);
        }
        if (ret.matches("^(.*)([々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+(.*)$")) {
          matchCount.put(CharType.KANJI, matchCount.get(CharType.KANJI) + 1);
        }
        if (ret.matches("^(.*)[！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣]+(.*)$")) {
          matchCount.put(CharType.WIDE_SPECIAL, matchCount.get(CharType.WIDE_SPECIAL) + 1);
        }

      }
      assertTrue(matchCount.get(CharType.NUMBER) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.NUMBER));
      assertTrue(matchCount.get(CharType.LOWER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.LOWER_ALPHA));
      assertTrue(matchCount.get(CharType.UPPER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.UPPER_ALPHA));
      assertTrue(matchCount.get(CharType.HALF_KANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.HALF_KANA));
      assertTrue(matchCount.get(CharType.SPECIAL) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.SPECIAL));
      assertTrue(matchCount.get(CharType.WIDE_NUMBER) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_NUMBER));
      assertTrue(matchCount.get(CharType.WIDE_LOWER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_LOWER_ALPHA));
      assertTrue(matchCount.get(CharType.WIDE_UPPER_ALPHA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_UPPER_ALPHA));
      assertTrue(matchCount.get(CharType.WIDE_KANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_KANA));
      assertTrue(matchCount.get(CharType.HIRAGANA) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.HIRAGANA));
      assertTrue(matchCount.get(CharType.KANJI) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.KANJI));
      assertTrue(matchCount.get(CharType.WIDE_SPECIAL) > 0, String.format("[%s]が1文字も含まれないのでNG", CharType.WIDE_SPECIAL));
    }

    @Test
    @DisplayName("生成Byte数が0以下は空文字が返る")
    void case90() {

      assertEquals("", getRandomString(0, CharType.ALL));
      assertEquals("", getRandomString(-1, CharType.ALL));
      assertNotEquals("", getRandomString(1, CharType.ALL));
    }

    @Test
    @DisplayName("CharTypeがnullや空は空文字が返る")
    void case91() {

      assertEquals("", getRandomString(10, (EnumSet<CharType>)null));
      assertEquals("", getRandomString(10, EnumSet.noneOf(CharType.class)));

    }

    @Test
    @DisplayName("全角指定で奇数Byteは桁足らずで返る")
    void case92() {

      String ret;
      ret = getRandomString(3, CharType.KANJI);
      assertEquals(2, ret.getBytes(Charset.forName("MS932")).length);
      ret = getRandomString(1, CharType.HIRAGANA);
      assertEquals(0, ret.getBytes(Charset.forName("MS932")).length);

    }

    @Test
    @DisplayName("生成しない文字パターンを指定した場合はその文字は含まれない")
    void case93() {
      int count = 10; //試行回数
      for (int i = 0; i < count; i++) {
        String ret;
        ret = getRandomString(20, CharType.NUMBER, Pattern.compile("[0-1]"));
        assertFalse(ret.matches("[0-1]+"));
      }

    }

  }

  @Nested
  @DisplayName("method: getRandomIndex")
  class GetRandomIndex {

    @Test
    @DisplayName("抽選結果は引き渡した重み配列の範囲で返る")
    void case1() {
      int[] weights = new int[] {10, 5, 1};
      int count = weights.length * 100; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        int idx = getRandomIndex(weights);
        String s = Integer.toString(idx);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomIndex() 試行回数: %d ---", count));
      System.out.println(ret);

      for (int i = 0; i < weights.length; i++) {
        String key = Integer.toString(i);
        // 最低1回は登場する
        assertTrue(ret.get(key) != null && ret.get(key) > 0, String.format("%sが出現していない", key));
        // 90%回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(key) < count * 0.9, String.format("%sが出現しすぎ(%d/%d回)", key, ret.get(key), count));
      }
    }

    @Test
    @DisplayName("抽選結果は引き渡した重み配列の範囲と重み合計値から返る")
    void case2() {
      int[] weights = new int[] {10, 5, 1};
      int totalWeight = 0;
      for (int i = 0; i < weights.length; i++) {
        totalWeight += weights[i];
      }
      int count = weights.length * 100; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        int idx = getRandomIndex(weights, totalWeight);
        String s = Integer.toString(idx);
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomIndex() 試行回数: %d ---", count));
      System.out.println(ret);

      for (int i = 0; i < weights.length; i++) {
        String key = Integer.toString(i);
        // 最低1回は登場する
        assertTrue(ret.get(key) != null && ret.get(key) > 0, String.format("%sが出現していない", key));
        // 90%回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(key) < count * 0.9, String.format("%sが出現しすぎ(%d/%d回)", key, ret.get(key), count));
      }
    }

  }

  @Nested
  @DisplayName("method: getRandomHiragana")
  class GetRandomHiragana {

    @Test
    @DisplayName("ひらがなが返る")
    void case1() {
      String ret = getRandomHiragana();
      assertTrue(ret.matches("[ぁ-ん]"));
    }

    @Test
    @DisplayName("ランダムに返る")
    void case2() {
      String target = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをんがぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽぁぃぅぇぉっゃゅょ";
      int count = target.length() * 1000; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomHiragana();
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomHiragana() 試行回数: %d ---", count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 10%回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < count / 10, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }
  }

  @Nested
  @DisplayName("method: getRandomSjisKanji")
  class GetRandomSjisKanji {

    @Test
    @DisplayName("全角漢字が返る")
    void case1() {
      String ret = getRandomSjisKanji();
      assertTrue(ret.matches("[々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF]"), String.format("[%s]は漢字ではない", ret));
    }

    @Test
    @DisplayName("ランダムに返る")
    void case2() {
      int count = 10000; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomSjisKanji();
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomSjisKanji() 試行回数: %d ---", count));
      System.out.println(ret);

      for (String key : ret.keySet()) {
        // 10%回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(key) < count / 10, String.format("%sが出現しすぎ(%d/%d回)", key, ret.get(key), count));
      }
    }
  }

  @Nested
  @DisplayName("method: getRandomSjisWideKana")
  class GetRandomSjisWideKana {

    @Test
    @DisplayName("全角カナが返る")
    void case1() {
      String ret = getRandomSjisWideKana();
      assertTrue(ret.matches("[ァ-ヴ]"));
    }

    @Test
    @DisplayName("ランダムに返る")
    void case2() {
      String target = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンガギグゲゴザジズゼゾダヂヅデドバビブベボヴパピプペポァィゥェォッャュョ";
      int count = target.length() * 100; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomSjisWideKana();
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomSjisWideKana() 試行回数: %d ---", count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 300回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 300, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }
  }

  @Nested
  @DisplayName("method: getRandomSjisWideUpperAlpha")
  class GetRandomSjisWideUpperAlpha {

    @Test
    @DisplayName("全角英大文字が返る")
    void case1() {
      String ret = getRandomSjisWideUpperAlpha();
      assertTrue(ret.matches("[Ａ-Ｚ]"));
    }

    @Test
    @DisplayName("ランダムに返る")
    void case2() {
      String target = "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomSjisWideUpperAlpha();
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomSjisWideUpperAlpha() 試行回数: %d ---", count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }
  }

  @Nested
  @DisplayName("method: getRandomSjisWideLowerAlpha")
  class GetRandomSjisWideLowerAlpha {

    @Test
    @DisplayName("全角英小文字が返る")
    void case1() {
      String ret = getRandomSjisWideLowerAlpha();
      assertTrue(ret.matches("[ａ-ｚ]"));
    }

    @Test
    @DisplayName("ランダムに返る")
    void case2() {
      String target = "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomSjisWideLowerAlpha();
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomSjisWideLowerAlpha() 試行回数: %d ---", count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

  }

  @Nested
  @DisplayName("method: getRandomSjisWideNumber")
  class GetRandomSjisWideNumber {

    @Test
    @DisplayName("全角数字が返る")
    void case1() {
      String ret = getRandomSjisWideNumber();
      assertTrue(ret.matches("[０-９]"));
    }

    @Test
    @DisplayName("ランダムに返る")
    void case2() {
      String target = "０１２３４５６７８９";
      int count = target.length() * 10; //試行回数
      Map<String, Integer> ret = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String s = getRandomSjisWideNumber();
        if (!ret.containsKey(s)) {
          ret.put(s, 0);
        }
        ret.put(s, ret.get(s) + 1);
      }
      System.out.println(String.format("--- getRandomSjisWideNumber() 試行回数: %d ---", count));
      System.out.println(ret);

      String[] val = target.split("");

      for (int i = 0; i < val.length; i++) {
        // 最低1回は登場する
        assertTrue(ret.get(val[i]) != null && ret.get(val[i]) > 0, String.format("%sが出現していない", val[i]));
        // 30回登場したら片寄りすぎなのでNGとする
        assertTrue(ret.get(val[i]) < 30, String.format("%sが出現しすぎ(%d/%d回)", val[i], ret.get(val[i]), count));
      }
    }

  }

  @Nested
  @DisplayName("method: getRandomNumber")
  class GetRandomNumber {

    @Test
    @DisplayName("指定範囲内の正の乱数が返る")
    void case1() {
      BigInteger min = new BigInteger("1");
      BigInteger max = new BigInteger("100");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigInteger ret = getRandomNumber(min, max);
        assertTrue(ret.compareTo(min) >= 0 && ret.compareTo(max) <= 0, String.format("%sは%s~%sの範囲でない", ret, min, max));
      }
    }

    @Test
    @DisplayName("指定範囲内の負の乱数が返る")
    void case2() {
      BigInteger min = new BigInteger("-100");
      BigInteger max = new BigInteger("0");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigInteger ret = getRandomNumber(min, max);
        assertTrue(ret.compareTo(min) >= 0 && ret.compareTo(max) <= 0, String.format("%sは%s~%sの範囲でない", ret, min, max));
      }
    }

    @Test
    @DisplayName("指定範囲内の実数が返る")
    void case3() {
      BigDecimal min = new BigDecimal("0");
      BigDecimal max = new BigDecimal("1.201");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigDecimal ret = getRandomNumber(min, max);
        assertTrue(ret.compareTo(min) >= 0 && ret.compareTo(max) <= 0, String.format("%sは%s~%sの範囲でない", ret, min, max));
      }
    }

    @Test
    @DisplayName("スケール0なら指定範囲内の整数が返る")
    void case4() {
      BigDecimal min = new BigDecimal("0");
      BigDecimal max = new BigDecimal("10");
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        BigDecimal ret = getRandomNumber(min, max);
        assertTrue(ret.compareTo(min) >= 0 && ret.compareTo(max) <= 0, String.format("%sは%s~%sの範囲でない", ret, min, max));
      }
    }

  }

  @Nested
  @DisplayName("method: getRandomDateTime")
  class GetRandomDateTime {

    @Test
    @DisplayName("指定範囲内の日付が返る")
    void case1() {
      LocalDateTime  min = LocalDateTime.of(2020, 12, 01, 0, 0, 0);
      LocalDateTime  max = LocalDateTime.of(2020, 12, 31, 23, 59, 59);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        LocalDateTime ret = getRandomDate(min, max);
        assertTrue(ret.compareTo(min) >= 0 && ret.compareTo(max) <= 0, String.format("%sは%s~%sの範囲でない", ret, min, max));
      }
    }
  }

  @Nested
  @DisplayName("method: toUpperHalfKana")
  class ToUpperHalfKana {

    @Test
    @DisplayName("半角カナの小文字を大文字にする")
    void case1() {
      assertEquals("ｵアﾔﾂｱｲｳｴｵ", toUpperHalfKana("ｫアｬｯｧｨｩｪｫ"));
    }

  }

  @Nested
  @DisplayName("method: toUpperWideKana")
  class ToUpperWideKana {

    @Test
    @DisplayName("全角カナの小文字を大文字にする")
    void case1() {
      assertEquals("オヤツワイカヨー", toUpperWideKana("ォャッヮィヵョー"));
    }

  }

  @Nested
  @DisplayName("method: wideKanaToHalfKana")
  class WideKanaToHalfKana {

    @Test
    @DisplayName("全角カナを半角にする")
    void case1() {
      assertEquals("ﾄｳｷｮｳﾄ１２３", wideKanaToHalfKana("トウキョウト１２３"));
    }

  }

  @Nested
  @DisplayName("method: halfKanaToWideKana")
  class HalfKanaToWideKana {

    @Test
    @DisplayName("半角カナを全角にする")
    void case1() {
      assertEquals("トウキョウト123", halfKanaToWideKana("ﾄｳｷｮｳﾄ123"));
    }

  }

  @Nested
  @DisplayName("method: halfNumberToWideNumber")
  class HalfNumberToWideNumber {

    @Test
    @DisplayName("半角数字を全角にする")
    void case1() {
      assertEquals("ﾄｳｷｮｳﾄ１-２-３-４５６", halfNumberToWideNumber("ﾄｳｷｮｳﾄ1-2-3-456"));
    }

  }

  @Nested
  @DisplayName("method: getLuhnDigit")
  class GetLuhnDigit {

    @Test
    @DisplayName("Luhnアルゴリズムでチェックディジットを取得")
    void case1() {
      String checkDigit = getLuhnDigit("201511190");
      assertTrue("3".equals(checkDigit));
      // 余りがゼロの場合はチェックディジットも0
      checkDigit = getLuhnDigit("201511490");
      assertTrue("0".equals(checkDigit));
    }

  }

  @Nested
  @DisplayName("method: toUpperHiragana")
  class ToUpperHiragana {

    @Test
    @DisplayName("かな小文字を渡した場合にはかな大文字が返る")
    void case1() {
      assertEquals("アあいうえおヵヶつやゆよわン", toUpperHiragana("アぁぃぅぇぉヵヶっゃゅょゎン"));
      assertEquals("あアンわ", toUpperHiragana("ぁアンゎ"));
    }

    @Test
    @DisplayName("対象文字でない場合はそのまま返る")
    void case2() {
      assertEquals("あいう", toUpperHiragana("あいう"));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() {
      assertEquals("", toUpperHiragana(""));
    }

  }

  @Nested
  @DisplayName("method: wideKanaToHiragana")
  class WideKanaToHiragana {

    @Test
    @DisplayName("全角カナを渡した場合にはひらがなが返る")
    void case1() {
      assertEquals("あいうえおわおん", wideKanaToHiragana("アイウエオワオン"));
      assertEquals("ぁあんゎ", wideKanaToHiragana("ァアンヮ"));
    }

    @Test
    @DisplayName("対象文字でない場合はそのまま返る")
    void case2() {
      assertEquals("あいう", wideKanaToHiragana("あいう"));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() {
      assertEquals("", wideKanaToHiragana(""));
    }

  }

  @Nested
  @DisplayName("method: hiraganaToWideKana")
  class HiraganaToWideKana {

    @Test
    @DisplayName("ひらがなを渡した場合には全角カナが返る")
    void case1() {
      assertEquals("アイウエオワオン", hiraganaToWideKana("あいうえおわおん"));
      assertEquals("ァアンヮ", hiraganaToWideKana("ぁあんゎ"));
    }

    @Test
    @DisplayName("対象文字でない場合はそのまま返る")
    void case2() {
      assertEquals("アイウabc", hiraganaToWideKana("アイウabc"));
    }

    @Test
    @DisplayName("空文字はそのまま返る")
    void case3() {
      assertEquals("", hiraganaToWideKana(""));
    }

  }

  @Nested
  @DisplayName("method: isInvalidNextLetter")
  class IsInvalidNextLetter {

    @Test
    @DisplayName("行頭禁止文字を渡すとTrueが返る")
    void case1() {
      assertTrue(isInvalidNextLetter("", "ャ"));
      assertTrue(isInvalidNextLetter("", "ン"));
      assertTrue(isInvalidNextLetter("", "ゅ"));
      assertTrue(isInvalidNextLetter("", "ん"));
      assertTrue(isInvalidNextLetter("あいう ", "ん"));
      assertTrue(isInvalidNextLetter("ほげ、", "ん"));
    }

    @Test
    @DisplayName("拗音(きゃ)の組み合わせでない場合はTrueが返る")
    void case2() {
      assertTrue(isInvalidNextLetter("は", "ゃ"));
      assertTrue(isInvalidNextLetter("キ", "ゃ"));
      assertTrue(isInvalidNextLetter("あ", "ゅ"));
      assertTrue(isInvalidNextLetter("ﾊﾞ", "ｬ"));
      assertTrue(isInvalidNextLetter("ｱﾊﾟ", "ｬ"));
    }

    @Test
    @DisplayName("連続した小文字の場合はTrueが返る")
    void case3() {
      assertTrue(isInvalidNextLetter("はぁ", "ぁ"));
      assertTrue(isInvalidNextLetter("イヤァ", "ァ"));
      assertTrue(isInvalidNextLetter("イヤァ", "ァ"));
      assertTrue(isInvalidNextLetter("ｲﾔｧ", "ｧ"));
    }

    @Test
    @DisplayName("拗音(きゃ)の組み合わせの場合はFalseが返る")
    void case4() {
      assertFalse(isInvalidNextLetter("キ", "ャ"));
      assertFalse(isInvalidNextLetter("き", "ゃ"));
      assertFalse(isInvalidNextLetter("き", "ょ"));
      assertFalse(isInvalidNextLetter("ﾁﾞ", "ｬ"));
      assertFalse(isInvalidNextLetter("ｱﾋﾟ", "ｬ"));
    }

    @Test
    @DisplayName("次の文字が小文字でない場合はFalseが返る")
    void case5() {
      assertFalse(isInvalidNextLetter("はぁ", "い"));
      assertFalse(isInvalidNextLetter("イヤァ", "？"));
      assertFalse(isInvalidNextLetter("ｲﾔｧ", "あ"));
    }

  }

}
