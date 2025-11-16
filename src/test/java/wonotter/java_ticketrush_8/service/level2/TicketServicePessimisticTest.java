package wonotter.java_ticketrush_8.service.level2;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wonotter.java_ticketrush_8.dto.TicketCreateRequest;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.repository.TicketOrderRepository;
import wonotter.java_ticketrush_8.repository.TicketRepository;
import wonotter.java_ticketrush_8.service.TicketService;

@Slf4j
@SpringBootTest
class TicketServicePessimisticTest {

    @Autowired
    private TicketServicePessimistic ticketServicePessimistic;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    private Long ticketId;
    private static final int TOTAL_STOCK = 100;
    private static final int THREAD_COUNT = 1000;

    @BeforeEach
    void setUp() {
        TicketCreateRequest request = new TicketCreateRequest(
                "비관적 락 선착순 티켓",
                "비관적 락 선착순 티켓입니다.",
                TOTAL_STOCK,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );

        ticketId = ticketService.createTickets(request);
    }

    @AfterEach
    void tearDown() {
        ticketOrderRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락을 사용하여 동시에 여러 사용자가 티켓을 주문해도 재고가 정확히 관리된다.")
    void orderTicketWithPessimisticLock() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < THREAD_COUNT; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    ticketServicePessimistic.orderTicket(ticketId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Ticket ticket = ticketServicePessimistic.getTicket(ticketId);
        long orderCount = ticketServicePessimistic.getOrderCount(ticketId);

        log.info("=== 비관적 락 테스트 결과 ===");
        log.info("총 시도: {}", THREAD_COUNT);
        log.info("성공: {}명, 실패 {}명", successCount.get(), failCount.get());
        log.info("남은 재고: {}", ticket.getRemainStock());
        log.info("주문 수: {}", orderCount);

        assertThat(successCount.get()).isEqualTo(TOTAL_STOCK);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - TOTAL_STOCK);
        assertThat(ticket.getRemainStock()).isZero();
        assertThat(orderCount).isEqualTo(TOTAL_STOCK);
    }
}
