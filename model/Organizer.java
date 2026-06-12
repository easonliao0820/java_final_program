package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 主辦者類別 (Organizer)
 * 繼承自 User。維護主辦活動清單，並實作父類序列化鉤子。
 */
public class Organizer extends User {
    private String organization;
    private List<Event> hostedEvents;

    public Organizer(String id, String password, String name, String organization) {
        super(id, password, name, "Organizer");
        this.organization = organization;
        this.hostedEvents = new ArrayList<>();
    }

    public void createEvent(Event e) {
        if (!hostedEvents.contains(e)) {
            hostedEvents.add(e);
            e.setOrganizer(this);
        }
    }

    public void editEvent(Event e) {
        if (!hostedEvents.contains(e)) {
            hostedEvents.add(e);
        }
        e.setOrganizer(this);
    }

    @Override
    public void displayMenu() {
        System.out.println("====== 主辦方管理後台 (" + getName() + " - " + organization + ") ======");
        System.out.println("1. 建立全新活動");
        System.out.println("2. 修改活動資料");
        System.out.println("3. 刪除活動項目");
        System.out.println("4. 查詢報名學生名單與匯出 CSV");
        System.out.println("===============================================================");
    }

    /**
     * OCP 序列化鉤子：回傳主辦單位名稱，供 FileUserRepository 寫檔使用。
     */
    @Override
    public String getSerializedExtra() {
        return organization;
    }

    public String getOrganization() { return organization; }
    public void   setOrganization(String organization) { this.organization = organization; }
    public List<Event> getHostedEvents() { return hostedEvents; }
    public void        setHostedEvents(List<Event> hostedEvents) { this.hostedEvents = hostedEvents; }
}
