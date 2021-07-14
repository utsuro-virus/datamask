package net.utsuro.mask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RandomDataPickup implements DataMask {

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
   * ランダムにデータを取得して置換する.
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

    if (src != null && rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src.toString()).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    if (rule.getSelectListTableName() == null
        || rule.getSelectListTableName().isBlank()) {
      // データリストの抽出用パラメータが未設定の場合はエラー
      throw new IllegalArgumentException(
          "データ選択リストの対象テーブル名 selectListTableName が指定されていません。");
    }
    if (rule.getSelectListColName() == null
        || rule.getSelectListColName().isBlank()) {
      // データリストの抽出用パラメータが未設定の場合はエラー
      throw new IllegalArgumentException(
          "データ選択リストの対象カラム名 selectListColName が指定されていません。");
    }

    StringBuilder sql;
    if (maxSeq < 0) {
      // 初回は最大値を取得
      sql = new StringBuilder();
      sql.append("SELECT")
        .append(" MAX(").append(rule.getSelectListSeqNoColName()).append(") AS maxseq")
        .append(" FROM ").append(rule.getSelectListTableName());
      try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            maxSeq = rs.getLong("maxseq");
          }
        }
      }
    }

    if (maxSeq < 0) {
      // データが無い場合はnullを返却
      return null;
    }

    // 対象テーブルからデータ取得
    sql = new StringBuilder();
    sql.append("SELECT ")
      .append(rule.getSelectListColName())
      .append(" FROM ").append(rule.getSelectListTableName())
      .append(" WHERE ")
      .append(rule.getSelectListSeqNoColName()).append(" = ?");

    Object ret = null;

    try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
      // 取得する連番をランダム数値から指定する
      long tarNo = MaskingUtil.getRandomNumber(0, maxSeq);
      stmt.setLong(1, tarNo);
      // 1件取得する
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          ret = rs.getObject(rule.getSelectListColName());
        }
      }
    }

    // 取得したデータを返却
    return ret;

  }

}
