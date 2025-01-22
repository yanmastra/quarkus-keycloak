package io.yanmastra.keycloakuserservice.resource;

import io.yanmastra.authorization.ResponseJson;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.quarkus.microservices.common.crud.Paginate;
import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.data.KcRoleGroupRepository;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRoleGroup;
import io.yanmastra.keycloakuserservice.dto.RoleGroupDto;
import io.yanmastra.keycloakuserservice.dto.RoleGroupMappingRoleDto;
import io.yanmastra.keycloakuserservice.services.GroupService;
import io.yanmastra.keycloakuserservice.services.RoleService;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

import static io.yanmastra.keycloakuserservice.UserManagementConstant.PATH_GROUP;
import static io.yanmastra.keycloakuserservice.UserManagementConstant.PATH_USER_MANAGEMENT;

@Path(PATH_USER_MANAGEMENT + PATH_GROUP)
@SecurityRequirement(name = "Keycloak")
@OpenAPIDefinition(info = @Info(title = "Role Group", version = "v1"), tags = {
        @Tag(name = "name", description = "something")
})
public class GroupResource extends CrudableEndpointResource<KeycloakRoleGroup, RoleGroupDto> {
    @Inject
    KcRoleGroupRepository groupRepository;
    @Inject
    Helper helper;
    @Inject
    Logger log;
    @Inject
    RoleService service;
    @Inject
    GroupService groupService;

    @Override
    protected PanacheRepositoryBase<KeycloakRoleGroup, String> getRepository() {
        return groupRepository;
    }

    @Override
    protected RoleGroupDto fromEntity(KeycloakRoleGroup entity) {
        return helper.convertToDto(entity);
    }

    @Override
    protected KeycloakRoleGroup toEntity(RoleGroupDto dao) {
        return helper.convertToEntity(dao);
    }

    @Override
    protected KeycloakRoleGroup update(KeycloakRoleGroup entity, RoleGroupDto dao) {
        return service.updateRoleGroup(entity, dao);
    }

    @Override
    protected Paginate<RoleGroupDto> getList(Integer page, Integer size, MultivaluedMap<String, String> requestQueries, ContainerRequestContext context) {
        requestQueries = new MultivaluedHashMap<>();
        for (String key : context.getUriInfo().getQueryParameters().keySet()) {
            log.debug("put: "+ key+":" + context.getUriInfo().getQueryParameters().get(key));
            requestQueries.put(key, context.getUriInfo().getQueryParameters().get(key));
        }

        try {
            String companyId = helper.getCompanyId();
            if (StringUtils.isBlank(companyId)) companyId = "unknown";
            String path = "/users/" + companyId;
            log.debug("path: " + path);
            String parentId = service.getIdByPath(path);
            if (StringUtils.isNotBlank(parentId)) {
                requestQueries.put("parent.id", new ArrayList<>(List.of(parentId)));
            }
            return super.getList(page, size, requestQueries, context);
        } catch (Exception e){
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Get Paginate data",
            description = "Possible parameters:\n<ul>\n<li>page: 1, 2, 3, ..., n</li>\n<li>size: 10, 20, etc.</li>\n<li>field name of entity class</li>\n</ul>\n"
    )
    @RunOnVirtualThread
    @Path("mapping")
    @GET
    @Transactional
    public Paginate<RoleGroupDto> getListMapping(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @Context ContainerRequestContext context) {
        if (page == null || page <= 0) {
            page = 1;
        }

        if (size == null || size < 5) {
            size = 5;
        }

        MultivaluedMap<String, String> requestQueries = context.getUriInfo().getQueryParameters();
        Paginate<RoleGroupDto> result = this.getList(page, size, requestQueries, context);
        result.getData().forEach(item -> groupService.fetchGroupMapping((RoleGroupMappingRoleDto) item));
        return result;
    }

    @Override
    public RoleGroupDto getOne(String id, SecurityContext context) {
        RoleGroupDto dto = super.getOne(id, context);
        return service.fetchDetails(dto);
    }

    @GET
    @Path("mapping/{id}")
    @Transactional
    public RoleGroupDto getOneMapping(@PathParam("id") String id, @Context SecurityContext context) {
        RoleGroupDto item = this.getOne(id, context);
        groupService.fetchGroupMapping((RoleGroupMappingRoleDto) item);
        return item;
    }

    @Override
    public ResponseJson<RoleGroupDto> create(RoleGroupDto dao, SecurityContext context) throws Exception {
        RoleGroupDto result = service.createGroup(dao, context);
        return new ResponseJson<>(result);
    }
}
