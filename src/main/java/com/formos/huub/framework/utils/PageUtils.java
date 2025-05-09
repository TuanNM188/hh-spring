package com.formos.huub.framework.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

public class PageUtils {

    private PageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, Object> toPage(Page<?> page) {
        Map<String, Object> map = new HashMap<>();
        map.put("content", page.getContent());
        map.put("totalElements", page.getTotalElements());
        map.put("totalPages", page.getTotalPages());
        map.put("currentPage", page.getNumber());
        map.put("first", page.isFirst());
        map.put("last", page.isLast());
        return map;
    }

    public static Sort createSort(String sortParams) {
        if (ObjectUtils.isEmpty(sortParams)) {
            sortParams = StringUtils.EMPTY;
        }
        Sort sort = Sort.unsorted();
        String[] parts = sortParams.split(",");
        if (parts.length == 2) {
            String field = parts[0];
            Sort.Direction direction = Sort.Direction.fromString(parts[1]);
            sort = sort.and(Sort.by(direction, field));
        }
        return sort;
    }
}
