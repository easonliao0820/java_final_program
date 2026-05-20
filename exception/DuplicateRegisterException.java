package exception;

/**
 * 自訂例外：重複報名活動
 * 當學生嘗試重複報名同一個活動時拋出此例外。
 */
public class DuplicateRegisterException extends Exception {
    
    private String eventTitle;
    
    public DuplicateRegisterException(String message) {
        super(message);
    }
    
    public DuplicateRegisterException(String message, String eventTitle) {
        super(message);
        this.eventTitle = eventTitle;
    }
    
    public String getEventTitle() {
        return eventTitle;
    }
}
