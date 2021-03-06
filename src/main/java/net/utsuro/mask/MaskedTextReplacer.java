package net.utsuro.mask;

import java.util.regex.Pattern;

/**
 * 文字列をパターンマスクするクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>ignoreValuePattern</td><td>対象外にする値のパターン(正規表現) ※マッチした場合は元の値そのまま返却</td></tr>
 * <tr><td>unmaksedLengthLeft</td><td>マスクしない文字数(左)</td></tr>
 * <tr><td>unmaksedLengthRight</td><td>マスクしない文字数(右)</td></tr>
 * <tr><td>unmaksedCharPattern</td><td>マスクしない文字パターン(正規表現)</td></tr>
 * <tr><td>useWhiteSpaceMask</td><td>全半角スペース、タブ、改行の置換有無</td></tr>
 * <tr><td>useOddCharMask</td><td>奇数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>useEvenCharMask</td><td>偶数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>useEvenCharMask</td><td>偶数目の文字のみマスクするパターンの使用有無</td></tr>
 * <tr><td>replacementWideNum</td><td>マスク時の置換文字列(全角数字) ※指定なしは"９"</td></tr>
 * <tr><td>replacementWideChar</td><td>マスク時の置換文字列(全角) ※指定なしは"○"</td></tr>
 * <tr><td>replacementHalfNum</td><td>マスク時の置換文字列(半角数字) ※指定なしは"9"</td></tr>
 * <tr><td>replacementHalfChar</td><td>マスク時の置換文字列(半角) ※指定なしは"X"</td></tr>
 * </table>
 */
public class MaskedTextReplacer implements DataMask {

  /**
   * 文字列をパターンマスクする.
   * @param src マスクしたい文字列
   * @param rule マスク化ルール
   * @return マスク後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された文字列がnullの場合はそのまま返却
      return src;
    }

    String tarStr;
    if (src instanceof String) {
      tarStr = (String) src;
    } else {
      tarStr = src.toString();
    }

    return replace(tarStr, rule);

  }

  /**
   * 文字列をパターンマスクする.
   * @param src マスクしたい文字列
   * @param rule マスク化ルール
   * @return マスク後の文字列
   */
  public static String replace(String src, MaskingRule rule) {

    StringBuilder sb = new StringBuilder();

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された文字列がnullの場合はそのまま返却
      return src;
    }

    if (src.isEmpty()) {
      // 引き渡された文字列が空の場合はそのまま返却
      return src;
    }

    if (rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    String[] c = src.split("");
    int len = c.length;
    int start = rule.getUnmaksedLengthLeft();
    if (start < 0) {
      // 開始がマイナスの場合は後ろから数える
      start = len + start;
    }
    int end = len - rule.getUnmaksedLengthRight() - 1;
    Pattern unMaskPtn = rule.getUnmaksedCharPattern();
    Pattern spaceMaskPtn = (rule.useWhiteSpaceMask()) ? null : Pattern.compile("[ 　、。､｡\r\n\t]");

    // 1文字ずつ入れ替え
    for (int i = 0; i < len; i++) {
      if (i >= start && i <= end
          && (!rule.useOddCharMask() || i % 2 == 0)
          && (!rule.useEvenCharMask() || i % 2 == 1)) {
        // 開始・終了の範囲内かつ奇数・偶数指定ありは該当文字目のみマスク
        if ((unMaskPtn == null || !unMaskPtn.matcher(c[i]).find())
            && (spaceMaskPtn == null || !spaceMaskPtn.matcher(c[i]).find())) {
          // 除外パターンが指定されていないか、マッチしなかった場合はマスク
          if (MaskingUtil.isWideChar(c[i])) {
            if (rule.useReplacementWideNum() && c[i].matches("[０-９]")) {
              // 全角数字
              sb.append(rule.getReplacementWideNum());
            } else {
              // 全角文字
              sb.append((rule.useReplacementWideChar()) ? rule.getReplacementWideChar() : c[i]);
            }
          } else {
            if (rule.useReplacementHalfNum() && c[i].matches("[0-9]")) {
              // 半角数字
              sb.append(rule.getReplacementHalfNum());
            } else {
              // 半角文字
              sb.append((rule.useReplacementHalfChar()) ? rule.getReplacementHalfChar() : c[i]);
            }
          }
        } else {
          // 除外パターンにマッチした場合はそのまま返却
          sb.append(c[i]);
        }
      } else {
        // マスク範囲外の文字はそのまま返却
        sb.append(c[i]);
      }
    }

    return sb.toString();
  }

}
