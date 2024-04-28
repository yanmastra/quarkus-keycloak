package io.yanmastra.microservices.common.crud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Paginate<E> {
    @JsonProperty("data")
    private List<E> data;
    @JsonProperty("current_page")
    private int currentPage;
    @JsonProperty("size")
    private int size;
    @JsonProperty("total_data")
    private long totalData;

    public Paginate() {
    }

    @Deprecated
    public Paginate(List<E> data, int currentPage, int size, boolean isFirst, boolean isLast, long totalData, int totalPage) {
        this.data = data;
        this.currentPage = currentPage;
        this.size = size;
        this.totalData = totalData;
    }

    public Paginate(List<E> data, int currentPage, int size, long totalData) {
        this.data = data;
        this.currentPage = currentPage;
        this.size = size;
        this.totalData = totalData;
    }

    @JsonIgnore
    public List<E> getData() {
        return data;
    }

    public void setData(List<E> data) {
        this.data = data;
    }

    @JsonIgnore
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
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
        return currentPage == 1;
    }

    @JsonProperty("is_last")
    public boolean isLast() {
        return ((long) getCurrentPage() * getSize()) >= totalData;
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
}
