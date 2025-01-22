package io.yanmastra.quarkus.microservices.common.repository;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.security.identity.SecurityIdentity;
import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.stream.Stream;

public abstract class BaseRepository<Entity extends BaseEntity, Id> implements PanacheRepositoryBase<Entity, Id>  {
    @Inject
    Logger log;

    protected String getUserIdentity() {
        try (InstanceHandle<SecurityIdentity> injectableBean = Arc.container().instance(SecurityIdentity.class)) {
            SecurityIdentity securityIdentity = injectableBean.orElse(null);
            if (securityIdentity == null) return null;
            if (securityIdentity.getPrincipal() == null) return null;
            return securityIdentity.getPrincipal().getName();
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    private void setUserMetadata(Entity entity) {
        String userIdentity = getUserIdentity();
        if (StringUtils.isBlank(userIdentity)) {
            return;
        }

        if (entity.getDeletedAt() == null) {
            if (StringUtils.isBlank(entity.getId()) || StringUtils.isBlank(entity.getCreatedBy())) {
                entity.setCreatedBy(getUserIdentity());
            } else {
                entity.setUpdatedBy(getUserIdentity());
            }
        } else {
            entity.setDeletedBy(userIdentity);
        }
    }

    @Override
    public void persist(Entity entity) {
        setUserMetadata(entity);
        PanacheRepositoryBase.super.persist(entity);
    }

    @Override
    public void persist(Stream<Entity> entities) {
        entities = entities.peek(this::setUserMetadata);
        PanacheRepositoryBase.super.persist(entities);
    }

    @SafeVarargs
    @Override
    public final void persist(Entity firstEntity, Entity... entities) {
        setUserMetadata(firstEntity);
        for (Entity entity : entities) {
            setUserMetadata(entity);
        }
        PanacheRepositoryBase.super.persist(firstEntity, entities);
    }

    @Override
    public void persist(Iterable<Entity> entities) {
        for (Entity entity: entities) {
            setUserMetadata(entity);
        }
        PanacheRepositoryBase.super.persist(entities);
    }

    @Override
    public void persistAndFlush(Entity entity) {
        setUserMetadata(entity);
        PanacheRepositoryBase.super.persistAndFlush(entity);
    }

    @Override
    public boolean deleteById(Id id) {
        boolean result = PanacheRepositoryBase.super.deleteById(id);
        if (result) {
            String userIdentity = getUserIdentity();
            if (StringUtils.isNotBlank(userIdentity)) {
                Entity entity = findById(id);
                if (entity != null) {
                    setUserMetadata(entity);
                }
            }
        }
        return result;
    }
}
