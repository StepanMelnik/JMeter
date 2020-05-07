package com.sme.jmeter.assertion;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Useful json assertion utility.
 */
public final class JsonAssertion
{
    private static final ObjectWriter PRETTY_WRITER;

    static
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper
                .getSerializationConfig()
                .getDefaultVisibilityChecker()
                .with(Visibility.NONE)
                .withFieldVisibility(Visibility.ANY));
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

        PRETTY_WRITER = mapper.writerWithDefaultPrettyPrinter();
    }

    private JsonAssertion()
    {
    }

    /**
     * Converts object to json string.
     * 
     * @param object The given object to be converted;
     * @return returns json value as a string.
     */
    public static String toPrettyJson(Object object)
    {
        try
        {
            return PRETTY_WRITER.writeValueAsString(getValueForPretty(object));
        }
        catch (Exception e)
        {
            return Objects.toString(object);
        }
    }

    private static Object getValueForPretty(Object value)
    {
        if (value instanceof List)
        {
            return ((List<?>) value).stream()
                    .map(v -> getValueForPretty(v))
                    .collect(Collectors.toList());
        }
        if (value instanceof Map)
        {
            Map<?, ?> actualMap = (Map<?, ?>) value;
            Map<Object, Object> newMap = new TreeMap<>((k1, k2) -> k1.toString().compareTo(k2.toString()));
            actualMap.forEach((k, v) -> newMap.put(k, getValueForPretty(v)));
            return newMap;
        }
        return value;
    }
}
