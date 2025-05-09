package com.formos.huub.service.webhookevent;

import com.formos.huub.domain.entity.WebhookEvent;
import com.formos.huub.repository.WebhookEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class WebhookEventSaveAllService {
    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public void saveEvent(List<WebhookEvent> events) {
        webhookEventRepository.saveAll(events);
    }
}
