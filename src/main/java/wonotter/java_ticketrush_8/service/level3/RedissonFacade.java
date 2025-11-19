package wonotter.java_ticketrush_8.service.level3;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import wonotter.java_ticketrush_8.exception.ErrorMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonFacade {

    private final TicketServiceRedisson ticketServiceRedisson;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "ticket:lock:";
    private static final long WAIT_TIME = 10L;
    private static final long LEASE_TIME = 3L;

    public Long orderTicketWithRedisson(Long ticketId, Long userId) {
        String lockKey = LOCK_PREFIX + ticketId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new IllegalArgumentException(ErrorMessage.TICKET_ORDER_FAILED.getMessage());
            }

            // 비즈니스 로직 실행
            return ticketServiceRedisson.orderTicket(ticketId, userId);
        } catch (InterruptedException e) {
            // 인터럽트가 발생한 경우
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(ErrorMessage.TICKET_ORDER_FAILED.getMessage());
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
