package net.utsuro.mask;

/**
 * 部分文字列に置換するクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>beginIndex</td><td>部分文字列取得時の開始インデックス。負数は末尾からn文字目として扱う。</td></tr>
 * <tr><td>endIndex</td><td>部分文字列取得時の終了インデックス。指定なしや0の場合文字列末尾まで。</td></tr>
 * <tr><td>truncateEbcdicBytes</td><td>バイト数(EBCDIC換算)で末尾省略。 ※半欠けにならないよう文字単位でカット</td></tr>
 * <tr><td>truncateSjisBytes</td><td>バイト数(SJIS換算)で末尾省略。 ※半欠けにならないよう文字単位でカット</td></tr>
 * </table>
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

    if (rule.getTruncateEbcdicBytes() > 0) {
      // EBCDIC換算で末尾省略
      return MaskingUtil.truncateByEbcdicBytes(src, rule.getTruncateEbcdicBytes());

    } else if (rule.getTruncateSjisBytes() > 0) {
      // SJIS換算で末尾省略
      return MaskingUtil.truncateBySjisBytes(src, rule.getTruncateSjisBytes());

    } else {
      // 開始インデックスが負数の場合は末尾からn文字目として扱う
      int st = (rule.getBeginIndex() < 0)
          ? src.length() + rule.getBeginIndex() : rule.getBeginIndex();
      // 終了インデックスが0の場合は省略されたとして文字列長から算出、文字列長を超過しないように調整
      int ed = (rule.getEndIndex() == 0) ? src.length() : Math.min(src.length(), rule.getEndIndex());

      // 部分文字列を取得して返す
      return src.substring(st, ed);
    }

  }

}
