package wonotter.java_ticketrush_8.dto;

public record TicketOrderResponse(
        boolean success,
        String message,
        Long orderId
) {

    public static TicketOrderResponse success(Long orderId) {
        return new TicketOrderResponse(true, "티켓 주문에 성공했습니다.", orderId);
    }

    public static TicketOrderResponse fail(String message) {
        return new TicketOrderResponse(false, message, null);
    }
}
