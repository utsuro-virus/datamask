package net.utsuro.mask;

/**
 * リストのランダム選択クラス.
 */
public class RandomListPickup implements DataMask {

  private int[] weights = null;
  private int totalWeight = 0;

  /**
   * ランダムにリストから選択して置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    int picupListCount = (rule.getPicupList() == null) ? 0 : rule.getPicupList().length;
    int weightsCount = (rule.getPicupWeights() == null) ? 0 : rule.getPicupWeights().length;

    if (picupListCount == 0) {
      // リストが設定されていない場合はそのまま返却
      return src;
    }

    if (weights == null) {
      // 未作成のときは重みリストの作成と重み合計値の計算
      weights = new int[picupListCount];
      for (int i = 0; i < picupListCount; i++) {
        // 重み指定がまったく無い場合は一律1をセット、足りない場合は0をセット
        weights[i] = (weightsCount == 0) ? 1 : ((i < weightsCount) ? rule.getPicupWeights()[i] : 0);
        totalWeight += weights[i];
      }
    }

    // 重み付きランダム取得のインデックスからリスト値を選択して返却
    int idx = MaskingUtil.getRandomIndex(weights, totalWeight);
    return rule.getPicupList()[idx];

  }

}
