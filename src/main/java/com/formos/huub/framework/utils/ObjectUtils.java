package com.formos.huub.framework.utils;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.request.common.SqlConditionRequest;
import com.formos.huub.domain.response.common.ResponseDiffObject;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ObjectUtils  extends org.springframework.util.ObjectUtils {


    /**
     * Get item add remove when update list
     *
     * @param oldList List<T>
     * @param newList List<T>
     * @return ResponseDiffObject<T>
     */
    public static <T> ResponseDiffObject<T> getDifferenceList(List<T> oldList, List<T> newList) {
        List<T> lcs = new ArrayList<>();
        int[][] lengths = new int[oldList.size() + 1][newList.size() + 1];

        for (int i = 0; i < oldList.size(); i++) {
            for (int j = 0; j < newList.size(); j++) {
                if (oldList.get(i).equals(newList.get(j))) {
                    lcs.add(oldList.get(i));
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                } else {
                    lengths[i + 1][j + 1] = Math.max(lengths[i][j + 1], lengths[i + 1][j]);
                }
            }
        }
        var response = new ResponseDiffObject<T>();
        List<T> added = new ArrayList<>(newList);
        added.removeAll(lcs);
        response.setAdded(added);
        List<T> removed = new ArrayList<>(oldList);
        removed.removeAll(lcs);
        response.setRemoved(removed);
        return response;
    }

    /**
     * Convert sort params
     *
     * @param requests List<SqlConditionRequest>
     * @param sortMap HashMap<String, String>
     * @return List<SqlConditionRequest>
     */
    public static List<SqlConditionRequest> convertSortParams(List<SqlConditionRequest> requests, HashMap<String, String> sortMap) {
        if(Objects.isNull(requests)) {
            return null;
        }
        var timezone = sortMap.get(BusinessConstant.TIMEZONE_KEY);
        return requests.stream().peek(element -> {
            if (!element.isGroup()){
                var sortExist = sortMap.get(element.getColumn());
                if (Objects.nonNull(sortExist)){
                    element.setColumn(sortExist);
                }
            }else {
                var newSubCon = element.getSubConditions().stream().peek(sub -> {
                    var sortExist = sortMap.get(element.getColumn());
                    if (Objects.nonNull(sortExist)){
                        sub.setColumn(sortExist);
                    }
                }).toList();
                element.setSubConditions(newSubCon);
            }
            if (Objects.nonNull(timezone)){
                element.setTimezone(timezone);
            }
        }).toList();
    }

    public static  <T, K> Map<K, T> convertToMap(List<T> list, Function<T, K> keyExtractor) {
        return ObjectUtils.isEmpty(list) ? new HashMap<>() : list.stream()
            .collect(Collectors.toMap(keyExtractor, Function.identity(), (existing, replacement) -> existing));
    }

    public static <T> List<String> toListString(List<T> list, Function<T, String> mapper) {
        return list.stream()
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static <T> boolean hasDuplicate(List<T> list) {
        Set<T> set = list.stream()
            .filter(i -> list.stream().filter(x -> x.equals(i)).count() > 1)
            .collect(Collectors.toSet());

        return !set.isEmpty();
    }

    public static String[] splitFullName(String fullname) {
        fullname = fullname.trim();

        String[] parts = fullname.split("\\s+", 2);
        String firstname = (parts.length > 0) ? parts[0] : "";
        if (firstname.length() > 50) {
            firstname = firstname.substring(0, 50);
        }
        String lastname = (parts.length > 1) ? parts[1] : "";
        if (lastname.length() > 50) {
            lastname = lastname.substring(0, 50);
        }
        return new String[]{firstname, lastname};
    }

    public static boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    public static String cleanStringWithRegex(String str, String regex) {
        // Retain only alphanumeric characters, dots, and underscores
        return str.replaceAll("[^a-zA-Z0-9._]", "");
    }

    public static List<UUID> convertToUUIDList(List<String> stringList) {
        return stringList.stream()
            .map(UUID::fromString)
            .collect(Collectors.toList());
    }
}
