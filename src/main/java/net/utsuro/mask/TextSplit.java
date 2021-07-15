package net.utsuro.mask;

/**
 * 文字列を分割するクラス.
 */
public class TextSplit implements DataMask {

  /**
   * 文字列を分割する.
   * @param src 分割したい値
   * @param rule マスク化ルール
   * @return 分割後の値の配列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return split((String) src, rule);

  }

  /**
   * 文字列を分割する.
   * @param src 分割したい値
   * @param rule マスク化ルール
   * @return 分割後の値の配列
   * @throws Exception エラー発生時
   */
  public static Object split(String src, MaskingRule rule) throws Exception {

    if (rule == null || src == null || src.isEmpty()) {
      // ルールが無い場合、引き渡された値がnullの場合はそのまま返却
      return src;
    }

    // 文字列分割して返却
    return ((String) src).split((rule.getSeparator() == null) ? "" : rule.getSeparator(), -1);

  }

}
