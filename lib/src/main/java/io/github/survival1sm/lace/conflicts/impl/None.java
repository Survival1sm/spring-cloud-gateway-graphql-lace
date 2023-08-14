package io.github.survival1sm.lace.conflicts.impl;

import graphql.ExecutionResultImpl;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import io.github.survival1sm.lace.conflicts.ConflictResolutionStrategy;
import io.github.survival1sm.lace.util.Util;
import net.minidev.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class None implements ConflictResolutionStrategy {
  @Override
  public Map<String, Document> apply(Map<String, JSONObject> schemaMap) {
    return Util.nullSafeStream(schemaMap.entrySet())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> getSchemaDocumentFromJson(entry.getValue())));
  }

  private Document getSchemaDocumentFromJson(JSONObject jsonObject) {
    return new IntrospectionResultToSchema()
        .createSchemaDefinition(new ExecutionResultImpl(jsonObject.get("data"), List.of()));
  }
}
