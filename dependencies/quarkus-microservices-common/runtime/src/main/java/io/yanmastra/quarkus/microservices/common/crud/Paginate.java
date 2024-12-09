package io.yanmastra.quarkus.microservices.common.crud;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Paginate<E> {
    @JsonProperty("data")
    private List<E> data;
    @JsonProperty("meta")
    private MetaPagination meta;

    public Paginate() {
        this.meta = new MetaPagination(1, 5, 0L, 0);
    }

    public Paginate(List<E> data, int currentPage, int size, long totalData) {
        if (data == null) throw new BadRequestException("data is null");
        this.data = data;
        this.meta = new MetaPagination(currentPage, size, totalData, data.size());
    }

    @JsonIgnore
    public List<E> getData() {
        return data;
    }

    public void setData(List<E> data) {
        this.data = data;
    }

    @JsonIgnore
    public MetaPagination getMeta() {
        return meta;
    }

    public void setMeta(MetaPagination meta) {
        this.meta = meta;
    }

    @Deprecated
    public void setTotalData(long totalData) {
        if (meta != null) meta.setTotalData(totalData);
    }

    @Deprecated
    public void setCurrentPage(Integer currentPage) {
        if (meta != null) meta.setPage(currentPage);
    }

    @Deprecated
    public void setSize(Integer size) {
        if (meta != null) meta.setSize(size);
    }
}
