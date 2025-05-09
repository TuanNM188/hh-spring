package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.PortalStatusEnum;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "portal")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE portal SET is_delete = true WHERE id = ?")
public class Portal extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "platform_name", nullable = false)
    private String platformName;

    @Column(name = "region_name", nullable = false)
    private String regionName;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "is_custom_domain")
    private Boolean isCustomDomain;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "primary_logo")
    private String primaryLogo;

    @Column(name = "secondary_logo")
    private String secondaryLogo;

    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "secondary_color")
    private String secondaryColor;

    @Column(name = "organization")
    private String organization;

    @Column(name = "address_1", length = 1000)
    private String address1;

    @Column(name = "address_2", length = 1000)
    private String address2;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private PortalStatusEnum status;

    @Column(name = "favicon")
    private String favicon;

    @Column(name = "primary_contact_name")
    private String primaryContactName;

    @Column(name = "primary_contact_Email")
    private String primaryContactEmail;

    @Column(name = "primary_contact_phone")
    private String primaryContactPhone;

    @Column(name = "primary_extension")
    private String primaryExtension;

    @Column(name = "is_same_as_primary")
    private Boolean isSameAsPrimary;

    @Column(name = "billing_name")
    private String billingName;

    @Column(name = "billing_Email")
    private String billingEmail;

    @Column(name = "billing_phone")
    private String billingPhone;

    @Column(name = "billing_extension")
    private String billingExtension;

    @Column(name = "about_page_content")
    @TrackTranslate
    private String aboutPageContent;

    @Column(name = "country_id")
    private String countryId;

    @Column(name = "user_send_message_id")
    private UUID userSendMessageId;

    @Column(name = "welcome_message")
    private String welcomeMessage;

    @OneToOne(mappedBy = "portal")
    private Program program;

    @ManyToMany(mappedBy = "portals")
    private Set<Funding> fundings = new HashSet<>();

    @OneToMany(mappedBy = "portal", fetch = FetchType.LAZY)
    private Set<PortalState> states = new HashSet<>();

    @OneToMany(mappedBy = "portal", fetch = FetchType.LAZY)
    private Set<PortalHost> portalHosts = new HashSet<>();

    @ManyToMany(mappedBy = "portals")
    private Set<TechnicalAdvisor> technicalAdvisors = new HashSet<>();

    @ManyToMany(mappedBy = "portals")
    private Set<CommunityPartner> communityPartners = new HashSet<>();
}
