package net.utsuro.mask;

/**
 * Nullまたは空文字の場合に固定値に置換するクラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>toClassName</td><td>置換後のクラス名(必須)</td></tr>
 * <tr><td>ignoreValuePattern</td><td>対象外にする値のパターン(正規表現) ※マッチした場合は元の値そのまま返却</td></tr>
 * <tr><td>fixedValue</td><td>固定値 ※システム日付をセットしたい場合は %sysdate を指定、タイムスタンプの場合は %systimestamp を指定する。</td></tr>
 * </table>
 */
public class NullToValue implements DataMask {

  /**
   * Nullまたは空文字の場合に固定値に置換する.
   * @param src 置換したい値
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (src != null && !(src instanceof String))
        || (src instanceof String && !((String) src).isEmpty())) {
      // ルールが無い場合、null以外の場合、文字列で空文字以外はそのまま返却
      return src;
    }

    MaskingRule tempRule = new MaskingRule(rule);
    tempRule.setNullReplace(true);

    // 固定値変換して返す
    return FixedValueConverter.convert(src, tempRule);

  }

}
