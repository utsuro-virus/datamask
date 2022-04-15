package net.utsuro.mask;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * マスク化処理のユーティリティクラス.
 */
public abstract class MaskingUtil {

  /**
   * 半角英小文字テーブル.
   */
  static final String[] LOWER_ALPHA_CHARACTER = {
      "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
      "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
  };

  /**
   * 半角英大文字テーブル.
   */
  static final String[] UPPER_ALPHA_CHARACTER = {
      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
      "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
  };

  /**
   * 半角数字テーブル.
   */
  static final String[] NUMBER_CHARACTER = {
      "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
  };

  /**
   * 半角記号テーブル.
   */
  static final String[] SPECIAL_CHARACTER = {
      "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".",
      "/", ":", ";", "<", "=", ">", "?", "@", "[", "\\", "]", "^", "_", "`",
      "{", "|", "}", "~"
  };

  /**
   * 全角記号テーブル.
   */
  static final String[] WSPECIAL_CHARACTER = {
      "！", "”", "＃", "＄", "％", "＆", "’", "（", "）", "＊", "＋", "，", "－", "．",
      "／", "：", "；", "＜", "＝", "＞", "？", "＠", "［", "￥", "］", "＾", "＿", "｀",
      "｛", "｜", "｝", "￣"
  };

  /**
   * 半角カナテーブル(変換用).
   */
  static final String[] HKANA_CHARACTER_FULL = {
      "ｱ", "ｲ", "ｳ", "ｴ", "ｵ",
      "ｶ", "ｷ", "ｸ", "ｹ", "ｺ",
      "ｻ", "ｼ", "ｽ", "ｾ", "ｿ",
      "ﾀ", "ﾁ", "ﾂ", "ﾃ", "ﾄ",
      "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ",
      "ﾊ", "ﾋ", "ﾌ", "ﾍ", "ﾎ",
      "ﾏ", "ﾐ", "ﾑ", "ﾒ", "ﾓ",
      "ﾔ", "ﾕ", "ﾖ",
      "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ",
      "ﾜ", "ｦ", "ﾝ",
      "ｶﾞ", "ｷﾞ", "ｸﾞ", "ｹﾞ", "ｺﾞ",
      "ｻﾞ", "ｼﾞ", "ｽﾞ", "ｾﾞ", "ｿﾞ",
      "ﾀﾞ", "ﾁﾞ", "ﾂﾞ", "ﾃﾞ", "ﾄﾞ",
      "ﾊﾞ", "ﾋﾞ", "ﾌﾞ", "ﾍﾞ", "ﾎﾞ",
      "ｳﾞ", "ｰ", "｡", "､", "｢", "｣",
      "ﾊﾟ", "ﾋﾟ", "ﾌﾟ", "ﾍﾟ", "ﾎﾟ",
      "ｧ", "ｨ", "ｩ", "ｪ", "ｫ",
      "ｯ", "ｬ", "ｭ", "ｮ"
  };

  /**
   * 全角カナテーブル(変換用).
   */
  static final String[] WKANA_CHARACTER_FULL = {
      "ア", "イ", "ウ", "エ", "オ",
      "カ", "キ", "ク", "ケ", "コ",
      "サ", "シ", "ス", "セ", "ソ",
      "タ", "チ", "ツ", "テ", "ト",
      "ナ", "ニ", "ヌ", "ネ", "ノ",
      "ハ", "ヒ", "フ", "ヘ", "ホ",
      "マ", "ミ", "ム", "メ", "モ",
      "ヤ", "ユ", "ヨ",
      "ラ", "リ", "ル", "レ", "ロ",
      "ワ", "ヲ", "ン",
      "ガ", "ギ", "グ", "ゲ", "ゴ",
      "ザ", "ジ", "ズ", "ゼ", "ゾ",
      "ダ", "ヂ", "ヅ", "デ", "ド",
      "バ", "ビ", "ブ", "ベ", "ボ",
      "ヴ", "ー", "。", "、", "「", "」",
      "パ", "ピ", "プ", "ペ", "ポ",
      "ァ", "ィ", "ゥ", "ェ", "ォ",
      "ッ", "ャ", "ュ", "ョ"
  };

  /**
   * 半角カナテーブル(生成用/出現率のレート順).
   * ※記号などは生成しないため除外している
   * フリガナ向けに日本郵便の郵便番号データのフリガナ約69万文字から出現率集計
   */
  static final String[] HKANA_CHARACTER = {
      "ｳ", "ﾁ", "ｼ", "ｮ", "ｶ", "ﾏ", "ｲ", "ﾐ", "ﾝ", "ﾀ",
      "ｵ", "ﾅ", "ﾜ", "ｷ", "ｶﾞ", "ｸ", "ﾉ", "ﾗ", "ﾔ", "ｻ",
      "ｼﾞ", "ﾄ", "ﾀﾞ", "ﾆ", "ﾓ", "ﾋ", "ﾘ", "ﾂ", "ﾊ", "ｺ",
      "ｱ", "ｴ", "ﾙ", "ｽ", "ﾊﾞ", "ﾌ", "ﾛ", "ﾄﾞ", "ﾖ", "ｾ",
      "ﾎ", "ｺﾞ", "ｭ", "ｹ", "ｰ", "ｷﾞ", "ｸﾞ", "ﾒ", "ｽﾞ", "ｻﾞ",
      "ﾑ", "ﾃ", "ﾃﾞ", "ﾈ", "ｿ", "ﾋﾞ", "ﾍﾞ", "ﾌﾞ", "ﾎﾞ", "ｯ",
      "ﾂﾞ", "ｹﾞ", "ﾕ", "ﾇ", "ｾﾞ", "ｨ", "ｿﾞ", "ﾚ", "ｬ", "ﾎﾟ",
      "ﾍ", "ﾊﾟ", "ｪ", "ﾌﾟ", "ﾋﾟ", "ﾍﾟ", "ﾁﾞ", "ｳﾞ", "ｦ"
  };

