package com.formos.huub.domain.response.activecampaign;

import lombok.*;

import javax.swing.plaf.BorderUIResource;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseContacts {

    private ResponseContact contact;

    private List<ResponseFieldValue> fieldValues;

}
