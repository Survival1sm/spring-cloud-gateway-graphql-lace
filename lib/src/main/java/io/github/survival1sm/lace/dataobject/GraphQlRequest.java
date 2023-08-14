package io.github.survival1sm.lace.dataobject;

import java.util.Map;

public record GraphQlRequest(String query, String operationName, Map<String, Object> variables) {}
