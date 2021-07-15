package net.utsuro.mask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * 氏名のランダム生成クラス.
 */
public class RandomFullNameGenerator implements DataMask {

  private static final int RETRY_MAX = 5;
  private Connection conn;
  private long lastNameMaxSeq = -1;
  private long firstNameMaxSeq = -1;

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
   * ランダムに個人名を生成して置換する.
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

    String[] names = null;
    if (rule.isNullReplace() && src == null) {
      names = new String[0];
    } else if (src instanceof String[]) {
      names = (String[]) src;
    } else {
      // 文字列配列でない場合はそのまま返却
      return src;
    }

    String[] ret = null;

    if (rule.isDeterministicReplace()) {
      // 既登録の結果を使用する場合
      String buff = (String) getRegisteredUniqueVal(rule.getUniqueId(), String.join("<>", names));
      if (buff != null) {
        ret = buff.split("<>", -1);
      }
    }

    if (ret == null) {
      // 新規生成
      boolean isValid = false;
      int retryCount = 0;
      while (!isValid) {
        ret = generate(names, rule);
        // ユニークでないとならない場合は生成結果のチェック
        if (!rule.isUniqueValue()
            || !isExistsInUniqueList(rule.getUniqueId(), String.join("<>", ret))) {
          isValid = true;
          if (ret != null && (rule.isUniqueValue() || rule.isDeterministicReplace())) {
            // 一貫性が必要な場合とユニーク性が必要な場合はユニークリストに追加
            // ※リストに追加失敗した場合は再抽選
            isValid = addUniqueList(
                rule.getUniqueId(), String.join("<>", names), String.join("<>", ret));
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
   * ランダムに個人名を生成して置換する.
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

    String namesBuff = rule.getFullNameFormat();
    if (namesBuff == null || namesBuff.isEmpty()) {
      // 返却書式が無い場合はそのまま返却
      return src;
    }

    StringBuilder sql;

    synchronized (this) {
      if (lastNameMaxSeq < 0) {
        // 初回は最大値を取得
        sql = new StringBuilder();
        sql.append("SELECT")
            .append(" MAX(CASE WHEN name_type = 'LAST_NAME' THEN ")
            .append(rule.getSelectListSeqNoColName())
            .append(" ELSE -1 END) AS sei_maxseq")
            .append(",MAX(CASE WHEN name_type = 'FIRST_NAME' THEN ")
            .append(rule.getSelectListSeqNoColName())
            .append(" ELSE -1 END) AS mei_maxseq")
            .append(" FROM m_jinmei");
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              lastNameMaxSeq = rs.getLong("sei_maxseq");
              firstNameMaxSeq = rs.getLong("mei_maxseq");
            }
          }
        }
      }
    }

    if (lastNameMaxSeq < 0) {
      // データが無い場合はnullを返却
      return null;
    }

    // 人名テーブルからデータ取得
    sql = new StringBuilder();
    sql.append("SELECT")
      .append(" name_type, kanji, yomi")
      .append(" FROM m_jinmei")
      .append(" WHERE (name_type = 'LAST_NAME' AND ")
      .append(rule.getSelectListSeqNoColName()).append(" = ?)")
      .append(" OR (name_type = 'FIRST_NAME' AND ")
      .append(rule.getSelectListSeqNoColName()).append(" = ?)");

    boolean isExists = false;

    try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
      // 取得する連番をランダム数値から指定する
      long tarLastNameNo = MaskingUtil.getRandomNumber(0, lastNameMaxSeq);
      long tarFirstNameNo = MaskingUtil.getRandomNumber(0, firstNameMaxSeq);
      stmt.setLong(1, tarLastNameNo);
      stmt.setLong(2, tarFirstNameNo);
      // 2件取得する
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          isExists = true;
          String kana = rs.getString("yomi");
          if (rule.useUpperCaseKana()) {
            // カナ小文字を大文字にする
            kana = MaskingUtil.toUpperHiragana(kana);
          }
          if (rule.useWideKana() || rule.useHalfKana()) {
            // カナを全角カナにする
            kana = MaskingUtil.hiraganaToWideKana(kana);
            if (rule.useHalfKana()) {
              // カナを半角カナにする
              kana = MaskingUtil.wideKanaToHalfKana(Normalizer.normalize(kana, Form.NFKC));
            }
          }
          if ("LAST_NAME".equals(rs.getString("name_type"))) {
            namesBuff = namesBuff.replaceAll("%lastNameKanji", rs.getString("kanji"));
            namesBuff = namesBuff.replaceAll("%lastNameKana", kana);
          } else {
            namesBuff = namesBuff.replaceAll("%firstNameKanji", rs.getString("kanji"));
            namesBuff = namesBuff.replaceAll("%firstNameKana", kana);
          }
        }
      }
    }

    // 分割
    String[] ret = null;
    if (isExists) {
      ret = namesBuff.split(",", -1);
    }

    // 配列を返却
    return ret;

  }

}
