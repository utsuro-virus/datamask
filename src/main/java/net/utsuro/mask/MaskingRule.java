package net.utsuro.mask;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import net.utsuro.mask.MaskingUtil.CharType;

@Data
public class MaskingRule {

  /**
   * マスクしない文字数(左).
   */
  private int unmaksedLengthLeft = 0;

  /**
   * マスクしない文字数(右).
   */
  private int unmaksedLengthRight = 0;

  /**
   * マスクしない文字パターン(正規表現).
   */
  private Pattern unmaksedCharPattern = null;

  /**
   * 対象外にする値のパターン(正規表現).
   * ※文字ではなく値全体にマッチさせる
   */
  private Pattern ignoreValuePattern = null;

  /**
   * 奇数目の文字のみマスクするパターンの使用有無.
   */
  @Accessors(fluent = true)
  private boolean useOddCharMask = false;

  /**
   * 偶数目の文字のみマスクするパターンの使用有無.
   */
  @Accessors(fluent = true)
  private boolean useEvenCharMask = false;

  /**
   * マスク後に置換マスクを使用するかどうか.
   * ※ランダム生成文字列とランダム置換で使える
   */
  @Accessors(fluent = true)
  private boolean useAfterTextReplace = false;

  /**
   * マスク後の置換マスクで奇数目の文字のみマスクするパターンの使用有無.
   * ※ランダム生成文字列とランダム置換で使える
   */
  @Accessors(fluent = true)
  private boolean useAfterRepOddCharMask = false;

  /**
   * マスク後の置換マスクで偶数目の文字のみマスクするパターンの使用有無.
   * ※ランダム生成文字列とランダム置換で使える
   */
  @Accessors(fluent = true)
  private boolean useAfterRepEvenCharMask = false;

  /**
   * 全半角スペース、タブ、改行の置換有無.
   */
  @Accessors(fluent = true)
  private boolean useWhiteSpaceMask = false;

  /**
   * マスク時の置換文字列(半角).
   */
  private String replacementHalfChar = "X";

  /**
   * マスク時の置換文字列(全角).
   */
  private String replacementWideChar = "○";

  /**
   * マスク時の置換文字列(半角数字).
   */
  private String replacementHalfNum = "9";

  /**
   * マスク時の置換文字列(全角数字).
   */
  private String replacementWideNum = "９";

  /**
   * 元値がNullの場合でも置換するかどうか.
   */
  private boolean isNullReplace = false;

  /**
   * ランダム生成文字の文字種.
   * ※無指定は元の文字種と同じものを生成
   */
  private CharType randomGenCharType = null;

  /**
   * ランダム生成しない文字パターン(正規表現).
   * ※記号はOKでもカンマとかクォートはNGとか自動生成パスワードのlとIやOと0は見分けが付きにくいから除外とか
   */
  private Pattern randomNoGenCharPattern = null;

  /**
   * 最小値(数値).
   * ※指定なしは0
   */
  private String minValue = "";

  /**
   * 最大値(数値).
   * ※指定なしは入力値の桁数のMAX
   */
  private String maxValue = "";

  /**
   * 最小値(数値).
   * ※指定なしは0
   */
  private BigDecimal minDecimalValue = null;

  /**
   * 最大値(数値).
   * ※指定なしは入力値の桁数のMAX
   */
  private BigDecimal maxDecimalValue = null;

  /**
   * ランダム生成時の接頭語.
   */
  private String prefix = "";

  /**
   * ランダム生成時の接尾語.
   */
  private String suffix = "";

  /**
   * 最小SJIS換算バイト数(文字列).
   */
  private int minSjisByteCount = 0;

  /**
   * 最大SJIS換算桁数(文字列).
   */
  private int maxSjisByteCount = 0;

  /**
   * メールアドレス生成時のドメイン名.
   */
  private String domainReplacement = "";

  /**
   * 最小値(日付).
   */
  private LocalDateTime minDate = null;

  /**
   * 最大値(日付).
   */
  private LocalDateTime maxDate = null;

  /**
   * ランダム生成の期間FROM(日付).
   * ※指定は元の値に加減算する 数値＋YMD で行う。負の数も指定可能。
   */
  private String termFrom = null;

  /**
   * ランダム生成の期間TO(日付).
   * ※指定は元の値に加減算する 数値＋YMD で行う。負の数も指定可能。
   */
  private String termTo = null;

  /**
   * ピックアップリスト.
   */
  private String[] picupList = null;

  /**
   * ピックアップリストの重み(確率).
   */
  private int[] picupWeights = null;

  /**
   * データ選択リストの対象テーブル名.
   */
  private String selectListTableName = "";

  /**
   * データ選択リストの対象カラム名.
   */
  private String selectListColName = "";

