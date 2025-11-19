package wonotter.java_ticketrush_8.service.level2;

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
import wonotter.java_ticketrush_8.service.level0.TicketService;

@Slf4j
@SpringBootTest
class TicketServiceOptimisticTest {

    // Facade 클래스 주입
    @Autowired
    private TicketOrderFacade ticketOrderFacade;

    @Autowired
    private TicketServiceOptimistic ticketServiceOptimistic;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private Long ticketId;
    private static final int TOTAL_STOCK = 100;
    private static final int THREAD_COUNT = 1000;

    @BeforeEach
    void setUp() {
        TicketCreateRequest request = new TicketCreateRequest(
                "낙관적 락 선착순 티켓",
                "낙관적 락 선착순 티켓입니다.",
                TOTAL_STOCK
        );

        ticketId = ticketService.createTickets(request);
    }

    @AfterEach
    void tearDown() {
        ticketOrderRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("낙관적 락을 사용하여 동시에 여러 사용자가 티켓을 주문해도 재고가 정확히 관리된다.")
    void orderTicketWithOptimisticLoc() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    // Facade를 통해 재시도 로직이 포함된 주문 수행
                    ticketOrderFacade.orderTicketWithRetry(ticketId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.debug("주문 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long endTime = System.currentTimeMillis();

        // then
        Ticket ticket = ticketServiceOptimistic.getTicket(ticketId);
        long orderCount = ticketServiceOptimistic.getOrderCount(ticketId);

        log.info("=== 낙관적 락 테스트 결과 ===");
        log.info("총 시도: {}", THREAD_COUNT);
        log.info("성공: {}명, 실패 {}명", successCount.get(), failCount.get());
        log.info("남은 재고: {}", ticket.getRemainStock());
        log.info("주문 수: {}", orderCount);
        log.info("소요 시간: {}ms", endTime - startTime);

        assertThat(successCount.get()).isEqualTo(TOTAL_STOCK);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - TOTAL_STOCK);
        assertThat(ticket.getRemainStock()).isZero();
        assertThat(orderCount).isEqualTo(TOTAL_STOCK);
    }

    @Test
    @DisplayName("중복 주문 방지 테스트")
    void preventDuplicateOrder() throws InterruptedException {
        // given
        long userId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    ticketOrderFacade.orderTicketWithRetry(ticketId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.debug("중복 주문 방지: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        long orderCount = ticketServiceOptimistic.getOrderCount(ticketId);

        log.info("=== 중복 주문 방지 테스트 결과 ===");
        log.info("동일 사용자 주문 시도: 10회");
        log.info("성공한 주문: {}회", successCount.get());
        log.info("실제 주문 수: {}", orderCount);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(orderCount).isEqualTo(1);
    }
}
