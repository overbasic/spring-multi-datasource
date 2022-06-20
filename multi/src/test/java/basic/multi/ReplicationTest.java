package basic.multi;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ReplicationTest {

    private static final String Test_Method_Name = "determineCurrentLookupKey";

    @Test
    @DisplayName("쓰기_전용_트랜잭션_테스트")
    @Transactional(readOnly = false)
    void writeOnlyTransactionTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // given
        RoutingDataSource replicationRoutingDataSource = new RoutingDataSource();

        // when
        Method determineCurrentLookupKey = RoutingDataSource.class.getDeclaredMethod(Test_Method_Name);
        determineCurrentLookupKey.setAccessible(true);

        DataSourceType dataSourceType = (DataSourceType) determineCurrentLookupKey
            .invoke(replicationRoutingDataSource);

        // then
        assertThat(dataSourceType).isEqualTo(DataSourceType.MASTER);
    }

    @Test
    @DisplayName("읽기_전용_트랜잭션_테스트")
    @Transactional(readOnly = true)
    void readOnlyTransactionTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // given
        RoutingDataSource replicationRoutingDataSource = new RoutingDataSource();

        // when
        Method determineCurrentLookupKey = RoutingDataSource.class.getDeclaredMethod(Test_Method_Name);
        determineCurrentLookupKey.setAccessible(true);

        DataSourceType dataSourceType = (DataSourceType) determineCurrentLookupKey
            .invoke(replicationRoutingDataSource);

        // then
        assertThat(dataSourceType).isEqualTo(DataSourceType.SLAVE);
    }

}