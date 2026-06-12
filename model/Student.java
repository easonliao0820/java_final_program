package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 學生類別 (Student)
 * 繼承自 User。維護已報名活動清單，並實作父類序列化鉤子。
 *
 * tempRegisteredEventIds 僅供 repository 層在兩階段載入時使用，
 * 控制器層不應存取此欄位。
 */
public class Student extends User {
    private List<Event> registeredEvents;

    /** 僅供 FileUserRepository / RelationshipResolver 使用的序列化暫存 */
    private List<String> tempRegisteredEventIds;

    public Student(String id, String password, String name) {
        super(id, password, name, "Student");
        this.registeredEvents = new ArrayList<>();
        this.tempRegisteredEventIds = new ArrayList<>();
    }

    public void registerEvent(Event e) {
        if (!registeredEvents.contains(e)) {
            registeredEvents.add(e);
        }
    }

    public void cancelEvent(Event e) {
        registeredEvents.remove(e);
    }

    @Override
    public void displayMenu() {
        System.out.println("====== 學生功能選單 (" + getName() + ") ======");
        System.out.println("1. 瀏覽活動並報名");
        System.out.println("2. 取消報名活動");
        System.out.println("3. 查詢已報名紀錄");
        System.out.println("=========================================");
    }

    /**
     * OCP 序列化鉤子：回傳已報名活動 ID 以分號串接，供 FileUserRepository 寫檔使用。
     */
    @Override
    public String getSerializedExtra() {
        return String.join(";", getRegisteredEventIds());
    }

    /**
     * OCP 反序列化鉤子：從 extra 字串還原 tempRegisteredEventIds，
     * 供 RelationshipResolver 建立物件關聯時使用。
     */
    @Override
    public void applySerializedExtra(String extra) {
        tempRegisteredEventIds.clear();
        if (extra != null && !extra.trim().isEmpty()) {
            tempRegisteredEventIds.addAll(Arrays.asList(extra.split(";")));
        }
    }

    public List<Event> getRegisteredEvents() { return registeredEvents; }
    public void setRegisteredEvents(List<Event> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    public List<String> getTempRegisteredEventIds() { return tempRegisteredEventIds; }

    /** 從已解析的物件關聯中提取 ID 清單（供序列化使用） */
    public List<String> getRegisteredEventIds() {
        List<String> ids = new ArrayList<>();
        for (Event e : registeredEvents) {
            ids.add(e.getId());
        }
        return ids;
    }
}
