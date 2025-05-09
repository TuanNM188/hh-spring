package com.formos.huub.service.common;

import com.formos.huub.repository.SequenceStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ***************************************************
 * * Description :
 * * File        : SequenceGeneratorService
 * * Author      : Hung Tran
 * * Date        : Mar 05, 2025
 * ***************************************************
 **/

@Service
@Slf4j
@RequiredArgsConstructor
public class SequenceService {

    private final SequenceStoreRepository sequenceStoreRepository;

    public Long getNextSequenceValue(String sequenceName) {
        return sequenceStoreRepository.getNextSequenceValue(sequenceName);
    }
}
