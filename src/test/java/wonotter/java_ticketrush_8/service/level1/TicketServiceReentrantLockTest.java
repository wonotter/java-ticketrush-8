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
class TicketServiceReentrantLockTest {

    @Autowired
    private TicketService ticketService; // 티켓 생성용

    @Autowired
    private TicketServiceReentrantLock ticketServiceReentrantLock;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    private Long ticketId;

    @BeforeEach
    void setUp() {
        TicketCreateRequest request = new TicketCreateRequest(
                "ReentrantLock 선착순 콘서트 티켓",
                "ReentrantLock 테스트용 티켓입니다.",
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
    @DisplayName("ReentrantLock: 1000명 동시 주문 시 정확히 100명만 성공한다.")
    void reentrantLockeTest() throws InterruptedException {
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
                    ticketServiceReentrantLock.orderTicket(ticketId, userId);
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
        Ticket ticket = ticketServiceReentrantLock.getTicket(ticketId);
        long orderCount = ticketServiceReentrantLock.getOrderCount(ticketId);

        log.info("=== ReentrantLock 테스트 결과 ===");
        log.info("소요 시간: {}ms", (endTime - startTime));
        log.info("성공: {}명, 실패: {}명", successCount.get(), failCount.get());
        log.info("DB 주문 건수: {}건, 남은 재고: {}개", orderCount, ticket.getRemainStock());
        log.info("==================================");

        assertThat(orderCount).isEqualTo(100);
        assertThat(ticket.getRemainStock()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(100);
    }

    @Test
    @DisplayName("ReentrantLock: 락 상태 확인")
    void lockStateTest() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> {
                try {
                    startLatch.await();  // 시작 신호 대기
                    ticketServiceReentrantLock.orderTicket(ticketId, userId);
                } catch (Exception e) {
                    // 무시
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 시작 신호 전송
        startLatch.countDown();

        // 약간의 지연 후 락 상태 확인
        Thread.sleep(50);

        // then
        log.info("=== 락 상태 테스트 ===");
        log.info("락 걸림 여부: {}", ticketServiceReentrantLock.isLocked());
        log.info("대기 중인 스레드 수: {}", ticketServiceReentrantLock.getQueueLength());

        // 모든 스레드 완료 대기
        endLatch.await();
        executorService.shutdown();

        // 모든 작업 완료 후 락이 해제되어야 함
        assertThat(ticketServiceReentrantLock.isLocked()).isFalse();
        assertThat(ticketServiceReentrantLock.getQueueLength()).isEqualTo(0);

        log.info("최종 락 상태: {}", ticketServiceReentrantLock.isLocked());
        log.info("최종 대기 스레드: {}", ticketServiceReentrantLock.getQueueLength());
        log.info("===================");
    }
}
