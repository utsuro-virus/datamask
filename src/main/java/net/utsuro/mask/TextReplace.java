package net.utsuro.mask;

/**
 * 文字列を置換するクラス.
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
