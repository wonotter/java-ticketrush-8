package wonotter.java_ticketrush_8.service.level2;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import wonotter.java_ticketrush_8.exception.ErrorMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketOrderFacade {

    private final TicketServiceOptimistic ticketServiceOptimistic;
    private static final int MAX_RETRY_COUNT = 100;

    public Long orderTicketWithRetry(Long ticketId, Long userId) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                return ticketServiceOptimistic.orderTicket(ticketId, userId);
            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                retryCount++;
                log.debug("낙관적 락 충돌 발생! 재시도 횟수: {}/{}", retryCount, MAX_RETRY_COUNT);

                if (retryCount >= MAX_RETRY_COUNT) {
                    log.error("최대 재시도 횟수 초과! ticketId: {}, userId: {}", ticketId, userId);
                    throw new IllegalArgumentException(ErrorMessage.TICKET_ORDER_FAILED.getMessage());
                }
            }
        }

        throw new IllegalArgumentException(ErrorMessage.TICKET_ORDER_FAILED.getMessage());
    }
}
