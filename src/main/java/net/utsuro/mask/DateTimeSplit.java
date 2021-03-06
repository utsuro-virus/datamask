package net.utsuro.mask;

import java.time.LocalDateTime;

/**
 * 日時を日付と時刻に分割するクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>toClassNames</td><td>日時分割後のクラス名の配列(2つ必須)</td></tr>
 * <tr><td>dateTimeFormat</td><td>from日付文字列の場合は入力値解析用の書式、to日付文字列の場合は返却時の書式を指定(DateTimeFormatter書式)</td></tr>
 * </table>
 */
public class DateTimeSplit implements DataMask {

  /**
   * 日時を日付と時刻に分割する.
   * @param src 分割したい値
   * @param rule マスク化ルール
   * @return 分割後の値の配列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return split(src, rule);

  }

  /**
   * 日時を日付と時刻に分割する.
   * @param src 分割したい値
   * @param rule マスク化ルール
   * @return 分割後の値の配列
   * @throws Exception エラー発生時
   */
  public static Object split(Object src, MaskingRule rule) throws Exception {

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された値がnullの場合はそのまま返却
      return src;
    }

    if (rule.getToClassNames() == null || rule.getToClassNames().size() != 2) {
      // 変換後のクラス名が2つ指定されていない場合はエラー
      throw new IllegalArgumentException("2つの型変換先が指定されていません。");
    }

    MaskingRule tempRule = new MaskingRule(rule);

    // LocalDateTimeに変換
    tempRule.setToClassName(LocalDateTime.class.getName());
    LocalDateTime dateTime = (LocalDateTime) TypeConverter.convert(src, tempRule);

    Object[] obj = new Object[2];

    // 2枠分の型変換
    tempRule.setToClassName(rule.getToClassNames().get(0));
    obj[0] = TypeConverter.convert(dateTime.toLocalDate(), tempRule);
    tempRule.setToClassName(rule.getToClassNames().get(1));
    obj[1] = TypeConverter.convert(dateTime.toLocalTime(), tempRule);

    return obj;

  }

}
