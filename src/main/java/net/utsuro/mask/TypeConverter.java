package net.utsuro.mask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 型変換するクラス.
 */
public class TypeConverter implements DataMask {

  private static final DateTimeFormatter NUMYEAR_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy");
  private static final DateTimeFormatter NUMYEARMONTH_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMM");
  private static final DateTimeFormatter NUMDATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter NUMDATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final DateTimeFormatter NUMTIME_FORMATTER =
      DateTimeFormatter.ofPattern("HHmmss");
  private static final DateTimeFormatter JISDATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter JPDATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy/MM/dd");
  private static final DateTimeFormatter JPDATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
  private static final DateTimeFormatter JPTIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final Pattern YEARMONTH_PATTERN = Pattern.compile(
      "^([0-9]{4})[-/\\.年]([0-9]{1,2})([-/\\.月])*$");
  private static final Pattern DATE_PATTERN = Pattern.compile(
      "^([0-9]{4})[-/\\.年]([0-9]{1,2})[-/\\.月]([0-9]{1,2})(日)*$");
  private static final Pattern DATETIME_PATTERN = Pattern.compile(
      "^([0-9]{4})[-/\\.年]([0-9]{1,2})[-/\\.月]([0-9]{1,2})[日]* ([0-9]{1,2})[:時]([0-9]{1,2})([:分]([0-9]{1,2})[秒]*)*$");
  private static final Pattern TIME_PATTERN = Pattern.compile(
      "^([0-9]{1,2})[:時]([0-9]{1,2})([:分]([0-9]{1,2})[秒]*)*$");

  private static Map<Class<?>, Class<?>> primitiveClassMap = new HashMap<>();

  static {
    primitiveClassMap.put(Byte.class, byte.class);
    primitiveClassMap.put(Short.class, short.class);
    primitiveClassMap.put(Integer.class, int.class);
    primitiveClassMap.put(Long.class, long.class);
    primitiveClassMap.put(Float.class, float.class);
    primitiveClassMap.put(Double.class, double.class);
    primitiveClassMap.put(Double.class, double.class);
    primitiveClassMap.put(Boolean.class, boolean.class);
  }

  /**
   * 型変換する.
   * @param src 入力値
   * @param rule マスク化ルール
   * @return 変換後の値
   * @throws Exception エラー発生時
   */
  @Override
  public Object execute(Object src, MaskingRule rule) throws Exception {

    return convert(src, rule);

  }

