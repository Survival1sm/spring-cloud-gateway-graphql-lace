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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.survival1sm.lace.util.SchemaTransformUtil.RENAME_DIRECTIVE_DEFINITION;

@SuppressWarnings("java:S3740")
public class AllFields implements ConflictResolutionStrategy {

  public Map<String, Document> apply(Map<String, JSONObject> schemaMap) {
    return Util.nullSafeStream(schemaMap.entrySet())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> getSchemaDocumentFromJson(entry.getKey(), entry.getValue())));
  }

  private Document getSchemaDocumentFromJson(String serviceId, JSONObject jsonObject) {
    ExecutionResult executionResult = new ExecutionResultImpl(jsonObject.get("data"), List.of());
    Document sdlDef = new IntrospectionResultToSchema().createSchemaDefinition(executionResult);

    List<Definition> transformedDefinitions = applyDirectiveToAllDefinitions(serviceId, sdlDef);

    transformedDefinitions.add(RENAME_DIRECTIVE_DEFINITION);

    return sdlDef.transform(builder -> builder.definitions(transformedDefinitions).build());
  }

  private List<Definition> applyDirectiveToAllDefinitions(String serviceId, Document sdlDef) {
    return Util.nullSafeStream(sdlDef.getDefinitions())
        .map(
            definition -> {
              if (definition instanceof ObjectTypeDefinition
                  || definition instanceof InterfaceTypeDefinition
                  || definition instanceof FieldDefinition
                  || definition instanceof InputObjectTypeDefinition
                  || definition instanceof EnumTypeDefinition) {
                String typeName = ((NamedNode<?>) definition).getName();

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

              return definition;
            })
        .collect(Collectors.toList());
  }
}
