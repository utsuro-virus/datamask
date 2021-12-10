package net.utsuro.mask;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日付のランダム生成クラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>nullReplace</td><td>元値がNullの場合でも置換するかどうか</td></tr>
 * <tr><td>invalidDateReplace</td><td>元値が不正日付の場合でも置換するかどうか</td></tr>
 * <tr><td>ignoreValuePattern</td><td>対象外にする値のパターン(正規表現) ※マッチした場合は元の値そのまま返却</td></tr>
 * <tr><td>minDate</td><td>最小値(日付)</td></tr>
 * <tr><td>maxDate</td><td>最大値(日付)</td></tr>
 * <tr><td>termFrom</td><td>ランダム生成の期間FROM(日付) ※指定は元の値に加減算する 数値＋YMD で行う。負の数も指定可能。</td></tr>
 * <tr><td>termTo</td><td>ランダム生成の期間TO(日付) ※指定は元の値に加減算する 数値＋YMD で行う。負の数も指定可能。</td></tr>
 * </table>
 */
public class RandomDateGenerator implements DataMask {

  /**
   * ランダム生成日付に置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の日付
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    LocalDateTime dt = null;

    if (src != null) {
      if (src.getClass() != LocalDateTime.class) {
        MaskingRule tempRule = new MaskingRule(rule);
        tempRule.setToClassName(LocalDateTime.class.getName());
        try {
          // LocalDateTimeに統一する
          dt = (LocalDateTime) TypeConverter.convert(src, tempRule);
        } catch (IllegalArgumentException | java.time.DateTimeException e) {
          if (rule.isInvalidDateReplace()) {
            // 不正日付置換ありなら現在日時を元値にセット
            dt = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
          } else {
            // 引き渡されたオブジェクトが日付や日付に変換可能な値でない場合はそのまま返却
            return src;
          }
        }
      } else {
        dt = (LocalDateTime) src;
      }
    }

    return generate(dt, rule);

  }

  /**
   * ランダム生成日付に置換する.
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の日付
   */
  public static LocalDateTime generate(LocalDateTime src, MaskingRule rule) {

    if (rule == null || (!rule.isNullReplace() && src == null)) {
      // ルールが無い場合、null置換無しで引き渡された値がnullの場合はそのまま返却
      return src;
    }

    if (src != null && rule.getIgnoreValuePattern() != null
        && rule.getIgnoreValuePattern().matcher(src.toString()).find()) {
      // 除外値パターンにマッチした場合はそのまま返す
      return src;
    }

    LocalDateTime dt = src;
    if (rule.isNullReplace() && src == null) {
      // null置換ありなら現在日時をセット
      dt = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
    }

    // 最小値・最大値の設定
    LocalDateTime min = rule.getMinDate();
    LocalDateTime max = rule.getMaxDate();

    // 元値を基準に期間指定の場合は最小・最大を計算
    Pattern ptn = Pattern.compile("(([-0-9]+)Y)?(([-0-9]+)M)?(([-0-9]+)D)?");
    if (rule.getTermFrom() != null && !rule.getTermFrom().isEmpty()) {
      Matcher m = ptn.matcher(rule.getTermFrom());
      if (m.find()) {
        LocalDateTime buff = dt.plusDays(0);
        if (m.group(2) != null) {
          buff = buff.plusYears(Integer.parseInt(m.group(2)));
        }
        if (m.group(4) != null) {
          buff = buff.plusMonths(Integer.parseInt(m.group(4)));
        }
        if (m.group(6) != null) {
          buff = buff.plusDays(Integer.parseInt(m.group(6)));
        }
        min = buff;
      }
    }
    if (rule.getTermTo() != null && !rule.getTermTo().isEmpty()) {
      Matcher m = ptn.matcher(rule.getTermTo());
      if (m.find()) {
        LocalDateTime buff = dt.plusDays(0);
        if (m.group(2) != null) {
          buff = buff.plusYears(Integer.parseInt(m.group(2)));
        }
        if (m.group(4) != null) {
          buff = buff.plusMonths(Integer.parseInt(m.group(4)));
        }
        if (m.group(6) != null) {
          buff = buff.plusDays(Integer.parseInt(m.group(6)));
        }
        max = buff;
      }
    }

    if (min == null) {
      // 最小値指定なしは最小日時
      min = LocalDateTime.parse("0000-01-01T00:00:00");
    }
    if (max == null) {
      // 最大値指定なしは最大日時
      max = LocalDateTime.parse("9999-12-31T23:59:59");
    }

    // 乱数生成して返す
    return MaskingUtil.getRandomDate(min, max);

  }

}