  /**
   * カナ出現率(簡略版).
   * フリガナ向けに日本郵便の郵便番号データのフリガナ約69万文字から出現率集計
   */
  static final int[] HKANA_RATIO = {
      533, 509, 367, 348, 336, 335, 274, 227, 218, 208,
      197, 162, 154, 146, 145, 144, 142, 138, 137, 118,
      117, 112, 98, 93, 91, 88, 88, 87, 85, 80, 74, 63,
      56, 54, 52, 47, 46, 46, 44, 42, 38, 36, 35, 34,
      33, 33, 32, 31, 29, 28, 27, 26, 26, 26, 25, 23,
      22, 20, 20, 19, 19, 13, 12, 11, 9, 8, 8, 8, 7, 5,
      5, 4, 3, 3, 2, 2, 1, 1, 1
  };

  /**
   * かなテーブル(出現率のレート順).
   * @see <a href="http://ameblo.jp/asuka-layout/entry-11421831908.html">飛鳥カナ配列 - カナ出現率データ</a>
   */
  static final String[] HIRAGANA_CHARACTER = {
      "い", "し", "う", "ん", "た", "の", "か", "と", "に", "な",
      "て", "は", "く", "ま", "こ", "き", "っ", "が", "る", "で",
      "お", "も", "あ", "り", "を", "ら", "す", "ょ", "じ", "つ",
      "さ", "ち", "だ", "れ", "そ", "よ", "わ", "け", "え", "せ",
      "み", "ど", "ろ", "ゅ", "ひ", "め", "や", "ば", "ご", "ふ",
      "ゃ", "ね", "ほ", "ぶ", "げ", "む", "ぎ", "び", "ず", "へ",
      "ぼ", "ゆ", "ぐ", "べ", "ざ", "ぜ", "ぱ", "ぞ", "ぷ", "ぴ",
      "づ", "ぽ", "ぬ", "ぃ", "ぺ", "ぇ", "ぁ", "ぉ", "ぅ", "ぢ",
      "ゎ"
  };

  /**
   * かな出現率(簡略版).
   * @see <a href="http://ameblo.jp/asuka-layout/entry-11421831908.html">飛鳥カナ配列 - カナ出現率データ</a>
   */
  static final int[] HIRAGANA_RATIO = {
      621, 467, 460, 440, 413, 394, 363, 324, 273, 267,
      25, 259, 233, 233, 232, 218, 217, 209, 184, 172,
      168, 165, 165, 165, 165, 162, 158, 149, 145, 143,
      139, 137, 136, 135, 113, 102, 96, 96, 92, 90,
      90, 81, 77, 74, 65, 64, 62, 55, 54, 51,
      50, 47, 45, 44, 35, 35, 34, 34, 33, 31,
      27, 21, 21, 20, 17, 17, 16, 15, 11, 10,
      9, 7, 7, 7, 4, 3, 3, 2, 1, 1,
      1
  };

  /**
   * カナ出現率の重み合計値.
   */
  static int halfKanaTotalWeight = 0;

  /**
   * かな出現率の重み合計値.
   */
  static int hiraganaTotalWeight = 0;

  /**
   * 乱数生成用.
   */
  private static final SecureRandom  rnd = new SecureRandom();

  /**
   * カナ大文字変換用.
   */
  private static Map<String, String> lowerHalfKanaMap = new HashMap<>();
  private static Map<String, String> lowerWideKanaMap = new HashMap<>();

  /**
   * ひらがな大文字変換用.
   */
  private static Map<String, String> lowerHiraganaMap = new HashMap<>();

  /**
   * カナ全角半角変換用.
   */
  private static List<String> halfKanaTable = new ArrayList<>();
  private static List<String> wideKanaTable = new ArrayList<>();

  /**
   * 文字種類.
   */
  public enum CharType {
    NONE(-1, "なし", 0),
    UNKNOWN(0, "不明", 2),
    // 単独(半角)
    LOWER_ALPHA(1, "英小文字", 1),
    UPPER_ALPHA(2, "英大文字", 1),
    NUMBER(3, "数字", 1),
    HALF_KANA(4, "半角カタカナ", 1),
    SPECIAL(5, "記号", 1),
    // 単独(全角)
    WIDE_LOWER_ALPHA(10, "全角英小文字", 2),
    WIDE_UPPER_ALPHA(11, "全角英大文字", 2),
    WIDE_NUMBER(12, "全角数字", 2),
    WIDE_KANA(13, "全角カタカナ", 2),
    HIRAGANA(14, "ひらがな", 2),
    KANJI(15, "漢字", 2),
    WIDE_SPECIAL(16, "全角記号", 2),
    // 複合(半角)
    ALPHA(80, "英字", 1),
    ALPHANUM(81, "英数", 1),
    HALF(82, "記号を除く半角文字", 1),
    HALF_WITH_SPECIAL(83, "半角文字", 1),
    // 複合(全角)
    WIDE_ALPHA(90, "全角英字", 2),
    WIDE_ALPHANUM(91, "全角英数", 2),
    WIDE(92, "記号を除く全角文字", 2),
    WIDE_WITH_SPECIAL(93, "全角文字", 2),
    // すべて
    ALL(99, "すべて", 2);

    /**
     * コード値.
     */
    private int code;

    /**
     * 名称.
     */
    private String name;

    /**
     * 必要容量(SJIS換算byte数).
     */
    private int reqByte;

    /**
     * コンストラクタ.
     */
    CharType(int code, String name, int reqByte) {
      this.code = code;
      this.name = name;
      this.reqByte = reqByte;
    }

    /**
     * コード値を取得.
     * @return コード値
     */
    public int getCode() {
      return code;
    }

    /**
     * 名称を取得.
     * @return 名称
     */
    public String getName() {
      return name;
    }

    /**
     * 必要容量(SJIS換算byte数)を取得.
     * @return 必要容量(SJIS換算byte数)
     */
    public int getReqByte() {
      return reqByte;
    }

