package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.core.config.CoreCostLimit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CostLimitCoreMapperImpl.class
})
class CostLimitCoreMapperTest {

    @Autowired
    private CostLimitCoreMapper mapper;

    @Test
    void toCostLimit_defaultCoreCostLimit() {
        // given
        CoreCostLimit coreCostLimit = new CoreCostLimit();

        CostLimit expected = new CostLimit();

        // when
        CostLimit actual = mapper.toCostLimit(coreCostLimit);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toCostLimit_allNonNullFieldsInCoreCostLimit() {
        // given
        CoreCostLimit coreCostLimit = new CoreCostLimit();
        coreCostLimit.setMinute(BigDecimal.valueOf(1));
        coreCostLimit.setDay(BigDecimal.valueOf(10));
        coreCostLimit.setWeek(BigDecimal.valueOf(100));
        coreCostLimit.setDay(BigDecimal.valueOf(1000));

        CostLimit expected = new CostLimit();
        expected.setMinute(BigDecimal.valueOf(1));
        expected.setDay(BigDecimal.valueOf(10));
        expected.setWeek(BigDecimal.valueOf(100));
        expected.setDay(BigDecimal.valueOf(1000));

        // when
        CostLimit actual = mapper.toCostLimit(coreCostLimit);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toCostLimit_nullDayInCoreCostLimit() {
        // given
        CoreCostLimit coreCostLimit = new CoreCostLimit();
        coreCostLimit.setDay(null);

        CostLimit expected = new CostLimit();
        expected.setDay(BigDecimal.valueOf(Long.MAX_VALUE));

        // when
        CostLimit actual = mapper.toCostLimit(coreCostLimit);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toCostLimit_nullCoreCostLimit() {
        // when
        CostLimit actual = mapper.toCostLimit(null);

        // then
        assertThat(actual).isEqualTo(new CostLimit());
    }

}