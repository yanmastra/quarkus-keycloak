package io.onebyone.microservices.mediaMailService;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import io.onebyone.quarkus.microservices.common.crud.CrudableEndpointResource;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.Map;

@Path("api/v1/mail")
@SecurityRequirement(name = "Keycloak")
public class MailResource extends CrudableEndpointResource<MyMailData, MyMailData> {

    @Inject MailDataRepository mailDataRepository;

    @GET
    @Path("hello")
    public Response hello() {
        return Response.ok(Json.createObjectBuilder().add("msg", "hello").build()).build();
    }

    @Inject
    ReactiveMailer mailer;

    @POST
    @Path("send/{templateCode}")
    public Uni<Response> sendEmail(
            @PathParam("templateCode") String templateCode,
            Map<String, Object> data
    ) {
        return Uni.createFrom().item(Response.ok().build());
    }

    @Override
    protected PanacheRepositoryBase<MyMailData, String> getRepository() {
        return mailDataRepository;
    }

    @Override
    protected MyMailData fromEntity(MyMailData entity) {
        return entity;
    }

    @Override
    protected MyMailData toEntity(MyMailData myMailData) {
        return myMailData;
    }

    @Override
    protected MyMailData update(MyMailData entity, MyMailData myMailData) {
        entity.setCode(myMailData.getCode());
        entity.setDateTime(myMailData.getDateTime());
        return entity;
    }
}