    /**
     * 文字列から文字種を取得.
     * @param str 任意の文字(概ね先頭1文字で判定)
     * @return 判定した文字種
     */
    public static CharType getTypeByString(String str) {
      CharType ret = UNKNOWN;
      if (str != null && str.length() > 0) {
        char c = str.charAt(0);
        String s = String.valueOf(c);
        Character.UnicodeBlock uniBlock = Character.UnicodeBlock.of(c);
        if (uniBlock == Character.UnicodeBlock.HIRAGANA) {
          ret = HIRAGANA;
        } else if (uniBlock == Character.UnicodeBlock.KATAKANA) {
          ret = WIDE_KANA;
        } else if (uniBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
          ret = KANJI;
        } else if (uniBlock == Character.UnicodeBlock.BASIC_LATIN) {
          // 半角英数とかいろいろ混ざるので細分化
          if (s.matches("[a-z]")) {
            ret = LOWER_ALPHA;
          } else if (s.matches("[A-Z]")) {
            ret = UPPER_ALPHA;
          } else if (s.matches("[0-9]")) {
            ret = NUMBER;
          } else if (s.matches("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~]")) {
            ret = SPECIAL;
          }
        } else if (uniBlock == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
          // 半角カナとか全角英字とかいろいろ混ざるので細分化
          if (s.matches("[ａ-ｚ]")) {
            ret = WIDE_LOWER_ALPHA;
          } else if (s.matches("[Ａ-Ｚ]")) {
            ret = WIDE_UPPER_ALPHA;
          } else if (s.matches("[０-９]")) {
            ret = WIDE_NUMBER;
          } else if (s.matches("[！”＃＄％＆’（）＊＋，－．／：；＜＝＞？＠［￥］＾＿｀｛｜｝￣]")) {
            ret = WIDE_SPECIAL;
          } else if (s.matches("[\\uFF61-\\uFF9F]")) {
            ret = HALF_KANA;
          }
        }
      }
      return ret;
    }

  }

  // 初期化
  static {
    // カナ出現率の重み合計値を求める
    for (int i = 0; i < HKANA_RATIO.length; i++) {
      halfKanaTotalWeight += HKANA_RATIO[i];
    }
    // かな出現率の重み合計値を求める
    for (int i = 0; i < HIRAGANA_RATIO.length; i++) {
      hiraganaTotalWeight += HIRAGANA_RATIO[i];
    }
    // 半角カナ大文字化マッピングテーブル生成
    lowerHalfKanaMap.put("ｧ", "ｱ");
    lowerHalfKanaMap.put("ｨ", "ｲ");
    lowerHalfKanaMap.put("ｩ", "ｳ");
    lowerHalfKanaMap.put("ｪ", "ｴ");
    lowerHalfKanaMap.put("ｫ", "ｵ");
    lowerHalfKanaMap.put("ｯ", "ﾂ");
    lowerHalfKanaMap.put("ｬ", "ﾔ");
    lowerHalfKanaMap.put("ｭ", "ﾕ");
    lowerHalfKanaMap.put("ｮ", "ﾖ");
    // 全角カナ大文字化マッピングテーブル生成
    lowerWideKanaMap.put("ァ", "ア");
    lowerWideKanaMap.put("ィ", "イ");
    lowerWideKanaMap.put("ゥ", "ウ");
    lowerWideKanaMap.put("ェ", "エ");
    lowerWideKanaMap.put("ォ", "オ");
    lowerWideKanaMap.put("ヵ", "カ");
    lowerWideKanaMap.put("ヶ", "ケ");
    lowerWideKanaMap.put("ッ", "ツ");
    lowerWideKanaMap.put("ャ", "ヤ");
    lowerWideKanaMap.put("ュ", "ユ");
    lowerWideKanaMap.put("ョ", "ヨ");
    lowerWideKanaMap.put("ヮ", "ワ");
    // ひらがな大文字化マッピングテーブル生成
    lowerHiraganaMap.put("ぁ", "あ");
    lowerHiraganaMap.put("ぃ", "い");
    lowerHiraganaMap.put("ぅ", "う");
    lowerHiraganaMap.put("ぇ", "え");
    lowerHiraganaMap.put("ぉ", "お");
    lowerHiraganaMap.put("っ", "つ");
    lowerHiraganaMap.put("ゃ", "や");
    lowerHiraganaMap.put("ゅ", "ゆ");
    lowerHiraganaMap.put("ょ", "よ");
    lowerHiraganaMap.put("ゎ", "わ");
    // カナ全角半角変換用
    halfKanaTable = Arrays.asList(HKANA_CHARACTER_FULL);
    wideKanaTable = Arrays.asList(WKANA_CHARACTER_FULL);
  }

  /**
   * 文字種のALL等を分解してセットする.
   * @param types 文字種
   * @return 複合文字種から分解セットしたEnumSet
   */
  static EnumSet<CharType> charTypeNormalize(EnumSet<CharType> types) {
    EnumSet<CharType> ret = types;
    if (types.contains(CharType.ALL)) {
      ret = EnumSet.range(CharType.LOWER_ALPHA, CharType.WIDE_SPECIAL);
      ret.remove(CharType.ALL);
    } else {
      if (types.contains(CharType.HALF)) {
        ret.add(CharType.LOWER_ALPHA);
        ret.add(CharType.UPPER_ALPHA);
        ret.add(CharType.NUMBER);
        ret.add(CharType.HALF_KANA);
        ret.remove(CharType.HALF);
      }
      if (types.contains(CharType.HALF_WITH_SPECIAL)) {
        ret.add(CharType.LOWER_ALPHA);
        ret.add(CharType.UPPER_ALPHA);
        ret.add(CharType.NUMBER);
        ret.add(CharType.SPECIAL);
        ret.add(CharType.HALF_KANA);
        ret.remove(CharType.HALF_WITH_SPECIAL);
      }
      if (types.contains(CharType.WIDE)) {
        ret.add(CharType.WIDE_LOWER_ALPHA);
        ret.add(CharType.WIDE_UPPER_ALPHA);
        ret.add(CharType.WIDE_ALPHANUM);
        ret.add(CharType.WIDE_KANA);
        ret.add(CharType.HIRAGANA);
        ret.add(CharType.KANJI);
        ret.remove(CharType.WIDE);
      }
      if (types.contains(CharType.WIDE_WITH_SPECIAL)) {
        ret.add(CharType.WIDE_LOWER_ALPHA);
        ret.add(CharType.WIDE_UPPER_ALPHA);
        ret.add(CharType.WIDE_SPECIAL);
        ret.add(CharType.WIDE_ALPHANUM);
        ret.add(CharType.WIDE_KANA);
        ret.add(CharType.HIRAGANA);
        ret.add(CharType.KANJI);
        ret.remove(CharType.WIDE_WITH_SPECIAL);
      }
      if (types.contains(CharType.ALPHA)) {
        ret.add(CharType.LOWER_ALPHA);
        ret.add(CharType.UPPER_ALPHA);
        ret.remove(CharType.ALPHA);
      }
      if (types.contains(CharType.ALPHANUM)) {
        ret.add(CharType.LOWER_ALPHA);
        ret.add(CharType.UPPER_ALPHA);
        ret.add(CharType.NUMBER);
        ret.remove(CharType.ALPHANUM);
      }
      if (types.contains(CharType.WIDE_ALPHA)) {
        ret.add(CharType.WIDE_LOWER_ALPHA);
        ret.add(CharType.WIDE_UPPER_ALPHA);
        ret.remove(CharType.WIDE_ALPHA);
      }
      if (types.contains(CharType.WIDE_ALPHANUM)) {
        ret.add(CharType.WIDE_LOWER_ALPHA);
        ret.add(CharType.WIDE_UPPER_ALPHA);
        ret.add(CharType.WIDE_NUMBER);
        ret.remove(CharType.WIDE_ALPHANUM);
      }
    }
    return ret;
  }