  /**
   * データ選択リストの連番カラム名.
   * ※ランダム選択するためには対象テーブルには空き番の無い連番カラム(数値)が必要。
   *  指定が無い場合はデフォルトのseqnoとなる。
   */
  private String selectListSeqNoColName = "";

  /**
   * 住所生成時に郵便番号をハイフン付きにするかどうか.
   */
  @Accessors(fluent = true)
  private boolean usePostCodeFormat = true;

  /**
   * 変換時や住所・個人名生成時にフリガナを半角にするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useHalfKana = false;

  /**
   * 変換時や住所・個人名生成時にフリガナを大文字にするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useUpperCaseKana = false;

  /**
   * 変換時や住所・個人名生成時にふりがなをカナにするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useWideKana = false;

  /**
   * 変換時に全半角カナをひらがなにするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useHiragana = false;

  /**
   * 変換時に英字の大文字変換をするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useUpperCase = false;

  /**
   * 変換時に英字の小文字変換をするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useLowerCase = false;

  /**
   * 住所生成時に番地部分に元の値を使用するかどうか.
   */
  @Accessors(fluent = true)
  private boolean useBanchiGenerate = true;

  /**
   * 住所生成時に返却する配列フォーマット(カンマ区切り).
   * ※デフォルトは下記
   *  [0] %zip        郵便番号
   *  [1] %pref       都道府県
   *  [2] %city       市区町村
   *  [3] %town       町域
   *  [4] %street     番地
   *  [5] %prefKana   都道府県カナ
   *  [6] %cityKana   市区町村カナ
   *  [7] %townKana   町域カナ
   *  [8] %streetKana 番地カナ
   */
  private String addrFormat =
      "%zip,%pref,%city,%town,%street,%prefKana,%cityKana,%townKana,%streetKana";

  /**
   * 個人名生成時に返却する配列フォーマット(カンマ区切り).
   * ※デフォルトは下記
   *  [0] %lastNameKanji %firstNameKanji 氏名漢字
   *  [1] %lastNameKana %firstNameKana   氏名カナ
   */
  private String fullNameFormat = "%lastNameKanji %firstNameKanji,%lastNameKana %firstNameKana";

  /**
   * 決定論的置換するかどうか.
   * ※INPUTが同じならOUTPUTも同じ値にする(NULL以外)
   */
  private boolean isDeterministicReplace = false;

  /**
   * 生成した値を一意にするかどうか(NULL以外).
   */
  private boolean isUniqueValue = false;

  /**
   * 決定論的/一意制管理の識別子.
   * ※カラム名で無くても良い
   */
  private String uniqueId = "";

  /**
   * 型変換後のクラス名.
   */
  private String toClassName = "";

  /**
   * 型変換後のクラス名(複数).
   */
  private List<String> toClassNames = null;

  /**
   * 型変換時の日時書式.
   * ・省略時 LocalDateTime はyyyy/MM/dd HH:mm:ss または yyyyMMddHHmmss(数値型) になる
   * ・省略時 LocalDate はyyyy/MM/dd または yyyyMMdd(数値型) になる
   * ・省略時 LocalTime はHH:mm:ss または HHmmss(数値型) になる
   */
  private String dateTimeFormat = "";

  /**
   * 固定値.
   * ※システム日付をセットしたい場合は %sysdate を指定、タイムスタンプの場合は %systimestamp を指定する。
   */
  private String fixedValue = "";

  /**
   * 全角スペースのTrimをするかどうか.
   */
  private boolean keepWideSpaceTrim = false;

  /**
   * LTrimをするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useLTrim = false;

  /**
   * RTrimをするかどうか.
   */
  @Accessors(fluent = true)
  private boolean useRTrim = false;

  /**
   * 文字列分割・結合時のセパレータ.
   */
  private String separator = "";

  /**
   * 部分文字列取得時の開始インデックス.
   */
  private int beginIndex = 0;

  /**
   * 部分文字列取得時の終了インデックス.
   */
  private int endIndex = 0;

  /**
   * 文字列置換時の正規表現.
   */
  private String textReplaceRegex = "";

  /**
   * 文字列置換時の置換文字列.
   */
  private String textReplacement = "";

  /**
   * デフォルトコンストラクタ.
   */
  public MaskingRule() {}