  /**
   * 型変換する.
   * @param src 入力値
   * @param rule マスク化ルール
   * @return 変換後の値
   * @throws Exception エラー発生時
   */
  public static Object convert(Object src, MaskingRule rule) throws Exception {

    if (rule == null || src == null) {
      // ルールが無い場合、引き渡された文字列がnullの場合はそのまま返却
      return src;
    }

    if (rule.getToClassName() == null || rule.getToClassName().isEmpty()) {
      // 変換後のクラス名が指定されていない場合はエラー
      throw new IllegalArgumentException("型変換先が指定されていません。");
    }

    Object obj = null;
    Class<?> clazz;

    try {
      clazz = Class.forName(rule.getToClassName());
    } catch (IllegalArgumentException | SecurityException | ClassNotFoundException e) {
      throw new IllegalArgumentException(
          String.format("型変換先の指定 %s に問題があります。", rule.getToClassName()), e);
    }

    if (clazz == String.class && clazz == src.getClass()) {
      // 文字列同士の変換の場合は文字種変換
      String s = (String) src;
      if (rule.useUpperCaseKana()) {
        // カナ小文字とかな小文字を大文字にする
        s = MaskingUtil.toUpperHalfKana(s);
        s = MaskingUtil.toUpperWideKana(s);
        s = MaskingUtil.toUpperHiragana(s);
      }
      if (rule.useHalfKana()) {
        // かなをカナにしてから半角カナにする
        s = MaskingUtil.hiraganaToWideKana(s);
        s = MaskingUtil.wideKanaToHalfKana(s);
      }
      if (rule.useWideKana()) {
        // 半角カナは全角に、ひらがなはカナにする
        s = MaskingUtil.halfKanaToWideKana(s);
        s = MaskingUtil.hiraganaToWideKana(s);
      }
      if (rule.useHiragana()) {
        // 半角カナを全角にしてからひらがなにする
        s = MaskingUtil.halfKanaToWideKana(s);
        s = MaskingUtil.wideKanaToHiragana(s);
      }
      if (rule.useUpperCase()) {
        // 英小文字を大文字にする
        s = s.toUpperCase();
      }
      if (rule.useLowerCase()) {
        // 英大文字を小文字にする
        s = s.toLowerCase();
      }
      return s;
    }

    if (clazz == src.getClass()) {
      // 変換前と同じ型の場合はそのまま返却
      return src;
    }

    if (isDateTimeClass(clazz)) {
      // 日付への変換
      return toDateTime(src, clazz, rule);
    }

    if (isDateTimeClass(src.getClass())) {
      // 日付からの変換
      return fromDateTime(src, clazz, rule);
    }

    if (clazz == java.lang.String.class) {
      // 変換先が文字列の場合はtoString()して返す
      return src.toString();
    }

    // 変換先のコピーコンストラクタを探す
    Constructor<?>[] constructors = clazz.getConstructors();
    for (int i = 0; i < constructors.length; i++) {
      if (constructors[i].getParameterCount() == 1
          && constructors[i].getParameterTypes()[0] == src.getClass()) {
        // 引数が1つで入力値の型を受け入れる場合はそれを使用してインスタンス生成
        obj = constructors[i].newInstance(src);
        break;
      }
    }
    if (obj == null) {
      // valueOfメソッドの有無を確認し、あればそれを使用してインスタンス生成
      try {
        Method method = clazz.getMethod("valueOf", src.getClass());
        obj = method.invoke(null, src);
      } catch (NoSuchMethodException e) {
        // メソッドが無い場合のエラーは無視
      }
      if (obj == null && primitiveClassMap.containsKey(src.getClass())) {
        // プリミティブ型の互換があるかもしれないのでそれも探す
        try {
          Method method = clazz.getMethod("valueOf", primitiveClassMap.get(src.getClass()));
          obj = method.invoke(null, src);
        } catch (NoSuchMethodException e) {
          // メソッドが無い場合のエラーは無視
        }
      }
    }

    if (obj == null) {
      // ここまで変換できなかったらエラー
      throw new IllegalArgumentException(
          String.format("型変換 %s → %s は非対応。",
              src.getClass().getSimpleName(), clazz.getSimpleName()));
    }

    return obj;

  }

  /**
   * 日付系クラスかどうかを判定する.
   * @param clazz 判定したいクラス
   * @return true=日付系クラス, false=日付以外のクラス
   */
  public static boolean isDateTimeClass(Class<?> clazz) {
    return (clazz == java.util.Date.class || clazz == java.sql.Date.class
        || clazz == java.sql.Time.class || clazz == java.sql.Timestamp.class
        || clazz == LocalDate.class || clazz == LocalDateTime.class
        || clazz == LocalTime.class);
  }

  /**
   * 時刻系クラスかどうかを判定する.
   * @param clazz 判定したいクラス
   * @return true=時刻系クラス, false=時刻以外のクラス
   */
  public static boolean isTimeClass(Class<?> clazz) {
    return (clazz == java.sql.Time.class || clazz == LocalTime.class);
  }

  /**
   * 数値系クラスかどうかを判定する.
   * @param clazz 判定したいクラス
   * @return true=数値系クラス, false=数値以外のクラス
   */
  public static boolean isNumClass(Class<?> clazz) {
    return (clazz == Long.class || clazz == Integer.class
        || clazz == BigInteger.class || clazz == BigDecimal.class);
  }

  /**
   * 日付系クラスの値に変換する.
   * ※数値→日付はyyyyMMddのみ対応
   * @param src 変換したい値
   * @param clazz 変換後の値のクラス
   * @return 変換後の値
   */
  public static Object toDateTime(Object src, Class<?> clazz) {
    return toDateTime(src, clazz, null);
  }

