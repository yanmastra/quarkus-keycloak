package io.onebyone.quarkus.microservices.common.crud;

import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.dto.SelectionDto;
import io.onebyone.quarkus.microservices.common.entity.BaseEntity;
import io.onebyone.quarkus.microservices.common.entity.SelectableEntity;
import io.onebyone.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SelectablePaginationResource<Entity extends BaseEntity, Dto extends BaseDto<Entity>> extends io.onebyone.quarkus.microservices.common.v2.crud.SelectablePaginationResource<Entity, Dto, String> {

    @Override
    protected String toId(String id) {
        return id;
    }
}
