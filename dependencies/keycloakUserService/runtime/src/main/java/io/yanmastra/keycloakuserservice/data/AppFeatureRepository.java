package io.yanmastra.keycloakuserservice.data;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.keycloakuserservice.data.entities.AppFeature;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppFeatureRepository implements PanacheRepositoryBase<AppFeature, Long> {
}
