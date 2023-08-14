package io.github.survival1sm.lace.service.factory.impl;

import com.intuit.graphql.orchestrator.ServiceProvider;
import io.github.survival1sm.lace.annotations.ConditionalOnSwaggerEnabled;
import io.github.survival1sm.lace.conflicts.ConflictResolutionStrategy;
import io.github.survival1sm.lace.dataobject.GraphQlRequest;
import io.github.survival1sm.lace.errors.IntrospectionRetrievalException;
import io.github.survival1sm.lace.errors.SwaggerRetrievalException;
import io.github.survival1sm.lace.service.OAuth2GraphQlService;
import io.github.survival1sm.lace.service.factory.ServiceFactory;
import io.github.survival1sm.lace.util.SchemaTransformUtil;
import io.github.survival1sm.lace.util.Util;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.github.survival1sm.lace.config.Constants.INTROSPECTION_QUERY;

@Component
@ConditionalOnSwaggerEnabled
public class SwaggerServiceFactory extends ServiceFactory {

  private final Map<String, ServiceProvider> serviceProviderMap = new ConcurrentHashMap<>();

  @Value("${graphql.lace.service-factory.swagger.api-docs-path:/v3/api-docs}")
  private String apiDocsPath;

  @Value("${graphql.lace.service-factory.swagger.graphql-path:/graphql}")
  private String graphQlPath;

  private final ObjectProvider<ReactiveOAuth2AuthorizedClientManager> clientManagerProvider;

  public SwaggerServiceFactory(
      RouteLocator routeLocator,
      WebClient.Builder loadBalancedWebClient,
      ObjectProvider<ReactiveOAuth2AuthorizedClientManager> clientManagerProvider) {
    super(routeLocator, loadBalancedWebClient);
    this.clientManagerProvider = clientManagerProvider;
  }

  private Mono<JSONObject> fetchSwaggerDefinition(String serviceId) {
    return loadBalancedWebClient
        .build()
        .get()
        .uri("http://%s%s".formatted(serviceId, apiDocsPath))
        .retrieve()
            .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new SwaggerRetrievalException(body))))
        .toEntity(JSONObject.class)
        .mapNotNull(HttpEntity::getBody);
  }

  @Override
  public List<ServiceProvider> getServiceProviderList() {
    return this.serviceProviderMap.values().stream().toList();
  }

  @Override
  @PostConstruct
  protected void loadServiceProviderMap() {
    routeLocator
        .getRoutes()
        .map(route -> route.getId().substring(route.getId().indexOf("_") + 1).toLowerCase())
        .filter(serviceId -> !serviceIgnoreList.contains(serviceId))
        .flatMap(
            serviceId ->
                fetchSwaggerDefinition(serviceId)
                    .map(
                        swaggerDef ->
                            new AbstractMap.SimpleImmutableEntry<>(serviceId, swaggerDef)))
        .filter(entry -> entry.getValue().get("paths") instanceof LinkedHashMap<?, ?>)
        .flatMap(
            entry -> {
              LinkedHashMap<?, ?> pathMap = (LinkedHashMap<?, ?>) entry.getValue().get("paths");
              return Flux.fromStream(Util.nullSafeStream(pathMap.keySet()))
                  .filter(
                      path ->
                          path instanceof String stringPath && (stringPath.contains(graphQlPath)))
                  .flatMap(
                      path ->
                          fetchServiceSchema(entry.getKey(), path.toString())
                              .map(
                                  schema ->
                                      new AbstractMap.SimpleImmutableEntry<>(
                                          entry.getKey(),
                                          new AbstractMap.SimpleImmutableEntry<>(
                                              path.toString(), schema)))
                              .flux());
            })
        .collectMap(
            AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue)
        .doOnNext(
            map -> {
              Map<String, JSONObject> schemaMap =
                  Util.nullSafeStream(map.entrySet())
                      .collect(Collectors.toMap(Map.Entry::getKey, k -> k.getValue().getValue()));

              Map<String, String> sdlMap =
                  fetchServiceToSdlMap(
                      conflictResolutionManager.getConflictResolutionStrategy(
                          conflictResolutionStrategy),
                      schemaMap);

              sdlMap.forEach(
                  (serviceId, sdl) ->
                      serviceProviderMap.put(
                          serviceId,
                          new OAuth2GraphQlService(
                              loadBalancedWebClient,
                              clientManagerProvider,
                              serviceId,
                              sdl,
                              map.get(serviceId).getKey())));
            })
        .subscribe();
  }

  @Override
  protected Map<String, String> fetchServiceToSdlMap(
          ConflictResolutionStrategy delegate, Map<String, JSONObject> schemaMap) {
    return Util.nullSafeStream(delegate.apply(schemaMap).entrySet())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> SchemaTransformUtil.schemaPrinter.print(entry.getValue())));
  }

  @Override
  @Scheduled(fixedDelayString = "${graphql.lace.update-interval-ms:30000}", initialDelayString = "${graphql.lace.update-interval-ms:30000}")
  protected void updateServiceProviderMap() {
    this.loadServiceProviderMap();
  }

  @Override
  protected Mono<JSONObject> fetchServiceSchema(String serviceId, String requestPath) {
    return loadBalancedWebClient
        .build()
        .post()
        .uri("http://%s%s".formatted(serviceId, requestPath))
        .headers(httpHeaders -> httpHeaders.addAll(introspectionRequestHeaderMap))
        .cookies(cookies -> cookies.addAll(introspectionRequestCookieMap))
        .bodyValue(new GraphQlRequest(INTROSPECTION_QUERY, "IntrospectionQuery", null))
        .retrieve()
            .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new IntrospectionRetrievalException(body))))
        .toEntity(JSONObject.class)
        .mapNotNull(HttpEntity::getBody);
  }
}
