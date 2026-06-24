package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.QueryComparisonDto;
import com.example.batchmonitor.dto.QueryComparisonResultDto;
import com.example.batchmonitor.dto.QueryComparisonSearchConditionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QueryComparisonMapper {

    List<QueryComparisonDto> findComparisons(QueryComparisonSearchConditionDto condition);

    long countComparisons(QueryComparisonSearchConditionDto condition);

    QueryComparisonDto findComparisonById(Long comparisonId);

    void insertComparison(QueryComparisonDto comparison);

    void updateComparison(QueryComparisonDto comparison);

    void deleteComparison(Long comparisonId);

    void insertResult(QueryComparisonResultDto result);

    QueryComparisonResultDto findResultById(Long resultId);

    QueryComparisonResultDto findLatestResultByComparisonId(Long comparisonId);

    List<QueryComparisonDto> findScheduledComparisons();

    List<QueryComparisonResultDto> findResults(QueryComparisonSearchConditionDto condition);

    long countResults(QueryComparisonSearchConditionDto condition);
}
