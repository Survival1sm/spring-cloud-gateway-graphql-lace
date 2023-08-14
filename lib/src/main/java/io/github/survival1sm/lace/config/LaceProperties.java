package io.github.survival1sm.lace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "graphql.lace", ignoreUnknownFields = false)
public class LaceProperties {

    private String endpoint;
    private Long updateIntervalMs;

    private String conflictResolutionStrategy;

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public void setServiceFactory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    private ServiceFactory serviceFactory;

    private static class ServiceFactory {
        private MetadataServiceFactoryConfiguration metadata;
        private SwaggerServiceFactoryConfiguration swagger;

        public MetadataServiceFactoryConfiguration getMetadata() {
            return metadata;
        }

        public void setMetadata(MetadataServiceFactoryConfiguration metadata) {
            this.metadata = metadata;
        }

        public SwaggerServiceFactoryConfiguration getSwagger() {
            return swagger;
        }

        public void setSwagger(SwaggerServiceFactoryConfiguration swagger) {
            this.swagger = swagger;
        }
    }

    private static class MetadataServiceFactoryConfiguration {
        private Boolean enabled;
        private String graphqlPath;
        private String key;
        private String value;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getGraphqlPath() {
            return graphqlPath;
        }

        public void setGraphqlPath(String graphqlPath) {
            this.graphqlPath = graphqlPath;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static class SwaggerServiceFactoryConfiguration {
        private Boolean enabled;
        private String graphqlPath;
        private String apiDocsPath;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getGraphqlPath() {
            return graphqlPath;
        }

        public void setGraphqlPath(String graphqlPath) {
            this.graphqlPath = graphqlPath;
        }

        public String getApiDocsPath() {
            return apiDocsPath;
        }

        public void setApiDocsPath(String apiDocsPath) {
            this.apiDocsPath = apiDocsPath;
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Long getUpdateIntervalMs() {
        return updateIntervalMs;
    }

    public void setUpdateIntervalMs(Long updateIntervalMs) {
        this.updateIntervalMs = updateIntervalMs;
    }

    public String getConflictResolutionStrategy() {
        return conflictResolutionStrategy;
    }

    public void setConflictResolutionStrategy(String conflictResolutionStrategy) {
        this.conflictResolutionStrategy = conflictResolutionStrategy;
    }
}
