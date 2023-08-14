package io.github.survival1sm.lace.util;

import graphql.language.Argument;
import graphql.language.Description;
import graphql.language.Directive;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.SourceLocation;
import graphql.language.StringValue;
import graphql.language.TypeName;
import graphql.schema.idl.SchemaPrinter;

import java.util.List;

public class SchemaTransformUtil {

  private static final List<DirectiveLocation> defaultRenameDirectiveLocations =
      List.of(
          DirectiveLocation.newDirectiveLocation().name("FIELD_DEFINITION").build(),
          DirectiveLocation.newDirectiveLocation().name("OBJECT").build(),
          DirectiveLocation.newDirectiveLocation().name("INTERFACE").build(),
          DirectiveLocation.newDirectiveLocation().name("INPUT_OBJECT").build(),
          DirectiveLocation.newDirectiveLocation().name("ENUM").build());
  private static List<DirectiveLocation> renameDirectiveLocations = defaultRenameDirectiveLocations;

  public static void setDefaultRenameDirectiveLocations(
      List<DirectiveLocation> directiveLocationList) {
    SchemaTransformUtil.renameDirectiveLocations = directiveLocationList;
  }

  public static final SchemaPrinter schemaPrinter =
      new SchemaPrinter(
          SchemaPrinter.Options.defaultOptions()
              .includeDirectives(true)
              .includeDirectiveDefinitions(true));

  public static final DirectiveDefinition RENAME_DIRECTIVE_DEFINITION =
      DirectiveDefinition.newDirectiveDefinition()
          .name("rename")
          .description(
              new Description(
                  "Directive used to rename a type or field when registering with the service factory",
                  new SourceLocation(0, 0),
                  false))
          .directiveLocations(renameDirectiveLocations)
          .inputValueDefinition(
              InputValueDefinition.newInputValueDefinition()
                  .name("to")
                  .type(
                      NonNullType.newNonNullType()
                          .type(TypeName.newTypeName().name("String").build())
                          .build())
                  .build())
          .build();

  public static Directive buildRenameDirectiveForServiceAndType(String serviceId, String typeName) {
    return Directive.newDirective()
        .name("rename")
        .argument(
            Argument.newArgument()
                .name("to")
                .value(
                    new StringValue(
                        "%s%s".formatted(serviceId.replace("-", ""), typeName.replace("_", ""))))
                .build())
        .build();
  }
}
