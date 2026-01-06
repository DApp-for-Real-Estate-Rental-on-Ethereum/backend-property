package ma.fstt.propertyservice.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParseUtil {
    @SuppressWarnings("unchecked")
    public static <T> Set<T> StringToSet(String text) {
        if (text == null || text.trim().isEmpty()) {
            return (Set<T>) Stream.<String>of().collect(Collectors.toSet());
        }
        
        String cleanedString = text.trim();
        
        if (cleanedString.startsWith("[") && cleanedString.endsWith("]")) {
            cleanedString = cleanedString.substring(1, cleanedString.length() - 1);
        }
        
        String[] rolesArray = cleanedString.split(",\\s*");
        
        return (Set<T>) Stream.of(rolesArray)
            .map(String::trim)
            // Strip surrounding quotes if present (e.g., "ROLE_HOST")
            .map(role -> role.replaceAll("^\"|\"$", ""))
            .filter(role -> !role.isEmpty())
            // Normalize Spring Security style roles like ROLE_HOST to HOST
            .map(role -> role.replaceFirst("^ROLE_", "").toUpperCase())
            .collect(Collectors.toSet());
    }
}
