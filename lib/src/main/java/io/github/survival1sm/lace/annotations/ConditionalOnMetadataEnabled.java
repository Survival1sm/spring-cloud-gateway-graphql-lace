package io.github.survival1sm.lace.annotations;

import io.github.survival1sm.lace.service.factory.impl.MetadataServiceFactory;
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
@Conditional(ConditionalOnMetadataEnabled.OnMetadataEnabledCondition.class)
public @interface ConditionalOnMetadataEnabled {

  /** Verifies multiple conditions to see if Consul should be enabled. */
  class OnMetadataEnabledCondition extends AllNestedConditions {

    OnMetadataEnabledCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    /** Consul property is enabled. */
    @ConditionalOnProperty(value = "graphql.lace.service-factory.metadata.enabled")
    static class FoundProperty {}

    /** Consul client class found. */
    @ConditionalOnClass(MetadataServiceFactory.class)
    static class FoundClass {}
  }
}
