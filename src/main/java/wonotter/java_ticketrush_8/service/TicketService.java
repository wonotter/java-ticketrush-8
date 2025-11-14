package wonotter.java_ticketrush_8.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonotter.java_ticketrush_8.dto.TicketCreateRequest;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.entity.TicketOrder;
import wonotter.java_ticketrush_8.exception.ErrorMessage;
import wonotter.java_ticketrush_8.repository.TicketOrderRepository;
import wonotter.java_ticketrush_8.repository.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;

    @Transactional
    public Long createTickets(TicketCreateRequest request) {
        Ticket ticket = Ticket.builder()
                .name(request.name())
                .description(request.description())
                .totalStock(request.totalStock())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();

        Ticket savedTickets = ticketRepository.save(ticket);
        log.info("티켓 생성 완료 - ID {}, 재고: {}", savedTickets.getId(), savedTickets.getTotalStock());

        return savedTickets.getId();
    }

    // 선착순 티켓 구매 로직: 현재 동시성 제어가 없으므로 문제가 발생함!
    // timeout: 5분 내에 트랜잭션이 완료되지 않으면 롤백 진행
    @Transactional(timeout = 300)
    public Long orderTicket(Long ticketId, Long userId) {
        // 여러 스레드가 동시에 같은 값을 읽을 수 있음
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.TICKET_NOT_FOUND.getMessage()));

        ticket.validateEventPeriod();

        // 이미 구매했는지 확인하기 전에 다른 스레드가 끼어들 수 있음
        ticketOrderRepository.findByTicketIdAndUserId(ticketId, userId)
                .ifPresent(order -> {
                    throw new IllegalArgumentException(ErrorMessage.TICKET_ALREADY_ORDERED.getMessage());
                });

        // 재고 확인과 감소 사이에 다른 스레드가 끼어들 수 있음
        if (!ticket.hasStock()) {
            throw new IllegalArgumentException(ErrorMessage.STOCK_NOT_AVAILABLE.getMessage());
        }
        ticket.decreaseStock();

        TicketOrder order = TicketOrder.builder()
                .ticketId(ticketId)
                .userId(userId)
                .build();

        TicketOrder savedOrder = ticketOrderRepository.save(order);
        log.info("티켓 주문 완료 - 주문 ID: {}, 티켓 ID: {}, 사용자 ID: {}, 남은 재고: {}",
                savedOrder.getId(), ticketId, userId, ticket.getRemainStock());

        return savedOrder.getId();
    }

    public Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.TICKET_NOT_FOUND.getMessage()));
    }

    public long getOrderCount(Long ticketId) {
        return ticketOrderRepository.countByTicketId(ticketId);
    }
}
