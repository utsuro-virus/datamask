package net.utsuro.mask;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