  /**
   * 日付系クラスの値に変換する.
   * ※数値→日付はyyyyMMddのみ対応
   * @param src 変換したい値
   * @param clazz 変換後の値のクラス
   * @param rule マスク化ルール
   * @return 変換後の値
   */
  public static Object toDateTime(Object src, Class<?> clazz, MaskingRule rule) {

    if (src == null) {
      // NULLはそのまま返却
      return src;
    }

    if (src instanceof String && ((String) src).isEmpty()) {
      // 文字列で空文字はそのまま返却
      return src;
    }

    if (src.getClass().isArray()) {
      // 配列は非対応なのでエラー
      throw new IllegalArgumentException(
          String.format("日付型変換 %s → %s は非対応。",
              src.getClass().getSimpleName(), clazz.getSimpleName()));
    }

    LocalDateTime dt = null;

    if (isNumClass(src.getClass()) || src instanceof String) {
      String s = src.toString();
      DateTimeFormatter df;
      if (rule != null && rule.getDateTimeFormat() != null && !rule.getDateTimeFormat().isEmpty()) {
        // 書式指定ありならそれを使用
        if (rule.getDateTimeFormat().indexOf("GGGG") >= 0) {
          // 和暦ありとして扱う
          df = DateTimeFormatter.ofPattern(rule.getDateTimeFormat())
              .withChronology(JapaneseChronology.INSTANCE);
        } else {
          // 西暦
          df = DateTimeFormatter.ofPattern(rule.getDateTimeFormat());
        }
      } else {
        // 書式指定無しなら自動判定
        if (s.matches("[0-9]{4}") && !isTimeClass(clazz)) {
          // 返却型が時刻でなければ年と判断
          df = NUMYEAR_FORMATTER;
        } else if (s.matches("[0-9]{1,6}") && isTimeClass(clazz)) {
          // 返却型が時刻なら時分秒扱い
          s = String.format("%06d", Integer.parseInt(s));
          df = NUMTIME_FORMATTER;
        } else if (s.matches("[0-9]{6}")) {
          // 返却型が時刻でなければ年月と判断
          df = NUMYEARMONTH_FORMATTER;
        } else if (s.matches("[0-9]{8}")) {
          // 年月日扱い
          df = NUMDATE_FORMATTER;
        } else if (s.matches("[0-9]{14}")) {
          // 年月日時分秒扱い
          df = NUMDATETIME_FORMATTER;
        } else if (DATETIME_PATTERN.matcher(s).find()) {
          // yyyy-MM-dd yyyy/MM/dd yyyy.MM.dd + HH:mm:ss と yyyy年MM月dd日 HH時mm分ss秒 は受け入れる
          Matcher m = DATETIME_PATTERN.matcher(s);
          m.find();
          String yy = m.group(1);
          String mm = m.group(2);
          String dd = m.group(3);
          String hh = m.group(4);
          String nn = m.group(5);
          String ss = m.group(7);
          s = String.format("%04d-%02d-%02d %02d:%02d:%02d", Integer.parseInt(yy),
              Integer.parseInt(mm), Integer.parseInt(dd),
              Integer.parseInt(hh), Integer.parseInt(nn), (ss == null) ? 0 : Integer.parseInt(ss));
          df = JISDATETIME_FORMATTER;
        } else if (DATE_PATTERN.matcher(s).find()) {
          // yyyy-MM-dd yyyy/MM/dd yyyy.MM.dd yyyy年MM月dd日 は受け入れる
          Matcher m = DATE_PATTERN.matcher(s);
          m.find();
          String yy = m.group(1);
          String mm = m.group(2);
          String dd = m.group(3);
          s = String.format("%04d-%02d-%02d 00:00:00", Integer.parseInt(yy),
              Integer.parseInt(mm), Integer.parseInt(dd));
          df = JISDATETIME_FORMATTER;
        } else if (YEARMONTH_PATTERN.matcher(s).find()) {
          // yyyy-MM yyyy/MM yyyy.MM yyyy年MM月 は受け入れる
          Matcher m = YEARMONTH_PATTERN.matcher(s);
          m.find();
          String yy = m.group(1);
          String mm = m.group(2);
          s = String.format("%04d-%02d-01 00:00:00", Integer.parseInt(yy),
              Integer.parseInt(mm));
          df = JISDATETIME_FORMATTER;
        } else if (TIME_PATTERN.matcher(s).find()) {
          // HH:mm:ss と HH時mm分ss秒 は受け入れる
          Matcher m = TIME_PATTERN.matcher(s);
          m.find();
          String hh = m.group(1);
          String nn = m.group(2);
          String ss = m.group(4);
          s = String.format("%02d%02d%02d", Integer.parseInt(hh),
              Integer.parseInt(nn), (ss == null) ? 0 : Integer.parseInt(ss));
          df = NUMTIME_FORMATTER;
        } else {
          // それ以外はエラーになるかもしれないが、ISO書式とする
          df = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }
      }

      // 書式の解析
      ParsePosition pos = new ParsePosition(0);
      TemporalAccessor temp = df.parseUnresolved(s, pos);
      if (temp == null) {
        throw new IllegalArgumentException(String.format("'%s'が現在の日付書式で解析できません。", s));
      }
      // 指定の無い桁を初期値で埋めてLocalDateTimeとして生成
      JapaneseEra ee = (temp.isSupported(ChronoField.ERA))
          ? JapaneseEra.of((int) temp.getLong(ChronoField.ERA)) : null;
      int yy = (temp.isSupported(ChronoField.YEAR)) ? (int) temp.getLong(ChronoField.YEAR) :
          ((temp.isSupported(ChronoField.YEAR_OF_ERA))
              ? (int) temp.getLong(ChronoField.YEAR_OF_ERA) : 0);
      int mm = (temp.isSupported(ChronoField.MONTH_OF_YEAR))
          ? (int) temp.getLong(ChronoField.MONTH_OF_YEAR) : 1;
      int dd = (temp.isSupported(ChronoField.DAY_OF_MONTH))
          ? (int) temp.getLong(ChronoField.DAY_OF_MONTH) : 1;
      int hh = (temp.isSupported(ChronoField.HOUR_OF_DAY))
          ? (int) temp.getLong(ChronoField.HOUR_OF_DAY) : 0;
      int nn = (temp.isSupported(ChronoField.MINUTE_OF_HOUR))
          ? (int) temp.getLong(ChronoField.MINUTE_OF_HOUR) : 0;
      int ss = (temp.isSupported(ChronoField.SECOND_OF_MINUTE))
          ? (int) temp.getLong(ChronoField.SECOND_OF_MINUTE) : 0;
      if (ee != null) {
        // 暦付きは和暦として扱う
        dt = LocalDateTime.of(LocalDate.ofEpochDay(
            JapaneseDate.of(ee, yy, mm, dd).toEpochDay()), LocalTime.of(hh, nn, ss));
      } else {
        dt = LocalDateTime.of(yy, mm, dd, hh, nn, ss);
      }
    } else if (src instanceof java.sql.Date) {
      // SQL日付の場合は一旦toLocalDateする
      dt = ((java.sql.Date) src).toLocalDate().atTime(LocalTime.MIN);
    } else if (src instanceof java.sql.Time) {
      // SQL時刻の場合は一旦toLocalTimeする
      dt = LocalDateTime.of(LocalDate.MIN, ((java.sql.Time) src).toLocalTime());
    } else if (src instanceof java.sql.Timestamp) {
      // SQL日時の場合は一旦toLocalDateTimeする
      dt = ((java.sql.Timestamp) src).toLocalDateTime();
    } else if (src instanceof java.util.Date) {
      // 日付の場合は一旦toLocalDateTimeする
      dt = ((java.util.Date) src).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } else if (src instanceof LocalDate) {
      // 日付の場合は時刻部分をつけてLocalDateTimeにする
      dt = ((LocalDate) src).atTime(LocalTime.MIN);
    } else if (src instanceof LocalTime) {
      // 時刻の場合は日付部分をつけてLocalDateTimeにする
      dt = LocalDateTime.of(LocalDate.MIN, (LocalTime) src);
    } else if (src instanceof LocalDateTime) {
      // そのまま代入
      dt = (LocalDateTime) src;
    }

    // 変換先の型に合わせて返却
    if (dt != null) {
      if (clazz == LocalDateTime.class) {
        return dt;
      }
      if (clazz == LocalDate.class) {
        return dt.toLocalDate();
      }
      if (clazz == LocalTime.class) {
        return dt.toLocalTime();
      }
      if (clazz == java.sql.Date.class) {
        return java.sql.Date.valueOf(dt.toLocalDate());
      }
      if (clazz == java.sql.Time.class) {
        return java.sql.Time.valueOf(dt.toLocalTime());
      }
      if (clazz == java.sql.Timestamp.class) {
        return java.sql.Timestamp.valueOf(dt);
      }
      if (clazz == java.util.Date.class) {
        return java.util.Date.from(ZonedDateTime.of(dt, ZoneId.systemDefault()).toInstant());
      }
    }

    // ここまで変換できなかったらエラー
    throw new IllegalArgumentException(
        String.format("日付型変換 %s → %s は非対応。",
            src.getClass().getSimpleName(), clazz.getSimpleName()));

  }

