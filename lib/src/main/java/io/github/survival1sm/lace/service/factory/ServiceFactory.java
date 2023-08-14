package io.github.survival1sm.lace.service.factory;

import com.intuit.graphql.orchestrator.ServiceProvider;
import io.github.survival1sm.lace.conflicts.ConflictResolutionManager;
import io.github.survival1sm.lace.conflicts.ConflictResolutionStrategy;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ServiceFactory {

  public ServiceFactory(RouteLocator routeLocator, WebClient.Builder loadBalancedWebClient) {
    this.routeLocator = routeLocator;
    this.loadBalancedWebClient = loadBalancedWebClient;
  }

  @Value("${graphql.lace.conflict-resolution-strategy:DuplicateFields}")
  protected String conflictResolutionStrategy;

  protected final List<String> serviceIgnoreList = new ArrayList<>();
  protected MultiValueMap<String, String> introspectionRequestHeaderMap = new MultiValueMapAdapter<>(new HashMap<>());
  protected MultiValueMap<String, String> introspectionRequestCookieMap = new MultiValueMapAdapter<>(new HashMap<>());

  protected final RouteLocator routeLocator;
  protected WebClient.Builder loadBalancedWebClient;
  protected final ConflictResolutionManager conflictResolutionManager =
      new ConflictResolutionManager();

  public abstract List<ServiceProvider> getServiceProviderList();

  public void addServicesToIgnoreList(String... serviceIds) {
    this.serviceIgnoreList.addAll(Arrays.asList(serviceIds));
  }

  public void addIntrospectionRequestHeader(String key, String value) {
    this.introspectionRequestHeaderMap.add(key, value);
  }

  public void addIntrospectionRequestCookie(String key, String value) {
    this.introspectionRequestCookieMap.add(key, value);
  }

  protected abstract Map<String, String> fetchServiceToSdlMap(
          ConflictResolutionStrategy delegate, Map<String, JSONObject> schemaMa);

  protected abstract void loadServiceProviderMap();

  protected abstract void updateServiceProviderMap();

  protected abstract Mono<JSONObject> fetchServiceSchema(String serviceId, String requestPath);
}
