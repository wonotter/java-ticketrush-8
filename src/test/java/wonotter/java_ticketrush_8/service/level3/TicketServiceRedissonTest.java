package wonotter.java_ticketrush_8.service.level3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import wonotter.java_ticketrush_8.exception.ErrorMessage;
import wonotter.java_ticketrush_8.repository.TicketOrderRepository;
import wonotter.java_ticketrush_8.repository.TicketRepository;
import wonotter.java_ticketrush_8.service.level0.TicketService;

@Slf4j
@SpringBootTest
class TicketServiceRedissonTest {

    // Facade를 주입받아 사용
    @Autowired
    private RedissonFacade redissonFacade;

    @Autowired
    private TicketServiceRedisson ticketServiceRedisson;

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
                "Redisson 선착순 콘서트 티켓",
                "Redisson 분산 락을 사용한 선착순 콘서트 티켓입니다.",
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
    @DisplayName("Redisson 분산 락을 통해 동시에 1000명이 주문해도 정확히 100명만 성공한다.")
    void redissonConcurrencyTest() throws InterruptedException {
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
                    // Facade를 통해 주문 (분산 락 + 트랜잭션 보장)
                    redissonFacade.orderTicketWithRedisson(ticketId, userId);
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
        Ticket ticket = ticketServiceRedisson.getTicket(ticketId);
        long orderCount = ticketServiceRedisson.getOrderCount(ticketId);

        log.info("=== [Redisson 분산 락] 동시성 테스트 결과 ===");
        log.info("소요 시간: {}ms", (endTime - startTime));
        log.info("성공 카운트: {}명", successCount.get());
        log.info("실패 카운트: {}명", failCount.get());
        log.info("DB 주문 건수: {}건", orderCount);
        log.info("남은 재고: {}개", ticket.getRemainStock());

        assertThat(successCount.get()).isEqualTo(TOTAL_STOCK);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - TOTAL_STOCK);
        assertThat(ticket.getRemainStock()).isZero();
        assertThat(orderCount).isEqualTo(TOTAL_STOCK);
    }

    @Test
    @DisplayName("Redisson 분산 락을 사용해도 중복 주문은 실패한다.")
    void redissonDuplicateOrderTest() {
        // given
        Long userId = 1L;

        // when
        redissonFacade.orderTicketWithRedisson(ticketId, userId);

        // then
        assertThatThrownBy(() -> redissonFacade.orderTicketWithRedisson(ticketId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMessage.TICKET_ALREADY_ORDERED.getMessage());
    }
}
