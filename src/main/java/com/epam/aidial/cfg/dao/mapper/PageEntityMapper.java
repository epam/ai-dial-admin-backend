package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.page.PageRequestModel;
import com.epam.aidial.cfg.domain.model.page.SortDirection;
import com.epam.aidial.cfg.domain.model.page.filter.Filter;
import jakarta.persistence.criteria.Expression;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PageEntityMapper {

    default org.springframework.data.domain.PageRequest toPageRequest(PageRequestModel pageRequest) {
        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();
        if (pageRequest.getSorts().isEmpty()) {
            return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
        }
        List<Sort.Order> orders = pageRequest.getSorts().stream()
                .map(sort -> new Sort.Order(mapDirection(sort.getDirection()), sort.getColumn()))
                .collect(Collectors.toList());
        return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize, Sort.by(orders));
    }

    default <T> List<Specification<T>> toSpecifications(PageRequestModel pageRequest, SpecificationContext specificationContext, Class<T> clazz) {
        if (pageRequest == null || CollectionUtils.isEmpty(pageRequest.getFilters())) {
            return List.of();
        }
        return pageRequest.getFilters().stream()
                .map(filter -> mapFilter(filter, specificationContext, clazz))
                .collect(Collectors.toList());
    }

    private <T> Specification<T> mapFilter(Filter filter, SpecificationContext specificationContext, Class<T> clazz) {
        return (root, query, criteriaBuilder) -> {
            if (filter == null) {
                return null;
            }
            final Expression<String> column;
            final String value;
            if (specificationContext.caseInSensitiveColumns().contains(filter.getColumn())
                    && filter.getValue() != null) {
                column = criteriaBuilder.lower(root.get(filter.getColumn()));
                value = filter.getValue().toLowerCase();
            } else {
                column = root.get(filter.getColumn());
                value = filter.getValue();
            }
            switch (filter.getOperator()) {
                case isnull -> {
                    return criteriaBuilder.isNull(column);
                }
                case isnotnull -> {
                    return criteriaBuilder.isNotNull(column);
                }
                case eq -> {
                    return criteriaBuilder.equal(column, value);
                }
                case ne -> {
                    return criteriaBuilder.notEqual(column, value);
                }
                case gt -> {
                    return criteriaBuilder.greaterThan(column, value);
                }
                case ge -> {
                    return criteriaBuilder.greaterThanOrEqualTo(column, value);
                }
                case lt -> {
                    return criteriaBuilder.lessThan(column, value);
                }
                case le -> {
                    return criteriaBuilder.lessThanOrEqualTo(column, value);
                }
                case co -> {
                    return criteriaBuilder.like(column, "%" + value + "%");
                }
                case nc -> {
                    return criteriaBuilder.notLike(column, "%" + value + "%");
                }
                default -> throw new IllegalArgumentException("Operator " + filter.getOperator() + " is not supported");
            }
        };
    }

    Sort.Direction mapDirection(SortDirection direction);

    record SpecificationContext(Set<String> caseInSensitiveColumns) {
    }
}