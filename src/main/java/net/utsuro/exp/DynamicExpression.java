package net.utsuro.exp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 動的条件式の判定.
 */
public class DynamicExpression {

  /**
   * 比較演算子.
   */
  public static enum Operator {
    EQ("=="),  // ==
    NE("!="),  //
    GT(">"),  // >
    GE(">="),  // >=
    LT("<"),  // <
    LE("<="),  // <=
    AND("&&"), // &&
    OR("||");  // ||

    /**
     * 文字列表現.
     */
    private String text;

    /**
     * コンストラクタ.
     * @param text 文字列表現
     */
    private Operator(String text) {
      this.text = text;
    }

    /**
     * 比較演算子の文字列表現を取得.
     * @return 文字列表現
     */
    public String getText() {
      return text;
    }

    /**
     * 比較演算子の文字列表現からenumを取得.
     * @param text 文字列表現
     * @return 対応するenum値
     */
    public static Operator valueOfText(String text) {
      Operator[] arr = values();
      for (Operator ope : arr) {
        if (ope.getText().equals(text)) {
          return ope;
        }
      }
      return null;
    }
  }

  /**
   * ルートノード.
   */
  @Getter @Setter
  private ExpNode root;

  /**
   * コンストラクタ.
   */
  public DynamicExpression() {
    root = new ExpNode();
  }

  /**
   * 動的条件を解析する.
   * @param exp 動的条件を表す文字列
   */
  public void parse(String exp) {

    this.root = new ExpNode();

    if (exp == null) {
      return;
    }

    String line = exp.trim();

    if (line.isEmpty()) {
      return;
    }

    // トークンの分割パターン
    // → space, ==, !=, (, ), ||, &&, <=, >=, <, >, <>
    Pattern p = Pattern.compile("[ ]+|(?![\\!<>])=[=]*|\\!=|[\\(\\)]|\\|\\||&&|[<>]=|<>|[<>]");
    List<String> tokens = new ArrayList<>();
    Matcher m = p.matcher(line);
    int lastPos = 0;
    while (m.find()) {
      String grp = m.group();
      grp = grp.trim();
      String prev = line.substring(lastPos, m.start()).trim();
      if (!prev.isEmpty()) {
        // マッチしたセパレータより前の文字をトークンに追加
        tokens.add(prev);
      }
      if (!grp.isEmpty()) {
        // 演算子を統一
        if ("=".equals(grp)) {
          grp = "==";
        } else if ("<>".equals(grp)) {
          grp = "!=";
        }
        // トークンに追加
        tokens.add(grp);
      }
      lastPos = m.end();
    }
    if (lastPos < line.length()) {
      tokens.add(line.substring(lastPos));
    }

    // 判定順序を誤らないようにツリーに格納
    ExpNode currentNode = root;
    for (String s : tokens) {
      if ("(".equals(s)) {
        // カッコ
        currentNode = currentNode.appendNode();
      } else if (")".equals(s)) {
        // 閉じカッコ
        currentNode = currentNode.getParent();
      } else if ("&&".equals(s) || "||".equals(s)) {
        // and/or
        currentNode = currentNode.shiftNode(Operator.valueOfText(s));
      } else if (p.matcher(s).find()) {
        // その他の演算子
        currentNode.setOperator(Operator.valueOfText(s));
      } else {
        // 値
        Object obj;
        if (s.startsWith("%")) {
          // 変数
          obj = new ExpParameter(s.substring(1));
        } else if ("'${blank}'".equals(s)) {
          // ブランク判定
          obj = new BlankValue();
        } else if (s.startsWith("'") && s.endsWith("'")) {
          // 文字列
          String buff = s.substring(1, s.length() - 1).replaceAll("''", "'");
          // スペースをセットできないので特殊変数を使う
          obj = buff.replaceAll("\\$\\{sp\\}", " ");
        } else if (s.matches("[-\\.0-9]+")) {
          // 数値
          obj = new BigDecimal(s);
        } else {
          // nullとか
          obj = ("null".equals(s.toLowerCase())) ? null : s;
        }
        currentNode.appendVar(obj);
      }
    }
  }

  /**
   * 動的条件式の判定を実行する.
   * @return 判定結果
   */
  public boolean execute() {
    return execute(null);
  }

