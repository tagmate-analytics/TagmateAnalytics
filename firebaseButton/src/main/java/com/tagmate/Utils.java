package com.tagmate;

import android.os.Bundle;

public class Utils {

    public String bundleToJsonString3(Bundle bundle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");

        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            stringBuilder.append("\"").append(key).append("\":").append(valueToJsonString(value)).append(",");
        }

        if (stringBuilder.length() > 1) {
            // Remove the trailing comma
            stringBuilder.setLength(stringBuilder.length() - 1);
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    // Method to convert a value to its JSON string representation
    private String valueToJsonString(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Boolean) {
            return value.toString();
        } else {
            // Handle other value types accordingly
            return "\"" + escapeString(value.toString()) + "\"";
        }
    }

    // Method to escape special characters in a string
    private String escapeString(String value) {
        value = value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        // Handle other special characters if needed

        return value;
    }

}
