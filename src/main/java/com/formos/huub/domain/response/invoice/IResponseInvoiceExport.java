package com.formos.huub.domain.response.invoice;

public interface IResponseInvoiceExport {

    String getInvoiceNumber();
    String getHoursCompleted();
    String getContractedRate();
    String getCost();
    String getBusinessOwnerName();
    String getBusinessName();
    String getAppointmentDate();
    String getProjectCompletionDate();
    String getCategoryName();
    String getServiceName();
    String getAdvisor();
    String getReportNumber();
}
