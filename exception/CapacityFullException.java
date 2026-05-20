package exception;

/**
 * 自訂例外：活動人數已滿
 * 當學生嘗試報名一個已經達到人數上限的活動時拋出此例外。
 */
public class CapacityFullException extends Exception {
    
    private String eventTitle;
    
    public CapacityFullException(String message) {
        super(message);
    }
    
    public CapacityFullException(String message, String eventTitle) {
        super(message);
        this.eventTitle = eventTitle;
    }
    
    public String getEventTitle() {
        return eventTitle;
    }
}
