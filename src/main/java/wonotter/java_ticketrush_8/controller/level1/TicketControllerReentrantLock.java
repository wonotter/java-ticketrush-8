package wonotter.java_ticketrush_8.controller.level1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wonotter.java_ticketrush_8.dto.TicketOrderRequest;
import wonotter.java_ticketrush_8.dto.TicketOrderResponse;
import wonotter.java_ticketrush_8.entity.Ticket;
import wonotter.java_ticketrush_8.service.level1.TicketServiceReentrantLock;

@RestController
@RequestMapping("/api/level1/reentrant/tickets/")
@RequiredArgsConstructor
public class TicketControllerReentrantLock {

    private final TicketServiceReentrantLock ticketServiceReentrantLock;

    @PostMapping("{ticketId}/order")
    public ResponseEntity<TicketOrderResponse> orderTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketOrderRequest request
    ) {
        try {
            Long orderId = ticketServiceReentrantLock.orderTicket(ticketId, request.userId());
            return ResponseEntity.ok(TicketOrderResponse.success(orderId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(TicketOrderResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long ticketId) {
        Ticket ticket = ticketServiceReentrantLock.getTicket(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{ticketId}/orders/count")
    public ResponseEntity<Long> getOrderCount(@PathVariable Long ticketId) {
        long count = ticketServiceReentrantLock.getOrderCount(ticketId);
        return ResponseEntity.ok(count);
    }
}
