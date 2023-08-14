package io.github.survival1sm.lace.orchestrator;

import com.intuit.graphql.orchestrator.GraphQLOrchestrator;
import org.springframework.stereotype.Component;

@Component
public interface OrchestratorManager {

  GraphQLOrchestrator getOrchestrator();
}
