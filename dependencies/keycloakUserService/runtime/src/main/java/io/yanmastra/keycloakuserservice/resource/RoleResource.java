package io.yanmastra.keycloakuserservice.resource;

import io.yanmastra.quarkus.microservices.common.crud.Paginate;
import io.yanmastra.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.data.KcRoleRepository;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRole;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.yanmastra.keycloakuserservice.UserManagementConstant.PATH_ROLE;
import static io.yanmastra.keycloakuserservice.UserManagementConstant.PATH_USER_MANAGEMENT;

@Path(PATH_USER_MANAGEMENT + PATH_ROLE)
@SecurityRequirement(name = "Keycloak")
public class RoleResource {

    @Inject
    KcRoleRepository roleRepository;
    @Inject
    Helper helper;

    protected PanacheRepositoryBase<KeycloakRole, String> getRepository() {
        return roleRepository;
    }

    protected RoleRepresentation fromEntity(KeycloakRole entity) {
        return helper.convertToDto(entity);
    }

    protected Sort getSort() {
        return Sort.descending(new String[]{"createdAt"}).and("createdAt", Sort.NullPrecedence.NULLS_LAST);
    }


    @Operation(
            summary = "Get Paginate data",
            description = "Possible parameters:\n<ul>\n<li>page: 1, 2, 3, ..., n</li>\n<li>size: 10, 20, etc.</li>\n<li>field name of entity class</li>\n</ul>\n"
    )
    @RunOnVirtualThread
    @GET
    @Transactional
    public Paginate<RoleRepresentation> getList(@QueryParam("page") Integer page, @QueryParam("page") Integer size, @Context ContainerRequestContext context) {
        if (page == null || page <= 0) {
            page = 1;
        }

        if (size == null || size < 5) {
            size = 5;
        }

        Page objPage = Page.of(page - 1, size);
        Sort sort = this.getSort();
        Map<String, Object> queryParams = new HashMap<>();
        String hql = CrudQueryFilterUtils.createFilterQuery(context.getUriInfo().getQueryParameters(), queryParams, this.searchAbleColumn());
        PanacheQuery<KeycloakRole> entityQuery = this.getRepository().find(hql, sort, queryParams);
        long totalCount = entityQuery.count();
        List<KeycloakRole> result = entityQuery.page(objPage).list();
        return new Paginate<>(result.stream().map(this::fromEntity).toList(), objPage.index + 1, objPage.size, totalCount);
    }

    protected Set<String> searchAbleColumn() {
        return Set.of("name", "description");
    }
}
