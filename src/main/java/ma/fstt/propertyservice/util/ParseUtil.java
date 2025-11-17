package ma.fstt.propertyservice.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseUtil {
    public static <T> Set<T> StringToSet(String text) {
        String cleanedString = text.substring(1, text.length() - 1);
        String[] rolesArray = cleanedString.split(", ");
        return (Set<T>) Stream.of(rolesArray).collect(Collectors.toSet());
    }
}