  /**
   * 動的条件式の判定を実行する.
   * @param params 埋め込みパラメータ.
   * @return 判定結果
   */
  public boolean execute(Map<String, Object> params) {
    if (root.getOperator() == null) {
      // 未解析時はfalse
      return false;
    }
    return (Boolean) root.execute(params);
  }

  /**
   * 動的条件式のパラメータ格納用内部クラス.
   * ※型判断しやすいようにクラス化しただけでパラメータ名しか保持しない
   */
  @Data
  @AllArgsConstructor
  public static class ExpParameter {

    /**
     * パラメータ名.
     */
    private String name;

  }

  /**
   * ブランク判定を行うため内部クラス.
   */
  public static class BlankValue {

    /**
     * ブランクかどうか判定する.
     * ※null/空文字/全部半角スペース/BlankValueの場合にtrueとなる。
     *  文字列/null/BlankValue以外はfalseとなる。
     * @param tar 比較対象のオブジェクト
     */
    public boolean equals(Object tar) {
      if (tar == null || (tar instanceof BlankValue)) {
        return true;
      }
      if (!(tar instanceof String)) {
        return false;
      }
      // 文字列なら空文字か全スペースは空白扱いする
      return (((String) tar).isEmpty() || ((String) tar).isBlank());
    }

  }

  /**
   * 動的条件式をツリー状に格納するための内部クラス.
   */
  public static class ExpNode {

    /**
     * 単一のデータ.
     */
    @Getter @Setter
    private Object data;

    /**
     * 親ノード.
     */
    @Getter @Setter
    private ExpNode parent;

    /**
     * 子ノードA.
     */
    @Getter @Setter
    private ExpNode dataA;

    /**
     * 子ノードB.
     */
    @Getter @Setter
    private ExpNode dataB;

    /**
     * 比較演算子.
     */
    @Getter @Setter
    private Operator operator;

    /**
     * dataAまたはdataBに値を追加する.
     * @param val 文字列/数値/変数
     */
    public void appendVar(Object val) {
      // AもBもnullで無い場合はNG
      if (dataA != null && dataB != null) {
        throw new IllegalStateException("A [演算子] B の形式になっていません。");
      }
      ExpNode node = new ExpNode();
      node.setParent(this);
      if (!(val instanceof BigDecimal) && val instanceof Number) {
        // 数値の場合はBigDecimalに詰め替え
        val = new BigDecimal(val.toString());
      }
      node.setData(val);
      if (dataA == null) {
        // Aがnullの場合はAにセット
        dataA = node;
      } else {
        // Aがnull以外の場合はBにセット
        dataB = node;
      }
    }

    /**
     * dataAまたはdataBにノードを追加する.
     * @return 追加したノード.
     */
    public ExpNode appendNode() {
      // AもBもnullで無い場合はNG
      if (dataA != null && dataB != null) {
        throw new IllegalStateException("A [演算子] B の形式になっていません。");
      }
      ExpNode node = new ExpNode();
      node.setParent(this);
      if (dataA == null) {
        // Aがnullの場合はAにセット
        dataA = node;
      } else {
        // Aがnull以外の場合はBにセット
        dataB = node;
      }
      return node;
    }

    /**
     * dataAをシフトして子ノードにし、dataBにノードを追加する.
     * @param operator 比較演算子
     * @return 追加したdataAのノード
     */
    public ExpNode shiftNode(Operator operator) {
      // 今のdataA,dataBを子供にする
      ExpNode nodeA = new ExpNode();
      nodeA.setParent(this);
      nodeA.setDataA(this.dataA);
      nodeA.setDataB(this.dataB);
      nodeA.setOperator(this.operator);
      ExpNode nodeB = new ExpNode();
      nodeB.setParent(this);
      this.data = null;
      this.dataA = nodeA;
      this.dataB = nodeB;
      this.operator = operator;
      return nodeB;
    }

    /**
     * 子ノードを保持しているかどうか.
     * @return true=保持している, false=子ノードなし
     */
    public boolean hasChild() {
      return (dataA != null);
    }

