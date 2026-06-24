package com.example.batchmonitor.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageResult<T> {

    private final List<T> items;
    private final long totalCount;
    private final int page;
    private final int size;
    private final int totalPages;
    private final int startPage;
    private final int endPage;

    public PageResult(List<T> items, long totalCount, int page, int size) {
        this.items = items == null ? Collections.<T>emptyList() : items;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.totalPages = totalCount <= 0 ? 1 : (int) Math.ceil((double) totalCount / (double) size);
        int blockSize = 5;
        this.startPage = Math.max(1, page - 2);
        this.endPage = Math.min(totalPages, startPage + blockSize - 1);
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < totalPages;
    }

    public List<Integer> getPageNumbers() {
        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = startPage; i <= endPage; i++) {
            numbers.add(i);
        }
        return numbers;
    }
}
