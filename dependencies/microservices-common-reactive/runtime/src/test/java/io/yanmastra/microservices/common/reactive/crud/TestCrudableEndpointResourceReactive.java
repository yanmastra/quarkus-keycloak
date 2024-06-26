package io.yanmastra.microservices.common.reactive.crud;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.common.constraint.Assert;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestCrudableEndpointResourceReactive {

    @Test
    public void testSearchQueryBuilder() {
        CrudableEndpointResourceReactive<SimpleEntity, SimpleEntity> crudableEndpointResourceReactive = new CrudableEndpointResourceReactive<>() {
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
            protected Uni<SimpleEntity> update(SimpleEntity entity, SimpleEntity simpleEntity) {
                return null;
            }

            @Override
            protected Set<String> searchAbleColumn() {
                return Set.of("name", "phone");
            }
        };

        Map<String, Object> queryParams = new HashMap<>();
        String query = crudableEndpointResourceReactive.createFilterQuery("something", Map.of(
                "name", "something1",
                "phone", "something2"
        ), queryParams);

        System.out.println(query);
        Assert.assertTrue(query.equals("where (name=:keyword or phone=:keyword) and name=:name and phone=:phone") ||
                query.equals("where (phone=:keyword or name=:keyword) and name=:name and phone=:phone") ||
                query.equals("where (phone=:keyword or name=:keyword) and phone=:phone and name=:name") ||
                query.equals("where (name=:keyword or phone=:keyword) and phone=:phone and name=:name"));

        query = crudableEndpointResourceReactive.createFilterQuery("", Map.of(
                "name", "something1",
                "phone", "something2"
        ), queryParams);

        System.out.println(query);
        Assert.assertTrue(query.equals("where name=:name and phone=:phone") || query.equals("where phone=:phone and name=:name"));

        query = crudableEndpointResourceReactive.createFilterQuery("something", new HashMap<>(), queryParams);
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
