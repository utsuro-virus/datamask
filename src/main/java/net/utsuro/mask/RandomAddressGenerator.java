package net.utsuro.mask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 住所のランダム生成クラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>isNullReplace</td><td>元値がNullの場合でも置換するかどうか</td></tr>
 * <tr><td>isUniqueValue</td><td>生成した値を一意にするかどうか(NULL以外)</td></tr>
 * <tr><td>isDeterministicReplace</td><td>決定論的置換するかどうか ※INPUTが同じならOUTPUTも同じ値にする(NULL以外)</td></tr>
 * <tr><td>uniqueId</td><td>決定論的/一意制管理の任意の識別子 ※カラム名で無くても良い</td></tr>
 * <tr><td>addrFormat</td><td>住所生成時に返却する配列フォーマット(カンマ区切り) ※デフォルトは下記<br>
 * [0] %zip        郵便番号<br>
 * [1] %pref       都道府県<br>
 * [2] %city       市区町村<br>
 * [3] %town       町域<br>
 * [4] %street     番地<br>
 * [5] %prefKana   都道府県カナ<br>
 * [6] %cityKana   市区町村カナ<br>
 * [7] %townKana   町域カナ<br>
 * [8] %streetKana 番地カナ<br>
 * [9] %jisKenCode JIS都道府県コード(2桁)<br>
 * [10] %jisCityCode JIS市区町村コード(5桁)<br>
 * [11] %jisCityShortCode JIS市区町村コード(3桁)<br>
 * [12] %addrCode 住所コード<br>
 * </td></tr>
 * <tr><td>selectListSeqNoColName</td><td>データ選択リストの連番カラム名 ※ランダム選択するためには対象テーブルには空き番の無い連番カラム(数値)が必要。指定が無い場合はデフォルトのseqnoとなる。</td></tr>
 * <tr><td>usePostCodeFormat</td><td>住所生成時に郵便番号をハイフン付きにするかどうか</td></tr>
 * <tr><td>useUpperCaseKana</td><td>生成時にカナを大文字にするかどうか</td></tr>
 * <tr><td>useHalfKana</td><td>生成時にカナを半角にするかどうか</td></tr>
 * <tr><td>useWideKana</td><td>生成時にカナを全角にするかどうか</td></tr>
 * <tr><td>useBanchiGenerate</td><td>住所生成時に番地部分に元の値を使用するかどうか</td></tr>
 * <tr><td>unmaksedLengthLeft</td><td>マスクしない文字数(左)</td></tr>
 * <tr><td>unmaksedLengthRight</td><td>マスクしない文字数(右)</td></tr>
 * <tr><td>useWhiteSpaceMask</td><td>全半角スペース、タブ、改行の置換有無</td></tr>
 * <tr><td>useOddCharMask</td><td>奇数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>useEvenCharMask</td><td>偶数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>randomGenCharType</td><td>ランダム生成文字の文字種 ※無指定は元の文字種と同じものを生成</td></tr>
 * <tr><td>useUpperCase</td><td>置換時に英字を大文字にするかどうか</td></tr>
 * <tr><td>useLowerCase</td><td>置換時に英字を小文字にするかどうか</td></tr>
 * <tr><td>useAfterTextReplace</td><td>ランダムマスク後に置換マスクを使用するかどうか</td></tr>
 * <tr><td>useAfterRepOddCharMask</td><td>マスク後の置換マスクで奇数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>useAfterRepEvenCharMask</td><td>マスク後の置換マスクで偶数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>maxSjisByteCounts</td><td>住所生成時に返却する配列ごとのSJIS換算byte数をint配列で指定</td></tr>
 * <tr><td>shiftOverflowStrings</td><td>住所生成時に返却する配列ごとの桁溢れ時に次の枠にシフトさせるかどうかをbool配列で指定</td></tr>
 * <tr><td>beforeTrim</td><td>入力値を処理前にTrimするかどうか</td></tr>
 * <tr><td>isKeepWideSpaceTrim</td><td>beforeTrim指定時に半角スペースのみTrimするならtrueを指定</td></tr>
 * <tr><td>useLTrim</td><td>beforeTrim指定時にLTrimをするならtrueを指定</td></tr>
 * <tr><td>useRTrim</td><td>beforeTrim指定時にRTrimをするならtrueを指定</td></tr>
 * </table>
 */
public class RandomAddressGenerator implements DataMask {

  private static final int RETRY_MAX = 5;
  private Connection conn;
  private long maxSeq = -1;

  /**
   * このマスク処理でテータベースを使用するかどうか.
   * @return true=使用する, false=使用しない
   */
  @Override
  public boolean useDatabase(MaskingRule rule) {
    return true;
  }

  /**
   * DBコネクションを取得.
   * @return conn
   */
  public Connection getConnection() {
    return conn;
  }

  /**
   * DBコネクションをセット.
   * @param conn セットする conn
   */
  public void setConnection(Connection conn) {
    this.conn = conn;
  }

