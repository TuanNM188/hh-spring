package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.UUID;

@Entity
@Table(name = "specialty_user")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE specialty_user SET is_delete = true WHERE id = ?")
public class SpecialtyUser extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "specialty_id", length = 36)
    private UUID specialtyId;

    @Column(name = "specialty_area_id", length = 36)
    private UUID specialtyAreaId;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
