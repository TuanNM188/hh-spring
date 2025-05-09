package com.formos.huub.framework.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UUIDUtils {

    public static UUID toUUID(String s) {
        if (StringUtils.isBlank(s) || StringUtils.isEmpty(s)) {
            return null;
        }
        try {
            return UUID.fromString(s);
        }catch (Exception e){
            return null;
        }
    }

    public static UUID convertToUUID(String s) {
        if (StringUtils.isBlank(s) || StringUtils.isEmpty(s)) {
            throw new NullPointerException();
        }
        try {
            return UUID.fromString(s);
        }catch (Exception e){
            return null;
        }
    }

    public static List<UUID> toUUIDs(List<String> s) {
        if (s == null || s.isEmpty()) return new ArrayList<>();
        return s.stream().map(UUIDUtils::toUUID).collect(Collectors.toList());
    }

}
