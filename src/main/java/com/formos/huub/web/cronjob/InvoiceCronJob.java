package com.formos.huub.web.cronjob;

import com.formos.huub.service.schedule.MonthlyInvoiceScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCronJob {
    private final MonthlyInvoiceScheduleService invoiceScheduleService;

    @Value("${schedule.timezone}")
    private String timeZoneId;

    /**
     * Generate monthly invoices for all technical advisors with activity in the previous month.
     * This is the main entry point for the invoice generation process.
     */
    @Scheduled(cron = "0 0 1 * * ?", zone = "America/Los_Angeles")
    public void createMonthlyInvoices() {
        log.info("Starting monthly invoice generation job");
        invoiceScheduleService.generateMonthlyInvoices(YearMonth.now(ZoneId.of(timeZoneId)));
    }

}
