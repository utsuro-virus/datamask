package net.utsuro.mask;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * クラス名から各データマスククラスのインスタンスを生成するファクトリクラス.
 */
public class DataMaskFactory {

  /**
   * クラス名からDataMaskインターフェースを持つクラスのインスタンスを生成する.
   * @param className 生成したいクラス名(パッケージ名は不要)
   * @return 生成したインスタンス
   * @throws IllegalArgumentException 指定のクラス名のクラスが見つからない場合など
   */
  public static DataMask newInstance(String className) throws IllegalArgumentException {

    StringBuilder classFullName = new StringBuilder();
    classFullName.append(DataMaskFactory.class.getPackage().getName())
        .append(".").append(className);

    // 指定のクラスのインスタンスを生成
    Class<?> clazz;
    try {
      clazz = Class.forName(classFullName.toString());
      return (DataMask) clazz.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException | InstantiationException
        | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      throw new IllegalArgumentException(
          String.format("指定されたクラス %s のインスタンス生成に失敗しました。", className), e);
    }

  }

  /**
   * ユニークリストの初期化.
   * @param conn DBコネクション
   * @throws SQLException DBアクセス時のエラー
   */
  public static void initUniqueList(Connection conn) throws SQLException {
    // ユニーク管理リストのクリア
    if (conn != null && !conn.isClosed()) {
      StringBuilder sql;
      sql = new StringBuilder();
      sql.append("TRUNCATE TABLE sys_unique_list");
      try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        stmt.execute();
      }
    }
  }


}
