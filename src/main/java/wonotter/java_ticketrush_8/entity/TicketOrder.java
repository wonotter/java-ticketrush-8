package wonotter.java_ticketrush_8.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wonotter.java_ticketrush_8.entity.common.BaseTimeEntity;

@Entity
@Table(
        name = "ticket_orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_user",
                        columnNames = {"ticket_id", "user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    public TicketOrder(Long ticketId, Long userId) {
        this.ticketId = ticketId;
        this.userId = userId;
    }
}
