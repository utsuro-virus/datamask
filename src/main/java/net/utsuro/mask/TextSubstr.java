package net.utsuro.mask;

/**
 * 部分文字列に置換するクラス.
 */
public class TextSubstr implements DataMask {

  /**
   * 部分文字列に置換する.
   * @see "String#substring"
   * @param src 元の値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return substr((String) src, rule);

  }

  /**
   * 部分文字列に置換する.
   * @see "String#substring"
   * @param src 元の値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   */
  public static String substr(String src, MaskingRule rule) {

    if (rule == null || src == null || src.isEmpty()) {
      // ルールが無い場合、引き渡された値がnullまたは空白の場合はそのまま返却
      return src;
    }

    // 開始インデックスが負数の場合は末尾からn文字目として扱う
    int st = (rule.getBeginIndex() < 0)
        ? src.length() + rule.getBeginIndex() : rule.getBeginIndex();
    // 終了インデックスが0の場合は省略されたとして文字列長から算出、文字列長を超過しないように調整
    int ed = (rule.getEndIndex() == 0) ? src.length() : Math.min(src.length(), rule.getEndIndex());

    // 部分文字列を取得して返す
    return src.substring(st, ed);

  }

}
