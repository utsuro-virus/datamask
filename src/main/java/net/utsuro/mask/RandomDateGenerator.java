package net.utsuro.mask;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
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
 * <tr><td>isUniqueValue</td><td>生成した値を一意にするかどうか(NULL以外)</td></tr>
 * <tr><td>isDeterministicReplace</td><td>決定論的置換するかどうか ※INPUTが同じならOUTPUTも同じ値にする(NULL以外)</td></tr>
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

  private static final int RETRY_MAX = 5;
  private Connection conn;

  /**
   * このマスク処理でテータベースを使用するかどうか.
   * @return true=使用する, false=使用しない
   */
  @Override
  public boolean useDatabase(MaskingRule rule) {
    return (rule.isUniqueValue() || rule.isDeterministicReplace());
  }

  /**
   * DBコネクションを取得.
   * @return conn
   */
  public Connection getConnection() {
    return conn;
  }

  /**
   * DBコネクションをセット.
   * @param conn セットする conn
   */
  public void setConnection(Connection conn) {
    this.conn = conn;
  }

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
    String dtStr = null;
    MaskingRule tempRule = new MaskingRule(rule);
    tempRule.setToClassName(LocalDateTime.class.getName());

    if (src != null) {
      if (src.getClass() != LocalDateTime.class) {
        try {
          // LocalDateTimeに統一する
          dt = (LocalDateTime) TypeConverter.convert(src, tempRule);
          dtStr = dt.toString();
        } catch (IllegalArgumentException | java.time.DateTimeException e) {
          if (rule.isInvalidDateReplace()) {
            // 不正日付置換ありなら現在日時を元値にセット
            dt = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
            dtStr = src.toString();
          } else {
            // 引き渡されたオブジェクトが日付や日付に変換可能な値でない場合はそのまま返却
            return src;
          }
        }
      } else {
        dt = (LocalDateTime) src;
        dtStr = dt.toString();
      }
    }

    LocalDateTime ret = null;
    String retStr = null;

    if (dtStr != null && rule.isDeterministicReplace()) {
      // 既登録の結果を使用する場合
      retStr = (String) getRegisteredUniqueVal(rule.getUniqueId(), dtStr);
      if (retStr != null) {
        try {
          // LocalDateTimeに統一する
          ret = (LocalDateTime) TypeConverter.convert(retStr, tempRule);
        } catch (IllegalArgumentException | java.time.DateTimeException e) {
          // 引き渡されたオブジェクトが日付や日付に変換可能な値でない場合はそのまま返却
          return retStr;
        }
      }
    }

    if (ret == null) {
      // 新規生成
      boolean isValid = false;
      int retryCount = 0;
      while (!isValid) {
        ret = generate(dt, rule);
        // ユニークでないとならない場合は生成結果のチェック
        if (!rule.isUniqueValue() || !isExistsInUniqueList(rule.getUniqueId(), ret.toString())) {
          isValid = true;
          if (rule.isUniqueValue() || rule.isDeterministicReplace()) {
            // 一貫性が必要な場合とユニーク性が必要な場合はユニークリストに追加
            // ※リストに追加失敗した場合は再抽選
            isValid = addUniqueList(rule.getUniqueId(), dtStr, ret.toString());
            if (!isValid) {
              retryCount++;
            }
            if (retryCount > RETRY_MAX) {
              // 何度やってもユニークにならない場合、設定ルールがおかしいと思われるのでエラー
              throw new SQLIntegrityConstraintViolationException(
                  String.format("%d回重複してユニークリストの登録に失敗しました。", RETRY_MAX));
            }
          }
        }
      }
    }
    return ret;

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
