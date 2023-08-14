package io.github.survival1sm.lace.annotations;

import io.github.survival1sm.lace.service.factory.impl.SwaggerServiceFactory;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(ConditionalOnSwaggerEnabled.OnSwaggerEnabledCondition.class)
public @interface ConditionalOnSwaggerEnabled {

  /** Verifies multiple conditions to see if Consul should be enabled. */
  class OnSwaggerEnabledCondition extends AllNestedConditions {

    OnSwaggerEnabledCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    /** Consul property is enabled. */
    @ConditionalOnProperty(value = "graphql.lace.service-factory.swagger.enabled")
    static class FoundProperty {}

    /** Consul client class found. */
    @ConditionalOnClass(SwaggerServiceFactory.class)
    static class FoundClass {}
  }
}
