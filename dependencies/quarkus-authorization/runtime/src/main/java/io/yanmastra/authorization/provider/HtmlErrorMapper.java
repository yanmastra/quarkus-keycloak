package io.yanmastra.authorization.provider;

import jakarta.ws.rs.core.Response;

public interface HtmlErrorMapper {
    Response getResponse(Throwable e);
}
