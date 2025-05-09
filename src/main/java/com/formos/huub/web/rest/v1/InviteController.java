package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.base.BaseController;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invites")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InviteController extends BaseController {

}
