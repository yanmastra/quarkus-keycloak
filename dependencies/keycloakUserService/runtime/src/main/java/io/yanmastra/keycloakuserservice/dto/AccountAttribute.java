package io.yanmastra.keycloakuserservice.dto;

import java.util.List;

public class AccountAttribute {
    private List<String> companyAccess;

    public AccountAttribute() {
    }

    public List<String> getCompanyAccess() {
        return companyAccess;
    }

    public void setCompanyAccess(List<String> companyAccess) {
        this.companyAccess = companyAccess;
    }
}
