package com.joblink.joblink.auth.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public final class ProfileJsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProfileJsonUtil() {}

    public static ArrayNode parseArray(String json) {
        try {
            if (json == null || json.isBlank()) return MAPPER.createArrayNode();
            JsonNode node = MAPPER.readTree(json);
            return node.isArray() ? (ArrayNode) node : MAPPER.createArrayNode();
        } catch (Exception e) {
            return MAPPER.createArrayNode();
        }
    }

    public static String addOrUpdate(ArrayNode arr, int indexOrMinus1, JsonNode item) {
        if (indexOrMinus1 >= 0 && indexOrMinus1 < arr.size()) {
            arr.set(indexOrMinus1, item);
        } else {
            arr.add(item);
        }
        return arr.toString();
    }

    public static String removeAt(ArrayNode arr, int index) {
        if (index >= 0 && index < arr.size()) {
            arr.remove(index);
        }
        return arr.toString();
    }
}