  /**
   * 全角文字(SJIS)かどうかを返す.
   * @param s 検査したい文字
   * @return 全角文字が含まれている場合true, 半角のみの場合false
   */
  public static boolean isWideChar(String s) {
    boolean ret = false;
    if (s != null && s.length() > 0) {
      ret = (s.getBytes(Charset.forName("MS932")).length != s.length());
    }
    return ret;
  }

  /**
   * バイト数(SJIS換算)を返す.
   * @param s カウントしたい文字
   * @return SJIS換算バイト数(全角2byte、半角1byte、nullは0byte)
   */
  public static int getSjisByteCount(String s) {
    int ret = 0;
    if (s != null && s.length() > 0) {
      ret = s.getBytes(Charset.forName("MS932")).length;
    }
    return ret;
  }

  /**
   * バイト数(SJIS換算)で文字列を分割する.
   * @param s 分割したい文字
   * @param bytes 分割したいSJIS換算バイト数
   * @return SJIS換算バイト数(全角2byte、半角1byte)で分割した文字列配列
   */
  public static String[] splitBySjisBytes(String s, int bytes) {
    String[] ret = {null, null};
    if (s != null) {
      ret = new String[2];
      int cnt = 0;
      boolean isOverflow = false;
      List<String> charList = Arrays.asList(s.split(""));
      StringBuilder buff = new StringBuilder();
      // 溢れるまで1文字ずつSJIS換算しながらバッファに格納
      for (String c : charList) {
        cnt += getSjisByteCount(c);
        if (cnt <= bytes) {
          buff.append(c);
        } else {
          isOverflow = true;
          break;
        }
      }
      ret[0] = buff.toString();
      // 溢れていたら残りを2枠目にセット
      if (isOverflow) {
        ret[1] = s.substring(buff.length());
      }
    }
    return ret;
  }

  /**
   * バイト数(SJIS換算)で文字列省略する.
   * @param s 省略したい文字
   * @param bytes SJIS換算バイト数
   * @return SJIS換算バイト数(全角2byte、半角1byte)で省略した文字列 ※半欠けにならないよう文字単位でカット
   */
  public static String truncateBySjisBytes(String s, int bytes) {
    String ret = null;
    if (s != null) {
      if (getSjisByteCount(s) <= bytes) {
        // 溢れないときはそのまま返す
        return s;
      }
      int cnt = 0;
      List<String> charList = Arrays.asList(s.split(""));
      StringBuilder buff = new StringBuilder();
      // 溢れるまで1文字ずつSJIS換算しながらバッファに格納
      for (String c : charList) {
        cnt += getSjisByteCount(c);
        if (cnt <= bytes) {
          buff.append(c);
        } else {
          break;
        }
      }
      ret = buff.toString();
    }
    return ret;
  }

  /**
   * バイト数(EBCDIC換算)を返す.
   * @param s カウントしたい文字
   * @return EBCDIC換算バイト数(全角2byte、半角1byte、nullは0byte、シフトコード込み)
   */
  public static int getEbcdicByteCount(String s) {
    int ret = 0;
    if (s != null && s.length() > 0) {
      ret = s.getBytes(Charset.forName("Cp930")).length;
    }
    return ret;
  }

  /**
   * バイト数(EBCDIC換算)で文字列省略する.
   * @param s 省略したい文字
   * @param bytes EBCDIC換算バイト数
   * @return EBCDIC換算バイト数(全角2byte、半角1byte、シフトコード込み)で省略した文字列 ※半欠けにならないよう文字単位でカット
   */
  public static String truncateByEbcdicBytes(String s, int bytes) {
    String ret = null;
    if (s != null) {
      if (getEbcdicByteCount(s) <= bytes) {
        // 溢れないときはそのまま返す
        return s;
      }
      int cnt = 0;
      List<String> charList = Arrays.asList(s.split(""));
      StringBuilder buff = new StringBuilder();
      // 溢れるまで1文字ずつEBCDIC換算しながらバッファに格納
      for (String c : charList) {
        cnt = getEbcdicByteCount(buff.append(c).toString());
        if (cnt > bytes) {
          buff.deleteCharAt(buff.length() - 1);
          break;
        } else if (cnt == bytes) {
          break;
        }
      }
      ret = buff.toString();
    }
    return ret;
  }

