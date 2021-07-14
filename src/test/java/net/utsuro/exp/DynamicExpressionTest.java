package net.utsuro.exp;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DynamicExpressionTest extends DynamicExpression {

  @Nested
  @DisplayName("enum: Operator")
  class EnumOperator {

    @Test
    @DisplayName("getText()で文字列表現取得")
    void case1() {
      Operator val;
      val = Operator.EQ;
      assertEquals("==", val.getText());
    }

    @Test
    @DisplayName("valueOfText()で文字列表現からEnum取得")
    void case2() {
      assertEquals(null, Operator.valueOfText(null));
      assertEquals(Operator.EQ, Operator.valueOfText("=="));
      assertEquals(Operator.NE, Operator.valueOfText("!="));
      assertEquals(Operator.GT, Operator.valueOfText(">"));
      assertEquals(Operator.GE, Operator.valueOfText(">="));
      assertEquals(Operator.LT, Operator.valueOfText("<"));
      assertEquals(Operator.LE, Operator.valueOfText("<="));
      assertEquals(Operator.AND, Operator.valueOfText("&&"));
      assertEquals(Operator.OR, Operator.valueOfText("||"));
    }

  }

  @Nested
  @DisplayName("method: parse")
  class Parse {

    DynamicExpression tree = new DynamicExpression();

    @Test
    @DisplayName("式がnullの場合はrootはクリアされる")
    void case1() {
      ExpNode en = new ExpNode();
      en.setData("hoge");
      tree.setRoot(en);
      assertEquals("hoge", tree.getRoot().getData());
      tree.parse(null);
      assertEquals(null, tree.getRoot().getData());
    }

    @Test
    @DisplayName("式がtrimして空文字の場合はrootはクリアされる")
    void case2() {
      ExpNode en = new ExpNode();
      en.setData("hoge");
      tree.setRoot(en);
      assertEquals("hoge", tree.getRoot().getData());
      tree.parse(" ");
      assertEquals(null, tree.getRoot().getData());
    }

    @Test
    @DisplayName("式の=は==に、<>は!=に変換される")
    void case3() {
      tree.parse("1=2");
      assertEquals(Operator.EQ, tree.getRoot().getOperator());
      tree.parse("1<>2");
      assertEquals(Operator.NE, tree.getRoot().getOperator());
    }

    @Test
    @DisplayName("式の'hoge'は文字列に変換される")
    void case4() {
      tree.parse("'hoge'=2");
      assertEquals(String.class, tree.getRoot().getDataA().getData().getClass());
      assertEquals("hoge", tree.getRoot().getDataA().getData());
    }

    @Test
    @DisplayName("式の'ho''ge'は文字列に変換される")
    void case5() {
      tree.parse("'ho''ge'=2");
      assertEquals(String.class, tree.getRoot().getDataA().getData().getClass());
      assertEquals("ho'ge", tree.getRoot().getDataA().getData());
    }

    @Test
    @DisplayName("式の半欠けクォーテーションはそのまま保管される")
    void case6() {
      tree.parse("hoge'='2");
      assertEquals("hoge'", tree.getRoot().getDataA().getData());
      assertEquals("'2", tree.getRoot().getDataB().getData());
    }

  }

  @Nested
  @DisplayName("method: execute")
  class Execute {

    DynamicExpression tree = new DynamicExpression();

    @Test
    @DisplayName("parseしないでexecuteはFalse")
    void CasePrevParse() throws Exception {
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("==")
    void caseEq() throws Exception {
      tree.parse("1=1");
      assertTrue(tree.execute());
      tree.parse("'hoge'='hoge'");
      assertTrue(tree.execute());
      tree.parse("null=null");
      assertTrue(tree.execute());
      tree.parse("1=2");
      assertFalse(tree.execute());
      tree.parse("'hoge'='hogeX'");
      assertFalse(tree.execute());
      tree.parse("1=null");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("!=")
    void caseNe() throws Exception {
      tree.parse("1!=2");
      assertTrue(tree.execute());
      tree.parse("'hoge'!='hogeX'");
      assertTrue(tree.execute());
      tree.parse("1!=null");
      assertTrue(tree.execute());
      tree.parse("1!=1");
      assertFalse(tree.execute());
      tree.parse("'hoge'!='hoge'");
      assertFalse(tree.execute());
      tree.parse("null!=null");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName(">")
    void caseGt() throws Exception {
      tree.parse("0 > 0");
      assertFalse(tree.execute());
      tree.parse("1 > 0");
      assertTrue(tree.execute());
      tree.parse("'Y' > 'Y'");
      assertFalse(tree.execute());
      tree.parse("'Z' > 'Y'");
      assertTrue(tree.execute());
      tree.parse("null > null");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName(">=")
    void caseGe() throws Exception {
      tree.parse("99 >= 100");
      assertFalse(tree.execute());
      tree.parse("100 >= 100");
      assertTrue(tree.execute());
      tree.parse("101 >= 100");
      assertTrue(tree.execute());
      tree.parse("'X' >= 'Y'");
      assertFalse(tree.execute());
      tree.parse("'Y' >= 'Y'");
      assertTrue(tree.execute());
      tree.parse("'Z' >= 'Y'");
      assertTrue(tree.execute());
      tree.parse("null >= null");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("<")
    void caseLt() throws Exception {
      tree.parse("0 < 0");
      assertFalse(tree.execute());
      tree.parse("0 < 1");
      assertTrue(tree.execute());
      tree.parse("'Y' < 'Y'");
      assertFalse(tree.execute());
      tree.parse("'Y' < 'Z'");
      assertTrue(tree.execute());
      tree.parse("null < null");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("<=")
    void caseLe() throws Exception {
      tree.parse("99 <= 100");
      assertTrue(tree.execute());
      tree.parse("100 <= 100");
      assertTrue(tree.execute());
      tree.parse("101 <= 100");
      assertFalse(tree.execute());
      tree.parse("-1 <= 0");
      assertTrue(tree.execute());
      tree.parse("-1 <= -1");
      assertTrue(tree.execute());
      tree.parse("-1 <= -2");
      assertFalse(tree.execute());
      tree.parse("0.9 <= 1");
      assertTrue(tree.execute());
      tree.parse("1.0 <= 1.0");
      assertTrue(tree.execute());
      tree.parse("1.1 <= 0.9");
      assertFalse(tree.execute());
      tree.parse("'X' <= 'Y'");
      assertTrue(tree.execute());
      tree.parse("'Y' <= 'Y'");
      assertTrue(tree.execute());
      tree.parse("'Z' <= 'Y'");
      assertFalse(tree.execute());
      tree.parse("null <= null");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("&&")
    void caseAnd() throws Exception {
      tree.parse("1 == 1 && 2 == 2");
      assertTrue(tree.execute());
      tree.parse("1 == 1 && 2 == 1");
      assertFalse(tree.execute());
      tree.parse("0 == 1 && 2 == 1");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("||")
    void caseOr() throws Exception {
      tree.parse("1 == 1 || 2 == 2");
      assertTrue(tree.execute());
      tree.parse("1 == 1 || 2 == 1");
      assertTrue(tree.execute());
      tree.parse("0 == 1 || 2 == 1");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("カッコ")
    void caseAndOr() throws Exception {
      tree.parse("1 == 1 && (1 == 1 || 2 == 1)");
      assertTrue(tree.execute());
      tree.parse("1 == 1 && (1 == 1 || 2 == 1) && (1 == 1 || 2 == 1)");
      assertTrue(tree.execute());
      tree.parse("1 == 1 && (1 == 'X' || 2 == 1)");
      assertFalse(tree.execute());
      tree.parse("1 == 1 && (1 == 'X' || 2 == 1) && (1 == 1 || 2 == 1)");
      assertFalse(tree.execute());
      tree.parse("1 == 1 && (1 == 0 || (2 == 2 && 1 == 1))");
      assertTrue(tree.execute());
      tree.parse("1 == 1 && (1 == 0 || (2 == 1 && 1 == 1))");
      assertFalse(tree.execute());
    }

    @Test
    @DisplayName("一致(パラメータ使用)")
    void caseParam1() throws Exception {
      Map<String, Object> params = new HashMap<>();
      params.put("a", 1);
      params.put("b", 1);
      params.put("c", 1);
      params.put("d", 2);
      params.put("e", "hoge");
      params.put("f", -1);
      params.put("g", 2);
      params.put("h", 9);
      tree.parse("%a == %b");
      assertTrue(tree.execute(params));
      tree.parse("%c != %d");
      assertTrue(tree.execute(params));
      tree.parse("%e == 'hoge'");
      assertTrue(tree.execute(params));
      tree.parse("%f <= 0");
      assertTrue(tree.execute(params));
      tree.parse("%g >= 1");
      assertTrue(tree.execute(params));
      tree.parse("%h < 10");
      assertTrue(tree.execute(params));
      tree.parse("%a == %b && (%c != %d || %e == 'hoge') && %f <= 0 && %g >= 1 && %h < 10");
      assertTrue(tree.execute(params));
    }

    @Test
    @DisplayName("不一致(パラメータ使用)")
    void caseParam2() throws Exception {
      Map<String, Object> params = new HashMap<>();
      params.put("a", 1L);
      params.put("b", 1L);
      params.put("c", 1L);
      params.put("d", 1L);
      params.put("e", 1L);
      params.put("f", 1L);
      params.put("g", 1L);
      params.put("h", 1L);
      tree.parse("%a == %b && (%c != %d || %e == 'hoge') && %f <= 0 && %g >= 1 && %h < 10");
      assertFalse(tree.execute(params));
    }
  }

  @Nested
  @DisplayName("inner class: ExpParameter")
  class InnerClassExpParameter {

    @Test
    @DisplayName("method: getName")
    void case1() {
      ExpParameter ep = new ExpParameter("hoge");
      assertEquals("hoge", ep.getName());
    }

  }

  @Nested
  @DisplayName("inner class: ExpNode")
  class InnerClassExpNode {

    ExpNode en = new ExpNode();

    @Test
    @DisplayName("method: appendNode")
    void caseAppendNode() {
      // 1回目のappendNodeはdataAにセット
      ExpNode childA = en.appendNode();
      assertEquals(childA, en.getDataA());
      assertEquals(null, en.getDataB());
      // 2回目のappendNodeはdataBにセット
      ExpNode childB = en.appendNode();
      assertEquals(childA, en.getDataA());
      assertEquals(childB, en.getDataB());
      // 3回目のappendNodeはエラー
      try {
        en.appendNode();
        fail("3回目のappendNode が NGにならなかった");
      } catch (IllegalStateException e) {
        assertEquals("A [演算子] B の形式になっていません。", e.getMessage());
      }
    }

    @Test
    @DisplayName("method: appendVar")
    void caseAppendVar() {
      // 1回目のappendVarはdataAにセット
      en.appendVar("a");
      assertEquals("a", en.getDataA().getData());
      assertEquals(null, en.getDataB());
      // 2回目のappendVarはdataBにセット
      en.appendVar(0xB);
      assertEquals("a", en.getDataA().getData());
      assertEquals(BigDecimal.valueOf(0xB), en.getDataB().getData());
      // 3回目のappendVarはエラー
      try {
        en.appendVar("c");
        fail("3回目のappendVar が NGにならなかった");
      } catch (IllegalStateException e) {
        assertEquals("A [演算子] B の形式になっていません。", e.getMessage());
      }
    }

    @Nested
    @DisplayName("method: execute")
    class Execute {

      @Test
      @DisplayName("パラメータ無しはdataが返る")
      void caseExecuteData1() {
        en.setData(123456789L);
        assertEquals(123456789L, en.execute(null));
      }

      @Test
      @DisplayName("パラメータ無しはdataが返る")
      void caseExecuteData2() {
        en.setData("あいうえお");
        assertEquals("あいうえお", en.execute(null));
      }

      @Test
      @DisplayName("パラメータ無しはdataが返る")
      void caseExecuteData3() {
        ExpParameter ep = new ExpParameter("あいうえお");
        en.setData(ep);
        assertEquals(ep, en.execute(null));
      }

      @Test
      @DisplayName("パラメータありはパラメータから値を取得")
      void caseExecuteData4() {
        ExpParameter epInt = new ExpParameter("intParam");
        Map<String, Object> params = new HashMap<>();
        params.put("intParam", 123);
        params.put("strParam", "あいうえお");
        params.put("objParam", new StringBuilder("hoge"));
        en.setData(epInt);
        assertEquals(BigDecimal.valueOf(123), en.execute(params));

        ExpParameter epStr = new ExpParameter("strParam");
        en.setData(epStr);
        assertEquals("あいうえお", en.execute(params));

        ExpParameter epObj = new ExpParameter("objParam");
        en.setData(epObj);
        assertEquals("hoge", en.execute(params));
      }

      @Test
      @DisplayName("同値でEQはTrue")
      void caseExecuteEQ1() {
        en.appendVar(123456789L);
        en.appendVar(123456789L);
        en.setOperator(Operator.EQ);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("異なる値でEQはFalse")
      void caseExecuteEQ2() {
        en.appendVar("100");
        en.appendVar("99");
        en.setOperator(Operator.EQ);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("片方nullでEQはFalse")
      void caseExecuteEQ3() {
        en.appendVar(null);
        en.appendVar(123456789L);
        en.setOperator(Operator.EQ);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("片方nullでEQはFalse")
      void caseExecuteEQ4() {
        en.appendVar("100");
        en.appendVar(null);
        en.setOperator(Operator.EQ);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("同値でNEはFalse")
      void caseExecuteNE1() {
        en.appendVar(123456789L);
        en.appendVar(123456789L);
        en.setOperator(Operator.NE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("異なる値でNEはTrue")
      void caseExecuteNE2() {
        en.appendVar("100");
        en.appendVar("99");
        en.setOperator(Operator.NE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("片方nullでNEはTrue")
      void caseExecuteNE3() {
        en.appendVar(null);
        en.appendVar(123456789L);
        en.setOperator(Operator.NE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("片方nullでEQはTrue")
      void caseExecuteNE4() {
        en.appendVar("100");
        en.appendVar(null);
        en.setOperator(Operator.NE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("true:trueでANDはTrue")
      void caseExecuteAND1() {
        en.appendVar(true);
        en.appendVar(true);
        en.setOperator(Operator.AND);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("true:falseでANDはFalse")
      void caseExecuteAND2() {
        en.appendVar(true);
        en.appendVar(false);
        en.setOperator(Operator.AND);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("null:trueでANDはFalse")
      void caseExecuteAND3() {
        en.appendVar(null);
        en.appendVar(true);
        en.setOperator(Operator.AND);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("true:nullでANDはFalse")
      void caseExecuteAND4() {
        en.appendVar(true);
        en.appendVar(null);
        en.setOperator(Operator.AND);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("true:trueでORはTrue")
      void caseExecuteOR1() {
        en.appendVar(true);
        en.appendVar(true);
        en.setOperator(Operator.OR);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("true:falseでORはTrue")
      void caseExecuteOR2() {
        en.appendVar(true);
        en.appendVar(false);
        en.setOperator(Operator.OR);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("false:falseでORはFalse")
      void caseExecuteOR3() {
        en.appendVar(false);
        en.appendVar(false);
        en.setOperator(Operator.OR);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("null:trueでORはFalse")
      void caseExecuteOR4() {
        en.appendVar(null);
        en.appendVar(true);
        en.setOperator(Operator.OR);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("true:nullでORはFalse")
      void caseExecuteOR5() {
        en.appendVar(true);
        en.appendVar(null);
        en.setOperator(Operator.OR);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1>0でGTはTrue")
      void caseExecuteGT1() {
        en.appendVar(1);
        en.appendVar(0);
        en.setOperator(Operator.GT);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0>0でGTはFalse")
      void caseExecuteGT2() {
        en.appendVar(0);
        en.appendVar(0);
        en.setOperator(Operator.GT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0>1でGTはFalse")
      void caseExecuteGT3() {
        en.appendVar(0);
        en.appendVar(1);
        en.setOperator(Operator.GT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("null>1でGTはFalse")
      void caseExecuteGT4() {
        en.appendVar(null);
        en.appendVar(1);
        en.setOperator(Operator.GT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1>nullでGTはFalse")
      void caseExecuteGT5() {
        en.appendVar(1);
        en.appendVar(null);
        en.setOperator(Operator.GT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1>=0でGEはTrue")
      void caseExecuteGE1() {
        en.appendVar(1);
        en.appendVar(0);
        en.setOperator(Operator.GE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0>=0でGEはTrue")
      void caseExecuteGE2() {
        en.appendVar(0);
        en.appendVar(0);
        en.setOperator(Operator.GE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0>=1でGEはFalse")
      void caseExecuteGE3() {
        en.appendVar(0);
        en.appendVar(1);
        en.setOperator(Operator.GE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("null>=1でGEはFalse")
      void caseExecuteGE4() {
        en.appendVar(null);
        en.appendVar(1);
        en.setOperator(Operator.GE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1>=nullでGEはFalse")
      void caseExecuteGE5() {
        en.appendVar(1);
        en.appendVar(null);
        en.setOperator(Operator.GE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1<0でLTはFalse")
      void caseExecuteLT1() {
        en.appendVar(1);
        en.appendVar(0);
        en.setOperator(Operator.LT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0<0でLTはFalse")
      void caseExecuteLT2() {
        en.appendVar(0);
        en.appendVar(0);
        en.setOperator(Operator.LT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0<1でLTはTrue")
      void caseExecuteLT3() {
        en.appendVar(0);
        en.appendVar(1);
        en.setOperator(Operator.LT);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("null<1でLTはFalse")
      void caseExecuteLT4() {
        en.appendVar(null);
        en.appendVar(1);
        en.setOperator(Operator.LT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1<nullでLTはFalse")
      void caseExecuteLT5() {
        en.appendVar(1);
        en.appendVar(null);
        en.setOperator(Operator.LT);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1<=0でLEはFalse")
      void caseExecuteLE1() {
        en.appendVar(1);
        en.appendVar(0);
        en.setOperator(Operator.LE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0<=0でLEはTrue")
      void caseExecuteLE2() {
        en.appendVar(0);
        en.appendVar(0);
        en.setOperator(Operator.LE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("0<=1でLEはTrue")
      void caseExecuteLE3() {
        en.appendVar(0);
        en.appendVar(1);
        en.setOperator(Operator.LE);
        assertTrue((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("null<=1でLEはFalse")
      void caseExecuteLE4() {
        en.appendVar(null);
        en.appendVar(1);
        en.setOperator(Operator.LE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("1<=nullでLEはFalse")
      void caseExecuteLE5() {
        en.appendVar(1);
        en.appendVar(null);
        en.setOperator(Operator.LE);
        assertFalse((Boolean) en.execute(null));
      }

      @Test
      @DisplayName("比較演算子がnullは値がそのまま返る")
      void caseExecuteX() {
        en.appendVar("s");
        assertEquals("s", en.execute(null));
      }

    }

  }

}
