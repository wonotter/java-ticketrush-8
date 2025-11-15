package wonotter.java_ticketrush_8.service.level1;

import java.util.concurrent.locks.ReentrantLock;
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
public class TicketServiceReentrantLock {

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;
    private final TicketOrderProcessor ticketOrderProcessor;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * ReentrantLock 적용
     * <p>
     * synchronized와 동일하게 락을 통해 스레드 접근을 제어하지만,
     * <p>
     * 메서드 단위가 아닌 코드 블럭 내부에서 수동으로 락을 설정해야한다.
     * <p>
     * tryLock(), getQueueLength(), isLocked()와 같이 스레드 상태에 대한 추가 메서드를 제공하므로,
     * <p>
     * synchronized에 비해 세밀한 제어가 가능하다.
     */
    public Long orderTicket(Long ticketId, Long userId) {
        lock.lock();

        try {
            return ticketOrderProcessor.processOrder(ticketId, userId);
        } finally {
            lock.unlock();
        }
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

    // 락 상태 확인
    public boolean isLocked() {
        return lock.isLocked();
    }

    // 대기 중인 스레드 수 확인
    public int getQueueLength() {
        return lock.getQueueLength();
    }
}