  /**
   * 半角カナ小文字を大文字にして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String toUpperHalfKana(String s) {

    StringBuffer sb = new StringBuffer();
    Pattern p = Pattern.compile("[ｧｨｩｪｫｯｬｭｮ]");
    Matcher m = p.matcher(s);
    while (m.find()) {
      m.appendReplacement(sb, lowerHalfKanaMap.get(m.group()));
    }
    m.appendTail(sb);
    return sb.toString();

  }

  /**
   * 全角カナ小文字を大文字にして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String toUpperWideKana(String s) {

    StringBuffer sb = new StringBuffer();
    Pattern p = Pattern.compile("[ァィゥェォヵヶッャュョヮ]");
    Matcher m = p.matcher(s);
    while (m.find()) {
      m.appendReplacement(sb, lowerWideKanaMap.get(m.group()));
    }
    m.appendTail(sb);
    return sb.toString();

  }

  /**
   * ひらがな小文字を大文字にして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String toUpperHiragana(String s) {

    StringBuffer sb = new StringBuffer();
    Pattern p = Pattern.compile("[ぁぃぅぇぉっゃゅょゎ]");
    Matcher m = p.matcher(s);
    while (m.find()) {
      m.appendReplacement(sb, lowerHiraganaMap.get(m.group()));
    }
    m.appendTail(sb);
    return sb.toString();

  }

  /**
   * 全角カナを半角にして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String wideKanaToHalfKana(String s) {

    StringBuffer sb = new StringBuffer();
    String[] c = s.split("");
    int len = c.length;
    for (int i = 0; i < len; i++) {
      int idx = wideKanaTable.indexOf(c[i]);
      if (idx < 0) {
        sb.append(c[i]);
      } else {
        sb.append(halfKanaTable.get(idx));
      }
    }
    return sb.toString();

  }

  /**
   * 半角カナを全角にして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String halfKanaToWideKana(String s) {

    StringBuffer sb = new StringBuffer();
    String[] c = s.split("");
    int len = c.length;
    for (int i = 0; i < len; i++) {
      if (c[i].isEmpty()) {
        continue;
      }
      if (i < c.length - 1 && c[i + 1].matches("[ﾟﾞ]")) {
        // 次の文字が半角カナの濁点・半濁点の場合は2文字セットで変換する
        c[i] = c[i].concat(c[i + 1]);
        c[i + 1] = "";
      }
      int idx = halfKanaTable.indexOf(c[i]);
      if (idx < 0) {
        sb.append(c[i]);
      } else {
        sb.append(wideKanaTable.get(idx));
      }
    }
    return sb.toString();

  }

  /**
   * 半角数字を全角にして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String halfNumberToWideNumber(String s) {

    StringBuffer sb = new StringBuffer();
    String[] c = s.split("");
    int len = c.length;
    for (int i = 0; i < len; i++) {
      if (c[i].matches("[0-9]")) {
        sb.append((char) ((int) c[i].charAt(0) + 0xFEE0));
      } else {
        sb.append(c[i]);
      }
    }
    return sb.toString();

  }

  /**
   * ひらがなを全角カナにして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String hiraganaToWideKana(String s) {

    StringBuffer sb = new StringBuffer();
    String[] c = s.split("");
    int len = c.length;
    for (int i = 0; i < len; i++) {
      if (c[i].matches("[ぁ-ん]")) {
        sb.append((char) ((int) c[i].charAt(0) + 0x0060));
      } else {
        sb.append(c[i]);
      }
    }
    return sb.toString();

  }

  /**
   * 全角カナをひらがなにして返す.
   * @param s 対象の文字
   * @return 置換後の文字
   */
  public static String wideKanaToHiragana(String s) {

    StringBuffer sb = new StringBuffer();
    String[] c = s.split("");
    int len = c.length;
    for (int i = 0; i < len; i++) {
      if (c[i].matches("[ァ-ン]")) {
        sb.append((char) ((int) c[i].charAt(0) - 0x0060));
      } else {
        sb.append(c[i]);
      }
    }
    return sb.toString();

  }

  /**
   * 行頭禁止の文字かどうか(かな小文字とかンとか)、拗音(きゃ)以外かどうかを返す.
   * @param str 文字列
   * @param nextChar 次に来る文字
   * @return true=禁止文字, false=問題なし
   */
  public static boolean isInvalidNextLetter(StringBuilder str, String nextChar) {
    return isInvalidNextLetter(str.toString(), nextChar);
  }

  /**
   * 行頭禁止の文字かどうか(かな小文字とかンとか)、拗音(きゃ)以外かどうかを返す.
   * @param str 文字列
   * @param nextChar 次に来る文字
   * @return true=禁止文字, false=問題なし
   */
  public static boolean isInvalidNextLetter(String str, String nextChar) {
    boolean ret = false;
    String lastChar = (str.isEmpty()) ? "" : str.substring(str.length() - 1);
    if (lastChar.matches("[ﾟﾞ]")) {
      // 最後の1文字が半角カナの濁点・半濁点の場合はもう1文字見る
      if (str.length() >= 2) {
        lastChar = str.substring(str.length() - 2);
      }
    } else if (lastChar.matches("[-、。､｡\r\n\t 　]")) {
      // 最後の1文字が句読点やスペース等の場合は行頭と同じ扱いにする
      lastChar = "";
    }
    // 次の文字が捨て仮名や「ん」の場合、前の文字を確認
    if (nextChar.matches("[ぁぃぅぇぉっゃゅょゎん、。]")) {
      if (lastChar.isEmpty()
          || (lastChar.matches("[ぁぃぅぇぉっ]") && nextChar.matches("[ぁぃぅぇぉ]"))
          || (!lastChar.matches("[きしちにひみりぎじぢびぴ]") && nextChar.matches("[ゃゅょ]"))) {
        // 先頭に来てしまった場合や、ありえない組み合わせになった場合はNG
        ret = true;
      }
    } else if (nextChar.matches("[ァィゥェォッャュョヮンー]")) {
      if (lastChar.isEmpty()
          || (lastChar.matches("[ァィゥェォッｰ]") && nextChar.matches("[ァィゥェォ]"))
          || (!lastChar.matches("[キシチニヒミリギジヂビピ]") && nextChar.matches("[ャュョ]"))) {
        // 先頭に来てしまった場合や、ありえない組み合わせになった場合はNG
        ret = true;
      }
    } else if (nextChar.matches("[ｧｨｩｪｫｯｬｭｮﾝｰﾟﾞ､｡]")) {
      if (lastChar.isEmpty()
          || (lastChar.matches("[ｧｨｩｪｫｯｰ]") && nextChar.matches("[ｧｨｩｪｫ]"))
          || (!lastChar.matches("(ｷﾞ|ｼﾞ|ﾁﾞ|ﾋﾞ|ﾋﾟ|[ｷｼﾁﾆﾋﾐﾘ])") && nextChar.matches("[ｬｭｮ]"))) {
        // 先頭に来てしまった場合や、ありえない組み合わせになった場合はNG
        ret = true;
      }
    }
    return ret;
  }

  /**
   * ランダムな文字列を生成します.
   * ※引き渡す文字種が単独の場合に使用します。
   * @param sjisLen 生成する文字列のSJIS換算byte数
   * @param charType 文字種
   * @return 生成した文字列
   */
  public static String getRandomString(int sjisLen, CharType charType) {

    return getRandomString(sjisLen,  EnumSet.of(charType), null);

  }

