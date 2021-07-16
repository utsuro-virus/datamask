package net.utsuro.mask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 日付と時刻を日時に結合するクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>toClassName</td><td>日時結合後のクラス名(必須)</td></tr>
 * <tr><td>dateTimeFormat</td><td>from日付文字列の場合は入力値解析用の書式、to日付文字列の場合は返却時の書式を指定(DateTimeFormatter書式)</td></tr>
 * </table>
 */
public class DateTimeConcat implements DataMask {

  /**
   * 日付と時刻を日時に結合する.
   * @param src 結合したい値の配列
   * @param rule マスク化ルール
   * @return 結合後の値
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return concat(src, rule);

  }

  /**
   * 日付と時刻を日時に結合する.
   * @param src 結合したい値の配列
   * @param rule マスク化ルール
   * @return 結合後の値
   * @throws Exception エラー発生時
   */
  public static Object concat(Object src, MaskingRule rule) throws Exception {

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された値がnullの場合はそのまま返却
      return src;
    }

    if (!src.getClass().isArray() || ((Object[]) src).length != 2) {
      throw new IllegalArgumentException("日付と時刻からなる要素数2の配列が必要です。");
    }

    Object[] arr = (Object[]) src;
    MaskingRule tempRule = new MaskingRule(rule);

    // 日付と時刻にそれぞれ変換
    tempRule.setToClassName(LocalDate.class.getName());
    LocalDate date = (LocalDate) TypeConverter.convert(arr[0], tempRule);
    tempRule.setToClassName(LocalTime.class.getName());
    LocalTime time = (LocalTime) TypeConverter.convert(arr[1], tempRule);

    // LocalDateTimeに結合して引き渡された元々のルールで変換して返す
    return TypeConverter.convert(LocalDateTime.of(date, time), rule);

  }

}
