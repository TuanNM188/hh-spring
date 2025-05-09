package com.formos.huub.domain.response.calendarintegrate;

import java.util.List;
import lombok.*;

@Getter
@Setter
public class ResponseOrganizationPageable extends AbstractResponsePageable<ResponseOrganization> {

    private List<ResponseOrganization> organizations;

    @Override
    public List<ResponseOrganization> getItems() {
        return organizations;
    }
}
