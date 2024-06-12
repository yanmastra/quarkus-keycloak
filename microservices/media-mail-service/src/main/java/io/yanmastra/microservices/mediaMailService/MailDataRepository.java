package io.yanmastra.microservices.mediaMailService;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MailDataRepository implements PanacheRepositoryBase<MyMailData, String> {
}
