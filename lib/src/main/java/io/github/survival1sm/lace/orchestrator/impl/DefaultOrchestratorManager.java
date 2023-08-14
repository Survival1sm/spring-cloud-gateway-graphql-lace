package io.github.survival1sm.lace.orchestrator.impl;

import com.intuit.graphql.orchestrator.GraphQLOrchestrator;
import com.intuit.graphql.orchestrator.schema.RuntimeGraph;
import com.intuit.graphql.orchestrator.stitching.SchemaStitcher;
import io.github.survival1sm.lace.orchestrator.OrchestratorManager;
import io.github.survival1sm.lace.service.factory.ServiceFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrchestratorManager implements OrchestratorManager {

  public DefaultOrchestratorManager(ServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  private final ServiceFactory serviceFactory;

  @Override
  public GraphQLOrchestrator getOrchestrator() {

    RuntimeGraph runtimeGraph =
        SchemaStitcher.newBuilder()
            .services(serviceFactory.getServiceProviderList())
            .build()
            .stitchGraph();

    return GraphQLOrchestrator.newOrchestrator().runtimeGraph(runtimeGraph).build();
  }
}
