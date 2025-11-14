package wonotter.java_ticketrush_8.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wonotter.java_ticketrush_8.entity.TicketOrder;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {

    // 특정 유저가 특정 티켓에 이미 구매했는지 확인
    Optional<TicketOrder> findByTicketIdAndUserId(Long ticketId, Long userId);

    // 티켓 주문 수량 조회
    long countByTicketId(Long ticketId);
}
