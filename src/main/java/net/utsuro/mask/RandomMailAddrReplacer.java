package net.utsuro.mask;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.ThreadLocalRandom;
import net.utsuro.mask.MaskingUtil.CharType;

/**
 * メールアドレスのランダム置換クラス.
 *
 * <table border="1" style="border-collapse: collapse;">
 * <caption>利用可能なマスキングルール</caption>
 * <tr><th>プロパティ</th><th>説明</th></tr>
 * <tr><td>isUniqueValue</td><td>生成した値を一意にするかどうか(NULL以外)</td></tr>
 * <tr><td>isDeterministicReplace</td><td>決定論的置換するかどうか ※INPUTが同じならOUTPUTも同じ値にする(NULL以外)</td></tr>
 * <tr><td>uniqueId</td><td>決定論的/一意制管理の任意の識別子 ※カラム名で無くても良い</td></tr>
 * <tr><td>ignoreValuePattern</td><td>対象外にする値のパターン(正規表現) ※マッチした場合は元の値そのまま返却</td></tr>
 * <tr><td>DomainReplacement</td><td>メールアドレス生成時のドメイン名</td></tr>
 * <tr><td>randomNoGenCharPattern</td><td>ランダム生成しない文字パターン(正規表現) ※記号はOKでもカンマとかクォートはNGとか自動生成パスワードのlとIやOと0は見分けが付きにくいから除外とか</td></tr>
 * </table>
 */
public class RandomMailAddrReplacer implements DataMask {

  private static final int RETRY_MAX = 5;
  private Connection conn;

  /**
   * ランダム生成用のトップ・セカンドレベルドメインリスト.
   */
  static final String[] TOPSEC_DOMAIN = {
      ".co.jp",
      ".com",
      ".net"
  };

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
   * ランダム生成メールアドレスに置換する.
   * ※原則元の値の長さと同じものを生成。ドメイン名を指定した場合、長さが足りなくなったら1文字ローカル名を追加して生成。
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
      // 文字列以外はそのまま返る
      return src;
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
   * ランダム生成メールアドレスに置換する.
   * ※原則元の値の長さと同じものを生成。ドメイン名を指定した場合、長さが足りなくなったら1文字ローカル名を追加して生成。
   * @param src 置換したい文字列
   * @param rule マスク化ルール
   * @return 置換後の文字列
   */
  public static String replace(String src, MaskingRule rule) {

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

    String base = src.trim();
    String[] c = base.split("");
    int len = c.length;

    String domain;
    if (rule.getDomainReplacement().length() > 0) {
      // 指定ドメインへの差し替え
      domain = new StringBuilder()
          .append((rule.getDomainReplacement().indexOf("@") < 0) ? "@" : "")
          .append(rule.getDomainReplacement()).toString();
    } else {
      // トップレベルドメインのランダム選択
      int idx = ThreadLocalRandom.current().nextInt(TOPSEC_DOMAIN.length);
      domain = TOPSEC_DOMAIN[idx];
      // 元のドメイン名の長さでランダム生成
      int domainLen = len - base.indexOf("@") - domain.length() - 1;
      if (domainLen <= 0) {
        // 長さが足りなくなった場合は1文字
        domainLen = 1;
      }
      domain = new StringBuilder("@")
          .append(MaskingUtil.getRandomString(domainLen,
          CharType.LOWER_ALPHA, rule.getRandomNoGenCharPattern()))
          .append(domain).toString();
    }
    len -= domain.length();

    if (len <= 0) {
      // 長さが足りなくなった場合、ローカルドメイン1文字と@で生成
      sb.append(MaskingUtil.getRandomString(1,
          CharType.LOWER_ALPHA, rule.getRandomNoGenCharPattern()));
    } else {
      // 1文字ずつ入れ替え
      for (int i = 0; i < len; i++) {
        // 選択したトップレベルドメイン分以外をランダム生成英字に差し替えする
        sb.append(MaskingUtil.getRandomString(1,
            CharType.LOWER_ALPHA, rule.getRandomNoGenCharPattern()));
      }
    }

    // ドメイン名を付与して返却
    return sb.append(domain).toString();

  }

}
