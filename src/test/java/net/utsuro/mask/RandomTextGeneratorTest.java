package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import net.utsuro.mask.MaskingUtil.CharType;

@ExtendWith(MockitoExtension.class)
class RandomTextGeneratorTest extends RandomTextGenerator {

  @Nested
  @DisplayName("method: useDatabase")
  class UseDatabase {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("デフォルトではDBは使用しない")
    void case1() throws Exception {
      assertFalse(useDatabase(rule));
    }

    @Test
    @DisplayName("isUniqueValue指定時はDBは使用する")
    void case2() throws Exception {
      rule.setUniqueValue(true);
      assertTrue(useDatabase(rule));
    }

    @Test
    @DisplayName("isDeterministicReplace指定時はDBは使用する")
    void case3() throws Exception {
      rule.setDeterministicReplace(true);
      assertTrue(useDatabase(rule));
    }

  }

  @Nested
  @DisplayName("method: getConnection")
  class GetConnection {

    @Mock
    Connection mockConn;

    @Test
    @DisplayName("デフォルトではDBは使用しないのでnullが返る")
    void case1() throws Exception {
      assertEquals(null, getConnection());
    }

    @Test
    @DisplayName("セットすればセットしたものが返る")
    void case2() throws Exception {
      setConnection(mockConn);
      assertEquals(mockConn, getConnection());
    }

  }

  @Nested
  @DisplayName("method: setConnection")
  class SetConnection {

    @Mock
    Connection mockConn;

    @Test
    @DisplayName("セットすればセットしたものが返る")
    void case1() throws Exception {
      setConnection(mockConn);
      assertEquals(mockConn, getConnection());
    }

  }

  @Nested
  @DisplayName("method: execute")
  class Execute {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      assertEquals("あいう", generate("あいう", null));
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
      assertEquals(String.class, ret.getClass());
      ret = generate("hoge", rule);
      assertNotEquals("hoge", ret);
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case4() throws Exception {
      rule.setIgnoreValue("^abc");
      assertNotEquals("xyzabc", generate("xyzabc", rule));
      assertEquals("abc1234", generate("abc1234", rule));
    }

    @Test
    @DisplayName("SJISサイズ指定なしなら元の文字列と同じサイズで返る")
    void case10() throws Exception {
      int count = 100; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = generate("xyzabc", rule);
        assertEquals(6, ret.getBytes("MS932").length);
      }
    }