    /**
     * 動的条件式の判定を実行する.
     * @param params 埋め込みパラメータ.
     * @return 判定結果
     */
    public Object execute(Map<String, Object> params) {
      if (hasChild()) {
        boolean ret = false;
        Object retA = false;
        Object retB = false;
        if (dataA != null) {
          retA = dataA.execute(params);
        }
        if (dataB != null) {
          retB = dataB.execute(params);
        }
        if (operator == null) {
          return retA;
        }
        switch (operator) {
          case EQ:
            if (retA == null) {
              if (retB instanceof BlankValue) {
                ret = true;
              } else {
                ret = Boolean.valueOf(retA == retB);
              }
            } else {
              if (retB instanceof BlankValue) {
                ret = Boolean.valueOf(retB.equals(retA));
              } else if (retA instanceof BigDecimal && retB instanceof BigDecimal) {
                ret = Boolean.valueOf(((BigDecimal) retA).compareTo((BigDecimal) retB) == 0);
              } else {
                ret = Boolean.valueOf(retA.equals(retB));
              }
            }
            break;
          case NE:
            if (retA == null) {
              if (retB instanceof BlankValue) {
                ret = false;
              } else {
                ret = Boolean.valueOf(retA != retB);
              }
            } else {
              if (retB instanceof BlankValue) {
                ret = Boolean.valueOf(!retB.equals(retA));
              } else if (retA instanceof BigDecimal && retB instanceof BigDecimal) {
                ret = Boolean.valueOf(((BigDecimal) retA).compareTo((BigDecimal) retB) != 0);
              } else {
                ret = Boolean.valueOf(!retA.equals(retB));
              }
            }
            break;
          case AND:
            if (retA == null || retB == null) {
              ret = Boolean.valueOf(false);
            } else {
              ret = Boolean.valueOf((Boolean) retA && (Boolean) retB);
            }
            break;
          case OR:
            if (retA == null || retB == null) {
              ret = Boolean.valueOf(false);
            } else {
              ret = Boolean.valueOf((Boolean) retA || (Boolean) retB);
            }
            break;
          case GT:
            if (retA == null || retB == null) {
              ret = Boolean.valueOf(false);
            } else {
              if (retA instanceof String) {
                ret = Boolean.valueOf(((String) retA).compareTo(retB.toString()) > 0);
              } else {
                ret = Boolean.valueOf(
                    ((BigDecimal) retA).compareTo(new BigDecimal(retB.toString())) > 0);
              }
            }
            break;
          case GE:
            if (retA == null || retB == null) {
              ret = Boolean.valueOf(false);
            } else {
              if (retA instanceof String) {
                ret = Boolean.valueOf(((String) retA).compareTo(retB.toString()) >= 0);
              } else {
                ret = Boolean.valueOf(
                    ((BigDecimal) retA).compareTo(new BigDecimal(retB.toString())) >= 0);
              }
            }
            break;
          case LT:
            if (retA == null || retB == null) {
              ret = Boolean.valueOf(false);
            } else {
              if (retA instanceof String) {
                ret = Boolean.valueOf(((String) retA).compareTo(retB.toString()) < 0);
              } else {
                ret = Boolean.valueOf(
                    ((BigDecimal) retA).compareTo(new BigDecimal(retB.toString())) < 0);
              }
            }
            break;
          case LE:
            if (retA == null || retB == null) {
              ret = Boolean.valueOf(false);
            } else {
              if (retA instanceof String) {
                ret = Boolean.valueOf(((String) retA).compareTo(retB.toString()) <= 0);
              } else {
                ret = Boolean.valueOf(
                    ((BigDecimal) retA).compareTo(new BigDecimal(retB.toString())) <= 0);
              }
            }
            break;
        }
        return ret;
      } else {
        // 子ノードが無い場合は自ノードの値
        if (params != null && data instanceof ExpParameter) {
          // 変数
          Object val = params.get(((ExpParameter) data).getName());
          if (val instanceof Number) {
            // 数値の場合はBigDecimalに詰め替え
            val = new BigDecimal(val.toString());
          } else if (val != null && !(val instanceof String)) {
            // Stringでも数値でもない値は非対応なのでtoString()してみる
            val = val.toString();
          }
          return val;
        } else {
          // 数値か文字列
          return data;
        }
      }
    }

  }

}
