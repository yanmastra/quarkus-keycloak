package io.onebyone.microservices.restSample.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleParentSummaryJson {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("child_count")
    private long childCount;
    @JsonProperty("child_of_child_count")
    private long childOfChildCount;
    @JsonProperty("child_of_child_total")
    private BigDecimal childOfChildTotal;

    public SampleParentSummaryJson() {
    }

    public SampleParentSummaryJson(String id, String name, long childCount, long childOfChildCount, BigDecimal childOfChildTotal) {
        this.id = id;
        this.name = name;
        this.childCount = childCount;
        this.childOfChildCount = childOfChildCount;
        this.childOfChildTotal = childOfChildTotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getChildCount() {
        return childCount;
    }

    public void setChildCount(long childCount) {
        this.childCount = childCount;
    }

    public long getChildOfChildCount() {
        return childOfChildCount;
    }

    public void setChildOfChildCount(long childOfChildCount) {
        this.childOfChildCount = childOfChildCount;
    }

    public BigDecimal getChildOfChildTotal() {
        return childOfChildTotal;
    }

    public void setChildOfChildTotal(BigDecimal childOfChildTotal) {
        this.childOfChildTotal = childOfChildTotal;
    }
}
