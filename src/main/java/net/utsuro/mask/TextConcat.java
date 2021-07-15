package net.utsuro.mask;

/**
 * 複数文字列を1つに結合するクラス.
 */
public class TextConcat implements DataMask {

  /**
   * 複数文字列を1つに結合する.
   * @param src 結合したい値の配列
   * @param rule マスク化ルール
   * @return 結合後の値
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return concat(src, rule);

  }

  /**
   * 複数文字列を1つに結合する.
   * @param src 結合したい値の配列
   * @param rule マスク化ルール
   * @return 結合後の値
   * @throws Exception エラー発生時
   */
  public static Object concat(Object src, MaskingRule rule) throws Exception {

    if (rule == null || src == null || !src.getClass().isArray()) {
      // ルールが無い場合、引き渡された値がnullの場合、配列でない場合はそのまま返却
      return src;
    }

    Object[] arr = (Object[]) src;
    StringBuilder sb = new StringBuilder();
    MaskingRule tempRule = new MaskingRule(rule);
    tempRule.setToClassName(String.class.getName());

    // 文字列に変換して結合
    for (int i = 0; i < arr.length; i++) {
      sb.append(TypeConverter.convert(arr[i], tempRule));
      if (rule.getSeparator() != null && !rule.getSeparator().isEmpty() && i < arr.length - 1) {
        // 指定があればセパレータを付与
        sb.append(rule.getSeparator());
      }
    }

    return sb.toString();

  }

}
