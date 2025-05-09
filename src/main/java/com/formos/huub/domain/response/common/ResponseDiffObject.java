package com.formos.huub.domain.response.common;

import lombok.Data;

import java.util.List;

@Data
public class ResponseDiffObject <T> {

    private List<T> added;

    private List<T> removed;
}

