package io.github.survival1sm.lace.conflicts;

import graphql.language.Document;
import net.minidev.json.JSONObject;

import java.util.Map;

public interface ConflictResolutionStrategy {

  Map<String, Document> apply(Map<String, JSONObject> schemas);
}
