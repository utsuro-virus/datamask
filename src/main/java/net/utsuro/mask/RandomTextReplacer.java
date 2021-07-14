package net.utsuro.mask;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import net.utsuro.mask.MaskingUtil.CharType;

public class RandomTextReplacer implements DataMask {

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
   * ランダム生成文字列に置換する.
   * ・生成する文字列は指定がなけれけば元の文字種と同じものの中から生成する
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
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

    String ret = null;

    if (rule.isDeterministicReplace()) {
      // 既登録の結果を使用する場合
      ret = (String) getRegisteredUniqueVal(rule.getUniqueId(), tarStr);
    }

    if (ret == null) {
      // 新規生成
      boolean isValid = false;
      int retryCount = 0;
      while (!isValid) {
        ret = replace(tarStr, rule);
        // ユニークでないとならない場合は生成結果のチェック
        if (!rule.isUniqueValue() || !isExistsInUniqueList(rule.getUniqueId(), ret)) {
          isValid = true;
          if (rule.isUniqueValue() || rule.isDeterministicReplace()) {
            // 一貫性が必要な場合とユニーク性が必要な場合はユニークリストに追加
            // ※リストに追加失敗した場合は再抽選
            isValid = addUniqueList(rule.getUniqueId(), tarStr, ret);
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
   * ランダム生成文字列に置換する.
   * ・生成する文字列は指定がなけれけば元の文字種と同じものの中から生成する
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   * @throws Exception エラー発生時
   */
  public static String replace(String src, MaskingRule rule) throws Exception {

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

    // 文字種判定
    CharType[] charTypes = new CharType[c.length];
    for (int i = 0; i < len; i++) {
      if (i >= start && i <= end
          && (!rule.useOddCharMask() || i % 2 == 0)
          && (!rule.useEvenCharMask() || i % 2 == 1)) {
        // 開始・終了の範囲内かつ奇数・偶数指定ありは該当文字目のみマスク
        if ((unMaskPtn == null || !unMaskPtn.matcher(c[i]).find())
            && (spaceMaskPtn == null || !spaceMaskPtn.matcher(c[i]).find())) {
          // 除外パターンが指定されていないか、マッチしなかった場合はランダム生成文字列に差し替えする
          boolean isWide = MaskingUtil.isWideChar(c[i]);
          if (rule.useRandomGenCharType()) {
            // 生成文字種が指定されている場合はそれを生成
            charTypes[i] = rule.getRandomGenCharType();
          } else {
            // 生成文字種の指定がなければ元の値と同じ文字種を生成
            charTypes[i] = CharType.getTypeByString(c[i]);
          }
          if (charTypes[i] == CharType.UNKNOWN) {
            // 文字種不明の場合はWIDEかHALFで指定
            charTypes[i] = (isWide) ? CharType.WIDE : CharType.HALF;
          }
        } else {
          // 除外パターンにマッチした場合はそのまま返却
          charTypes[i] = CharType.NONE;
        }
      } else {
        // マスク範囲外の文字はそのまま返却
        charTypes[i] = CharType.NONE;
      }
    }

    // 文字種ごとに生成
    int byteCount = 0;
    for (int i = 0; i < len; i++) {
      if (charTypes[i] != CharType.NONE) {
        byteCount += charTypes[i].getReqByte();
        // 最後の1文字または次の文字と文字種が異なる場合はまとめてランダム生成
        if (i == len - 1 || charTypes[i] != charTypes[i + 1]) {
          sb.append(MaskingUtil.getRandomString(byteCount, charTypes[i],
              rule.getRandomNoGenCharPattern()));
          byteCount = 0;
        }
      } else {
        // マスク範囲外や除外パターンにマッチした場合はそのまま返却
        sb.append(c[i]);
        byteCount = 0;
      }
    }

    if (rule.useUpperCaseKana() || rule.useHalfKana()
        || rule.useWideKana() || rule.useHiragana()
        || rule.useUpperCase() || rule.useLowerCase()) {
      // 文字変換が指定されている場合は変換
      MaskingRule afterRepRule = new MaskingRule(rule);
      afterRepRule.setToClassName(String.class.getName());
      sb = new StringBuilder().append(TypeConverter.convert(sb.toString(), afterRepRule));
    }

    if (rule.useAfterTextReplace()) {
      // ランダム生成後に更に置換するかどうか
      MaskingRule afterRepRule = new MaskingRule(rule);
      afterRepRule.useOddCharMask(rule.useAfterRepOddCharMask());
      afterRepRule.useEvenCharMask(rule.useAfterRepEvenCharMask());
      sb = new StringBuilder(MaskedTextReplacer.replace(sb.toString(), afterRepRule));
    }

    return sb.toString();

  }

}
