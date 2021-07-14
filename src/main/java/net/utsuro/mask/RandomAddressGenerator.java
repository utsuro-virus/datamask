package net.utsuro.mask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

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
    } else if (src instanceof String[]) {
      addr = (String[]) src;
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

    // 配列を返却
    return ret;

  }

}
