package net.utsuro.mask;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import net.utsuro.mask.MaskingUtil.CharType;

public class RandomCardnoReplacer implements DataMask {

  private static final int RETRY_MAX = 5;
  private Connection conn;

  /**
   * このマスク処理でテータベースを使用するかどうか.
   * @return true=使用する, false=使用しない
   */
  @Override
  public boolean useDatabase(MaskingRule rule) {
    return (rule.isUniqueValue() || rule.isDeterministicReplace());
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
   * ランダム生成クレジットカード番号に置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された文字列がnullの場合はそのまま返却
      return src;
    }

    String tarStr;
    if (src instanceof String) {
      tarStr = (String) src;
    } else {
      tarStr = src.toString();
    }

    String ret = null;

    if (tarStr != null && rule.isDeterministicReplace()) {
      // 既登録の結果を使用する場合
      ret = (String) getRegisteredUniqueVal(rule.getUniqueId(), tarStr);
    }

    if (ret == null) {
      // 新規生成
      boolean isValid = false;
      int retryCount = 0;
      while (!isValid) {
        ret = replace(tarStr, rule);
        // ユニークでないとならない場合は生成結果のチェック
        if (!rule.isUniqueValue() || !isExistsInUniqueList(rule.getUniqueId(), ret)) {
          isValid = true;
          if (rule.isUniqueValue() || rule.isDeterministicReplace()) {
            // 一貫性が必要な場合とユニーク性が必要な場合はユニークリストに追加
            // ※リストに追加失敗した場合は再抽選
            isValid = addUniqueList(rule.getUniqueId(), tarStr, ret);
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
   * ランダム生成クレジットカード番号に置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  public static String replace(String src, MaskingRule rule) {

    StringBuilder sb = new StringBuilder();

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された文字列がnullの場合はそのまま返却
      return src;
    }

    if (src.isEmpty()) {
      // 引き渡された文字列が空の場合はそのまま返却
      return src;
    }

    if (rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    String[] c = src.split("");
    int len = c.length;
    int start = rule.getUnmaksedLengthLeft();
    if (start < 0) {
      // 開始がマイナスの場合は後ろから数える
      start = len + start;
    }
    int end = len - rule.getUnmaksedLengthRight() - 1;
    Pattern ptn = Pattern.compile("[- ]");

    // 1文字ずつ入れ替え
    for (int i = 0; i < len; i++) {
      if (i >= start && i <= end) {
        // 開始・終了の範囲内のみ置換
        if (!ptn.matcher(c[i]).find()) {
          // 除外パターンにマッチしなかった場合はランダム生成数字に差し替えする
          sb.append(MaskingUtil.getRandomString(1,
              CharType.NUMBER, rule.getRandomNoGenCharPattern()));
        } else {
          // 除外パターンにマッチした場合はそのまま返却
          sb.append(c[i]);
        }
      } else {
        // マスク範囲外の文字はそのまま返却
        sb.append(c[i]);
      }
    }

    // 最後の桁をチェックディジットと置換する
    String checkDigit = MaskingUtil.getLuhnDigit(sb.toString());
    sb.replace(sb.length() - 1, sb.length(), checkDigit);

    return sb.toString();

  }

}
