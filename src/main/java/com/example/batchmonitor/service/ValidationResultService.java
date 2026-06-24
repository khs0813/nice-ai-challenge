package com.example.batchmonitor.service;

import com.example.batchmonitor.dto.PageResult;
import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import com.example.batchmonitor.dto.ValidationResultDto;
import com.example.batchmonitor.mapper.ValidationResultMapper;
import com.example.batchmonitor.util.DateTimeUtils;
import org.springframework.stereotype.Service;

@Service
public class ValidationResultService {

    private final ValidationResultMapper validationResultMapper;

    public ValidationResultService(ValidationResultMapper validationResultMapper) {
        this.validationResultMapper = validationResultMapper;
    }

    public PageResult<ValidationResultDto> findResults(QueryComparisonSearchConditionDto condition) {
        prepare(condition);
        return new PageResult<ValidationResultDto>(
                validationResultMapper.findResults(condition),
                validationResultMapper.countResults(condition),
                condition.getPage(),
                condition.getSize()
        );
    }

    private void prepare(QueryComparisonSearchConditionDto condition) {
        condition.normalizePaging();
        condition.setStartDateTime(DateTimeUtils.parseStartDate(condition.getStartDate()));
        condition.setEndDateTime(DateTimeUtils.parseEndDateExclusive(condition.getEndDate()));
    }
}
