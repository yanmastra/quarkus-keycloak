package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    @JsonProperty("id")
    public String id;

    @JsonProperty("username")
    public String username;

    @JsonProperty("email")
    public String email;

    @JsonProperty("full_name")
    public String fullName;

    @JsonProperty( "job_title")
    public String jobTitle;

    @JsonProperty("job_category")
    public String jobCategory;

    @JsonProperty("address")
    public String address;

    @JsonProperty("country")
    public String country;

    @JsonProperty("state")
    public String state;

    @JsonProperty( "city")
    public String city;

    @JsonProperty("contact_phone")
    public String contactPhone;

    @JsonProperty( "contact_mobile")
    public String contactMobile;

    @JsonProperty("emergency_contact_name")
    public String emergencyContactName;

    @JsonProperty("emergency_contact_phone")
    public String emergencyContactPhone;

    @JsonProperty("groups")
    public List<RoleGroupDto> groups = new ArrayList<>();

    @JsonProperty("password")
    public String password;

    @JsonProperty("is_verified")
    public Boolean isVerified;

    public UserDto() {
    }

    public boolean validate() {
        if (StringUtils.isBlank(email)) throw new BadRequestException("Email is required");
        if (StringUtils.isBlank(fullName)) throw new BadRequestException("Full name is required");
        if (groups == null || groups.isEmpty()) throw new BadRequestException("Group is required");
        return true;
    }
}
