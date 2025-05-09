package com.formos.huub.domain.entity;

import com.formos.huub.framework.enums.LanguageEnum;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "translate")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE translate SET is_delete = true WHERE id = ?")
public class Translate extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_language")
    private LanguageEnum sourceLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_language")
    private LanguageEnum targetLanguage;

    @Column(name = "source_hash")
    private String sourceHash;

    @Column(name = "source_content")
    private String sourceContent;

    @Column(name = "target_content")
    private String targetContent;
}
