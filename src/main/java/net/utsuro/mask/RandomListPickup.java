package net.utsuro.mask;

/**
 * リストのランダム選択クラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>isNullReplace</td><td>元値がNullの場合でも置換するかどうか</td></tr>
 * <tr><td>pickupList</td><td>ピックアップリスト</td></tr>
 * <tr><td>pickupWeights</td><td>ピックアップリストの重み(確率)</td></tr>
 * </table>
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

    int pickupListCount = (rule.getPickupList() == null) ? 0 : rule.getPickupList().length;
    int weightsCount = (rule.getPickupWeights() == null) ? 0 : rule.getPickupWeights().length;

    if (pickupListCount == 0) {
      // リストが設定されていない場合はそのまま返却
      return src;
    }

    if (weights == null) {
      // 未作成のときは重みリストの作成と重み合計値の計算
      weights = new int[pickupListCount];
      for (int i = 0; i < pickupListCount; i++) {
        // 重み指定がまったく無い場合は一律1をセット、足りない場合は0をセット
        weights[i] = (weightsCount == 0) ? 1 : ((i < weightsCount) ? rule.getPickupWeights()[i] : 0);
        totalWeight += weights[i];
      }
    }

    // 重み付きランダム取得のインデックスからリスト値を選択して返却
    int idx = MaskingUtil.getRandomIndex(weights, totalWeight);
    return rule.getPickupList()[idx];

  }

}
