package net.utsuro.mask;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.ThreadLocalRandom;
import net.utsuro.mask.MaskingUtil.CharType;

/**
 * 文字列のランダム生成クラス.
 */
public class RandomTextGenerator implements DataMask {

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
   * 文字列ランダム生成.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    String tarStr;
    if (src instanceof String || src == null) {
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
        ret = generate(tarStr, rule);
        // ユニークでないとならない場合は生成結果のチェック
        if (!rule.isUniqueValue() || !isExistsInUniqueList(rule.getUniqueId(), ret)) {
          isValid = true;
          if (tarStr != null && (rule.isUniqueValue() || rule.isDeterministicReplace())) {
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
   * 文字列ランダム生成.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  public static String generate(String src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    if (src != null && rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    String tarStr = src;
    if (rule.isNullReplace() && tarStr == null) {
      tarStr = "";
    }

    CharType charType;
    int byteCount;
    if (rule.getMinSjisByteCount() > 0 || rule.getMaxSjisByteCount() > 0) {
      int min = Math.max(rule.getMinSjisByteCount(), 1);
      int max = (rule.getMaxSjisByteCount() == 0) ?
          MaskingUtil.getSjisByteCount(tarStr) :
          Math.max(rule.getMaxSjisByteCount(), rule.getMinSjisByteCount());
      byteCount = ThreadLocalRandom.current().nextInt(max + 1 - min) + min;
    } else {
      byteCount = MaskingUtil.getSjisByteCount(tarStr);
    }
    byteCount -= MaskingUtil.getSjisByteCount(rule.getPrefix());
    byteCount -= MaskingUtil.getSjisByteCount(rule.getSuffix());

    if (byteCount <= 0) {
      // 生成する長さがゼロ以下になってしまった場合は空文字で返す
      return "";
    }

    StringBuilder sb = new StringBuilder();

    // 接頭語を付与
    sb.append(rule.getPrefix());

    if (rule.useRandomGenCharType()) {
      // 生成文字種が指定されている場合はそれを生成
      charType = rule.getRandomGenCharType();
    } else {
      // 生成文字種の指定がなければ元の値(1文字目)と同じ文字種を生成
      charType = CharType.getTypeByString(tarStr);
    }
    if (charType != CharType.UNKNOWN) {
      sb.append(MaskingUtil.getRandomString(byteCount, charType,
          rule.getRandomNoGenCharPattern()));
    } else {
      // 文字種不明の場合はALLで指定
      sb.append(MaskingUtil.getRandomString(byteCount, CharType.ALL,
          rule.getRandomNoGenCharPattern()));
    }

    // 接尾語を付与
    sb.append(rule.getSuffix());

    if (rule.useUpperCaseKana() || rule.useHalfKana()
        || rule.useWideKana() || rule.useHiragana()
        || rule.useUpperCase() || rule.useLowerCase()) {
      // 文字変換が指定されている場合は変換
      MaskingRule afterRepRule = new MaskingRule(rule);
      afterRepRule.setToClassName(String.class.getName());
      sb = new StringBuilder().append(TypeConverter.convert(sb.toString(), afterRepRule));
    }

    if (rule.useAfterTextReplace()) {
      // ランダム生成後に更に置換するかどうか
      MaskingRule afterRepRule = new MaskingRule(rule);
      afterRepRule.useOddCharMask(rule.useAfterRepOddCharMask());
      afterRepRule.useEvenCharMask(rule.useAfterRepEvenCharMask());
      sb = new StringBuilder(MaskedTextReplacer.replace(sb.toString(), afterRepRule));
    }

    return sb.toString();

  }

}
