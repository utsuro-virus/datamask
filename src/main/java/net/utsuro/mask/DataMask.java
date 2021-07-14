package net.utsuro.mask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DataMask {

  /**
   * このマスク処理でテータベースを使用するかどうか.
   * @return true=使用する, false=使用しない
   */
  public default boolean useDatabase(MaskingRule rule) {
    return false;
  }

  /**
   * DBコネクションを取得.
   * @return conn
   */
  public default Connection getConnection() {
    return null;
  }

  /**
   * DBコネクションをセット.
   * @param conn セットする conn
   */
  public default void setConnection(Connection conn) {}

  /**
   * マスク化した値に置換する.
   * @param src 対象データ
   * @param rule マスク化ルール
   * @return 置換後のデータ
   * @throws Exception エラー発生時
   */
  public Object execute(Object src, MaskingRule rule) throws Exception;

  /**
   * ユニークリストから既登録の値を取得する.
   * ※INPUTが同じものがあれば再利用する決定論的置換で使用
   * @param id 識別子(カラム名とは限らず)
   * @param val 入力値
   * @return INPUTが同じマスク済の値
   * @throws SQLException DBアクセス時のエラー
   */
  public default Object getRegisteredUniqueVal(String id, String val) throws SQLException {

    Object ret = null;
    Connection conn = getConnection();
    if (conn != null && !conn.isClosed() && val != null) {
      StringBuilder sql;
      sql = new StringBuilder();
      sql.append("SELECT")
          .append(" output_val")
          .append(" FROM sys_unique_list")
          .append(" WHERE id = ?")
          .append(" AND input_val = ?");
      try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        stmt.setString(1, id);
        stmt.setString(2, val);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            ret = rs.getString("output_val");
          }
        }
      }
    }
    return ret;

  }

  /**
   * ユニークリストに存在するかどうかを取得.
   * @param id 識別子(カラム名とは限らず)
   * @param val 出力値
   * @return true=存在する, false=存在しない
   * @throws SQLException DBアクセス時のエラー
   */
  public default boolean isExistsInUniqueList(String id, String val) throws SQLException {

    boolean ret = false;
    Connection conn = getConnection();
    if (conn != null && !conn.isClosed() && val != null) {
      synchronized (this) {
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append("SELECT")
            .append(" COUNT(*) AS cnt")
            .append(" FROM sys_unique_list")
            .append(" WHERE id = ?")
            .append(" AND output_val = ?");
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
          stmt.setString(1, id);
          stmt.setString(2, val);
          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getLong("cnt") > 0) {
              ret = true;
            }
          }
        }
      }
    }
    return ret;
  }

  /**
   * ユニークリストに登録する.
   * ※ユニーク値生成の場合、OUTPUTに同値はNGなので再抽選する
   * @param id 識別子(カラム名とは限らず)
   * @param inputVal 入力値
   * @param outputVal 出力地
   * @return true=登録成功, false=一意にならず登録失敗
   * @throws SQLException DBアクセス時のエラー
   */
  public default boolean addUniqueList(String id, String inputVal, String outputVal)
      throws SQLException {

    boolean ret = true;
    Connection conn = getConnection();
    if (conn != null && !conn.isClosed() && inputVal != null && outputVal != null) {
      synchronized (this) {
        StringBuilder sql;
        sql = new StringBuilder();
        sql.append("INSERT INTO sys_unique_list")
            .append(" (id, input_val, output_val)")
            .append(" VALUES")
            .append(" (?, ?, ?)");
        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
          stmt.setString(1, id);
          stmt.setString(2, inputVal);
          stmt.setString(3, outputVal);
          stmt.execute();
        } catch (SQLException e) {
          // 一意誓約違反になったら再抽選
          ret = false;
        }
      }
    }
    return ret;

  }

}
