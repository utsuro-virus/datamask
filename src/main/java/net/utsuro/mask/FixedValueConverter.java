package net.utsuro.mask;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 固定値に置換するクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>isNullReplace</td><td>元値がNullの場合でも置換するかどうか</td></tr>
 * <tr><td>toClassName</td><td>置換後のクラス名(Null置換ありなら必須)</td></tr>
 * <tr><td>ignoreValuePattern</td><td>対象外にする値のパターン(正規表現) ※マッチした場合は元の値そのまま返却</td></tr>
 * <tr><td>fixedValue</td><td>固定値 ※システム日付をセットしたい場合は %sysdate を指定、タイムスタンプの場合は %systimestamp を指定する。</td></tr>
 * </table>
 */
public class FixedValueConverter implements DataMask {

  /**
   * 固定値に置換する.
   * @param src 置換したい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return convert(src, rule);

  }

  /**
   * 固定値に置換する.
   * @param src 置換したい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  public static Object convert(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    if (rule.getToClassName() == null || rule.getToClassName().isEmpty()) {
      // nullの場合は自動判定できないため、置換後のクラス名が指定されていない場合はエラー
      throw new IllegalArgumentException("置換先型名が指定されていません。");
    }

    if (src != null && rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src.toString()).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    // システム日付の場合は値を置換する
    String val = rule.getFixedValue();
    if ("%sysdate".equals(val)) {
      val = LocalDate.now().toString();
    } else if ("%systimestamp".equals(val)) {
      val = LocalDateTime.now().toString();
    }

    // 返却型に合わせて変換して返す
    return TypeConverter.convert(val, rule);

  }

}
