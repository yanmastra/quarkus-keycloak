package io.yanmastra.keycloakuserservice.data.entities;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.util.List;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted_at=NOW() WHERE id=?")
@Filter(name = "deletedUserFilter", condition = "deleted_at is not null = :isDeleted")
@FilterDef(name = "deletedUserFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
public class User extends BaseEntity {
    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 36, unique = true)
    private String username;

    @Column(length = 64, unique = true)
    private String email;

    @Column(length = 64, name = "full_name")
    private String fullName;

    @Column(name = "job_title", length = 36)
    private String jobTitle;

    @Column(name = "job_category", length = 36)
    private String jobCategory;

    @Column(name = "address")
    private String address;

    @Column(name = "country", length = 36)
    private String country;

    @Column(name = "state", length = 36)
    private String state;

    @Column(name = "city", length = 36)
    private String city;

    @Column(name = "contact_phone", length = 36)
    private String contactPhone;

    @Column(name = "contact_mobile", length = 36)
    private String contactMobile;

    @Column(name = "emrg_name", length = 36)
    private String emergencyContactName;

    @Column(name = "emrg_phone", length = 36)
    private String emergencyContactPhone;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true, cascade = CascadeType.MERGE)
    private List<UserRoleGroup> groups;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    public User() {
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public List<UserRoleGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<UserRoleGroup> groups) {
        this.groups = groups;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }
}
