package com.formos.huub.domain.entity;

import com.formos.huub.domain.response.clientnote.ResponseSearchClientNote;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "client_note")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE client_note SET is_delete = true WHERE id = ?")
@SqlResultSetMapping(
    name = "search_client_note",
    classes = @ConstructorResult(
        targetClass = ResponseSearchClientNote.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "createdDate", type = Instant.class),
            @ColumnResult(name = "note", type = String.class),
            @ColumnResult(name = "hasAttachments", type = String.class),
            @ColumnResult(name = "createdBy", type = String.class)
        }
    )
)
public class ClientNote extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_owner_id", referencedColumnName = "id")
    private BusinessOwner businessOwner;

}
