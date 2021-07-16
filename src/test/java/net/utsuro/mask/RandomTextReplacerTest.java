package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.utsuro.mask.MaskingUtil.CharType;

@ExtendWith(MockitoExtension.class)
class RandomTextReplacerTest extends RandomTextReplacer {

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
    @DisplayName("文字種指定無しは元の値と同じ文字種で生成")
    void case4() throws Exception {
      assertTrue((replace("あイｳ江0aZ？!", rule)).matches("[ぁ-ん][ァ-ヴ][\uFF61-\uFF9F]([々〇〻\u3400-\u9FFF\uF900-\uFAFF]|[\uD840-\uD87F][\uDC00-\uDFFF])[0-9][a-z][A-Z][！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣][!\"#$%&'()*+-./:;<=>?@\\[\\\\\\]^_`{|}~]"));
    }

    @Test
    @DisplayName("指定した文字種で生成")
    void case5() throws Exception {
      rule.setRandomGenCharType(CharType.ALPHA);
      assertTrue((replace("あイｳ江0aZ？!", rule)).matches("[a-zA-Z]+"));
    }

    @Test
    @DisplayName("開始位置を指定した場合は左からn文字除外してマスクする")
    void case6() throws Exception {
      rule.setUnmaksedLengthLeft(2);
      rule.setRandomGenCharType(CharType.NUMBER);
      assertTrue((replace("aBcdefG", rule)).matches("aB[0-9]{5}"));
      assertTrue((replace("aBcdefGe", rule)).matches("aB[0-9]{6}"));
    }

    @Test
    @DisplayName("終了位置を指定した場合は右からn文字除外してマスクする")
    void case7() throws Exception {
      rule.setUnmaksedLengthRight(2);
      rule.setRandomGenCharType(CharType.NUMBER);
      assertTrue((replace("aBcdefG", rule)).matches("[0-9]{5}fG"));
    }

    @Test
    @DisplayName("両方指定した場合は中央n文字マスクする")
    void case8() throws Exception {
      rule.setUnmaksedLengthLeft(2);
      rule.setUnmaksedLengthRight(2);
      rule.setRandomGenCharType(CharType.NUMBER);
      assertTrue((replace("aBcdefG", rule)).matches("aB[0-9]{3}fG"));
    }

    @Test
    @DisplayName("除外パターンを指定した場合はその文字はマスクされずに返る")
    void case9() throws Exception {
      rule.setUnmaksedLengthLeft(4);
      rule.setUnmaksedChar("[-－]");
      assertTrue((replace("0123-0000-0000-0000", rule)).matches("0123-[0-9]{4}-[0-9]{4}-[0-9]{4}"));
    }

    @Test
    @DisplayName("開始位置にマイナスを指定した場合は右からからn文字マスクする")
    void case10() throws Exception {
      rule.setUnmaksedLengthLeft(-2);
      rule.setRandomGenCharType(CharType.NUMBER);
      String ret = replace("aBcdefG", rule);
      assertTrue(ret.matches("aBcde[0-9]{2}"), String.format("[%s]はNG", ret));
      ret = replace("aBcdefGe", rule);
      assertTrue(ret.matches("aBcdef[0-9]{2}"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("空白はマスクされずに返るがホワイトスペースマスクも指定するとマスクされる")
    void case11() throws Exception {
      rule.setRandomGenCharType(CharType.NUMBER);
      assertTrue((replace(" abcd 4567　 8901 ", rule)).matches(" [0-9]{4} [0-9]{4}　 [0-9]{4} "));
      rule.useWhiteSpaceMask(true);
      assertTrue((replace(" abcd 4567　 8901 ", rule)).matches("[0-9]{17}"));
    }

    @Test
    @DisplayName("文字種不明は半角か全角で生成")
    void case12() throws Exception {
      rule.useWhiteSpaceMask(true);
      String[] arr = replace("　 ", rule).split("");
      assertTrue(MaskingUtil.isWideChar(arr[0]));
      assertFalse(MaskingUtil.isWideChar(arr[1]));
    }

    @Test
    @DisplayName("奇数指定した場合、奇数文字目だけマスク")
    void case20() throws Exception {
      rule.setUnmaksedLengthLeft(4);
      rule.useOddCharMask(true);
      rule.setRandomGenCharType(CharType.NUMBER);
      String ret = replace("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("1234[0-9]X[0-9]X[0-9]X[0-9]X[0-9]X[0-9]X"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("偶数指定した場合、偶数文字目だけマスク")
    void case21() throws Exception {
      rule.setUnmaksedLengthLeft(4);
      rule.useEvenCharMask(true);
      rule.setRandomGenCharType(CharType.NUMBER);
      String ret = replace("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("1234X[0-9]X[0-9]X[0-9]X[0-9]X[0-9]X[0-9]"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("マスク後に型変換する")
    void case22() throws Exception {
      rule.useLowerCase(true);
      rule.setRandomGenCharType(CharType.UPPER_ALPHA);
      String ret = replace("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("^[a-z]+$"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("除外値パターンにマッチしたら何もしない")
    void case23() throws Exception {
      rule.setIgnoreValue("ab.+f.$");
      assertEquals("aBcdefG", replace("aBcdefG", rule));
    }

    @Test
    @DisplayName("生成禁止文字が指定されている場合はその文字では置換しない(半角)")
    void case24() throws Exception {
      rule.setUnmaksedChar("[-－ ]");
      rule.setRandomNoGenCharPattern(Pattern.compile("[0]"));
      String ret = replace("1-2-3", rule);
      int count = 100; //試行回数
      for (int i = 0; i < count; i++) {
        assertTrue(ret.matches("[1-9]-[1-9]-[1-9]"), String.format("[%s]はNG", ret));
      }
    }

    @Test
    @DisplayName("生成禁止文字が指定されている場合はその文字では置換しない(全角)")
    void case25() throws Exception {
      rule.setUnmaksedChar("[-－ ]");
      rule.setRandomNoGenCharPattern(Pattern.compile("[０「」]"));
      String ret = replace("ダミーバンチ１－２－３", rule);
      int count = 1000; //試行回数
      for (int i = 0; i < count; i++) {
        assertTrue(ret.matches("[ァ-ヴ]{6}[１-９]－[１-９]－[１-９]"), String.format("[%s]はNG", ret));
      }
    }

    @Test
    @DisplayName("ランダム置換後、固定マスク")
    void case30() throws Exception {
      rule.useAfterRepEvenCharMask(true);
      rule.useAfterTextReplace(true);
      rule.setReplacementHalfNum("x");
      rule.setRandomGenCharType(CharType.NUMBER);
      String ret = replace("1234XXXXXXXXXXXX", rule);
      assertTrue(ret.matches("[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x[0-9]x"), String.format("[%s]はNG", ret));
    }

    @Test
    @DisplayName("インスタンスメソッドでも基本的には処理は同じ")
    void case90() throws Exception {
      assertEquals("あいう", execute("あいう", null));
      assertEquals(null, execute(null, rule));
      assertTrue(((String) execute(123456, rule)).matches("[0-9]{6}"));
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
      DataMask converter = spy(new RandomTextReplacer());
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