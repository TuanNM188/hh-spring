package com.formos.huub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.domain.enums.*;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.sound.sampled.Port;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_partner")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_partner SET is_delete = true WHERE id = ?")
public class CommunityPartner extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    @TrackTranslate
    private String name;

    @Column(name = "is_vendor")
    private Boolean isVendor;

    @Column(name = "eventbrite_url")
    private String eventbriteUrl;

    @Column(name = "ical_url")
    private String iCalUrl;

    @Column(name = "service_types")
    private String serviceTypes;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StatusEnum status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "extension")
    private String extension;

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Size(max = 10)
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "bio")
    @TrackTranslate
    private String bio;

    @Column(name = "website")
    private String website;

    @Column(name = "additional_website")
    private String additionalWebsite;

    @Column(name = "tiktok_profile")
    private String tiktokProfile;

    @Column(name = "linked_in_profile")
    private String linkedInProfile;

    @Column(name = "instagram_profile")
    private String instagramProfile;

    @Column(name = "facebook_profile")
    private String facebookProfile;

    @Column(name = "twitter_profile")
    private String twitterProfile;

    @OneToMany(mappedBy = "communityPartner")
    private Set<TechnicalAdvisor> technicalAdvisors = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "portal_community_partner",
        joinColumns = { @JoinColumn(name = "community_partner_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "portal_id", referencedColumnName = "id") }
    )
    @BatchSize(size = 20)
    private Set<Portal> portals = new HashSet<>();
}
