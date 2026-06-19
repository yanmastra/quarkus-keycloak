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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        File file = withExtension(fileUpload);
        try {
            MediaService.ImageStore store = mediaService.storeImage(file)
                    .addWidthVariant(320)
                    .addWidthVariant(640);
            if (StringUtils.isNotBlank(location)) {
                store.specificLocation(location);
            }
            return store.store();
        } finally {
            file.delete();
        }
    }

    @POST
    @Path("/upload/image/secured")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadSecuredImage(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = withExtension(fileUpload);
        try {
            MediaService.ImageStore store = mediaService.storeSecuredImage(file)
                    .addWidthVariant(320)
                    .addWidthVariant(640);
            if (StringUtils.isNotBlank(location)) {
                store.specificLocation(location);
            }
            return store.store();
        } finally {
            file.delete();
        }
    }

    @POST
    @Path("/upload/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadFile(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = withExtension(fileUpload);
        try {
            MediaService.FileStore store = mediaService.storeFile(file);
            if (StringUtils.isNotBlank(location)) {
                store.specificLocation(location);
            }
            return store.store();
        } finally {
            file.delete();
        }
    }

    @POST
    @Path("/upload/file/secured")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> uploadSecuredFile(
            @RestForm("file") FileUpload fileUpload,
            @RestForm("location") String location
    ) {
        File file = withExtension(fileUpload);
        try {
            MediaService.FileStore store = mediaService.storeSecuredFile(file);
            if (StringUtils.isNotBlank(location)) {
                store.specificLocation(location);
            }
            return store.store();
        } finally {
            file.delete();
        }
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

    /**
     * Copies the uploaded temp file to a new temp file that preserves the original filename extension,
     * since Quarkus saves multipart uploads without the original extension.
     */
    private static File withExtension(FileUpload fileUpload) {
        String originalName = fileUpload.fileName();
        String ext = "";
        if (StringUtils.isNotBlank(originalName) && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        try {
            java.nio.file.Path tmp = Files.createTempFile("upload-", ext);
            Files.copy(fileUpload.uploadedFile(), tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to stage uploaded file", e);
        }
    }
}
