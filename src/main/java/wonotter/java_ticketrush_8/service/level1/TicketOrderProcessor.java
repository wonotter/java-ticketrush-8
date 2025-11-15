package wonotter.java_ticketrush_8.service.level1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.entity.TicketOrder;
import wonotter.java_ticketrush_8.exception.ErrorMessage;
import wonotter.java_ticketrush_8.repository.TicketOrderRepository;
import wonotter.java_ticketrush_8.repository.TicketRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketOrderProcessor {

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;

    @Transactional
    public Long processOrder(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
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
}
