package wonotter.java_ticketrush_8.service.level0;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

@Slf4j
@SpringBootTest
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    private Long ticketId;

    @BeforeEach
    void setUp() {
        TicketCreateRequest request = new TicketCreateRequest(
                "선착순 콘서트 티켓",
                "선착순 콘서트 티켓 설명입니다.",
                100
        );

        ticketId = ticketService.createTickets(request);
        log.info("테스트 티켓 생성 완료 - ID: {}, 재고: 100개", ticketId);
    }

    @AfterEach
    void tearDown() {
        ticketOrderRepository.deleteAll();
        ticketRepository.deleteAll();
        log.info("테스트 데이터 정리 완료");
    }

    @Test
    @DisplayName("동시에 1000명이 주문하면 동시성 문제가 발생한다. (재고 100개 기준)")
    void concurrencyProblemTest() throws InterruptedException {
        // given
        int threadCount = 1000; // 1000명 사용자

        // 32개 스레드 구성
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 1000개 작업이 완료될 때 까지 대기시켜주는 래치(걸쇠)
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 원자적(스레드 안전한)으로 성공 또는 실패에 따라 값을 증가시키는 카운터 생성
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        log.info("=== 동시성 테스트 시작 ===");
        log.info("동시 주문 시도: {}명", threadCount);

        // when
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executorService.submit(() -> { // 스레드 실행
                try {
                    ticketService.orderTicket(ticketId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 타임아웃 설정: 최대 2분 대기 (1000개 작업 처리 시간 고려)
        boolean completed = latch.await(120, TimeUnit.SECONDS);
        executorService.shutdown(); // 스레드 풀 종료

        // 10초 안에 종료되지 않는 경우 스레드 풀 강제종료
        if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }

        if (!completed) {
            log.error("=== 타임아웃 발생 ===");
            log.error("완료된 작업: {}개", threadCount - latch.getCount());
            log.error("대기 중인 작업: {}개", latch.getCount());
            executorService.shutdownNow(); // 강제 종료
            throw new RuntimeException("테스트 타임아웃: 2분 내에 완료되지 않았습니다.");
        }

        long endTime = System.currentTimeMillis();

        // then
        Ticket ticket = ticketService.getTicket(ticketId);
        long orderCount = ticketService.getOrderCount(ticketId);

        log.info("=== 동시성 테스트 결과 ===");
        log.info("소요 시간: {}ms", (endTime - startTime));
        log.info("전체 재고: 100개");
        log.info("동시 주문 시도: {}명", threadCount);
        log.info("성공 카운트: {}명", successCount.get());
        log.info("실패 카운트: {}명", failCount.get());
        log.info("DB에 저장된 주문 건수: {}건", orderCount);
        log.info("남은 재고: {}개", ticket.getRemainStock());
        log.info("======================");

        /**
         * 100명만 성공하는 것이 정상이지만,
         * 동시성 문제로 인해 실제로는 100명 이상이 성공한다.
         */
        assertThat(orderCount).isNotEqualTo(100);
        assertThat(ticket.getRemainStock()).isNotEqualTo(0);
        assertThat(successCount.get()).isNotEqualTo(100);
    }

    @Test
    @DisplayName("순차적으로 주문하면 정확히 100명만 성공한다.")
    void sequentialOrderTest() {
        // given
        int applicantCount = 150;
        int successCount = 0;
        int failCount = 0;

        log.info("=== 순차 주문 테스트 시작 ===");
        log.info("순차 주문 시도: {}명", applicantCount);

        // when
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= applicantCount; i++) {
            try {
                ticketService.orderTicket(ticketId, (long) i);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }

        long endTime = System.currentTimeMillis();

        // then
        Ticket ticket = ticketService.getTicket(ticketId);
        long orderCount = ticketService.getOrderCount(ticketId);

        log.info("=== 순차 주문 테스트 결과 ===");
        log.info("소요 시간: {}ms", (endTime - startTime));
        log.info("전체 재고: 100개");
        log.info("순차 주문 시도: {}명", applicantCount);
        log.info("성공: {}명", successCount);
        log.info("실패: {}명", failCount);
        log.info("DB 주문 건수: {}건", orderCount);
        log.info("남은 재고: {}개", ticket.getRemainStock());
        log.info("========================");

        assertThat(orderCount).isEqualTo(100);
        assertThat(successCount).isEqualTo(100);
        assertThat(failCount).isEqualTo(50);
        assertThat(ticket.getRemainStock()).isEqualTo(0);
    }
}
