package io.yanmastra.microservices.common.reactive;

import io.yanmastra.authorization.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BeanProvider {

    @ApplicationScoped
    public ObjectMapper provideObjectMapper() {
        return JsonUtils.getObjectMapper();
    }
}
