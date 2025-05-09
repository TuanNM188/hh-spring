package com.formos.huub.domain.entity;

import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "language")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE language SET is_delete = true WHERE id = ?")
public class Language extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name", length = 100)
    @TrackTranslate
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "is_active")
    private Boolean isActive;
}
