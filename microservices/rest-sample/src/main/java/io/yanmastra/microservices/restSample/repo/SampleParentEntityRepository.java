package io.yanmastra.microservices.restSample.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.commonClass.utils.CrudQueryFilterUtils;
import io.yanmastra.microservices.common.crud.Paginate;
import io.yanmastra.microservices.restSample.entity.SampleChildEntity;
import io.yanmastra.microservices.restSample.entity.SampleChildOfChildEntity;
import io.yanmastra.microservices.restSample.entity.SampleParentEntity;
import io.yanmastra.microservices.restSample.json.SampleParentSummaryJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.hibernate.query.Page;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SampleParentEntityRepository implements PanacheRepositoryBase<SampleParentEntity, String> {

    @Inject
    EntityManager entityManager;
    @Inject
    Logger log;

    @Inject
    CrudQueryFilterUtils queryFilterUtils;

    public Paginate<SampleParentSummaryJson> getParentSummary(Page page) {

        Map<String, Object> sqlParams = new HashMap<>();
        String whereClause = queryFilterUtils.getQueryWhereClause(sqlParams, "P.");

        String sql = "select new "+SampleParentSummaryJson.class.getName()+"("+
                "P.id, P.name, " +
                "(select count(*) from "+ SampleChildEntity.class.getSimpleName() + " C where C.parent.id = P.id and C.deletedAt is null), " +
                "(select count(*) from " + SampleChildOfChildEntity.class.getSimpleName() + " CC where CC.parent.parent.id = P.id and CC.deletedAt is null), " +
                "(select sum(CC.amount) from " + SampleChildOfChildEntity.class.getSimpleName() + " CC where CC.parent.parent.id = P.id and CC.deletedAt is null)) " +
                "from " + SampleParentEntity.class.getSimpleName() + " P where " +
                whereClause + " order by P.createdAt DESC";

        log.debug("executing:"+sql);
        long count = count("from " + SampleParentEntity.class.getSimpleName() + " P where " + whereClause, sqlParams);

        long first = Math.min(page.getFirstResult(), count);
        Paginate<SampleParentSummaryJson> paginate = new Paginate<>();
        paginate.setTotalData(count);
        paginate.setCurrentPage(page.getNumber()+1);
        paginate.setSize(page.getSize());

        TypedQuery<SampleParentSummaryJson> query = entityManager.createQuery(sql, SampleParentSummaryJson.class);
        for (String key: sqlParams.keySet()) {
            query.setParameter(key, sqlParams.get(key));
        }

        List<SampleParentSummaryJson> data = query.setMaxResults(page.getSize())
                .setFirstResult((int) first)
                .getResultList();

        paginate.setData(data);
        return paginate;
    }
}
