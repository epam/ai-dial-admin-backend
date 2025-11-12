package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.CostLimit;
import com.epam.aidial.cfg.dto.CostLimitDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CostLimitDtoMapperImpl.class
})
class CostLimitDtoMapperTest {

    @Autowired
    private CostLimitDtoMapper mapper;

    @Test
    void toDomain_defaultCostLimitDto() {
        // given
        CostLimitDto costLimitDto = new CostLimitDto();

        CostLimit expected = new CostLimit();

        // when
        CostLimit actual = mapper.toDomain(costLimitDto);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toDomain_allNonNullFieldsInCostLimitDto() {
        // given
        CostLimitDto costLimitDto = new CostLimitDto();
        costLimitDto.setMinute(BigDecimal.valueOf(1));
        costLimitDto.setDay(BigDecimal.valueOf(10));
        costLimitDto.setWeek(BigDecimal.valueOf(100));
        costLimitDto.setDay(BigDecimal.valueOf(1000));

        CostLimit expected = new CostLimit();
        expected.setMinute(BigDecimal.valueOf(1));
        expected.setDay(BigDecimal.valueOf(10));
        expected.setWeek(BigDecimal.valueOf(100));
        expected.setDay(BigDecimal.valueOf(1000));

        // when
        CostLimit actual = mapper.toDomain(costLimitDto);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toDomain_nullDayInCostLimitDto() {
        // given
        CostLimitDto costLimitDto = new CostLimitDto();
        costLimitDto.setDay(null);

        CostLimit expected = new CostLimit();
        expected.setDay(BigDecimal.valueOf(Long.MAX_VALUE));

        // when
        CostLimit actual = mapper.toDomain(costLimitDto);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void toDomain_nullCostLimitDto() {
        // when
        CostLimit actual = mapper.toDomain(null);

        // then
        assertThat(actual).isEqualTo(new CostLimit());
    }
}