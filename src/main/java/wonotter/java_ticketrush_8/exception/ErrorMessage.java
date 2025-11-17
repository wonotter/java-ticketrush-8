package wonotter.java_ticketrush_8.exception;

public enum ErrorMessage {

    STOCK_NOT_AVAILABLE("재고가 부족합니다."),
    EVENT_NOT_STARTED("이벤트가 아직 시작하지 않았습니다."),
    EVENT_ALREADY_END("이벤트가 이미 종료되었습니다."),

    TICKET_NOT_FOUND("존재하지 않는 티켓입니다."),
    TICKET_ALREADY_ORDERED("이미 주문한 티켓입니다"),
    TICKET_ORDER_FAILED("티켓 주문에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final String ERROR_PREFIX = "[ERROR] ";

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return ERROR_PREFIX + message;
    }
}
