package io.onebyone.microservices.restSample.data.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onebyone.microservices.restSample.data.entity.SampleChildEntity;
import io.onebyone.microservices.restSample.data.entity.SampleChildOfChildEntity;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleChildOfChildEntityDto {
    private String id;

    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("column_1")
    private String column1;
    @JsonProperty("column_2")
    private String column2;
    @JsonProperty("column_3")
    private String column3;
    @JsonProperty("column_4")
    private String column4;
    @JsonProperty("column_5")
    private String column5;
    @JsonProperty("column_6")
    private String column6;
    @JsonProperty("column_7")
    private String column7;
    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("parent_name")
    private String parentName;

    public SampleChildOfChildEntityDto() {
    }

    public SampleChildOfChildEntity toEntity() {
        SampleChildOfChildEntity entity = new SampleChildOfChildEntity();
        return update(entity, this);
    }

    public static SampleChildOfChildEntity update(SampleChildOfChildEntity entity, SampleChildOfChildEntityDto json) {
        entity.setId(json.getId());
        entity.setAmount(json.getAmount());
        entity.setColumn1(json.getColumn1());
        entity.setColumn2(json.getColumn2());
        entity.setColumn3(json.getColumn3());
        entity.setColumn4(json.getColumn4());
        entity.setColumn5(json.getColumn5());
        entity.setColumn6(json.getColumn6());
        entity.setColumn7(json.getColumn7());
        entity.setColumn7(json.getColumn7());

        if (StringUtils.isNotBlank(json.getParentId())) {
            SampleChildEntity parent = new SampleChildEntity();
            parent.setId(json.getParentId());
            parent.setName(json.getParentName());
            entity.setParent(parent);
        }
        return entity;
    }

    public static SampleChildOfChildEntityDto fromEntity(SampleChildOfChildEntity entity) {
        SampleChildOfChildEntityDto json = new SampleChildOfChildEntityDto();
        json.setId(entity.getId());
        json.setAmount(entity.getAmount());
        json.setColumn1(entity.getColumn1());
        json.setColumn2(entity.getColumn2());
        json.setColumn3(entity.getColumn3());
        json.setColumn4(entity.getColumn4());
        json.setColumn5(json.getColumn5());
        json.setColumn6(json.getColumn6());
        json.setColumn7(entity.getColumn7());

        if (entity.getParent() != null) {
            json.setParentId(entity.getParent().getId());
            json.setParentName(entity.getParent().getName());
        }
        return json;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    public String getColumn4() {
        return column4;
    }

    public void setColumn4(String column4) {
        this.column4 = column4;
    }

    public String getColumn5() {
        return column5;
    }

    public void setColumn5(String column5) {
        this.column5 = column5;
    }

    public String getColumn6() {
        return column6;
    }

    public void setColumn6(String column6) {
        this.column6 = column6;
    }

    public String getColumn7() {
        return column7;
    }

    public void setColumn7(String column7) {
        this.column7 = column7;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