  /**
   * 日付系クラスの値から変換する.
   * ※日付→数値はyyyyMMddのみ対応、日付→文字列はyyyy/MM/ddまたは+HH:mm:ssのみ対応
   * @param src 変換したい日付系の値
   * @param clazz 変換後の値のクラス
   * @return 変換後の値
   */
  public static Object fromDateTime(Object src, Class<?> clazz) {
    return fromDateTime(src, clazz, null);
  }

  /**
   * 日付系クラスの値から変換する.
   * ※日付→数値はyyyyMMddのみ対応、日付→文字列はyyyy/MM/ddまたは+HH:mm:ssのみ対応
   * @param src 変換したい日付系の値
   * @param clazz 変換後の値のクラス
   * @param rule マスク化ルール
   * @return 変換後の値
   */
  public static Object fromDateTime(Object src, Class<?> clazz, MaskingRule rule) {

    if (src == null) {
      // NULLはそのまま返却
      return src;
    }

    if (src.getClass().isArray()) {
      // 配列は非対応なのでエラー
      throw new IllegalArgumentException(
          String.format("日付型変換 %s → %s は非対応。",
              src.getClass().getSimpleName(), clazz.getSimpleName()));
    }

    LocalDateTime dt;
    boolean isDateTime = false;
    boolean isTime = false;

    // 受け入れ可能な型のみ変換
    if (src instanceof java.sql.Date) {
      // SQL日付の場合は一旦toLocalDateする
      dt = ((java.sql.Date) src).toLocalDate().atTime(LocalTime.MIN);
    } else if (src instanceof java.sql.Time) {
      // SQL時刻の場合は一旦toLocalTimeする
      dt = LocalDateTime.of(LocalDate.MIN, ((java.sql.Time) src).toLocalTime());
      isTime = true;
    } else if (src instanceof java.sql.Timestamp) {
      // SQL日時の場合は一旦toLocalDateTimeする
      dt = ((java.sql.Timestamp) src).toLocalDateTime();
      isDateTime = true;
    } else if (src instanceof java.util.Date) {
      // 日付の場合は一旦toLocalDateTimeする
      dt = ((java.util.Date) src).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    } else if (src instanceof LocalDate) {
      // 日付の場合は時刻部分をつけてLocalDateTimeにする
      dt = ((LocalDate) src).atTime(LocalTime.MIN);
    } else if (src instanceof LocalDateTime) {
      dt = (LocalDateTime) src;
      isDateTime = true;
    } else if (src instanceof LocalTime) {
      dt = LocalDateTime.of(LocalDate.MIN, (LocalTime) src);
      isTime = true;
    } else {
      throw new IllegalArgumentException(
          String.format("日付型変換 %s → %s は非対応。",
              src.getClass().getSimpleName(), clazz.getSimpleName()));
    }

    // 返却書式の選択
    DateTimeFormatter df;
    if (rule != null && rule.getDateTimeFormat() != null && !rule.getDateTimeFormat().isEmpty()) {
      if (rule.getDateTimeFormat().indexOf("GGGG") >= 0) {
        // 和暦ありとして扱う
        df = DateTimeFormatter.ofPattern(rule.getDateTimeFormat())
            .withChronology(JapaneseChronology.INSTANCE);
      } else {
        // 西暦
        df = DateTimeFormatter.ofPattern(rule.getDateTimeFormat());
      }
    } else if (isTime) {
      if (isNumClass(clazz)) {
        df = NUMTIME_FORMATTER;
      } else {
        df = JPTIME_FORMATTER;
      }
    } else if (isDateTime) {
      if (isNumClass(clazz)) {
        if (clazz == Integer.class) {
          // Integerの場合はオーバーフローするので日付にする
          df = NUMDATE_FORMATTER;
        } else {
          df = NUMDATETIME_FORMATTER;
        }
      } else {
        df = JPDATETIME_FORMATTER;
      }
    } else {
      if (isNumClass(clazz)) {
        df = NUMDATE_FORMATTER;
      } else {
        df = JPDATE_FORMATTER;
      }
    }

    // 型と書式にしたがって変換
    if (clazz == Long.class) {
      return Long.parseLong(dt.format(df));
    } else if (clazz == Integer.class) {
      return Integer.parseInt(dt.format(df));
    } else if (clazz == BigInteger.class) {
      return new BigInteger(dt.format(df));
    } else if (clazz == BigDecimal.class) {
      return new BigDecimal(dt.format(df));
    } else if (clazz == String.class) {
      return dt.format(df);
    }

    // ここまで変換できなかったらエラー
    throw new IllegalArgumentException(
        String.format("日付型変換 %s → %s は非対応。",
            src.getClass().getSimpleName(), clazz.getSimpleName()));

  }

}
