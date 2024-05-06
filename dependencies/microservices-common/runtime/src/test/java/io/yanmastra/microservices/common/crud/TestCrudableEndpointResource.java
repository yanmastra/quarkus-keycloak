package io.yanmastra.microservices.common.crud;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.common.constraint.Assert;
import io.yanmastra.commonClass.utils.CrudQueryFilterUtils;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@QuarkusTest
public class TestCrudableEndpointResource {


    @Test
    public void testSearchQueryBuilder() {
        CrudQueryFilterUtils crudQueryFilterUtils = new CrudQueryFilterUtils();

        CrudableEndpointResource<SimpleEntity, SimpleEntity> crudableEndpointResourceReactive = new CrudableEndpointResource<>() {
            @Override
            protected PanacheRepositoryBase<SimpleEntity, String> getRepository() {
                return null;
            }

            @Override
            protected SimpleEntity fromEntity(SimpleEntity entity) {
                return null;
            }

            @Override
            protected SimpleEntity toEntity(SimpleEntity simpleEntity) {
                return null;
            }

            @Override
            protected SimpleEntity update(SimpleEntity entity, SimpleEntity simpleEntity) {
                return null;
            }

            @Override
            protected Set<String> searchAbleColumn() {
                return Set.of("name", "phone");
            }
        };

        Map<String, Object> queryParams = new HashMap<>();
        MultivaluedMap<String,String> params = new MultivaluedHashMap<>();
        params.add("keyword", "something");
        params.add("phone", "something1");
        params.add("name", "something2");
        String query = crudQueryFilterUtils.createFilterQuery(params, queryParams, crudableEndpointResourceReactive.searchAbleColumn());

        System.out.println(query);
        Assert.assertTrue(query.equals("where (name=:keyword or phone=:keyword) and name=:name and phone=:phone") ||
                query.equals("where (phone=:keyword or name=:keyword) and name=:name and phone=:phone") ||
                query.equals("where (phone=:keyword or name=:keyword) and phone=:phone and name=:name") ||
                query.equals("where (name=:keyword or phone=:keyword) and phone=:phone and name=:name"));

        params = new MultivaluedHashMap<>(params);
        params.add("phone", "something1");
        params.add("name", "something2");
        query = crudQueryFilterUtils.createFilterQuery(params, queryParams, crudableEndpointResourceReactive.searchAbleColumn());

        System.out.println(query);
        Assert.assertTrue(query.equals("where name=:name and phone=:phone") || query.equals("where phone=:phone and name=:name"));

        params = new MultivaluedHashMap<>(params);
        params.add("phone", "something1");
        params.add("name", "something2");
        query = crudQueryFilterUtils.createFilterQuery(params, queryParams, crudableEndpointResourceReactive.searchAbleColumn());
        System.out.println(query);
        Assert.assertTrue(query.equals("where (name=:keyword or phone=:keyword)") || query.equals("where (phone=:keyword or name=:keyword)"));
    }

    static class SimpleEntity extends CrudableEntity {
        String id;
        String name;
        String phone;

        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }
}
