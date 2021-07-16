package net.utsuro.mask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 文字列のシャッフル置換クラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>ignoreValuePattern</td><td>対象外にする値のパターン(正規表現) ※マッチした場合は元の値そのまま返却</td></tr>
 * </table>
 */
public class ShuffleTextReplacer implements DataMask {

  /**
   * 文字列をシャッフル置換する.
   * @param src シャッフルしたい値
   * @param rule マスク化ルール
   * @return シャッフル後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return replace((String) src, rule);

  }

  /**
   * 文字列をシャッフル置換する.
   * @param src シャッフルしたい値
   * @param rule マスク化ルール
   * @return シャッフル後の文字列
   */
  public static String replace(String src, MaskingRule rule) {

    if (rule == null || src == null || src.isEmpty()) {
      // ルールが無い場合、引き渡された値がnullまたは空白の場合はそのまま返却
      return src;
    }

    if (rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    StringBuilder sb = new StringBuilder();

    // 分割してシャッフルして結合
    List<String> list = Arrays.asList(src.split(""));
    Collections.shuffle(list, ThreadLocalRandom.current());
    for (String s : list) {
      sb.append(s);
    }

    return sb.toString();

  }

}
