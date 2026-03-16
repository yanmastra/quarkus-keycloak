package io.yanmastra.quarkusBase.provider;

import jakarta.ws.rs.core.Response;

public interface HtmlErrorMapper {
    Response getResponse(Throwable e);
}
