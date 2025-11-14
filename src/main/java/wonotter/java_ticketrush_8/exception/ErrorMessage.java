package wonotter.java_ticketrush_8.exception;

public enum ErrorMessage {

    STOCK_NOT_AVAILABLE("재고가 부족합니다."),
    EVENT_NOT_STARTED("이벤트가 아직 시작하지 않았습니다."),
    EVENT_ALREADY_END("이벤트가 이미 종료되었습니다.");

    private final String ERROR_PREFIX = "[ERROR] ";

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return ERROR_PREFIX + message;
    }
}
