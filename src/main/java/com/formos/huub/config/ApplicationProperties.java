package com.formos.huub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Huub.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Liquibase liquibase = new Liquibase();

    // jhipster-needle-application-properties-property

    public Liquibase getLiquibase() {
        return liquibase;
    }

    // jhipster-needle-application-properties-property-getter

    public static class Liquibase {

        private Boolean asyncStart;

        public Boolean getAsyncStart() {
            return asyncStart;
        }

        public void setAsyncStart(Boolean asyncStart) {
            this.asyncStart = asyncStart;
        }
    }

    // jhipster-needle-application-properties-property-class

    // reset-password
    public final ResetPassword resetPassword = new ResetPassword();

    public ResetPassword getResetPassword() {
        return resetPassword;
    }

    @Setter
    @Getter
    public static class ResetPassword {
        private long resetKeyValidityInSeconds = 3600L;
    }

    // invite-technical-advisor
    public final InviteTechnicalAdvisor inviteTechnicalAdvisor = new InviteTechnicalAdvisor();

    public InviteTechnicalAdvisor getInviteTechnicalAdvisor() {
        return inviteTechnicalAdvisor;
    }

    @Setter
    @Getter
    public static class InviteTechnicalAdvisor {
        private long inviteTokenValidityInSeconds = 7776000;
    }

    // customer-care
    public final ClientApp clientApp = new ClientApp();

    public ClientApp getClientApp() {
        return clientApp;
    }

    @Setter
    @Getter
    public static class ClientApp {
        private String baseUrl;
        private String baseUrlPortal;
        private String baseCustomUrlPortal;
    }

    // customer-care
    public final CustomerCare customerCare = new CustomerCare();

    public CustomerCare getCustomerCare() {
        return customerCare;
    }

    @Setter
    @Getter
    public static class CustomerCare {
        private String joinHuubSupport;
        private String joinHuubLiveChat;
    }

}
