package net.utsuro.mask;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RandomNumGenerator implements DataMask {

  /**
   * ランダム生成数値に置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の数値
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    BigDecimal num;
    if (rule.isNullReplace() && src == null) {
      num = BigDecimal.ZERO;
    } else if (src instanceof Long) {
      num = BigDecimal.valueOf((Long) src);
    } else if (src instanceof Integer) {
      num = BigDecimal.valueOf((Integer) src);
      num = new BigDecimal(((Integer) src));
    } else if (src instanceof Float) {
      num = BigDecimal.valueOf((Float) src);
    } else if (src instanceof Double) {
      num = BigDecimal.valueOf((Double) src);
    } else if (src instanceof BigInteger) {
      num = new BigDecimal((BigInteger) src);
    } else if (src instanceof BigDecimal) {
      num = (BigDecimal) src;
    } else if ((src instanceof String) && ((String) src).matches("-?\\d+(\\.\\d+)?")) {
      num = new BigDecimal((String) src);
    } else {
      // 引き渡されたオブジェクトが数値でない場合はそのまま返却
      return src;
    }

    return generate(num, rule);

  }

  /**
   * ランダム生成数値に置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の数値
   * @throws Exception エラー発生時
   */
  public static BigDecimal generate(BigDecimal src, MaskingRule rule) {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    if (src != null && rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src.toString()).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    BigDecimal num = src;
    if (rule.isNullReplace() && src == null) {
      num = BigDecimal.ZERO;
    }

    // 最小値・最大値の設定
    BigDecimal min = rule.getMinDecimalValue();
    BigDecimal max = rule.getMaxDecimalValue();
    if (min == null) {
      // 最小値指定なしはゼロ
      min = BigDecimal.ZERO;
    }
    if (max == null) {
      // 最大値指定なしは今の値の桁数の最大値 (ex: 123 → 999)
      max = BigDecimal.TEN.pow(num.precision()).subtract(BigDecimal.ONE);
    }

    // 乱数生成して返す
    return MaskingUtil.getRandomNumber(min, max);

  }

}
