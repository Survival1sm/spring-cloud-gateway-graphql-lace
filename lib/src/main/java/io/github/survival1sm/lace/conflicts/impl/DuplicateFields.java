package io.github.survival1sm.lace.conflicts.impl;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.EnumTypeDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.NamedNode;
import graphql.language.NodeChildrenContainer;
import graphql.language.ObjectTypeDefinition;
import io.github.survival1sm.lace.conflicts.ConflictResolutionStrategy;
import io.github.survival1sm.lace.util.SchemaTransformUtil;
import io.github.survival1sm.lace.util.Util;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.survival1sm.lace.util.SchemaTransformUtil.RENAME_DIRECTIVE_DEFINITION;

public class DuplicateFields implements ConflictResolutionStrategy {

  public Map<String, Document> apply(Map<String, JSONObject> schemaMap) {
    Map<String, List<String>> typeToServiceMap = new HashMap<>();
    schemaMap.forEach(
        (service, schema) -> {
          Document document = getSchemaDocumentFromJson(schema);

          document
              .getDefinitions()
              .forEach(
                  definition -> {
                    if (definition instanceof ObjectTypeDefinition
                        || definition instanceof InterfaceTypeDefinition
                        || definition instanceof FieldDefinition
                        || definition instanceof InputObjectTypeDefinition
                        || definition instanceof EnumTypeDefinition) {
                      String name = ((NamedNode<?>) definition).getName();

                      if (typeToServiceMap.containsKey(name)) {
                        typeToServiceMap.get(name).add(service);
                      } else {
                        List<String> serviceList = new ArrayList<>();
                        serviceList.add(service);
                        typeToServiceMap.put(name, serviceList);
                      }
                    }
                  });
        });

    return Util.nullSafeStream(schemaMap.entrySet())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                  Document document = getSchemaDocumentFromJson(entry.getValue());

                  List<Definition> transformedDefinitions =
                      applyDirectiveToDuplicateDefinitions(
                          entry.getKey(), document, typeToServiceMap);

                  transformedDefinitions.add(RENAME_DIRECTIVE_DEFINITION);

                  return document.transform(
                      builder -> builder.definitions(transformedDefinitions).build());
                }));
  }

  private List<Definition> applyDirectiveToDuplicateDefinitions(
      String serviceId, Document sdlDef, Map<String, List<String>> typeToServiceMap) {
    return Util.nullSafeStream(sdlDef.getDefinitions())
        .map(
            definition -> {
              if (definition instanceof ObjectTypeDefinition
                  || definition instanceof InterfaceTypeDefinition
                  || definition instanceof FieldDefinition
                  || definition instanceof InputObjectTypeDefinition
                  || definition instanceof EnumTypeDefinition) {
                String typeName = ((NamedNode<?>) definition).getName();

                if (typeToServiceMap.get(typeName).size() > 1
                    && typeToServiceMap.get(typeName).contains(serviceId)) {
                  NodeChildrenContainer transformedNode =
                      definition
                          .getNamedChildren()
                          .transform(
                              builder ->
                                  builder.child(
                                      "directives",
                                      SchemaTransformUtil.buildRenameDirectiveForServiceAndType(
                                          serviceId, typeName)));
                  definition = (Definition) definition.withNewChildren(transformedNode);
                }
              }

              return definition;
            })
        .collect(Collectors.toList());
  }

  private Document getSchemaDocumentFromJson(JSONObject jsonObject) {
    ExecutionResult executionResult = new ExecutionResultImpl(jsonObject.get("data"), List.of());

    return new IntrospectionResultToSchema().createSchemaDefinition(executionResult);
  }
}
