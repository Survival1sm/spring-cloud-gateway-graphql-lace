package io.github.survival1sm.lace.conflicts;

import io.github.survival1sm.lace.conflicts.impl.AllFields;
import io.github.survival1sm.lace.conflicts.impl.DuplicateFields;
import io.github.survival1sm.lace.conflicts.impl.None;

import java.util.HashMap;
import java.util.Map;

public class ConflictResolutionManager {

  private final Map<String, ConflictResolutionStrategy> resolutionStrategyMap = new HashMap<>();

  public ConflictResolutionManager() {
    addConflictResolutionStrategy(new AllFields());
    addConflictResolutionStrategy(new DuplicateFields());
    addConflictResolutionStrategy(new None());
  }

  public void addConflictResolutionStrategy(ConflictResolutionStrategy conflictResolutionStrategy) {
    resolutionStrategyMap.put(
        conflictResolutionStrategy.getClass().getSimpleName(), conflictResolutionStrategy);
  }

  public ConflictResolutionStrategy getConflictResolutionStrategy(String className) {
    return resolutionStrategyMap.get(className);
  }
}
