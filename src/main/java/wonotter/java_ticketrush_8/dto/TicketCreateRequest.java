package wonotter.java_ticketrush_8.dto;

import java.time.LocalDateTime;

public record TicketCreateRequest(
        String name,
        String description,
        Integer totalStock,
        LocalDateTime startAt,
        LocalDateTime endAt
) {

    public TicketCreateRequest(String name, String description, Integer totalStock) {
        this(name, description, totalStock, null, null);
    }
}
