package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import com.example.batchmonitor.dto.ValidationResultDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ValidationResultMapper {

    List<ValidationResultDto> findResults(QueryComparisonSearchConditionDto condition);

    long countResults(QueryComparisonSearchConditionDto condition);
}