  /**
   * コピーコンストラクタ.
   * @param that コピー元
   */
  public MaskingRule(MaskingRule that) {
    this.unmaksedLengthLeft = that.getUnmaksedLengthLeft();
    this.unmaksedLengthRight = that.getUnmaksedLengthRight();
    this.unmaksedCharPattern = that.getUnmaksedCharPattern();
    this.ignoreValuePattern = that.getIgnoreValuePattern();
    this.useOddCharMask = that.useOddCharMask();
    this.useEvenCharMask = that.useEvenCharMask();
    this.useAfterTextReplace = that.useAfterTextReplace();
    this.useAfterRepOddCharMask = that.useAfterRepOddCharMask();
    this.useAfterRepEvenCharMask = that.useAfterRepEvenCharMask();
    this.useWhiteSpaceMask = that.useWhiteSpaceMask();
    this.replacementHalfChar = that.getReplacementHalfChar();
    this.replacementWideChar = that.getReplacementWideChar();
    this.replacementHalfNum = that.getReplacementHalfNum();
    this.replacementWideNum = that.getReplacementWideNum();
    this.isNullReplace = that.isNullReplace();
    this.randomGenCharType = that.getRandomGenCharType();
    this.randomNoGenCharPattern = that.getRandomNoGenCharPattern();
    this.minValue = that.getMinValue();
    this.maxValue = that.getMaxValue();
    this.minDecimalValue = that.getMinDecimalValue();
    this.maxDecimalValue = that.getMaxDecimalValue();
    this.prefix = that.getPrefix();
    this.suffix = that.getSuffix();
    this.minSjisByteCount = that.getMinSjisByteCount();
    this.maxSjisByteCount = that.getMaxSjisByteCount();
    this.domainReplacement = that.getDomainReplacement();
    this.minDate = that.getMinDate();
    this.maxDate = that.getMaxDate();
    this.termFrom = that.getTermFrom();
    this.termTo = that.getTermTo();
    if (that.getPicupList() != null) {
      this.picupList = new String[that.getPicupList().length];
      System.arraycopy(that.getPicupList(), 0, this.picupList, 0, that.getPicupList().length);
    }
    if (that.getPicupWeights() != null) {
      this.picupWeights = new int[that.getPicupWeights().length];
      System.arraycopy(
          that.getPicupWeights(), 0, this.picupWeights, 0, that.getPicupWeights().length);
    }
    this.selectListTableName = that.getSelectListTableName();
    this.selectListColName = that.getSelectListColName();
    this.selectListSeqNoColName = that.getSelectListSeqNoColName();
    this.usePostCodeFormat = that.usePostCodeFormat();
    this.useHalfKana = that.useHalfKana();
    this.useUpperCaseKana = that.useUpperCaseKana();
    this.useWideKana = that.useWideKana();
    this.useHiragana = that.useHiragana();
    this.useUpperCase = that.useUpperCase();
    this.useLowerCase = that.useLowerCase();
    this.useBanchiGenerate = that.useBanchiGenerate();
    this.addrFormat = that.getAddrFormat();
    this.fullNameFormat = that.getFullNameFormat();
    this.isDeterministicReplace = that.isDeterministicReplace();
    this.isUniqueValue = that.isUniqueValue();
    this.toClassName = that.getToClassName();
    if (that.getToClassNames() != null) {
      this.toClassNames = new ArrayList<>(that.getToClassNames());
    }
    this.dateTimeFormat = that.getDateTimeFormat();
    this.fixedValue = that.getFixedValue();
    this.keepWideSpaceTrim = that.isKeepWideSpaceTrim();
    this.useLTrim = that.useLTrim();
    this.useRTrim = that.useRTrim();
    this.separator = that.getSeparator();
    this.beginIndex = that.getBeginIndex();
    this.endIndex = that.getEndIndex();
    this.textReplaceRegex = that.getTextReplaceRegex();
    this.textReplacement = that.getTextReplacement();
  }

  /**
   * マスクしない文字数(右)を取得.
   * ※負の数を指定した場合はゼロに丸められる
   * @param unmaksedLengthRight セットする unmaksedLengthRight
   */
  public int getUnmaksedLengthRight() {
    return Math.max(unmaksedLengthRight, 0);
  }

  /**
   * マスクしない文字パターン(正規表現)を取得.
   * @return unmaksedChar
   */
  public String getUnmaksedChar() {
    return (unmaksedCharPattern != null) ? unmaksedCharPattern.toString() : "";
  }

  /**
   * マスクしない文字パターン(正規表現)をセット.
   * @param unmaksedChar セットする unmaksedChar
   */
  public void setUnmaksedChar(String unmaksedChar) {
    if (unmaksedChar != null && !unmaksedChar.isEmpty()) {
      this.unmaksedCharPattern = Pattern.compile(unmaksedChar, Pattern.CASE_INSENSITIVE);
    } else {
      this.unmaksedCharPattern = null;
    }
  }

  /**
   * 対象外にする値のパターン(正規表現)を取得.
   * ※文字ではなく値全体にマッチさせる
   * @return ignoreValue
   */
  public String getIgnoreValue() {
    return (ignoreValuePattern != null) ? ignoreValuePattern.toString() : "";
  }

