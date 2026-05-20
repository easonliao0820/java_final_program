package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 主辦者類別 (Organizer)
 * 繼承自 User。遵循規格書設計：
 * - 維護主辦活動物件清單 private List<Event> hostedEvents;
 * - 提供 createEvent(Event e) 與 editEvent(Event e) 方法。
 */
public class Organizer extends User {
    private String organization; // 隸屬單位/組織名稱
    private List<Event> hostedEvents;

    public Organizer(String id, String password, String name, String organization) {
        super(id, password, name, "Organizer");
        this.organization = organization;
        this.hostedEvents = new ArrayList<>();
    }

    // 規格書要求的核心主辦方法：建立活動項目
    public void createEvent(Event e) {
        if (!hostedEvents.contains(e)) {
            hostedEvents.add(e);
            e.setOrganizer(this); // 雙向綁定
        }
    }

    // 規格書要求的核心修改方法：編輯活動項目
    public void editEvent(Event e) {
        // 在記憶體中確認或標記此活動由本主辦方主導
        if (!hostedEvents.contains(e)) {
            hostedEvents.add(e);
        }
        e.setOrganizer(this);
    }

    // 實作 User 抽象選單方法
    @Override
    public void displayMenu() {
        System.out.println("====== 主辦方管理後台 (" + getName() + " - " + organization + ") ======");
        System.out.println("1. 建立全新活動");
        System.out.println("2. 修改活動資料");
        System.out.println("3. 刪除活動項目");
        System.out.println("4. 查詢報名學生名單與匯出 CSV");
        System.out.println("===============================================================");
    }

    // Getters 和 Setters
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<Event> getHostedEvents() {
        return hostedEvents;
    }

    public void setHostedEvents(List<Event> hostedEvents) {
        this.hostedEvents = hostedEvents;
    }
}
