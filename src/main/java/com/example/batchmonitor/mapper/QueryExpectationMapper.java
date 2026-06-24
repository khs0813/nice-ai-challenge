package com.example.batchmonitor.mapper;

import com.example.batchmonitor.dto.QueryExpectationDto;
import com.example.batchmonitor.dto.QueryExpectationResultDto;
import com.example.batchmonitor.dto.QueryExpectationSearchConditionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QueryExpectationMapper {

    List<QueryExpectationDto> findExpectations(QueryExpectationSearchConditionDto condition);

    long countExpectations(QueryExpectationSearchConditionDto condition);

    QueryExpectationDto findExpectationById(Long expectationId);

    void insertExpectation(QueryExpectationDto expectation);

    void updateExpectation(QueryExpectationDto expectation);

    void deleteExpectation(Long expectationId);

    void insertResult(QueryExpectationResultDto result);

    QueryExpectationResultDto findResultById(Long resultId);

    QueryExpectationResultDto findLatestResultByExpectationId(Long expectationId);

    List<QueryExpectationDto> findScheduledExpectations();

    List<QueryExpectationResultDto> findResults(QueryExpectationSearchConditionDto condition);

    long countResults(QueryExpectationSearchConditionDto condition);
}