  /**
   * ランダムに住所を生成して置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws SQLException DBアクセス時のエラー
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    String[] addr = null;
    if (rule.isNullReplace() && src == null) {
      addr = new String[0];
    } else if (src instanceof String[] || src instanceof Object[]) {
      addr = Arrays.copyOf((Object[]) src, ((Object[]) src).length, String[].class);
      if (rule.isBeforeTrim()) {
        // 入力値を処理前にTrimする場合
        for (int i = 0; i < addr.length; i++) {
          if (addr[i] != null) {
            addr[i] = TextTrim.trim(addr[i], rule);
          }
        }
      }
    } else {
      // 文字列配列でない場合はそのまま返却
      return src;
    }

    String[] ret = null;

    if (rule.isDeterministicReplace()) {
      // 既登録の結果を使用する場合
      String buff = (String) getRegisteredUniqueVal(rule.getUniqueId(), String.join("<>", addr));
      if (buff != null) {
        ret = buff.split("<>", -1);
      }
    }

    if (ret == null) {
      // 新規生成
      boolean isValid = false;
      int retryCount = 0;
      while (!isValid) {
        ret = generate(addr, rule);
        // ユニークでないとならない場合は生成結果のチェック
        if (!rule.isUniqueValue()
            || !isExistsInUniqueList(rule.getUniqueId(), String.join("<>", ret))) {
          isValid = true;
          if (ret != null && (rule.isUniqueValue() || rule.isDeterministicReplace())) {
            // 一貫性が必要な場合とユニーク性が必要な場合はユニークリストに追加
            // ※リストに追加失敗した場合は再抽選
            isValid = addUniqueList(
                rule.getUniqueId(), String.join("<>", addr), String.join("<>", ret));
            if (!isValid) {
              retryCount++;
            }
            if (retryCount > RETRY_MAX) {
              // 何度やってもユニークにならない場合、設定ルールがおかしいと思われるのでエラー
              throw new SQLIntegrityConstraintViolationException(
                  String.format("%d回重複してユニークリストの登録に失敗しました。", RETRY_MAX));
            }
          }
        }
      }
    }
    return ret;

  }

  /**
   * ランダムに住所を生成して置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws SQLException DBアクセス時のエラー
   */
  public String[] generate(String[] src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    String[] addr = src;
    if (rule.isNullReplace() && src == null) {
      addr = new String[0];
    }

    String addrBuff = rule.getAddrFormat();
    if (addrBuff == null || addrBuff.isEmpty()) {
      // 返却書式が無い場合はそのまま返却
      return src;
    }

    StringBuilder sql;

    synchronized (this) {
      if (maxSeq < 0) {
        // 初回は最大値を取得
        sql = new StringBuilder();
        sql.append("SELECT")
            .append(" MAX(").append(rule.getSelectListSeqNoColName()).append(") AS maxseq")
            .append(" FROM m_postal_code");
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              maxSeq = rs.getLong("maxseq");
            }
          }
        }
      }
    }

    if (maxSeq < 0) {
      // データが無い場合はnullを返却
      return null;
    }

    // 住所テーブルからデータ取得
    sql = new StringBuilder();
    sql.append("SELECT * FROM m_postal_code WHERE ")
      .append(rule.getSelectListSeqNoColName()).append(" = ?");

    try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
      // 取得する連番をランダム数値から指定する
      long tarNo = MaskingUtil.getRandomNumber(0, maxSeq);
      stmt.setLong(1, tarNo);
      // 1件取得する
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String kana;
          if (rule.usePostCodeFormat()) {
            // 郵便番号ハイフン付き
            addrBuff = addrBuff.replaceAll("%zip", rs.getString("zip"));
          } else {
            // 郵便番号ハイフン抜き
            addrBuff = addrBuff.replaceAll("%zip", rs.getString("zip").replaceAll("-", ""));
          }
          // 都道府県カナ
          kana = rs.getString("ken_furi");
          if (rule.useUpperCaseKana()) {
            // カナ小文字を大文字にする
            kana = MaskingUtil.toUpperWideKana(kana);
          }
          if (rule.useHalfKana()) {
            // カナを半角カナにする
            kana = MaskingUtil.wideKanaToHalfKana(kana);
          } else if (!rule.useWideKana()) {
            // カナをひらがなにする
            kana = MaskingUtil.wideKanaToHiragana(kana);
          }
          addrBuff = addrBuff.replaceAll("%prefKana", kana);
          // 市区町村カナ
          kana = rs.getString("city_furi");
          if (rule.useUpperCaseKana()) {
            // カナ小文字を大文字にする
            kana = MaskingUtil.toUpperWideKana(kana);
          }
          if (rule.useHalfKana()) {
            // カナを半角カナにする
            kana = MaskingUtil.wideKanaToHalfKana(Normalizer.normalize(kana, Form.NFKC));
          } else if (!rule.useWideKana()) {
            // カナをひらがなにする
            kana = MaskingUtil.wideKanaToHiragana(kana);
          }
          addrBuff = addrBuff.replaceAll("%cityKana", kana);
          // 町域カナ
          kana = rs.getString("town_furi");
          if (kana != null && rs.getString("block_furi") != null) {
            kana = kana.concat(rs.getString("block_furi"));
          }
          if (rule.useUpperCaseKana()) {
            // カナ小文字を大文字にする
            kana = MaskingUtil.toUpperWideKana(kana);
          }
          if (rule.useHalfKana()) {
            // カナを半角カナにする
            kana = MaskingUtil.wideKanaToHalfKana(Normalizer.normalize(kana, Form.NFKC));
          } else if (!rule.useWideKana()) {
            // カナをひらがなにする
            kana = MaskingUtil.wideKanaToHiragana(kana);
          }
          addrBuff = addrBuff.replaceAll("%townKana", kana);
          // 都道府県
          addrBuff = addrBuff.replaceAll("%pref", rs.getString("ken_name"));
          // 市区町村
          addrBuff = addrBuff.replaceAll("%city", rs.getString("city_name"));
          // 町域
          addrBuff = addrBuff.replaceAll("%town",
              rs.getString("town_name").concat(
                  (rs.getString("block_name") == null) ? "" : rs.getString("block_name")));
          // JIS都道府県コード
          addrBuff = addrBuff.replaceAll("%jisKenCode", String.format("%02d", rs.getInt("ken_id")));
          // JIS市区町村コード(5桁)
          addrBuff = addrBuff.replaceAll("%jisCityCode", String.format("%05d", rs.getInt("city_id")));
          // JIS市区町村コード(3桁)
          addrBuff = addrBuff.replaceAll("%jisCityShortCode", String.format("%05d", rs.getInt("city_id")).substring(2));
          // 住所コード
          addrBuff = addrBuff.replaceAll("%addrCode", String.format("%09d", rs.getInt("id")));

        } else {
          // データが無い場合はnullを返却
          return null;
        }
      }
    }

    // 分割
    String[] ret = addrBuff.split(",", -1);

    // 番地生成
    String street = "";
    String streetKana = "";
    if (rule.useBanchiGenerate()) {
      // 重み付きランダムで1～4枠用意
      int count = MaskingUtil.getRandomIndex(new int[] {2, 7, 10, 5}) + 1;
      String[] buff = new String[count];
      // 後ろの枠ほど桁が大きくなるように生成
      int[][] maxTable = {{5000}, {10, 1000}, {10, 12, 12}, {10, 12, 12, 999}};
      for (int i = 0; i < buff.length; i++) {
        buff[i] = Integer.toString(MaskingUtil.getRandomNumber(1, maxTable[buff.length - 1][i]));
        if (i == 3 && buff[i].length() == 3) {
          // 3枠目は号室想定の番号なので3桁の中央が大きいときは0にする
          buff[i] = buff[i].replaceAll("([0-9])[3-9]([0-9])", "$10$2");
        }
      }
      streetKana = String.join("-", buff);
      street = MaskingUtil.halfNumberToWideNumber(streetKana).replaceAll("-", "－");
    }

    for (int i = 0; i < ret.length; i++) {
      if (ret[i].indexOf("%streetKana") >= 0) {
        // 番地カナ編集
        if (!rule.useBanchiGenerate()) {
          // 番地カナは元の値をマスク
          if (i < addr.length) {
            MaskingRule streetRule = new MaskingRule(rule);
            streetRule.setUnmaksedChar("[-－ ]");
            streetRule.setRandomNoGenCharPattern(Pattern.compile("[0０「」]"));
            ret[i] = RandomTextReplacer.replace(addr[i], streetRule);
          } else {
            ret[i] = "";
          }
        } else {
          // 番地カナをランダム生成したものに差し替え
          ret[i] = ret[i].replaceAll("%streetKana", streetKana);
        }
      } else if (ret[i].indexOf("%street") >= 0) {
        // 番地編集
        if (!rule.useBanchiGenerate()) {
          // 番地は元の値をマスク
          if (i < addr.length) {
            MaskingRule streetRule = new MaskingRule(rule);
            streetRule.setUnmaksedChar("[-－ ]");
            streetRule.setRandomNoGenCharPattern(Pattern.compile("[0０「」]"));
            ret[i] = RandomTextReplacer.replace(addr[i], streetRule);
          } else {
            ret[i] = "";
          }
        } else {
          // 番地をランダム生成したものに差し替え
          ret[i] = ret[i].replaceAll("%street", street);
        }
      }
    }

    // 文字列長さ調整
    if (rule.getMaxSjisByteCounts() != null && rule.getMaxSjisByteCounts().length > 0) {
      for (int i = 0; i < ret.length; i++) {
        int byteCount = 0;
        boolean isShiftOverflow = false;
        if (rule.getMaxSjisByteCounts().length > i) {
          byteCount = rule.getMaxSjisByteCounts()[i];
        }
        if (rule.getShiftOverflowStrings() != null && rule.getShiftOverflowStrings().length > i) {
          isShiftOverflow = rule.getShiftOverflowStrings()[i];
        }
        if (byteCount > 0) {
          String[] buff = MaskingUtil.splitBySjisBytes(ret[i], byteCount);
          ret[i] = buff[0];
          if (isShiftOverflow && buff[1] != null && i < ret.length - 1) {
            ret[i + 1] = buff[1].concat(ret[i + 1]);
          }
        }
      }
    }

    // 配列を返却
    return ret;

  }

}
