package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.entity.Authority;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.repository.AuthorityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing {@link Authority}.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/authorities")
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorityController extends BaseController {

    private static final String ENTITY_NAME = "adminAuthority";

    AuthorityRepository authorityRepository;
}
