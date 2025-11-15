package wonotter.java_ticketrush_8.service.level1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.exception.ErrorMessage;
import wonotter.java_ticketrush_8.repository.TicketOrderRepository;
import wonotter.java_ticketrush_8.repository.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceSynchronized {

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;
    private final TicketOrderProcessor ticketOrderProcessor;

    /**
     * synchronized 적용
     * <p>
     * synchronized로 동기화하고, 내부에서 별도 Component의 트랜잭션 메서드를 호출하여
     * <p>
     * 트랜잭션이 완전히 커밋될 때 까지 다른 스레드가 진입하지 못하게 함
     */
    public synchronized Long orderTicket(Long ticketId, Long userId) {
        return ticketOrderProcessor.processOrder(ticketId, userId);
    }

    @Transactional(readOnly = true)
    public Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.TICKET_NOT_FOUND.getMessage()));
    }

    @Transactional(readOnly = true)
    public long getOrderCount(Long ticketId) {
        return ticketOrderRepository.countByTicketId(ticketId);
    }
}
