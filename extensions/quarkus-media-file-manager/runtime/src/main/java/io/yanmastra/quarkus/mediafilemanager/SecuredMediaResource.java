package io.yanmastra.quarkus.mediafilemanager;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import static io.yanmastra.quarkus.mediafilemanager.MediaUtils.BASE_SECURED_MEDIA_PATH;

@Authenticated
@Path(BASE_SECURED_MEDIA_PATH)
public class SecuredMediaResource {

    @Inject
    MediaService mediaService;

    @RunOnVirtualThread
    @GET
    @Path("id/{fileId}")
    public Response getMediaById(
            @PathParam("fileId") String fileId,
            @QueryParam("download") Boolean isDownload
    ) {
        if (isDownload == null) isDownload = false;
        return mediaService.loadSecuredMediaById(fileId, null, isDownload);
    }

    @RunOnVirtualThread
    @GET
    @Path("id/{fileId}/{size}")
    public Response getMediaByIdAndSize(
            @PathParam("fileId") String fileId,
            @PathParam("size") String size,
            @QueryParam("download") Boolean isDownload
    ) {
        if (isDownload == null) isDownload = false;
        return mediaService.loadSecuredMediaById(fileId, size, isDownload);
    }

    @RunOnVirtualThread
    @GET
    @Path("{filePath: .+}")
    public Response getMedia(
            @PathParam("filePath") String filePath,
            @QueryParam("download") Boolean isDownload
    ) {
        if (isDownload == null) isDownload = false;
        return mediaService.loadSecuredMedia(filePath, isDownload);
    }
}
