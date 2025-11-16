package wonotter.java_ticketrush_8.service.level2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.entity.TicketOrder;
import wonotter.java_ticketrush_8.exception.ErrorMessage;
import wonotter.java_ticketrush_8.repository.TicketOrderRepository;
import wonotter.java_ticketrush_8.repository.TicketRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServicePessimistic {

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;

    @Transactional(timeout = 300)
    public Long orderTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByIdWithLock(ticketId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.TICKET_NOT_FOUND.getMessage()));

        ticket.validateEventPeriod();

        ticketOrderRepository.findByTicketIdAndUserId(ticketId, userId)
                .ifPresent(order -> {
                    throw new IllegalArgumentException(ErrorMessage.TICKET_ALREADY_ORDERED.getMessage());
                });

        if (!ticket.hasStock()) {
            throw new IllegalArgumentException(ErrorMessage.STOCK_NOT_AVAILABLE.getMessage());
        }
        ticket.decreaseStock();

        TicketOrder order = TicketOrder.builder()
                .ticketId(ticketId)
                .userId(userId)
                .build();

        TicketOrder savedOrder = ticketOrderRepository.save(order);

        return savedOrder.getId();
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
}
