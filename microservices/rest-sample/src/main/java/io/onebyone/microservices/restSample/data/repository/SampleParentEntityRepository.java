package io.onebyone.microservices.restSample.data.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.onebyone.microservices.restSample.data.entity.SampleChildEntity;
import io.onebyone.microservices.restSample.data.entity.SampleChildOfChildEntity;
import io.onebyone.microservices.restSample.data.entity.SampleParentEntity;
import io.onebyone.microservices.restSample.dto.SampleParentSummaryDto;
import io.onebyone.quarkus.microservices.common.crud.Paginate;
import io.onebyone.quarkus.microservices.common.utils.CrudQueryFilterUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.MultivaluedMap;
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

    public Paginate<SampleParentSummaryDto> getParentSummary(Page page, MultivaluedMap<String, String> requestQueries) {

        Map<String, Object> sqlParams = new HashMap<>();
        String whereClause = CrudQueryFilterUtils.getQueryWhereClause(requestQueries, sqlParams, "P.");
        log.debug("whereClause:"+whereClause);

        String sql = "select new "+ SampleParentSummaryDto.class.getName()+"("+
                "P.id, P.name, " +
                "(select count(*) from "+ SampleChildEntity.class.getSimpleName() + " C where C.parent.id = P.id and C.deletedAt is null), " +
                "(select count(*) from " + SampleChildOfChildEntity.class.getSimpleName() + " CC where CC.parent.parent.id = P.id and CC.deletedAt is null), " +
                "(select sum(CC.amount) from " + SampleChildOfChildEntity.class.getSimpleName() + " CC where CC.parent.parent.id = P.id and CC.deletedAt is null)) " +
                "from " + SampleParentEntity.class.getSimpleName() + " P where " +
                whereClause + " order by P.createdAt DESC";

        log.debug("executing:"+sql);
        long count = count("from " + SampleParentEntity.class.getSimpleName() + " P where " + whereClause, sqlParams);

        long first = Math.min(page.getFirstResult(), count);
        Paginate<SampleParentSummaryDto> paginate = new Paginate<>();
        paginate.setTotalData(count);
        paginate.setCurrentPage(page.getNumber()+1);
        paginate.setSize(page.getSize());

        TypedQuery<SampleParentSummaryDto> query = entityManager.createQuery(sql, SampleParentSummaryDto.class);
        for (String key: sqlParams.keySet()) {
            query.setParameter(key, sqlParams.get(key));
        }

        List<SampleParentSummaryDto> data = query.setMaxResults(page.getSize())
                .setFirstResult((int) first)
                .getResultList();

        paginate.setData(data);
        return paginate;
    }
}
