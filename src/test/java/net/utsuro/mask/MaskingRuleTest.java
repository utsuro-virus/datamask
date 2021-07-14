package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.utsuro.mask.MaskingUtil.CharType;

class MaskingRuleTest extends MaskingRule {

  @Nested
  @DisplayName("constructor: copyConstructor")
  class TestConstructor {

    @Test
    @DisplayName("コピー元を引き渡すと内容をコピーして生成する")
    void case1() throws Exception {
      MaskingRule ruleA = new MaskingRule();
      ruleA.setBeginIndex(1);
      ruleA.setUnmaksedCharPattern(Pattern.compile("[A-z]", Pattern.MULTILINE));
      MaskingRule ruleB = new MaskingRule();
      assertNotEquals(ruleA, ruleB);
      ruleB = new MaskingRule(ruleA);
      assertEquals(ruleA, ruleB);
      ruleB.setUnmaksedCharPattern(Pattern.compile("[A-z]", Pattern.MULTILINE));
      ruleB.setIgnoreValuePattern(Pattern.compile("^[0-9]+$"));
      ruleB.setRandomNoGenCharPattern(Pattern.compile("[ぁ-ん]", Pattern.CASE_INSENSITIVE));
      ruleB.setMinDecimalValue(new BigDecimal("0.0002"));
      ruleB.setMaxDecimalValue(new BigDecimal("-1234567890"));
      ruleB.setMinDate(LocalDateTime.parse("2020-12-31T00:01:59"));
      ruleB.setMaxDate(LocalDateTime.parse("2021-01-16T18:56:01"));
      ruleB.setPicupList(new String[] {"hoge", "fuga"});
      ruleB.setPicupWeights(new int[] {1, 2});
      ruleB.setToClassNames(Arrays.asList(new String[] {"hogeClass", "fugaClass"}));
      assertNotEquals(ruleA, ruleB);
      ruleA = new MaskingRule(ruleB);
      assertEquals(ruleA, ruleB);
      ruleA.setMinDecimalValue(new BigDecimal("999"));
      ruleA.setMinDate(LocalDateTime.parse("2099-12-31T00:01:59"));
      assertNotEquals(ruleA, ruleB);
    }

  }

  @Nested
  @DisplayName("method: getUnmaksedLengthRight")
  class GetUnmaksedLengthRight {

    @Test
    @DisplayName("0以上はそのまま返る")
    void case1() throws Exception {
      setUnmaksedLengthRight(0);
      assertEquals(0, getUnmaksedLengthRight());
      setUnmaksedLengthRight(10);
      assertEquals(10, getUnmaksedLengthRight());
    }

    @Test
    @DisplayName("0未満は0に丸める")
    void case2() throws Exception {
      setUnmaksedLengthRight(-1);
      assertEquals(0, getUnmaksedLengthRight());
      setUnmaksedLengthRight(-10);
      assertEquals(0, getUnmaksedLengthRight());
    }

  }

  @Nested
  @DisplayName("method: getUnmaksedChar/setUnmaksedChar")
  class GetSetUnmaksedChar {

    @Test
    @DisplayName("Patternしか指定していない場合はPatternの文字列表現が返る")
    void case1() throws Exception {
      Pattern ptn = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE);
      assertEquals("", getUnmaksedChar());
      setUnmaksedCharPattern(ptn);
      assertEquals(ptn.toString(), getUnmaksedChar());
    }

