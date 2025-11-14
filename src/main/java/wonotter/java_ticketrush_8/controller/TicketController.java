package wonotter.java_ticketrush_8.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wonotter.java_ticketrush_8.dto.TicketCreateRequest;
import wonotter.java_ticketrush_8.dto.TicketOrderRequest;
import wonotter.java_ticketrush_8.dto.TicketOrderResponse;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.service.TicketService;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/create")
    public ResponseEntity<Long> createTicket(@RequestBody TicketCreateRequest request) {
        Long ticketId = ticketService.createTickets(request);
        return ResponseEntity.ok(ticketId);
    }

    @PostMapping("/{ticketId}/order")
    public ResponseEntity<TicketOrderResponse> orderTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketOrderRequest request
    ) {
        try {
            Long orderId = ticketService.orderTicket(ticketId, request.userId());
            return ResponseEntity.ok(TicketOrderResponse.success(orderId));
        } catch (IllegalArgumentException e) {
            log.warn("티켓 주문 실패 - 티켓 ID: {}, 사용자 ID: {}, 이유: {}",
                    ticketId, request.userId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(TicketOrderResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{ticketId}/orders/count")
    public ResponseEntity<Long> getOrderCount(@PathVariable Long ticketId) {
        long count = ticketService.getOrderCount(ticketId);
        return ResponseEntity.ok(count);
    }
}
