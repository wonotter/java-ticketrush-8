package wonotter.java_ticketrush_8.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wonotter.java_ticketrush_8.entity.common.BaseTimeEntity;
import wonotter.java_ticketrush_8.exception.ErrorMessage;

@Entity
@Table(name = "tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Ticket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    // 전체 재고
    @Column(nullable = false)
    private Integer totalStock;

    @Column(nullable = false)
    private Integer remainStock;

    // 선착순 이벤트 시작 시간 (선택)
    private LocalDateTime startAt;

    // 선착순 이벤트 종료 시간 (선택)
    private LocalDateTime endAt;

    @Builder
    public Ticket(String name, String description, Integer totalStock,
                  LocalDateTime startAt, LocalDateTime endAt) {
        this.name = name;
        this.description = description;
        this.totalStock = totalStock;
        this.remainStock = totalStock;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void decreaseStock() {
        validateStockAvailable();
        this.remainStock--;
    }

    private void validateStockAvailable() {
        if (this.remainStock <= 0) {
            throw new IllegalArgumentException(ErrorMessage.STOCK_NOT_AVAILABLE.getMessage());
        }
    }

    // 이벤트 기간 확인 (선택)
    public void validateEventPeriod() {
        if (startAt != null && endAt != null) {
            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(this.startAt)) {
                throw new IllegalArgumentException(ErrorMessage.EVENT_NOT_STARTED.getMessage());
            }

            if (now.isAfter(this.endAt)) {
                throw new IllegalArgumentException(ErrorMessage.EVENT_ALREADY_END.getMessage());
            }
        }
    }

    public boolean hasStock() {
        return this.remainStock > 0;
    }
}
