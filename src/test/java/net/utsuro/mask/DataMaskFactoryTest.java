package net.utsuro.mask;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataMaskFactoryTest extends DataMaskFactory {

  @Nested
  @DisplayName("method: newInstance")
  class NewInstance {

    @Test
    @DisplayName("指定したクラス名のインスタンスを生成")
    void case1() throws Exception {
      assertEquals(NullToValue.class, newInstance("NullToValue").getClass());
      assertEquals(MaskedTextReplacer.class, newInstance("MaskedTextReplacer").getClass());
    }

    @Test
    @DisplayName("指定したクラス名が不正な場合はエラー")
    void case2() throws Exception {
      try {
        assertEquals(NullToValue.class, newInstance("hoge").getClass());
        fail("不正なクラス が NGにならなかった");
      } catch (IllegalArgumentException e) {
        assertEquals("指定されたクラス hoge のインスタンス生成に失敗しました。", e.getMessage());
      }
    }

  }

  @Nested
  @DisplayName("method: initUniqueList")
  class InitUniqueList {

    @Mock
    Connection mockConn;
    @Mock
    PreparedStatement mockPreparedStmnt;
    @Mock
    ResultSet mockResultSet;

    @Test
    @DisplayName("コネクションがnullの場合は何もしない")
    void case1() throws Exception {
      initUniqueList(null);
      verify(mockConn, times(0)).prepareStatement(anyString());
    }

    @Test
    @DisplayName("コネクションがcloseの場合は何もしない")
    void case2() throws Exception {
      // モックの設定
      when(mockConn.isClosed()).thenReturn(true);

      initUniqueList(mockConn);
      verify(mockConn, times(0)).prepareStatement(anyString());
    }

    @Test
    @DisplayName("コネクションが正常の場合、TruncateTableを実行する")
    void case3() throws Exception {
      // モックの設定
      when(mockConn.isClosed()).thenReturn(false);
      when(mockConn.prepareStatement("TRUNCATE TABLE sys_unique_list")).thenReturn(mockPreparedStmnt);
      when(mockPreparedStmnt.execute()).thenReturn(true);

      initUniqueList(mockConn);
      verify(mockConn, times(1)).prepareStatement("TRUNCATE TABLE sys_unique_list");
    }

  }

}