    @Test
    @DisplayName("文字列でセットしてもPatternにもセットされる")
    void case2() throws Exception {
      Pattern ptn = Pattern.compile("[a-z]+");
      setUnmaksedChar(null);
      assertEquals(null, getUnmaksedCharPattern());
      setUnmaksedChar(ptn.toString());
      assertEquals(ptn.toString(), getUnmaksedCharPattern().toString());
      setUnmaksedChar("");
      assertEquals(null, getUnmaksedCharPattern());
    }

  }

  @Nested
  @DisplayName("method: getIgnoreValue/setIgnoreValue")
  class GetSetIgnoreValue {

    @Test
    @DisplayName("Patternしか指定していない場合はPatternの文字列表現が返る")
    void case1() throws Exception {
      Pattern ptn = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE);
      assertEquals("", getIgnoreValue());
      setIgnoreValuePattern(ptn);
      assertEquals(ptn.toString(), getIgnoreValue());
    }

    @Test
    @DisplayName("文字列でセットしてもPatternにもセットされる")
    void case2() throws Exception {
      Pattern ptn = Pattern.compile("[a-z]+");
      setIgnoreValue(null);
      assertEquals(null, getIgnoreValuePattern());
      setIgnoreValue(ptn.toString());
      assertEquals(ptn.toString(), getIgnoreValuePattern().toString());
      setIgnoreValue("");
      assertEquals(null, getIgnoreValuePattern());
    }

  }

  @Nested
  @DisplayName("method: getRandomNoGenChar/setRandomNoGenChar")
  class GetSetRandomNoGenChar {

    @Test
    @DisplayName("Patternしか指定していない場合はPatternの文字列表現が返る")
    void case1() throws Exception {
      Pattern ptn = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE);
      assertEquals("", getRandomNoGenChar());
      setRandomNoGenCharPattern(ptn);
      assertEquals(ptn.toString(), getRandomNoGenChar());
    }

    @Test
    @DisplayName("文字列でセットしてもPatternにもセットされる")
    void case2() throws Exception {
      Pattern ptn = Pattern.compile("[a-z]+");
      setRandomNoGenChar(null);
      assertEquals(null, getRandomNoGenCharPattern());
      setRandomNoGenChar(ptn.toString());
      assertEquals(ptn.toString(), getRandomNoGenCharPattern().toString());
      setRandomNoGenChar("");
      assertEquals(null, getRandomNoGenCharPattern());
    }

  }

  @Nested
  @DisplayName("method: useReplacementHalfChar")
  class UseReplacementHalfChar {

    @Test
    @DisplayName("指定されていればtrueされていなければfalseが返る")
    void case1() throws Exception {
      assertTrue(useReplacementHalfChar()); // デフォルト値あり
      setReplacementHalfChar(null);
      assertFalse(useReplacementHalfChar());
      setReplacementHalfChar("X");
      assertTrue(useReplacementHalfChar());
      setReplacementHalfChar("");
      assertFalse(useReplacementHalfChar());
    }

  }

  @Nested
  @DisplayName("method: useReplacementHalfNum")
  class useReplacementHalfNum {

    @Test
    @DisplayName("指定されていればtrueされていなければfalseが返る")
    void case1() throws Exception {
      assertTrue(useReplacementHalfNum()); // デフォルト値あり
      setReplacementHalfNum(null);
      assertFalse(useReplacementHalfNum());
      setReplacementHalfNum("9");
      assertTrue(useReplacementHalfNum());
      setReplacementHalfNum("");
      assertFalse(useReplacementHalfNum());
    }

  }

  @Nested
  @DisplayName("method: useReplacementWideChar")
  class useReplacementWideChar {

    @Test
    @DisplayName("指定されていればtrueされていなければfalseが返る")
    void case1() throws Exception {
      assertTrue(useReplacementWideChar()); // デフォルト値あり
      setReplacementWideChar(null);
      assertFalse(useReplacementWideChar());
      setReplacementWideChar("※");
      assertTrue(useReplacementWideChar());
      setReplacementWideChar("");
      assertFalse(useReplacementWideChar());
    }

  }

  @Nested
  @DisplayName("method: useReplacementWideNum")
  class useReplacementWideNum {

    @Test
    @DisplayName("指定されていればtrueされていなければfalseが返る")
    void case1() throws Exception {
      assertTrue(useReplacementWideNum()); // デフォルト値あり
      setReplacementWideNum(null);
      assertFalse(useReplacementWideNum());
      setReplacementWideNum("※");
      assertTrue(useReplacementWideNum());
      setReplacementWideNum("");
      assertFalse(useReplacementWideNum());
    }

  }

  @Nested
  @DisplayName("method: useRandomGenCharType")
  class useRandomGenCharType {

    @Test
    @DisplayName("指定されていればtrueされていなければfalseが返る")
    void case1() throws Exception {
      assertFalse(useRandomGenCharType()); // デフォルト値なし
      setRandomGenCharType(null);
      assertFalse(useRandomGenCharType());
      setRandomGenCharType(CharType.WIDE);
      assertTrue(useRandomGenCharType());
      setRandomGenCharType(CharType.UNKNOWN);
      assertFalse(useRandomGenCharType());
    }

  }

  @Nested
  @DisplayName("method: getMinValue/setMinValue")
  class GetSetMinValue {

    @Test
    @DisplayName("Decimalしか指定していない場合はDecimalの文字列表現が返る")
    void case1() throws Exception {
      assertEquals("", getMinValue());
      setMinDecimalValue(new BigDecimal("0.002"));
      assertEquals("0.002", getMinValue());
      setMinValue("1");
      setMinDecimalValue(null);
      assertEquals("1", getMinValue());
      setMinDecimalValue(new BigDecimal("123456789"));
      assertEquals("123456789", getMinValue());
    }

    @Test
    @DisplayName("文字列でセットしてもDecimalにもセットされる")
    void case2() throws Exception {
      setMinValue(null);
      assertEquals(null, getMinDecimalValue());
      setMinValue("0.002");
      assertEquals("0.002", getMinDecimalValue().toString());
      setMinValue("");
      assertEquals(null, getMinDecimalValue());
    }

    @Test
    @DisplayName("数値でない文字列は無視されてDecimalにはセットされない")
    void case3() throws Exception {
      setMinValue("-12.345");
      assertEquals("-12.345", getMinDecimalValue().toString());
      setMinValue("abc");
      assertEquals(null, getMinDecimalValue());
    }

  }

  @Nested
  @DisplayName("method: getMaxValue/setMaxValue")
  class GetSetMaxValue {

    @Test
    @DisplayName("Decimalしか指定していない場合はDecimalの文字列表現が返る")
    void case1() throws Exception {
      assertEquals("", getMaxValue());
      setMaxDecimalValue(new BigDecimal("0.002"));
      assertEquals("0.002", getMaxValue());
      setMaxValue("1");
      setMaxDecimalValue(null);
      assertEquals("1", getMaxValue());
      setMaxDecimalValue(new BigDecimal("123456789"));
      assertEquals("123456789", getMaxValue());
    }

    @Test
    @DisplayName("文字列でセットしてもDecimalにもセットされる")
    void case2() throws Exception {
      setMaxValue(null);
      assertEquals(null, getMaxDecimalValue());
      setMaxValue("0.002");
      assertEquals("0.002", getMaxDecimalValue().toString());
      setMaxValue("");
      assertEquals(null, getMaxDecimalValue());
    }

    @Test
    @DisplayName("数値でない文字列は無視されてDecimalにはセットされない")
    void case3() throws Exception {
      setMaxValue("-12.345");
      assertEquals("-12.345", getMaxDecimalValue().toString());
      setMaxValue("abc");
      assertEquals(null, getMaxDecimalValue());
    }

  }

  @Nested
  @DisplayName("method: getSelectListSeqNoColName/setSelectListSeqNoColName")
  class GetSetSelectListSeqNoColName {

    @Test
    @DisplayName("指定なしの場合はseqnoが返る")
    void case1() throws Exception {
      assertEquals("seqno", getSelectListSeqNoColName());
      setSelectListSeqNoColName(null);
      assertEquals("seqno", getSelectListSeqNoColName());
      setSelectListSeqNoColName("HOGE");
      assertEquals("HOGE", getSelectListSeqNoColName());
      setSelectListSeqNoColName("");
      assertEquals("seqno", getSelectListSeqNoColName());
    }

  }

}
