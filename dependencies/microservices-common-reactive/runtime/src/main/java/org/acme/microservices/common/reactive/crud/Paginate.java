package org.acme.microservices.common.reactive.crud;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Paginate<E> {
    private List<E> data;
    @JsonProperty("current_page")
    private int currentPage;
    @JsonProperty("size")
    private int size;
    @JsonProperty("is_first")
    private boolean isFirst;
    @JsonProperty("is_last")
    private boolean isLast;
    @JsonProperty("total_data")
    private long totalData;
    @JsonProperty("total_page")
    private int totalPage;

    public Paginate() {
    }

    public Paginate(List<E> data, int currentPage, int size, boolean isFirst, boolean isLast, long totalData, int totalPage) {
        this.data = data;
        this.currentPage = currentPage;
        this.size = size;
        this.isFirst = isFirst;
        this.isLast = isLast;
        this.totalData = totalData;
        this.totalPage = totalPage;
    }

    public List<E> getData() {
        return data;
    }

    public void setData(List<E> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public long getTotalData() {
        return totalData;
    }

    public void setTotalData(long totalData) {
        this.totalData = totalData;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
