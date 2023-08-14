package io.github.survival1sm.lace.service;

import com.google.common.collect.ImmutableMap;
import com.intuit.graphql.orchestrator.ServiceProvider;
import graphql.ExecutionInput;
import graphql.GraphQLContext;
import io.github.survival1sm.lace.dataobject.GraphQlRequest;
import io.github.survival1sm.lace.errors.QueryRetrievalException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OAuth2GraphQlService implements ServiceProvider {

  public OAuth2GraphQlService(WebClient.Builder loadBalancedWebClient, ObjectProvider<ReactiveOAuth2AuthorizedClientManager> clientManagerProvider, String serviceId, String serviceSchema, String endpointUrl) {
    this.loadBalancedWebClient = loadBalancedWebClient;
    this.clientManagerProvider = clientManagerProvider;
    this.serviceId = serviceId;
    this.serviceSchema = serviceSchema;
    this.endpointUrl = endpointUrl;
  }

  private final WebClient.Builder loadBalancedWebClient;
  private final ObjectProvider<ReactiveOAuth2AuthorizedClientManager> clientManagerProvider;
  private final String serviceId;
  private final String serviceSchema;
  private final String endpointUrl;

  @Override
  public String getNameSpace() {
    return this.serviceId.toUpperCase();
  }

  @Override
  public Map<String, String> sdlFiles() {
    return ImmutableMap.of("schema.graphqls", serviceSchema);
  }

  @Override
  public CompletableFuture<Map<String, Object>> query(
      ExecutionInput executionInput, GraphQLContext context) {
    OAuth2AuthenticationToken authToken = context.get(OAuth2AuthenticationToken.class);
    ServerWebExchange exchange = context.get(ServerWebExchange.class);

    return this.getAccessTokenFromAuthentication(exchange, authToken)
        .flatMap(
            token ->
                loadBalancedWebClient
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .post()
                    .uri("http://%s%s".formatted(serviceId, endpointUrl))
                    .header("Authorization", "Bearer %s".formatted(token))
                    .bodyValue(
                        new GraphQlRequest(
                            executionInput.getQuery(),
                            executionInput.getOperationName(),
                            executionInput.getVariables()))
                    .retrieve()
                        .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new QueryRetrievalException(body))))
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .mapNotNull(HttpEntity::getBody))
        .toFuture();
  }

  public Mono<String> getAccessTokenFromAuthentication(
      ServerWebExchange exchange, OAuth2AuthenticationToken oauth2AuthenticationToken) {
    return getAuthorizedClient(exchange, oauth2AuthenticationToken)
        .map(oauth2AuthorizedClient -> oauth2AuthorizedClient.getAccessToken().getTokenValue());
  }

  private Mono<OAuth2AuthorizedClient> getAuthorizedClient(
      ServerWebExchange exchange, OAuth2AuthenticationToken oauth2Authentication) {
    String clientRegistrationId = oauth2Authentication.getAuthorizedClientRegistrationId();
    OAuth2AuthorizeRequest request =
        OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
            .principal(oauth2Authentication)
            .attribute(ServerWebExchange.class.getName(), exchange)
            .build();
    ReactiveOAuth2AuthorizedClientManager clientManager = clientManagerProvider.getIfAvailable();
    if (clientManager == null) {
      return Mono.error(
          new IllegalStateException(
              "No ReactiveOAuth2AuthorizedClientManager bean was found. Did you include the "
                  + "org.springframework.boot:spring-boot-starter-oauth2-client dependency?"));
    }

    return clientManager.authorize(request);
  }
}
