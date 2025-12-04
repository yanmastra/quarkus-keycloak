package io.yanmastra.quarkus.microservices.common.crud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaPagination {
    @JsonProperty("page")
    private Integer page = null;
    @JsonProperty("size")
    private Integer size = null;
    @JsonProperty("total_data")
    private Long totalData = null;
    @JsonProperty("data_count")
    private Integer dataCount = null;

    public MetaPagination() {
    }

    public MetaPagination(Integer page, Integer size, Long totalData, Integer dataCount) {
        this.page = page;
        this.size = size;
        this.totalData = totalData;
        this.dataCount = dataCount;
    }

    @JsonIgnore
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @JsonProperty("is_first")
    public boolean isFirst() {
        return page == 1;
    }

    @JsonProperty("is_last")
    public boolean isLast() {
        return ((long) page * getSize()) >= totalData;
    }

    @JsonIgnore
    public long getTotalData() {
        return totalData;
    }

    public void setTotalData(long totalData) {
        this.totalData = totalData;
    }

    @JsonProperty("total_page")
    public int getTotalPage() {
        return (int) (totalData / size + (totalData % size == 0 ?  0 : 1));
    }

    @JsonIgnore
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setTotalData(Long totalData) {
        this.totalData = totalData;
    }

    @JsonProperty("start")
    public Long getStart() {
        return ((long) (getPage() - 1) * getSize()) + 1L;
    }

    @JsonProperty("end")
    public Long getEnd() {
        return ((long) (getPage() - 1) * getSize()) + getDataCount();
    }

    @JsonIgnore
    public Integer getDataCount() {
        return dataCount;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }
}
