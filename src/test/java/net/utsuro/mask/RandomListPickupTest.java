package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RandomListPickupTest extends RandomListPickup {

  @Nested
  @DisplayName("method: useDatabase")
  class UseDatabase {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("DBは使用しない")
    void case1() throws Exception {
      assertFalse(useDatabase(rule));
    }

  }

  @Nested
  @DisplayName("method: getConnection")
  class GetConnection {

    @Test
    @DisplayName("DBは使用しないのでnullが返る")
    void case1() throws Exception {
      assertEquals(null, getConnection());
    }

  }

  @Nested
  @DisplayName("method: setConnection")
  class SetConnection {

    @Mock
    Connection mockConn;

    @Test
    @DisplayName("DBは使用しないのでセットしてもnullが返る")
    void case1() throws Exception {
      setConnection(mockConn);
      assertEquals(null, getConnection());
    }

  }

  @Nested
  @DisplayName("method: execute")
  class Execute {

    MaskingRule rule = new MaskingRule();

    @Test
    @DisplayName("ルールが無い場合はそのまま返る")
    void case1() throws Exception {
      assertEquals("あいう", execute("あいう", null));
    }

    @Test
    @DisplayName("null置換なしでnullはそのまま返る")
    void case2() throws Exception {
      assertEquals(null, execute(null, rule));
    }

    @Test
    @DisplayName("null置換ありでnullは処理される")
    void case3() throws Exception {
      rule.setNullReplace(true);
      rule.setPickupList(new String[] {"hoge", "fuga"});
      assertNotEquals(null, execute(null, rule));
    }

    @Test
    @DisplayName("picklit無しはそのまま返る")
    void case4() throws Exception {
      assertEquals("あいう", execute("あいう", rule));
    }

    @Test
    @DisplayName("ランダムリスト選択")
    void case10() throws Exception {
      String[] pickList = new String[] {"会長", "社長", "本部長", "部長", "課長", "係長", "社員"};
      int[] pickupWeights = new int[] {1, 1, 4, 10, 40, 80, 400};
      int total = Arrays.stream(pickupWeights).sum();
      rule.setPickupList(pickList);
      rule.setPickupWeights(pickupWeights);
      int count = 3000; //試行回数
      Map<String, Integer> retMap = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String ret = (String) execute("hoge", rule);
        if (!retMap.containsKey(ret)) {
          retMap.put(ret, 0);
        }
        retMap.put(ret, retMap.get(ret) + 1);
      }
      // 重み通りに出現しているかを確認
      for (int i = 0; i < pickList.length; i++) {
        // 1回は出現している
        assertTrue(retMap.containsKey(pickList[i]));
        // 出現予測値
        double exp = count * ((double) pickupWeights[i] / total);
        int min = (int) Math.floor(exp * 0.5);
        int max = (int) Math.ceil(exp * 1.5);
        // 前後50%以内ならOK(前後1回はOK)
        assertTrue(retMap.get(pickList[i]) >= (min - 1)
            && retMap.get(pickList[i]) <= (max + 1),
            String.format("%sが%d～%dのはずが%d回出現しているのでNG",
                pickList[i], min, max, retMap.get(pickList[i])));
      }

    }

    @Test
    @DisplayName("重みリストの指定が無い場合は同率")
    void case11() throws Exception {
      String[] pickList = new String[] {"りんご", "みかん", "バナナ"};
      int[] pickupWeights = new int[] {1, 1, 1};
      int total = Arrays.stream(pickupWeights).sum();
      rule.setPickupList(pickList);
      int count = 3000; //試行回数
      Map<String, Integer> retMap = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String ret = (String) execute("hoge", rule);
        if (!retMap.containsKey(ret)) {
          retMap.put(ret, 0);
        }
        retMap.put(ret, retMap.get(ret) + 1);
      }
      // 重み通りに出現しているかを確認
      for (int i = 0; i < pickList.length; i++) {
        // 1回は出現している
        assertTrue(retMap.containsKey(pickList[i]));
        // 出現予測値
        double exp = count * ((double) pickupWeights[i] / total);
        int min = (int) Math.floor(exp * 0.5);
        int max = (int) Math.ceil(exp * 1.5);
        // 前後50%以内ならOK
        assertTrue(retMap.get(pickList[i]) >= min
            && retMap.get(pickList[i]) <= max,
            String.format("%sが%d～%dのはずが%d回出現しているのでNG",
                pickList[i], min, max, retMap.get(pickList[i])));
      }
    }

    @Test
    @DisplayName("重みリストに不足がある場合は出現しない")
    void case12() throws Exception {
      String[] pickList = new String[] {"りんご", "みかん", "バナナ", "もも"};
      int[] pickupWeights = new int[] {1, 1, 1};
      rule.setPickupList(pickList);
      rule.setPickupWeights(pickupWeights);
      int count = 3000; //試行回数
      Map<String, Integer> retMap = new HashMap<>();
      for (int i = 0; i < count; i++) {
        String ret = (String) execute("hoge", rule);
        if (!retMap.containsKey(ret)) {
          retMap.put(ret, 0);
        }
        retMap.put(ret, retMap.get(ret) + 1);
      }
      // 1回でも出現していたらNG
      assertFalse(retMap.containsKey("もも"));
    }

  }

}
