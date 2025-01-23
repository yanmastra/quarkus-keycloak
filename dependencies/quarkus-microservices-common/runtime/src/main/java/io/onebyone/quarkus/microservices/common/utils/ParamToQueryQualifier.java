package io.onebyone.quarkus.microservices.common.utils;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author Wayan Mastra
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface ParamToQueryQualifier {

    String operator();

    class Literal extends AnnotationLiteral<ParamToQueryQualifier> implements ParamToQueryQualifier {
        String operator;

        public Literal(String operator) {
            this.operator = operator;
        }

        public static Literal of(String operator) {
            return new Literal(operator);
        }

        @Override
        public String operator() {
            return operator;
        }

        public String value() {
            return operator;
        }
    }
}
