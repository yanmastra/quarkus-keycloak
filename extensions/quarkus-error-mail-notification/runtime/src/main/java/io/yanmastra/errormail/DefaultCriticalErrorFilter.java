package io.yanmastra.errormail;

import io.yanmastra.quarkusBase.event.ErrorEvent;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.arc.DefaultBean;

/**
 * Default filter that considers all 500 status errors as critical.
 * Services can override this by providing their own CDI bean implementing CriticalErrorFilter.
 */
@DefaultBean
@ApplicationScoped
public class DefaultCriticalErrorFilter implements CriticalErrorFilter {

    @Override
    public boolean isCritical(ErrorEvent event) {
        return event.getStatusCode() >= 500;
    }
}
