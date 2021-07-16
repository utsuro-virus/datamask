package net.utsuro.mask;

/**
 * 文字列を置換するクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>textReplaceRegex</td><td>文字列置換時の正規表現</td></tr>
 * <tr><td>textReplacement</td><td>文字列置換時の置換文字列</td></tr>
 * </table>
 */
public class TextReplace implements DataMask {

  /**
   * 文字列を置換する.
   * @see "String#replaceAll"
   * @param src 置換したい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return replace((String) src, rule);

  }

  /**
   * 文字列を置換する.
   * @see "String#replaceAll"
   * @param src 置換したい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   */
  public static String replace(String src, MaskingRule rule) {

    if (rule == null || src == null || src.isEmpty()
        || rule.getTextReplaceRegex() == null || rule.getTextReplaceRegex().isEmpty()) {
      // ルールが無い場合、引き渡された値がnullまたは空白の場合はそのまま返却
      return src;
    }

    // 置換文字列がnullの場合は空文字に置き換え
    String replacement = (rule.getTextReplacement() == null) ? "" : rule.getTextReplacement();

    // 置換して返す
    return src.replaceAll(rule.getTextReplaceRegex(), replacement);

  }

}