  /**
   * 対象外にする値のパターン(正規表現)をセット.
   * ※文字ではなく値全体にマッチさせる
   * @param ignoreValue セットする ignoreValue
   */
  public void setIgnoreValue(String ignoreValue) {
    if (ignoreValue != null && !ignoreValue.isEmpty()) {
      this.ignoreValuePattern = Pattern.compile(ignoreValue, Pattern.CASE_INSENSITIVE);
    } else {
      this.ignoreValuePattern = null;
    }
  }

  /**
   * マスク時の置換文字列(半角)が設定されているかどうか.
   * @return true=設定されている, false=設定されていない
   */
  public boolean useReplacementHalfChar() {
    return (replacementHalfChar != null && !replacementHalfChar.isEmpty());
  }

  /**
   * マスク時の置換文字列(半角数字)が設定されているかどうか.
   * @return true=設定されている, false=設定されていない
   */
  public boolean useReplacementHalfNum() {
    return (replacementHalfNum != null && !replacementHalfNum.isEmpty());
  }

  /**
   * マスク時の置換文字列(全角)が設定されているかどうか.
   * @return true=設定されている, false=設定されていない
   */
  public boolean useReplacementWideChar() {
    return (replacementWideChar != null && !replacementWideChar.isEmpty());
  }

  /**
   * マスク時の置換文字列(全角)が設定されているかどうか.
   * @return true=設定されている, false=設定されていない
   */
  public boolean useReplacementWideNum() {
    return (replacementWideNum != null && !replacementWideNum.isEmpty());
  }

  /**
   * ランダム生成文字の文字種が設定されているかどうか.
   * @return true=設定されている, false=設定されていない
   */
  public boolean useRandomGenCharType() {
    return (randomGenCharType != null && randomGenCharType != CharType.UNKNOWN);
  }

  /**
   * ランダム生成しない文字パターン(正規表現)を取得.
   * @return ignoreValue
   */
  public String getRandomNoGenChar() {
    return (randomNoGenCharPattern != null) ? randomNoGenCharPattern.toString() : "";
  }

  /**
   * ランダム生成しない文字パターン(正規表現)をセット.
   * @param randomNoGenChar セットする randomNoGenChar
   */
  public void setRandomNoGenChar(String randomNoGenChar) {
    if (randomNoGenChar != null && !randomNoGenChar.isEmpty()) {
      this.randomNoGenCharPattern = Pattern.compile(randomNoGenChar, Pattern.CASE_INSENSITIVE);
    } else {
      this.randomNoGenCharPattern = null;
    }
  }

  /**
   * 最小値(数値)をセット.
   * ※指定なしは0
   * @param minValue セットする minValue
   */
  public void setMinValue(String minValue) {
    this.minValue = minValue;
    if (minValue != null && !minValue.isEmpty()
        && minValue.matches("-?\\d+(\\.\\d+)?")) {
      this.minDecimalValue = new BigDecimal(minValue);
    } else {
      this.minDecimalValue = null;
    }
  }

  /**
   * 最大値(数値)をセット.
   * ※指定なしは入力値の桁数のMAX
   * @param maxValue セットする maxValue
   */
  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
    if (maxValue != null && !maxValue.isEmpty()
        && maxValue.matches("-?\\d+(\\.\\d+)?")) {
      this.maxDecimalValue = new BigDecimal(maxValue);
    } else {
      this.maxDecimalValue = null;
    }
  }

  /**
   * 最小値(数値)をセット.
   * ※指定なしは0
   * @param minDecimalValue セットする minDecimalValue
   */
  public void setMinDecimalValue(BigDecimal minDecimalValue) {
    this.minDecimalValue = minDecimalValue;
    if (minDecimalValue != null) {
      minValue = this.minDecimalValue.toString();
    }
  }

  /**
   * 最大値(数値)をセット.
   * ※指定なしは入力値の桁数のMAX
   * @param maxDecimalValue セットする maxDecimalValue
   */
  public void setMaxDecimalValue(BigDecimal maxDecimalValue) {
    this.maxDecimalValue = maxDecimalValue;
    if (maxDecimalValue != null) {
      maxValue = this.maxDecimalValue.toString();
    }
  }

  /**
   * データ選択リストの連番カラム名を取得.
   * ※ランダム選択するためには対象テーブルには空き番の無い連番カラム(数値)が必要。
   *  指定が無い場合はデフォルトのseqnoとなる。
   * @return selectListSeqNoColName
   */
  public String getSelectListSeqNoColName() {
    if (selectListSeqNoColName == null || selectListSeqNoColName.isEmpty()) {
      return "seqno";
    } else {
      return selectListSeqNoColName;
    }
  }

}