  /**
   * ランダムな文字列を生成します.
   * ※引き渡す文字種が単独の場合に使用します。
   * @param sjisLen 生成する文字列のSJIS換算byte数
   * @param charType 文字種
   * @param noGenPattern 生成しない文字パターン(正規表現)
   * @return 生成した文字列
   */
  public static String getRandomString(int sjisLen, CharType charType, Pattern noGenPattern) {

    return getRandomString(sjisLen,  EnumSet.of(charType), noGenPattern);

  }

  /**
   * ランダムな文字列を生成します.
   * ※引き渡す文字種が単独の場合に使用します。
   * @param sjisLen 生成する文字列のSJIS換算byte数
   * @param charType 文字種
   * @return 生成した文字列
   */
  public static String getRandomString(int sjisLen, EnumSet<CharType> charType) {

    return getRandomString(sjisLen, charType, null);

  }

  /**
   * ランダムな文字列を生成します.
   * @param sjisLen 生成する文字列のSJIS換算byte数
   * @param charType 文字種
   * @param noGenPattern 生成しない文字パターン(正規表現)
   * @return 生成した文字列
   */
  public static String getRandomString(
      int sjisLen, EnumSet<CharType> charType, Pattern noGenPattern) {

    List<String> tarCharList = new ArrayList<>();
    boolean useHalfChar = false;
    boolean useSjisGenerator = false;
    int cardinality = 0;
    StringBuilder sb = new StringBuilder();

    if (sjisLen <= 0 || charType == null || charType.isEmpty()) {
      return "";
    }

    // 文字種のALL等を分解してセットする
    EnumSet<CharType> tarType = charTypeNormalize(charType);

    // 対象文字テーブル作成
    if (tarType.contains(CharType.LOWER_ALPHA)) {
      tarCharList.addAll(Arrays.asList(LOWER_ALPHA_CHARACTER));
      useHalfChar = true;
      cardinality += LOWER_ALPHA_CHARACTER.length;
    }
    if (tarType.contains(CharType.UPPER_ALPHA)) {
      tarCharList.addAll(Arrays.asList(UPPER_ALPHA_CHARACTER));
      useHalfChar = true;
      cardinality += UPPER_ALPHA_CHARACTER.length;
    }
    if (tarType.contains(CharType.NUMBER)) {
      tarCharList.addAll(Arrays.asList(NUMBER_CHARACTER));
      useHalfChar = true;
      cardinality += NUMBER_CHARACTER.length;
    }
    if (tarType.contains(CharType.SPECIAL)) {
      tarCharList.addAll(Arrays.asList(SPECIAL_CHARACTER));
      useHalfChar = true;
      cardinality += SPECIAL_CHARACTER.length;
    }
    if (tarType.contains(CharType.WIDE_LOWER_ALPHA)) {
      useSjisGenerator = true;
    }
    if (tarType.contains(CharType.WIDE_UPPER_ALPHA)) {
      useSjisGenerator = true;
    }
    if (tarType.contains(CharType.WIDE_NUMBER)) {
      useSjisGenerator = true;
    }
    if (tarType.contains(CharType.WIDE_SPECIAL)) {
      tarCharList.addAll(Arrays.asList(WSPECIAL_CHARACTER));
      cardinality += WSPECIAL_CHARACTER.length;
    }
    if (tarType.contains(CharType.HIRAGANA)) {
      useSjisGenerator = true;
    }
    if (tarType.contains(CharType.WIDE_KANA)) {
      useSjisGenerator = true;
    }
    if (tarType.contains(CharType.HALF_KANA)) {
      useHalfChar = true;
      useSjisGenerator = true;
    }
    if (tarType.contains(CharType.KANJI)) {
      useSjisGenerator = true;
    }

    if (!useHalfChar && sjisLen < 2) {
      // 全角文字のみなのに2byte未満の指定は組み立てが無駄なのでブランクで返す
      return "";
    }

    int i = 0;

    while (i < sjisLen) {
      String s;
      int plen;
      if (useSjisGenerator) {
        // 出現頻度を調整してSJIS文字生成
        if (EnumSet.of(CharType.KANJI).equals(tarType)
            || tarType.contains(CharType.KANJI) && rnd.nextInt(100) < 30) {
          // 漢字のみ または 漢字込みのとき30%は漢字範囲から生成
          s = getRandomSjisKanji();
        } else if (EnumSet.of(CharType.HIRAGANA).equals(tarType)
            || tarType.contains(CharType.HIRAGANA) && rnd.nextInt(100) < 90) {
          // ひらがなのみ または ひらがな込みのとき90%はひらがなから生成
          s = "";
          do {
            s = getRandomHiragana();
            // 生成されたものが捨て仮名や「ン」の場合、前の文字を確認し、
            // 先頭に来てしまった場合や、ありえない組み合わせになった場合は再抽選
          } while (isInvalidNextLetter(sb, s));
        } else if (EnumSet.of(CharType.WIDE_LOWER_ALPHA).equals(tarType)
            || tarType.contains(CharType.WIDE_LOWER_ALPHA) && rnd.nextInt(100) < 10) {
          // 全角英小文字のみ または 全角英小文字込みのとき10%は全角英子文字から生成
          s = getRandomSjisWideLowerAlpha();
        } else if (EnumSet.of(CharType.WIDE_UPPER_ALPHA).equals(tarType)
            ||            tarType.contains(CharType.WIDE_UPPER_ALPHA) && rnd.nextInt(100) < 10) {
          // 全角英大文字のみ または 全角英大文字込みのとき10%は全角大文字から生成
          s = getRandomSjisWideUpperAlpha();
        } else if (EnumSet.of(CharType.WIDE_NUMBER).equals(tarType)
            || tarType.contains(CharType.WIDE_NUMBER) && rnd.nextInt(100) < 10) {
          // 全角数字のみ または 全角数字込みのとき10%は数字から生成
          s = getRandomSjisWideNumber();
        } else if (EnumSet.of(CharType.WIDE_KANA).equals(tarType)
            || tarType.contains(CharType.WIDE_KANA) && rnd.nextInt(100) < 10) {
          // 全角カタカナのみ または 全角カタカナ込みのとき10%はカタカナから生成
          s = "";
          do {
            s = getRandomSjisWideKana();
            // 生成されたものが捨て仮名や「ン」の場合、前の文字を確認し、
            // 先頭に来てしまった場合や、ありえない組み合わせになった場合は再抽選
          } while (isInvalidNextLetter(sb, s));
        } else if (EnumSet.of(CharType.HALF_KANA).equals(tarType)
            || tarType.contains(CharType.HALF_KANA) && rnd.nextInt(100) < 10) {
          // 半角カタカナのみ または 半角カタカナ込みのとき10%はカタカナから生成
          s = "";
          do {
            s = getRandomHalfKana();
            // 生成されたものが捨て仮名や「ン」の場合、前の文字を確認し、
            // 先頭に来てしまった場合や、ありえない組み合わせになった場合は再抽選
          } while (isInvalidNextLetter(sb, s));
        } else if (cardinality > 0) {
          // 対象文字リストから生成
          s = tarCharList.get(rnd.nextInt(cardinality));
        } else {
          // ここまで決まらなかったら再抽選
          continue;
        }
        plen = s.getBytes(Charset.forName("MS932")).length;
      } else {
        s = tarCharList.get(rnd.nextInt(cardinality));
        plen = s.getBytes(Charset.forName("MS932")).length;
      }
      if (noGenPattern != null && noGenPattern.matcher(s).find()) {
        // 生成しない文字パターンになってしまった場合は再抽選
        continue;
      }
      if (i + plen <= sjisLen) {
        sb.append(s);
        i += plen;
      }
      if (!useHalfChar && (sjisLen - i) == 1) {
        // 全角文字のみなのに残り1byteになってしまった場合は強制終了
        break;
      }
    }

    return sb.toString();

  }

