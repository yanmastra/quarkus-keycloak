package io.yanmastra.quarkus.mediafilemanager;

import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface MediaUtils {
    String DEFAULT_FILE = "default_file";
    String FILE_ID = "file_id";
    String META_PATH = "/meta";
    String BASE_MEDIA_PATH = "/media";
    String BASE_SECURED_MEDIA_PATH = "/secured-media";


    static Response createFileResponse(ByteArrayOutputStream outputStream, String fileName, boolean isDownload) {
        byte[] data = outputStream.toByteArray();
        Response.ResponseBuilder builder = Response.ok()
                .entity(data)
                .header(HttpHeaders.CONTENT_LENGTH, data.length)
                .type(getMediaTypeOfFileName(fileName));

        if (isDownload) {
            builder = builder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+fileName+"\"")
                    .header(HttpHeaders.CACHE_CONTROL, "private");
        }
        return builder.build();
    }

    static void createFileResponse(ContainerResponseContext response, String fileName, boolean isDownload) throws IOException {
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, getMediaTypeOfFileName(fileName));

        if (isDownload) {
            response.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+fileName+"\"");
            response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "private");
        }
    }

    static MediaType getMediaTypeOfFileName(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        return switch (suffix) {
            case "CSV","csv" -> MediaType.valueOf("text/csv");
            case "PDF","pdf" -> MediaType.valueOf("application/pdf");
            case "HTML","html" -> MediaType.TEXT_HTML_TYPE;
            case "PNG","png" -> MediaType.valueOf("image/png");
            case "JPG","jpg" -> MediaType.valueOf("image/jpg");
            case "JPEG","jpeg", "jfif" -> MediaType.valueOf("image/jpeg");
            case "MP4","mp4" -> MediaType.valueOf("video/mp4");
            case "MPEG","mpeg" -> MediaType.valueOf("audio/mp4");
            case "doc", "DOC" -> MediaType.valueOf("application/msword");
            case "xls", "XLS" -> MediaType.valueOf("application/vnd.ms-excel");
            case "ppt", "PPT" -> MediaType.valueOf("application/vnd.ms-powerpoint");
            case "docx", "xlsx", "pptx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument");
            case "gif" -> MediaType.valueOf("image/gif");
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "webp" -> MediaType.valueOf("image/webp");
            case "svg" -> MediaType.valueOf("image/svg+xml");
            case "tiff", "tif" -> MediaType.valueOf("image/tiff");
            case "ico" -> MediaType.valueOf("image/x-icon");
            case "heif" -> MediaType.valueOf("image/heif");
            case "heic" -> MediaType.valueOf("image/heic");
            case "pjpeg", "pjp" -> MediaType.valueOf("image/pjpeg");
            case "apng" -> MediaType.valueOf("image/apng");
            case "avif" -> MediaType.valueOf("image/avif");
            default -> MediaType.APPLICATION_OCTET_STREAM_TYPE;
        };
    }
}