    @Test
    @DisplayName("生成する最小SJISサイズのみを指定した場合は指定値～元の文字列長の範囲でランダム生成する")
    void case11() throws Exception {
      rule.setMinSjisByteCount(5);
      int count = 100; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = generate("xyzabcxyzabcxyzabc", rule);
        byte[] bytes = ret.getBytes("MS932");
        assertTrue(bytes.length >= 5);
        assertTrue(bytes.length <= 18);
      }
    }

    @Test
    @DisplayName("生成する最大SJISサイズのみを指定した場合は1～指定値の範囲でランダム生成する")
    void case12() throws Exception {
      rule.setMaxSjisByteCount(12);
      int count = 100; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = generate("xyzabc", rule);
        byte[] bytes = ret.getBytes("MS932");
        assertTrue(bytes.length >= 1);
        assertTrue(bytes.length <= 12);
      }
    }

    @Test
    @DisplayName("最小最大SJISサイズ両方を指定した場合は最小～最大の範囲でランダム生成する")
    void case13() throws Exception {
      rule.setMinSjisByteCount(20);
      rule.setMaxSjisByteCount(40);
      int count = 100; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = generate("xyzabc", rule);
        byte[] bytes = ret.getBytes("MS932");
        assertTrue(bytes.length >= 20);
        assertTrue(bytes.length <= 40);
      }
    }

    @Test
    @DisplayName("prefixやsuffixを指定した場合にランダム生成部分が0になった場合は空文字が返る")
    void case14() throws Exception {
      rule.setMinSjisByteCount(8);
      rule.setMaxSjisByteCount(8);
      rule.setPrefix("hoge");
      rule.setSuffix("fuga");
      assertEquals("", generate("xyzabc", rule));
    }

    @Test
    @DisplayName("文字種指定無しは元の値1文字目と同じ文字種で生成")
    void case20() throws Exception {
      String ret;
      ret = generate("あイｳ江0aZ？!", rule);
      assertTrue(ret.matches("^[ぁ-ん]+$"));
      ret = generate("イｳ江0aZ？あ!", rule);
      assertTrue(ret.matches("^[ァ-ン]+$"), String.format("%sは元の値1文字目と異なる文字種なのでNG", ret));
      ret = generate("ｳ江0aZ？あ!イ", rule);
      assertTrue(ret.matches("^[\\uFF61-\\uFF9F]+$"));
      ret = generate("江0aZ？あ!イｳ", rule);
      assertTrue(ret.matches("^([々〇〻\\u3400-\\u9FFF\\uF900-\\uFAFF]|[\\uD840-\\uD87F][\\uDC00-\\uDFFF])+$"));
      ret = generate("0aZ？あ!イｳ江", rule);
      assertTrue(ret.matches("^[0-9]+$"));
      ret = generate("aZ？あ!イｳ江0", rule);
      assertTrue(ret.matches("^[a-z]+$"));
      ret = generate("Z？あ!イｳ江0a", rule);
      assertTrue(ret.matches("^[A-Z]+$"));
      ret = generate("？あ!イｳ江0aZ", rule);
      assertTrue(ret.matches("^[！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣]+$"));
      ret = generate("!イｳ江0aZ？", rule);
      assertTrue(ret.matches("^[!-~]+$"));
    }

    @Test
    @DisplayName("指定した文字種で生成")
    void case21() throws Exception {
      rule.setRandomGenCharType(CharType.ALPHA);
      assertTrue((generate("あイｳ江0aZ？!", rule)).matches("^[a-zA-Z]+$"));
      rule.setRandomGenCharType(CharType.HIRAGANA);
      assertTrue((generate("あイｳ江0aZ？!", rule)).matches("^[ぁ-ん]+$"));
      rule.setRandomGenCharType(CharType.NUMBER);
      assertTrue((generate("あイｳ江0aZ？!", rule)).matches("^[0-9]+$"));
    }

    @Test
    @DisplayName("文字種指定無しで元の文字種不明はALLで生成")
    void case22() throws Exception {
      rule.setMaxSjisByteCount(256);
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
      // 全角文字
      for (int i = 0; i < count; i++) {
        String ret = generate("", rule);
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
    @DisplayName("生成除外文字を指定するとその文字は生成されない")
    void case23() throws Exception {
      rule.setMinSjisByteCount(10);
      rule.setMaxSjisByteCount(20);
      rule.setRandomNoGenChar("[2-4]");
      rule.setRandomGenCharType(CharType.NUMBER);
      assertTrue(generate("xyzabc", rule).matches("^[^2-4]+$"));
    }

    @Test
    @DisplayName("suffixを指定すると末尾にsuffixが付与されて返る")
    void case24() throws Exception {
      rule.setMinSjisByteCount(10);
      rule.setMaxSjisByteCount(20);
      rule.setSuffix("fuga");
      assertTrue(generate("xyzabc", rule).endsWith("fuga"));
    }

    @Test
    @DisplayName("suffixを指定すると末尾にsuffixが付与されて返る")
    void case25() throws Exception {
      rule.setMinSjisByteCount(10);
      rule.setMaxSjisByteCount(20);
      rule.setSuffix("fuga");
      assertTrue(generate("xyzabc", rule).endsWith("fuga"));
    }

    @Test
    @DisplayName("マスク後に型変換する(英大文字)")
    void case30() throws Exception {
      rule.useLowerCase(true);
      rule.setRandomGenCharType(CharType.UPPER_ALPHA);
      String ret = generate("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("^[a-z]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("マスク後に型変換する(英小文字)")
    void case31() throws Exception {
      rule.useUpperCase(true);
      rule.setRandomGenCharType(CharType.LOWER_ALPHA);
      String ret = generate("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("^[A-Z]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("マスク後に型変換する(カナ大文字)")
    void case32() throws Exception {
      rule.useUpperCaseKana(true);
      rule.setRandomGenCharType(CharType.HALF_KANA);
      rule.setPrefix("ｫｬｯはぃぃョ");
      String ret = generate("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("^ｵﾔﾂはいいヨ[\\uFF61-\\uFF9F]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("マスク後に型変換する(半角カナ)")
    void case33() throws Exception {
      rule.useHalfKana(true);
      rule.setRandomGenCharType(CharType.WIDE_KANA);
      rule.setPrefix("おやつ");
      String ret = generate("1234XXXXXXXXXXXX", rule);
      // 「ヱ」「ヰ」「ヵ」「ヶ」「ヮ」が全角カナで生成されると半角変換不可なので許容する
      assertTrue(ret.matches("^ｵﾔﾂ[\\uFF61-\\uFF9Fヱヰヵヶヮ]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("マスク後に型変換する(全角カナ)")
    void case34() throws Exception {
      rule.useWideKana(true);
      rule.setRandomGenCharType(CharType.HIRAGANA);
      rule.setPrefix("おやつ");
      String ret = generate("1234XXXXXXXXXXXX", rule);
      // 「ヱ」「ヰ」「ヵ」「ヶ」「ヮ」が全角カナで生成されると半角変換不可なので許容する
      assertTrue(ret.matches("^オヤツ[ァ-ン]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("マスク後に型変換する(ひらがな)")
    void case35() throws Exception {
      rule.useHiragana(true);
      rule.setRandomGenCharType(CharType.HALF_KANA);
      rule.setPrefix("ｵﾔﾂ");
      String ret = generate("1234XXXXXXXXXXXX", rule);
      // 「ヱ」「ヰ」「ヵ」「ヶ」「ヮ」が全角カナで生成されると半角変換不可なので許容する
      assertTrue(ret.matches("^おやつ[ぁ-ん]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("ランダム置換後、固定マスク")
    void case40() throws Exception {
      rule.useAfterRepEvenCharMask(true);
      rule.useAfterTextReplace(true);
      rule.setReplacementHalfNum("x");
      rule.setRandomGenCharType(CharType.NUMBER);
      String ret = generate("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("インスタンスメソッドでも基本的には処理は同じ")
    void case90() throws Exception {
      assertEquals("あいう", execute("あいう", null));
      assertEquals(null, execute(null, rule));
      assertNotEquals("123456", execute("123456", rule));
      assertTrue(((String) execute(123456, rule)).matches("[0-9]{6}"));
      rule.setNullReplace(true);
      assertNotEquals(null, execute(null, rule));
    }

  }

  @Nested
  @DisplayName("method: execute with Database")
  class ExecuteWithDatabase {

    MaskingRule rule = new MaskingRule();
    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @Test
    @DisplayName("一貫性テスト")
    void case1() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);

      setConnection(mockConn);
      rule.setDeterministicReplace(true);
      rule.setUniqueId("HOGE");

      // モックの設定: 1回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String ret1 = (String) execute("1234XXXXXXXXXXXX", rule);

      // モックの設定: 2回目は値がある想定なのでレコードあり、さっき登録した値を返す
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getString("output_val")).thenReturn(ret1);

      String ret2 = (String) execute("1234XXXXXXXXXXXX", rule);

      // モックの設定: 3回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String ret3 = (String) execute("4567XXXXXXXXXXXX", rule);
      String ret4 = (String) execute(null, rule);
      assertEquals(ret1, ret2, String.format("[%s]<>[%s]はNG", ret1, ret2));
      assertFalse(ret1.equals(ret3), String.format("[%s]=[%s]はNG", ret1, ret3));
      assertEquals(null, ret4);
    }

    @Test
    @DisplayName("ユニーク性テスト")
    void case2() throws Exception {
      // モックの設定
      when(mockConn.isClosed()).thenReturn(false);
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.execute()).thenReturn(true);

      // モックの設定： 1回生成した値で呼ばれたらtrueで返す
      List<String> retList = new ArrayList<>();
      DataMask converter = spy(new RandomTextGenerator());
      when(converter.isExistsInUniqueList(anyString(), anyString())).thenAnswer(new Answer<Boolean>() {
        public Boolean answer(InvocationOnMock invocation) {
          Object[] args = invocation.getArguments();
          return Boolean.valueOf(retList.indexOf(args[1]) >= 0);
        }
      });

      converter.setConnection(mockConn);
      rule.setUniqueValue(true);
      rule.setUniqueId("HOGE");
      int count = 10; //試行回数
      for (int i = 0; i < count; i++) {
        String ret = (String) converter.execute("1", rule);
        int idx = retList.indexOf(ret);
        assertFalse(idx >= 0, String.format("[%d回目:%s]は%d回目ですでに生成されているのでNG", i, ret, idx));
        retList.add(ret);
      }
      assertEquals(10, retList.size());
      for (int i = 0; i < count; i++) {
        // ユニークなので一通り存在するはず
        assertTrue(retList.indexOf(Integer.toString(i)) >= 0);
        // 1回は必ず登場する
        verify(converter, atLeast(1)).isExistsInUniqueList("HOGE", Integer.toString(i));
        // 23回以上(50%)同じ値で呼ばれていたら偏りすぎ
        verify(converter, atMost(22)).isExistsInUniqueList("HOGE", Integer.toString(i));
      }
    }

    @Test
    @DisplayName("一貫性NGテスト")
    void case3() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      // insert時にexecuteで重複エラーを発生させる
      doThrow(new SQLIntegrityConstraintViolationException()).when(mockPreparedStmnt).execute();
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.FALSE);

      try {
        setConnection(mockConn);
        rule.setDeterministicReplace(true);
        rule.setUniqueId("HOGE");
        String ret = (String) execute("1234XXXXXXXXXXXX", rule);
        fail(String.format("%s が NGにならなかった", ret));
      } catch(SQLIntegrityConstraintViolationException e) {
        assertEquals("5回重複してユニークリストの登録に失敗しました。", e.getMessage());
      } finally {
        mockConn.close();
      }
    }

  }

}
