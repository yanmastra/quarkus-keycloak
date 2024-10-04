package io.onebyone.microservices.mediaMailService;

import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mail_data")
public class MyMailData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(length = 36)
    private String code;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    public MyMailData() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected <Dto> Dto toDto() {
        return null;
    }

    @Override
    public <Dto> void updateByDto(Dto dto) {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
