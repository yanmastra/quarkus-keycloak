package io.yanmastra.quarkus.mediafilemanager.it;

import io.yanmastra.quarkus.mediafilemanager.MediaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.util.Map;

@Path("/test")
@ApplicationScoped
public class QuarkusMediaFileManagerResource {

    @Inject
    MediaService mediaService;

    @POST
    @Path("/upload/image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadImage(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = fileUpload.uploadedFile().toFile();
        MediaService.ImageStore store = mediaService.storeImage(file, fileUpload.fileName())
                .addWidthVariant(320)
                .addWidthVariant(640);
        if (StringUtils.isNotBlank(location)) {
            store.specificLocation(location);
        }
        return store.store();
    }

    @POST
    @Path("/upload/image/secured")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadSecuredImage(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = fileUpload.uploadedFile().toFile();
        MediaService.ImageStore store = mediaService.storeSecuredImage(file, fileUpload.fileName())
                .addWidthVariant(320)
                .addWidthVariant(640);
        if (StringUtils.isNotBlank(location)) {
            store.specificLocation(location);
        }
        return store.store();
    }

    @POST
    @Path("/upload/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadFile(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = fileUpload.uploadedFile().toFile();
        MediaService.FileStore store = mediaService.storeFile(file, fileUpload.fileName());
        if (StringUtils.isNotBlank(location)) {
            store.specificLocation(location);
        }
        return store.store();
    }

    @POST
    @Path("/upload/file/secured")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadSecuredFile(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = fileUpload.uploadedFile().toFile();
        MediaService.FileStore store = mediaService.storeSecuredFile(file, fileUpload.fileName());
        if (StringUtils.isNotBlank(location)) {
            store.specificLocation(location);
        }
        return store.store();
    }

    @DELETE
    @Path("/media/{fileId}")
    public void deleteMedia(@PathParam("fileId") String fileId) {
        mediaService.removeMedia(fileId);
    }

    @DELETE
    @Path("/media/secured/{fileId}")
    public void deleteSecuredMedia(@PathParam("fileId") String fileId) {
        mediaService.removeSecuredMedia(fileId);
    }
}
