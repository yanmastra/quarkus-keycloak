package io.yanmastra.errormail;

import io.yanmastra.quarkusBase.event.ErrorEvent;

/**
 * Implement this interface to customize which errors are considered critical
 * and should trigger an email notification.
 *
 * If no custom implementation is provided, the default filter treats
 * all 500 (Internal Server Error) responses as critical.
 */
public interface CriticalErrorFilter {

    /**
     * Determine whether the given error event is critical enough to send an email.
     *
     * @param event the error event from ErrorMapper
     * @return true if an email notification should be sent
     */
    boolean isCritical(ErrorEvent event);
}
