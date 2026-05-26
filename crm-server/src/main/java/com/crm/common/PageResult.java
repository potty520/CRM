package com.crm.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;

    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        PageResult<T> p = new PageResult<>();
        p.setRecords(records);
        p.setTotal(total);
        p.setPageNum(pageNum);
        p.setPageSize(pageSize);
        return p;
    }
}
