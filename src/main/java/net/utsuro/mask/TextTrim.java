package net.utsuro.mask;

public class TextTrim implements DataMask {

  /**
   * 前後の空白をTrimする.
   * @param src Trimしたい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return trim((String) src, rule);

  }

  /**
   * 前後の空白をTrimする.
   * @param src Trimしたい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  public static String trim(String src, MaskingRule rule) {

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された値がnullの場合はそのまま返却
      return src;
    }

    String s = src;

    if (rule.isKeepWideSpaceTrim()) {
      // 半角スペースのみTrimする
      if (!rule.useLTrim() && !rule.useRTrim()) {
        s = s.trim();
      } else {
        if (rule.useLTrim()) {
          s = ltrim(s);
        }
        if (rule.useRTrim()) {
          s = rtrim(s);
        }
      }
    } else {
      // 全角スペースもTrimする
      if (!rule.useLTrim() && !rule.useRTrim()) {
        s = s.strip();
      } else {
        if (rule.useLTrim()) {
          s = s.stripLeading();
        }
        if (rule.useRTrim()) {
          s = s.stripTrailing();
        }
      }
    }

    return s;

  }

  /**
   * 左側の空白を除去する.
   * @param s 対象の文字列
   * @return 除去後の文字列
   */
  private static String ltrim(String s) {
    int i = 0;
    int len = s.length();
    while (i < len && s.charAt(i) <= ' ') {
      i++;
    }
    return s.substring(i);
  }

  /**
   * 右側の空白を除去する.
   * @param s 対象の文字列
   * @return 除去後の文字列
   */
  private static String rtrim(String s) {
    int i = s.length() - 1;
    while (i >= 0 && s.charAt(i) <= ' ') {
      i--;
    }
    return s.substring(0, i + 1);
  }

}
