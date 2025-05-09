package com.formos.huub.domain.entity;

import static com.formos.huub.framework.constant.AppConstants.KEY_SPACE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.config.Constants;
import com.formos.huub.domain.enums.NotificationPreferenceEnum;
import com.formos.huub.domain.enums.PhoneTypeEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

/**
 * A user.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jhi_user")
@NamedStoredProcedureQuery(
    name = "User.insertUserSettings",
    procedureName = "insert_user_settings",
    parameters = { @StoredProcedureParameter(mode = ParameterMode.IN, name = "userId", type = UUID.class) }
)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@SQLDelete(sql = "UPDATE jhi_user SET is_delete = true WHERE id = ?")
@SQLRestriction("is_delete = 'false'")
public class User extends AbstractAuditingEntity<UUID> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Column(name = "reset_key")
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "extension")
    private String extension;

    @Column(name = "phone_type")
    @Enumerated(EnumType.STRING)
    private PhoneTypeEnum phoneType;

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

    @Column(name = "normalized_full_name")
    private String normalizedFullName;

    @Column(name = "profile_picture", length = 255)
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_preferences", length = 50)
    private NotificationPreferenceEnum notificationPreferences;

    @Column(name = "bio")
    @TrackTranslate
    private String bio;

    @Column(name = "appt")
    private String appt;

    @Column(name = "organization")
    private String organization;

    @Column(name = "title")
    private String title;

    @Column(name = "personal_website")
    private String personalWebsite;

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

    @Column(name = "first_logged_in", columnDefinition = "boolean default false")
    private Boolean firstLoggedIn;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    @Pattern(regexp = Constants.USERNAME_REGEX)
    @Size(min = 2, max = 50)
    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "allow_booking", columnDefinition = "boolean default false")
    private Boolean allowBooking;

    @OneToOne(mappedBy = "user")
    private TechnicalAdvisor technicalAdvisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_partner_id", referencedColumnName = "id")
    private CommunityPartner communityPartner;

    @Column(name = "is_navigator", columnDefinition = "boolean default false")
    private Boolean isNavigator;

    @Column(name = "is_primary", columnDefinition = "boolean default false")
    private Boolean isPrimary;

    @Column(name = "active_campaign_contact_id")
    private String activeCampaignContactId;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "community_partner_user",
        joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "partner_id", referencedColumnName = "id") }
    )
    @BatchSize(size = 20)
    private Set<CommunityPartner> communityPartners = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "jhi_user_authority",
        joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private Set<AdvisementCategory> advisementCategories = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<TermsAcceptance> termsAcceptances = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<LearningLibraryRegistration> learningLibraryRegistrations = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private BookingSetting bookingSetting;

    // Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "User{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated='" + activated + '\'' +
            ", langKey='" + langKey + '\'' +
            ", activationKey='" + activationKey + '\'' +
            "}";
    }

    @PrePersist
    @PreUpdate
    public void makeNormalizedFullName() {
        normalizedFullName = (firstName.trim() + KEY_SPACE + lastName.trim());
    }
}
