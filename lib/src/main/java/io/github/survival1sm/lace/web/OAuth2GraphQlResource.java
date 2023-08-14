package io.github.survival1sm.lace.web;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import io.github.survival1sm.lace.dataobject.GraphQlRequest;
import io.github.survival1sm.lace.orchestrator.OrchestratorManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Graph ql resource. */
@RestController
@RequestMapping("${graphql.lace.endpoint:/graphql}")
public class OAuth2GraphQlResource {

  private final OrchestratorManager orchestratorManager;

  public OAuth2GraphQlResource(OrchestratorManager orchestratorManager) {
    this.orchestratorManager = orchestratorManager;
  }

  @PostMapping
  Mono<Map<String, Object>> executeOrchestratedQuery(
      ServerWebExchange exchange,
      OAuth2AuthenticationToken oAuth2AuthenticationToken,
      @RequestBody GraphQlRequest query) {
    CompletableFuture<ExecutionResult> execute =
        orchestratorManager
            .getOrchestrator()
            .execute(
                ExecutionInput.newExecutionInput()
                    .query(query.query())
                    .graphQLContext(
                        Map.of(
                            ServerWebExchange.class,
                            exchange,
                            OAuth2AuthenticationToken.class,
                            oAuth2AuthenticationToken))
                    .build());

    return Mono.fromFuture(execute.thenApply(ExecutionResult::toSpecification));
  }
}