  /**
   * 重み付きランダム抽選を行います.
   * ※重み合計値未計算の場合はこちらを使用。抽選前に計算します。
   * @param weights 抽選対象の重み配列
   * @return 抽選結果のインデックス
   */
  public static int getRandomIndex(int[] weights) {
    return getRandomIndex(weights, -1);
  }

  /**
   * 重み付きランダム抽選を行います.
   * ※計算済の重み合計値を引き渡す場合に使用。重み合計値に-1を渡すと抽選前に計算します。
   * @param weights 抽選対象の重み配列
   * @param totalWeight 重み合計値
   * @return 抽選結果のインデックス
   */
  public static int getRandomIndex(int[] weights, int totalWeight) {
    int total = totalWeight;
    if (total < 0) {
      // 未計算の場合は重み合計値を算出
      total = 0;
      for (int i = 0; i < weights.length; i++) {
        total += weights[i];
      }
    }
    // 抽選
    int val = rnd.nextInt(total) + 1;
    int idx = -1;
    int len = weights.length;
    for (int i = 0; i < len; i++) {
      if (weights[i] >= val) {
        idx = i;
        break;
      }
      val -= weights[i];
    }
    return idx;
  }

  /**
   * ランダムにひらがな1文字を生成.
   *  ・生成される文字列が不自然になりすぎないように、かな出現率の重みによるランダム生成
   * @return String 生成したひらがな
   */
  public static String getRandomHiragana() {

    int idx = -1;
    while (idx < 0) {
      // かな出現率の重み付き抽選でランダムにIndexを取得
      idx = getRandomIndex(HIRAGANA_RATIO, hiraganaTotalWeight);
    }
    return HIRAGANA_CHARACTER[idx];

  }

  /**
   * ランダムに半角カナ1文字を生成.
   *  ・生成される文字列が不自然になりすぎないように、カナ出現率の重みによるランダム生成
   * @return String 生成したひらがな
   */
  public static String getRandomHalfKana() {

    int idx = -1;
    while (idx < 0) {
      // かな出現率の重み付き抽選でランダムにIndexを取得
      idx = getRandomIndex(HKANA_RATIO, halfKanaTotalWeight);
    }
    return HKANA_CHARACTER[idx];

  }

  /**
   * ランダムに漢字1文字を生成.
   *  ・SJIS(MS932)の範囲の漢字を生成する
   *  ・NEC特殊文字 0x8740～0x879C、外字 0xF040～0xF9FC は生成しない
   *  ・NEC選定IBM拡張文字 0xED40～0xEEFC もIBM拡張文字と重複しているため生成しない
   *  ・80%は第一水準から抽選されるように調整して生成
   * @return String 生成した漢字
   */
  public static String getRandomSjisKanji() {

    int[] bytes = new int[2]; //Javaのbyte型は-128～127で比較が面倒なのでintで処理
    boolean isValid = false;

    while (!isValid) {
      int min;
      int max;

      if (rnd.nextInt(100) < 80) {
        // 漢字の80%は第一水準から抽選されるように調整
        min = 0x88;
        max = 0x98;
      } else {
        min = 0x98;
        max = 0xFC;
      }

      // 上位バイト
      bytes[0] = rnd.nextInt(max + 1 - min) + min;
      // 下位バイト
      bytes[1] = rnd.nextInt(0xFC + 1 - 0x40) + 0x40;

      if (bytes[1] == 0x7F
          || (bytes[0] >= 0xA0 && bytes[0] <= 0xDF)
          || (bytes[0] >= 0xEB && bytes[0] <= 0xF9)) {
        // 下位バイトが 0x7F は再抽選
        // 上位バイトが 0xA0～0xDF、0xEB～0xF9 は再抽選
        continue;
      }

      int code = Integer.valueOf(
          String.format("%02x%02x", bytes[0], bytes[1]), 16);

      if ((code >= 0x889F && code <= 0x9872)
          || (code >= 0x989F && code <= 0x9FFC)
          || (code >= 0xE040 && code <= 0xEAA4)
          || (code >= 0xFA5C && code <= 0xFC4B)) {
        // 以下の漢字ならなら抜ける、それ以外は再抽選
        //  ・JIS第一水準 0x889F～0x9872
        //  ・JIS第二水準 0x989F～0x9FFC、0xE040～0xEAA4
        //  ・IBM拡張文字 0xFA40～0xFC4B (但し記号のため0xFA40～0xFA5Bは生成しない)
        isValid = true;
      }

    }

    // SJISバイト配列から文字に変換して返す
    return new String(
        new byte[] {(byte) bytes[0], (byte) bytes[1]},
        Charset.forName("MS932"));

  }

