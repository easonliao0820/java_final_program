package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 學生類別 (Student)
 * 繼承自 User。遵循規格書設計：
 * - 維護已報名活動物件清單 private List<Event> registeredEvents;
 * - 提供 registerEvent(Event e) 與 cancelEvent(Event e) 方法。
 */
public class Student extends User {
    private List<Event> registeredEvents;
    
    // 用於檔案載入時暫存活動代碼 (稍後由 Repository 解析關聯)
    private List<String> tempRegisteredEventIds;

    public Student(String id, String password, String name) {
        super(id, password, name, "Student");
        this.registeredEvents = new ArrayList<>();
        this.tempRegisteredEventIds = new ArrayList<>();
    }

    // 規格書要求的核心報名方法：報名活動
    public void registerEvent(Event e) {
        if (!registeredEvents.contains(e)) {
            registeredEvents.add(e);
        }
    }

    // 規格書要求的核心取消方法：取消報名活動
    public void cancelEvent(Event e) {
        registeredEvents.remove(e);
    }

    // 實作 User 抽象選單方法
    @Override
    public void displayMenu() {
        System.out.println("====== 學生功能選單 (" + getName() + ") ======");
        System.out.println("1. 瀏覽活動並報名");
        System.out.println("2. 取消報名活動");
        System.out.println("3. 查詢已報名紀錄");
        System.out.println("=========================================");
    }

    // Getters 和 Setters
    public List<Event> getRegisteredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(List<Event> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    public List<String> getTempRegisteredEventIds() {
        return tempRegisteredEventIds;
    }

    public void setTempRegisteredEventIds(List<String> tempRegisteredEventIds) {
        this.tempRegisteredEventIds = tempRegisteredEventIds;
    }
    
    /**
     * 動態回傳已報名活動的 ID 清單（用於檔案寫入序列化）
     */
    public List<String> getRegisteredEventIds() {
        List<String> ids = new ArrayList<>();
        for (Event e : registeredEvents) {
            ids.add(e.getId());
        }
        // 若尚未解析關聯，回傳暫存的 ID
        if (ids.isEmpty() && tempRegisteredEventIds != null) {
            return tempRegisteredEventIds;
        }
        return ids;
    }
}
