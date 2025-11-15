package wonotter.java_ticketrush_8.service.level1;

import static org.assertj.core.api.Assertions.assertThat;

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
class TicketServiceSynchronizedTest {

    // 티켓 생성용
    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketServiceSynchronized ticketServiceSynchronized;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    private Long ticketId;

    @BeforeEach
    void setUp() {
        TicketCreateRequest request = new TicketCreateRequest(
                "synchronized 선착순 콘서트 티켓",
                "synchronized 콘서트 티켓입니다.",
                100
        );
        ticketId = ticketService.createTickets(request);
    }

    @AfterEach
    void tearDown() {
        ticketOrderRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("synchronized: 1000명 동시 주문 시 정확히 100명만 성공한다.")
    void synchronizedTest() throws InterruptedException {
        // given
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    ticketServiceSynchronized.orderTicket(ticketId, userId);
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

        long endTime = System.currentTimeMillis();

        // then
        Ticket ticket = ticketServiceSynchronized.getTicket(ticketId);
        long orderCount = ticketServiceSynchronized.getOrderCount(ticketId);

        log.info("=== synchronized 테스트 결과 ===");
        log.info("소요 시간: {}ms", (endTime - startTime));
        log.info("성공: {}명, 실패: {}명", successCount.get(), failCount.get());
        log.info("DB 주문 건수: {}건, 남은 재고: {}개", orderCount, ticket.getRemainStock());

        assertThat(orderCount).isEqualTo(100);
        assertThat(ticket.getRemainStock()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(100);
    }
}