  /**
   * ランダムに全角カナ1文字を生成.
   *  ・SJIS(MS932)の範囲のカナを生成する
   * @return String 生成したカナ
   */
  public static String getRandomSjisWideKana() {

    int[] bytes = new int[2]; //Javaのbyte型は-128～127で比較が面倒なのでintで処理
    boolean isValid = false;

    while (!isValid) {
      // 全角カナの範囲 0x8340～0x8394
      // 上位バイト
      bytes[0] = 0x83;
      // 下位バイト
      bytes[1] = rnd.nextInt(0x94 + 1 - 0x40) + 0x40;

      if (bytes[1] == 0x7F) {
        // 下位バイトが 0x7F は再抽選
        continue;
      }

      isValid = true;

    }

    // SJISバイト配列から文字に変換して返す
    return new String(
        new byte[] {(byte) bytes[0], (byte) bytes[1]},
        Charset.forName("MS932"));

  }

  /**
   * ランダムに全角英大文字1文字を生成.
   *  ・SJIS(MS932)の範囲の全角英大文字を生成する
   * @return String 生成した全角英大文字
   */
  public static String getRandomSjisWideUpperAlpha() {

    int[] bytes = new int[2]; //Javaのbyte型は-128～127で比較が面倒なのでintで処理

    // 全角英大文字の範囲 0x8260～0x8279
    // 上位バイト
    bytes[0] = 0x82;
    // 下位バイト
    bytes[1] = rnd.nextInt(0x79 + 1 - 0x60) + 0x60;

    // SJISバイト配列から文字に変換して返す
    return new String(
        new byte[] {(byte) bytes[0], (byte) bytes[1]},
        Charset.forName("MS932"));

  }

  /**
   * ランダムに全角英小文字1文字を生成.
   *  ・SJIS(MS932)の範囲の全角英小文字を生成する
   * @return String 生成した全角英小文字
   */
  public static String getRandomSjisWideLowerAlpha() {

    int[] bytes = new int[2]; //Javaのbyte型は-128～127で比較が面倒なのでintで処理

    // 全角英小文字の範囲 0x8281～0x829A
    // 上位バイト
    bytes[0] = 0x82;
    // 下位バイト
    bytes[1] = rnd.nextInt(0x9A + 1 - 0x81) + 0x81;

    // SJISバイト配列から文字に変換して返す
    return new String(
        new byte[] {(byte) bytes[0], (byte) bytes[1]},
        Charset.forName("MS932"));

  }

  /**
   * ランダムに全角数字1文字を生成.
   *  ・SJIS(MS932)の範囲の全角数字を生成する
   * @return String 生成した全角数字
   */
  public static String getRandomSjisWideNumber() {

    int[] bytes = new int[2]; //Javaのbyte型は-128～127で比較が面倒なのでintで処理

    // 全角数字の範囲 0x824F～0x8258
    // 上位バイト
    bytes[0] = 0x82;
    // 下位バイト
    bytes[1] = rnd.nextInt(0x58 + 1 - 0x4F) + 0x4F;

    // SJISバイト配列から文字に変換して返す
    return new String(
        new byte[] {(byte) bytes[0], (byte) bytes[1]},
        Charset.forName("MS932"));

  }

  /**
   * ランダムに整数を生成.
   * @param min 生成する最小値
   * @param max 生成する最大値
   * @return 生成した整数
   */
  public static int getRandomNumber(int min, int max) {

    return getRandomNumber(
        BigInteger.valueOf(min), BigInteger.valueOf(max)).intValue();

  }

  /**
   * ランダムに整数を生成.
   * @param min 生成する最小値
   * @param max 生成する最大値
   * @return 生成した整数
   */
  public static long getRandomNumber(long min, long max) {

    return getRandomNumber(
        BigInteger.valueOf(min), BigInteger.valueOf(max)).longValue();

  }

  /**
   * ランダムに整数を生成.
   * @param min 生成する最小値
   * @param max 生成する最大値
   * @return 生成した整数
   */
  public static BigInteger getRandomNumber(BigInteger min, BigInteger max) {

    // 最小値の分をシフトする
    BigInteger upperLimit = max.subtract(min);
    int len = upperLimit.bitLength();

    // ゼロから有効bit数までの乱数生成
    BigInteger bi = new BigInteger(len, new SecureRandom());
    if (bi.compareTo(upperLimit) > 0) {
      // 最大値を超えてしまった場合は調整
      bi = bi.mod(upperLimit);
    }

    // 最小値分シフトして返却
    return bi.add(min);

  }

  /**
   * ランダムに実数を生成.
   * @param min 生成する最小値
   * @param max 生成する最大値
   * @return 生成した実数
   */
  public static BigDecimal getRandomNumber(BigDecimal min, BigDecimal max) {

    // 小数点以下の桁数分シフト
    BigDecimal p = BigDecimal.TEN.pow(Math.max(min.scale(), max.scale()));
    BigInteger biMin = min.multiply(p).toBigInteger();
    BigInteger biMax = max.multiply(p).toBigInteger();

    // 乱数生成
    BigDecimal ret = new BigDecimal(getRandomNumber(biMin, biMax));

    // 小数点以下の桁数分シフトして返却
    return ret.divide(p);

  }

  /**
   * ランダムに日付を生成.
   * @param min 生成する最小値
   * @param max 生成する最大値
   * @return 生成した日付
   */
  public static LocalDateTime getRandomDate(LocalDateTime min, LocalDateTime max) {

    long days = ChronoUnit.DAYS.between(min, max);
    return min.plusDays(ThreadLocalRandom.current().nextLong(days + 1));

  }

  /**
   * クレジットカードのチェックディジットをLuhnアルゴリズムで求める.
   * @param cardNo チェックディジット用の最後の1桁込みのカード番号文字列
   * @return チェックディジット
   */
  public static String getLuhnDigit(String cardNo) {

    int[] numbers = cardNo.chars().map(e -> Character.digit(e, 10)).toArray();
    int len = numbers.length - 1; // 入力値の最後の桁はチェックディジット想定なので除外
    int sum = 0;
    for (int i = 0; i < len; i++) {
      // 偶数番目の桁を2倍にする
      if (i % 2 != 0) {
        numbers[i] *= 2;
      }
      // 各桁の結果が2桁の場合は、十の位と一の位を分けて足す
      if (numbers[i] > 9) {
        numbers[i] = (numbers[i] % 10) + (numbers[i] / 10);
      }
      // 総和を求める
      sum += numbers[i];
    }
    // 合計を10で割り、余りを求め、余りを10から引いたものがチェックディジット
    int checkDigit = (10 - (sum % 10)) % 10;
    return Integer.toString(checkDigit);

  }

}
