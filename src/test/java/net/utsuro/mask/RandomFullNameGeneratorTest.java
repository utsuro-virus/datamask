package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class RandomFullNameGeneratorTest extends RandomFullNameGenerator {

  @Nested
  @DisplayName("method: useDatabase")
  class UseDatabase {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("常にDBは使用する")
    void case1() throws Exception {
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
    @Mock
    Connection mockConn;

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
    @DisplayName("配列でない場合はそのまま返る")
    void case3() throws Exception {
      assertEquals(123, execute(123, rule));
      assertEquals("hoge", execute("hoge", rule));
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
    @DisplayName("null置換ありなら処理されて返る")
    void case1() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong(anyString())).thenReturn(1L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      when(mockResultSet.getString("yomi")).thenReturn("hoge", "fuga");
      when(mockResultSet.getString("name_type")).thenReturn("LAST_NAME", "FIRST_NAME");
      when(mockResultSet.getString("kanji")).thenReturn("名１", "姓１");
      // コネクションをセット
      setConnection(mockConn);

      rule.setFullNameFormat("%firstNameKanji %lastNameKanji");
      rule.setNullReplace(true);

      String[] ret = (String[]) execute(null, rule);
      assertEquals("姓１ 名１", ret[0]);
    }

    @Test
    @DisplayName("一貫性テスト")
    void case2() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      DataMask converter = spy(new RandomFullNameGenerator());
      when(converter.addUniqueList(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
      when(((RandomFullNameGenerator) converter).generate(any(String[].class), any(MaskingRule.class)))
          .thenReturn(new String[] {"里中 智"}, new String[] {"殿馬 一人"});
      // コネクションをセット
      converter.setConnection(mockConn);

      rule.setDeterministicReplace(true);
      rule.setUniqueId("HOGE");
      rule.setFullNameFormat("%firstNameKanji %lastNameKanji");

      // モックの設定: 1回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);


      String[] ret1 = (String[]) converter.execute(new String[] {"山田", "太郎"}, rule);

      // モックの設定: 2回目は値がある想定なのでレコードあり、さっき登録した値を返す
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getString("output_val")).thenReturn(String.join("<>", ret1));

      String[] ret2 = (String[]) converter.execute(new String[] {"山田", "太郎"}, rule);

      // モックの設定: 3回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String[] ret3 = (String[]) converter.execute(new String[] {"岩鬼", "正美"}, rule);

      // モックの設定: 4回目はTrimすれば値がある想定なのでレコードあり、さっき登録した値を返す
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getString("output_val")).thenReturn(String.join("<>", ret1));

      rule.setBeforeTrim(true);
      String[] ret4 = (String[]) converter.execute(new String[] {" 山田 ", " 太郎     "}, rule);

      assertEquals(ret1[0], ret2[0], String.format("[%s]<>[%s]はNG", ret1[0], ret2[0]));
      assertEquals(ret1[0], ret4[0], String.format("[%s]<>[%s]はNG", ret1[0], ret4[0]));
      assertFalse(ret1[0].equals(ret3[0]), String.format("[%s]=[%s]はNG", ret1[0], ret3[0]));
    }

    @Test
    @DisplayName("ユニーク性テスト")
    void case3() throws Exception {
      String[] exp = {
          "山田, 太郎", "岩鬼, 正美", "里中, 智", "殿馬, 一人", "土井垣, 将",
          "微笑, 三太郎", "不知火, 守", "犬飼, 小次郎", "犬飼, 知三郎", "中西, 球道"};
      // モックの設定
      when(mockConn.isClosed()).thenReturn(false);
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.execute()).thenReturn(true);

      // モックの設定： 1回生成した値で呼ばれたらtrueで返す
      List<String> retList = new ArrayList<>();
      DataMask converter = spy(new RandomFullNameGenerator());
      when(((RandomFullNameGenerator) converter).generate(any(String[].class), any(MaskingRule.class)))
        .thenReturn(new String[] {exp[0]}, new String[] {exp[1]},
           new String[] {exp[2]}, new String[] {exp[0]}, new String[] {exp[3]},
           new String[] {exp[4]}, new String[] {exp[5]}, new String[] {exp[6]},
           new String[] {exp[1]}, new String[] {exp[7]}, new String[] {exp[8]},
           new String[] {exp[9]});
      when(converter.isExistsInUniqueList(anyString(), anyString())).thenAnswer(new Answer<Boolean>() {
        public Boolean answer(InvocationOnMock invocation) {
          Object[] args = invocation.getArguments();
          return Boolean.valueOf(retList.indexOf(args[1]) >= 0);
        }
      });

      converter.setConnection(mockConn);
      rule.setUniqueValue(true);
      rule.setUniqueId("HOGE");
      rule.setFullNameFormat("%firstNameKanji %lastNameKanji");
      int count = 10; //試行回数
      for (int i = 0; i < count; i++) {
        String[] ret = (String[]) converter.execute(new String[] {"1"}, rule);
        int idx = retList.indexOf(ret[0]);
        assertFalse(idx >= 0, String.format("[%d回目:%s]は%d回目ですでに生成されているのでNG", i, ret, idx));
        retList.add(ret[0]);
      }
      assertEquals(10, retList.size());
      for (int i = 0; i < count; i++) {
        // ユニークなので一通り存在するはず
        assertTrue(retList.indexOf(exp[i]) >= 0);
        // 1回は必ず登場する
        verify(converter, atLeast(1)).isExistsInUniqueList("HOGE", exp[i]);
      }
    }

    @Test
    @DisplayName("一貫性NGテスト")
    void case4() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      // insert時にexecuteで重複エラーを発生させる
      doThrow(new SQLIntegrityConstraintViolationException()).when(mockPreparedStmnt).execute();
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.FALSE);
      DataMask converter = spy(new RandomFullNameGenerator());
      when(((RandomFullNameGenerator) converter).generate(any(String[].class), any(MaskingRule.class)))
          .thenReturn(new String[] {"里中 智"});
      // コネクションをセット
      converter.setConnection(mockConn);

      try {
        setConnection(mockConn);
        rule.setDeterministicReplace(true);
        rule.setUniqueId("HOGE");
        rule.setFullNameFormat("%firstNameKanji %lastNameKanji");
        String ret = (String) converter.execute(new String[] {"1234XXXXXXXXXXXX"}, rule);
        fail(String.format("%s が NGにならなかった", ret));
      } catch(SQLIntegrityConstraintViolationException e) {
        assertEquals("5回重複してユニークリストの登録に失敗しました。", e.getMessage());
      } finally {
        mockConn.close();
      }
    }

  }

  @Nested
  @DisplayName("method: generate")
  class Generate {

    MaskingRule rule = new MaskingRule();
    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      String[] params = new String[] {"あいう"};
      assertEquals(params, generate(params, null));
    }

    @Test
    @DisplayName("nullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, generate(null, rule));
    }

    @Test
    @DisplayName("null置換ありなら処理されて返る")
    void case3() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong(anyString())).thenReturn(1L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      when(mockResultSet.getString("yomi")).thenReturn("hoge", "fuga");
      when(mockResultSet.getString("name_type")).thenReturn("LAST_NAME", "FIRST_NAME");
      when(mockResultSet.getString("kanji")).thenReturn("名１", "姓１");
      // コネクションをセット
      setConnection(mockConn);

      rule.setFullNameFormat("%firstNameKanji %lastNameKanji");
      rule.setNullReplace(true);

      String[] ret = generate(null, rule);
      assertEquals("姓１ 名１", ret[0]);
    }

    @Test
    @DisplayName("書式が無い場合はそのまま返る")
    void case4() throws Exception {
      String[] params = new String[] {"あいう"};
      rule.setFullNameFormat(null);
      assertEquals(params, generate(params, rule));
      rule.setFullNameFormat("");
      assertEquals(params, generate(params, rule));
    }

    @Test
    @DisplayName("データ無しの場合nullが返る(件数ゼロ)")
    void case5() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.FALSE);
      // コネクションをセット
      setConnection(mockConn);

      rule.setFullNameFormat("%firstNameKanji %lastNameKanji");
      assertEquals(null, generate(new String[] {"hoge"}, rule));
    }

    @Test
    @DisplayName("データ無しの場合nullが返る(対象IDのデータ無し)")
    void case6() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong("sei_maxseq")).thenReturn(12L);
      when(mockResultSet.getLong("mei_maxseq")).thenReturn(123L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      // コネクションをセット
      setConnection(mockConn);

      rule.setFullNameFormat("%firstNameKanji %lastNameKanji");
      assertEquals(null, generate(new String[] {"hoge"}, rule));
    }

    @Test
    @DisplayName("SQL文の組み立て検証")
    void case10() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong("sei_maxseq")).thenReturn(12L);
      when(mockResultSet.getLong("mei_maxseq")).thenReturn(123L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      // コネクションをセット
      setConnection(mockConn);

      rule.setSelectListSeqNoColName("idx");
      generate(new String[] {"テスト"}, rule);

      verify(mockConn, times(1)).prepareStatement("SELECT MAX(CASE WHEN name_type = 'LAST_NAME' THEN idx ELSE -1 END) AS sei_maxseq,MAX(CASE WHEN name_type = 'FIRST_NAME' THEN idx ELSE -1 END) AS mei_maxseq FROM m_jinmei");
      verify(mockConn, times(1)).prepareStatement("SELECT name_type, kanji, yomi FROM m_jinmei WHERE (name_type = 'LAST_NAME' AND idx = ?) OR (name_type = 'FIRST_NAME' AND idx = ?)");
    }
  }

  @Nested
  @DisplayName("method: generate with Database")
  class GenerateWithDatabase {

    MaskingRule rule = new MaskingRule();
    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong(anyString())).thenReturn(1L, 2L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      when(mockResultSet.getString("yomi")).thenReturn("どいがき", "しょう");
      when(mockResultSet.getString("name_type")).thenReturn("LAST_NAME", "FIRST_NAME");
      when(mockResultSet.getString("kanji")).thenReturn("土井垣", "将");
      // コネクションをセット
      setConnection(mockConn);
    }

    @Test
    @DisplayName("ランダムセレクト")
    void case1() throws Exception {
      String[] params = new String[] {"山田", "太郎", "ヤマダ", "タロウ"};
      rule.setFullNameFormat("%lastNameKanji,%firstNameKanji,%lastNameKana,%firstNameKana");
      String[] ret = generate(params, rule);
      assertEquals("土井垣", ret[0]);
      assertEquals("将", ret[1]);
      assertEquals("どいがき", ret[2]);
      assertEquals("しょう", ret[3]);
    }

    @Test
    @DisplayName("ランダムセレクト(カナ変換)")
    void case2() throws Exception {
      String[] params = new String[] {"山田", "太郎", "ヤマダ", "タロウ"};
      rule.setFullNameFormat("%lastNameKanji,%firstNameKanji,%lastNameKana,%firstNameKana");
      rule.useWideKana(true);
      String[] ret = generate(params, rule);
      assertEquals("土井垣", ret[0]);
      assertEquals("将", ret[1]);
      assertEquals("ドイガキ", ret[2]);
      assertEquals("ショウ", ret[3]);
    }

    @Test
    @DisplayName("ランダムセレクト(半角カナ変換)")
    void case3() throws Exception {
      String[] params = new String[] {"山田", "太郎", "ヤマダ", "タロウ"};
      rule.setFullNameFormat("%lastNameKanji,%firstNameKanji,%lastNameKana,%firstNameKana");
      rule.useHalfKana(true);
      String[] ret = generate(params, rule);
      assertEquals("土井垣", ret[0]);
      assertEquals("将", ret[1]);
      assertEquals("ﾄﾞｲｶﾞｷ", ret[2]);
      assertEquals("ｼｮｳ", ret[3]);
    }

    @Test
    @DisplayName("ランダムセレクト(かな大文字変換)")
    void case4() throws Exception {
      String[] params = new String[] {"山田", "太郎", "ヤマダ", "タロウ"};
      rule.setFullNameFormat("%lastNameKanji,%firstNameKanji,%lastNameKana,%firstNameKana");
      rule.useUpperCaseKana(true);
      String[] ret = generate(params, rule);
      assertEquals("土井垣", ret[0]);
      assertEquals("将", ret[1]);
      assertEquals("どいがき", ret[2]);
      assertEquals("しよう", ret[3]);
    }

    @Test
    @DisplayName("ランダムセレクト(カナ大文字変換)")
    void case5() throws Exception {
      String[] params = new String[] {"山田", "太郎", "ヤマダ", "タロウ"};
      rule.setFullNameFormat("%lastNameKanji,%firstNameKanji,%lastNameKana,%firstNameKana");
      rule.useUpperCaseKana(true);
      rule.useWideKana(true);
      String[] ret = generate(params, rule);
      assertEquals("土井垣", ret[0]);
      assertEquals("将", ret[1]);
      assertEquals("ドイガキ", ret[2]);
      assertEquals("シヨウ", ret[3]);
    }

    @Test
    @DisplayName("ランダムセレクト(半角カナ大文字変換)")
    void case6() throws Exception {
      String[] params = new String[] {"山田", "太郎", "ヤマダ", "タロウ"};
      rule.setFullNameFormat("%lastNameKanji,%firstNameKanji,%lastNameKana,%firstNameKana");
      rule.useUpperCaseKana(true);
      rule.useHalfKana(true);
      String[] ret = generate(params, rule);
      assertEquals("土井垣", ret[0]);
      assertEquals("将", ret[1]);
      assertEquals("ﾄﾞｲｶﾞｷ", ret[2]);
      assertEquals("ｼﾖｳ", ret[3]);
    }

  }

}
