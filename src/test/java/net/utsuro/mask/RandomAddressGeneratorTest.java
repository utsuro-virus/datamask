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
class RandomAddressGeneratorTest extends RandomAddressGenerator {

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
      when(mockResultSet.getLong(anyString())).thenReturn(1L, 2L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      // 郵便番号
      when(mockResultSet.getString("zip")).thenReturn("064-0822");
      // 都道府県カナ
      when(mockResultSet.getString("ken_furi")).thenReturn("ホッカイドウ");
      // 市区町村カナ
      when(mockResultSet.getString("city_furi")).thenReturn("サッポロシチュウオウク");
      // 町域カナ
      when(mockResultSet.getString("town_furi")).thenReturn("キタ０２ジョウニシ");
      // 字丁目カナ
      when(mockResultSet.getString("block_furi")).thenReturn(null);
      // 都道府県
      when(mockResultSet.getString("ken_name")).thenReturn("北海道");
      // 市区町村
      when(mockResultSet.getString("city_name")).thenReturn("札幌市中央区");
      // 町域
      when(mockResultSet.getString("town_name")).thenReturn("北二条西");
      // 字丁目
      when(mockResultSet.getString("block_name")).thenReturn(null);
      // コネクションをセット
      setConnection(mockConn);

      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.setNullReplace(true);
      rule.useBanchiGenerate(false);

      String[] ret = (String[]) execute(null, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西", ret[3]);
      assertEquals("", ret[4]);
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし", ret[7]);
      assertEquals("", ret[8]);
    }

    @Test
    @DisplayName("一貫性テスト")
    void case2() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      DataMask converter = spy(new RandomAddressGenerator());
      when(converter.addUniqueList(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);
      when(((RandomAddressGenerator) converter).generate(any(String[].class), any(MaskingRule.class)))
          .thenReturn(new String[] {"山形県東置賜郡高畠町小其塚１－２－３"}, new String[] {"神奈川県横浜市西区みなとみらいクイーンズタワーＣ １６階"});
      // コネクションをセット
      converter.setConnection(mockConn);

      rule.setDeterministicReplace(true);
      rule.setUniqueId("HOGE");
      rule.setAddrFormat("%pref%city%town%street");

      // モックの設定: 1回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);


      String[] ret1 = (String[]) converter.execute(new String[] {"北海道札幌市中央区北二条西２７丁目ダミー番地１－２－３"}, rule);

      // モックの設定: 2回目は値がある想定なのでレコードあり、さっき登録した値を返す
      when(mockResultSet.next()).thenReturn(true);
      when(mockResultSet.getString("output_val")).thenReturn(String.join("<>", ret1));

      String[] ret2 = (String[]) converter.execute(new String[] {"北海道札幌市中央区北二条西２７丁目ダミー番地１－２－３"}, rule);

      // モックの設定: 3回目は値が無い想定なのでレコード無し
      when(mockResultSet.next()).thenReturn(false);

      String[] ret3 = (String[]) converter.execute(new String[] {"北海道札幌市中央区北二条西２７丁目ダミー番地４－５－６"}, rule);
      assertEquals(ret1[0], ret2[0], String.format("[%s]<>[%s]はNG", ret1[0], ret2[0]));
      assertFalse(ret1[0].equals(ret3[0]), String.format("[%s]=[%s]はNG", ret1[0], ret3[0]));
    }

    @Test
    @DisplayName("ユニーク性テスト")
    void case3() throws Exception {
      String[] exp = {
          "北海道札幌市中央区北二条西２７丁目ダミー番地１－２－３",
          "山形県東置賜郡高畠町小其塚１－２－３",
          "神奈川県横浜市西区みなとみらいクイーンズタワーＣ １６階",
          "福井県福井市市ノ瀬町９９９－１２",
          "広島県呉市汐見町１２３４",
          "沖縄県八重山郡竹富町波照間５５５",
          "東京都品川区テスト住所ああああああ７－８２－９０１",
          "秋田県能代市下内崎９１８－２",
          "宮城県仙台市青葉区北根黒松４３２－５",
          "広島県広島市西区三滝本町３－７２－１６"};
      // モックの設定
      when(mockConn.isClosed()).thenReturn(false);
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
      when(mockPreparedStmnt.execute()).thenReturn(true);

      // モックの設定： 1回生成した値で呼ばれたらtrueで返す
      List<String> retList = new ArrayList<>();
      DataMask converter = spy(new RandomAddressGenerator());
      when(((RandomAddressGenerator) converter).generate(any(String[].class), any(MaskingRule.class)))
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
      rule.setAddrFormat("%pref%city%town%street");
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
      DataMask converter = spy(new RandomAddressGenerator());
      when(((RandomAddressGenerator) converter).generate(any(String[].class), any(MaskingRule.class)))
          .thenReturn(new String[] {"山形県東置賜郡高畠町小其塚１－２－３"});
      // コネクションをセット
      converter.setConnection(mockConn);

      try {
        setConnection(mockConn);
        rule.setDeterministicReplace(true);
        rule.setUniqueId("HOGE");
        rule.setAddrFormat("%pref%city%town%street");
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
      when(mockResultSet.getString(anyString())).thenReturn("hoge");
      // コネクションをセット
      setConnection(mockConn);

      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.setNullReplace(true);

      String[] ret = generate(null, rule);
      assertNotEquals(null, ret[0]);
    }

    @Test
    @DisplayName("書式が無い場合はそのまま返る")
    void case4() throws Exception {
      String[] params = new String[] {"あいう"};
      rule.setAddrFormat(null);
      assertEquals(params, generate(params, rule));
      rule.setAddrFormat("");
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

      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      assertEquals(null, generate(new String[] {"hoge"}, rule));
    }

    @Test
    @DisplayName("データ無しの場合nullが返る(対象IDのデータ無し)")
    void case6() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong("maxseq")).thenReturn(12L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      // コネクションをセット
      setConnection(mockConn);

      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      assertEquals(null, generate(new String[] {"hoge"}, rule));
    }

    @Test
    @DisplayName("SQL文の組み立て検証")
    void case10() throws Exception {
      // モックの設定
      when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.executeQuery()).thenReturn(mockResultSet);
      when(mockResultSet.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
      when(mockResultSet.getLong("maxseq")).thenReturn(12L);
      doNothing().when(mockPreparedStmnt).setLong(anyInt(), anyLong());
      // コネクションをセット
      setConnection(mockConn);

      rule.setSelectListSeqNoColName("idx");
      generate(new String[] {"テスト"}, rule);

      verify(mockConn, times(1)).prepareStatement("SELECT MAX(idx) AS maxseq FROM m_postal_code");
      verify(mockConn, times(1)).prepareStatement("SELECT * FROM m_postal_code WHERE idx = ?");
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
      // 郵便番号
      when(mockResultSet.getString("zip")).thenReturn("064-0822");
      // 都道府県カナ
      when(mockResultSet.getString("ken_furi")).thenReturn("ホッカイドウ");
      // 市区町村カナ
      when(mockResultSet.getString("city_furi")).thenReturn("サッポロシチュウオウク");
      // 町域カナ
      when(mockResultSet.getString("town_furi")).thenReturn("キタ０２ジョウニシ");
      // 字丁目カナ
      when(mockResultSet.getString("block_furi")).thenReturn("２７チョウメ");
      // 都道府県
      when(mockResultSet.getString("ken_name")).thenReturn("北海道");
      // 市区町村
      when(mockResultSet.getString("city_name")).thenReturn("札幌市中央区");
      // 町域
      when(mockResultSet.getString("town_name")).thenReturn("北二条西");
      // 字丁目
      when(mockResultSet.getString("block_name")).thenReturn("２７丁目");
      // 都道府県コード
      when(mockResultSet.getInt("ken_id")).thenReturn(1);
      // 市区町村コード
      when(mockResultSet.getInt("city_id")).thenReturn(1101);
      // 住所コード
      when(mockResultSet.getInt("id")).thenReturn(60872100);
      // コネクションをセット
      setConnection(mockConn);
    }

    @Test
    @DisplayName("ランダムセレクト")
    void case1() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし２７ちょうめ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(カナ変換)")
    void case2() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useWideKana(true);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ホッカイドウ", ret[5]);
      assertEquals("サッポロシチュウオウク", ret[6]);
      assertEquals("キタ０２ジョウニシ２７チョウメ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(半角カナ変換)")
    void case3() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useHalfKana(true);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ﾎｯｶｲﾄﾞｳ", ret[5]);
      assertEquals("ｻｯﾎﾟﾛｼﾁｭｳｵｳｸ", ret[6]);
      assertEquals("ｷﾀ02ｼﾞｮｳﾆｼ27ﾁｮｳﾒ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(かな大文字変換)")
    void case4() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useUpperCaseKana(true);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ほつかいどう", ret[5]);
      assertEquals("さつぽろしちゆうおうく", ret[6]);
      assertEquals("きた０２じようにし２７ちようめ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(カナ大文字変換)")
    void case5() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useUpperCaseKana(true);
      rule.useWideKana(true);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ホツカイドウ", ret[5]);
      assertEquals("サツポロシチユウオウク", ret[6]);
      assertEquals("キタ０２ジヨウニシ２７チヨウメ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(半角カナ大文字変換)")
    void case6() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useUpperCaseKana(true);
      rule.useHalfKana(true);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ﾎﾂｶｲﾄﾞｳ", ret[5]);
      assertEquals("ｻﾂﾎﾟﾛｼﾁﾕｳｵｳｸ", ret[6]);
      assertEquals("ｷﾀ02ｼﾞﾖｳﾆｼ27ﾁﾖｳﾒ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(郵便番号ハイフン抜き)")
    void case7() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.usePostCodeFormat(false);
      String[] ret = generate(params, rule);
      assertEquals("0640822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし２７ちょうめ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("ランダムセレクト(番地は生成せずマスクする)")
    void case8() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useBanchiGenerate(false);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ダミー番地１－２－３".length(), ret[4].length());
      assertTrue(ret[4].matches("[ァ-ヴ]{3}([々〇〻\u3400-\u9FFF\uF900-\uFAFF]|[\uD840-\uD87F][\uDC00-\uDFFF]){2}[１-９]－[１-９]－[１-９]"), String.format("[%s]はNG", ret[4]));
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし２７ちょうめ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
      assertEquals("ダミーバンチ１－２－３".length(), ret[8].length());
      assertTrue(ret[8].matches("[ァ-ヴ]{6}[１-９]－[１-９]－[１-９]"), String.format("[%s]はNG", ret[8]));
    }

    @Test
    @DisplayName("ランダムセレクト(番地は生成せずマスクするが番地カナ指定なし)")
    void case9() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.useBanchiGenerate(false);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ダミー番地１－２－３".length(), ret[4].length());
      assertTrue(ret[4].matches("[ァ-ヴ]{3}([々〇〻\u3400-\u9FFF\uF900-\uFAFF]|[\uD840-\uD87F][\uDC00-\uDFFF]){2}[１-９]－[１-９]－[１-９]"), String.format("[%s]はNG", ret[4]));
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし２７ちょうめ", ret[7]);
      assertEquals("", ret[8]);
    }
    @Test
    @DisplayName("ランダムセレクト(番地は生成せずマスクするが番地漢字指定なし)")
    void case10() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street");
      rule.useBanchiGenerate(false);
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北海道", ret[1]);
      assertEquals("札幌市中央区", ret[2]);
      assertEquals("北二条西２７丁目", ret[3]);
      assertEquals("", ret[4]);
    }

    @Test
    @DisplayName("ランダムセレクト(コード類)")
    void case11() throws Exception {
      String[] params = new String[] {
          "01", "01101", "101", "123456", "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%jisKenCode,%jisCityCode,%jisCityShortCode,%addrCode,%zip");
      rule.useWideKana(true);
      String[] ret = generate(params, rule);
      assertEquals("01", ret[0]);
      assertEquals("01101", ret[1]);
      assertEquals("101", ret[2]);
      assertEquals("060872100", ret[3]);
      assertEquals("064-0822", ret[4]);
    }

    @Test
    @DisplayName("SJIS換算文字列長さ指定(カット)")
    void case12() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.setMaxSjisByteCounts(new int[] {0, 2, 2, 2});
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北", ret[1]);
      assertEquals("札", ret[2]);
      assertEquals("北", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし２７ちょうめ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

    @Test
    @DisplayName("SJIS換算文字列長さ指定(シフト)")
    void case13() throws Exception {
      String[] params = new String[] {
          "123-0001",
          "ダミー県", "ダミー市", "ダミー町", "ダミー番地１－２－３",
          "ダミーケン", "ダミーシ", "ダミーチョウ", "ダミーバンチ１－２－３"};
      rule.setAddrFormat("%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana");
      rule.setMaxSjisByteCounts(new int[] {0, 2, 2, 2, 2});
      rule.setShiftOverflowStrings(new boolean[] {false, true, true, false, false});
      String[] ret = generate(params, rule);
      assertEquals("064-0822", ret[0]);
      assertEquals("北", ret[1]);
      assertEquals("海", ret[2]);
      assertEquals("道", ret[3]);
      assertNotEquals("ダミー番地１－２－３", ret[4]);
      assertEquals("ほっかいどう", ret[5]);
      assertEquals("さっぽろしちゅうおうく", ret[6]);
      assertEquals("きた０２じょうにし２７ちょうめ", ret[7]);
      assertNotEquals("ダミーバンチ１－２－３", ret[8]);
    }

  }

}
