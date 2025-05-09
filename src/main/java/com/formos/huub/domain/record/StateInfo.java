/**
 * ***************************************************
 * * Description :
 * * File        : StateInfo
 * * Author      : Hung Tran
 * * Date        : Nov 07, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.record;

import java.util.Optional;

public record StateInfo(String origin, Optional<String> subdomain) {}
