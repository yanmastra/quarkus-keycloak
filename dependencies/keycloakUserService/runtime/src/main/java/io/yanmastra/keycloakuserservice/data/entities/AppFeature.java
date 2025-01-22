package io.yanmastra.keycloakuserservice.data.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_app_feature", indexes = {
        @Index(name = "_unique_key", columnList = "realm_name, feature_key", unique = true)
})
public class AppFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "realm_name", length = 36, nullable = false)
    private String realmName;
    @Column(name = "feature_key", length = 4, nullable = false)
    private String featureKey;
    @Column(name = "label")
    private String label;

    public AppFeature() {
    }

    public AppFeature(String realmName, String featureKey, String label) {
        this.realmName = realmName;
        this.featureKey = featureKey;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
