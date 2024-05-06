package io.yanmastra.microservices.restSample.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.yanmastra.microservices.common.crud.Paginate;
import io.yanmastra.microservices.restSample.entity.SampleChildEntity;
import io.yanmastra.microservices.restSample.entity.SampleChildOfChildEntity;
import io.yanmastra.microservices.restSample.entity.SampleParentEntity;
import io.yanmastra.microservices.restSample.json.SampleParentSummaryJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.query.Page;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class SampleParentEntityRepository implements PanacheRepositoryBase<SampleParentEntity, String> {

    @Inject
    EntityManager entityManager;
    @Inject
    Logger log;

    public Paginate<SampleParentSummaryJson> getParentSummary(Page page) {
        String sql = "select new "+SampleParentSummaryJson.class.getName()+"("+
                "P.id, P.name, " +
                "(select count(*) from "+ SampleChildEntity.class.getSimpleName() + " C where C.parent.id = P.id and C.deletedAt is null), " +
                "(select count(*) from " + SampleChildOfChildEntity.class.getSimpleName() + " CC where CC.parent.parent.id = P.id and CC.deletedAt is null), " +
                "(select sum(CC.amount) from " + SampleChildOfChildEntity.class.getSimpleName() + " CC where CC.parent.parent.id = P.id and CC.deletedAt is null)) " +
                "from " + SampleParentEntity.class.getSimpleName() + " P where P.deletedAt is null order by P.createdAt DESC";

//        sql = "select new "+SampleParentSummaryJson.class.getName()+"("+
//                "P.id, P.name, " +
//                "(select count(*) from "+SampleChildEntity.class.getSimpleName() + " C where C.parent = P and C.deletedAt is null) as cCount, " +
//                "count(CC), " +
//                "sum(CC.amount)) " +
//                "from " + SampleParentEntity.class.getSimpleName() + " P " +
////                "left join " + SampleChildEntity.class.getSimpleName() + " C on (C.parent = P) " +
//                "left join " + SampleChildOfChildEntity.class.getSimpleName() + " CC on (CC.parent.parent = P) " +
//                "where (P.deletedAt is null and CC.deletedAt is null) " +
//                "group by P.id " +
//                "order by P.createdAt DESC";

        long count = findAll().filter("deletedAppFilter", Parameters.with("isDeleted", false))
                .count();

        long first = Math.min(page.getFirstResult(), count);

        log.warn("executing:"+sql);
        Paginate<SampleParentSummaryJson> paginate = new Paginate<>();
        paginate.setTotalData(count);
        paginate.setCurrentPage(page.getNumber()+1);
        paginate.setSize(page.getSize());

        List<SampleParentSummaryJson> data = entityManager.createQuery(sql, SampleParentSummaryJson.class)
                .setMaxResults(page.getSize())
                .setFirstResult((int) first)
                .getResultList();

        paginate.setData(data);
        return paginate;
    }
}
