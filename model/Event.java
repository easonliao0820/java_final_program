package model;

import exception.CapacityFullException;
import exception.DuplicateRegisterException;

import java.util.ArrayList;
import java.util.List;

/**
 * 活動類別 (Event)
 * 遵循規格書設計：
 * - 欄位：id, title, location, time, capacity, organizer, participants。
 * - 方法：學生報名與取消報名（基於物件導向參考傳遞，會拋出額滿/重複自訂例外）。
 */
public class Event {
    private String id;
    private String title;
    private String location; // 活動舉辦地點 (規格書規定)
    private String time;     // 活動舉辦時間 (規格書規定)
    private int capacity;    // 人數上限
    private String description; // 詳細活動介紹 (輔助 GUI 呈現)
    private Organizer organizer; // 主辦者物件 (規格書規定)
    private List<Student> participants; // 參與學生清單 (規格書規定)

    // 用於檔案讀取時暫存的 ID
    private String tempOrganizerId;
    private List<String> tempParticipantIds;

    public Event(String id, String title, String location, String time, int capacity, String description) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.time = time;
        this.capacity = capacity;
        this.description = description;
        this.participants = new ArrayList<>();
        this.tempParticipantIds = new ArrayList<>();
    }

    /**
     * 檢查活動是否額滿
     */
    public boolean isFull() {
        return participants.size() >= capacity;
    }

    /**
     * 取得已報名學生人數
     */
    public int getRegisteredCount() {
        return participants.size();
    }

    /**
     * 取得活動目前的狀態描述 (進階功能：活動狀態 - 開放中、額滿)
     */
    public String getStatus() {
        return isFull() ? "額滿" : "開放中";
    }

    /**
     * 核心報名邏輯（純物件導向傳遞，拋出異常以供控制器攔截）
     */
    public void registerStudent(Student student) throws CapacityFullException, DuplicateRegisterException {
        if (participants.contains(student)) {
            throw new DuplicateRegisterException("您已經報名過此活動囉！", this.title);
        }
        if (isFull()) {
            throw new CapacityFullException("很抱歉，此活動報名人數已滿！", this.title);
        }
        
        participants.add(student);
        student.registerEvent(this); // 雙向維護
    }

    /**
     * 核心取消報名邏輯
     */
    public void cancelStudent(Student student) {
        if (participants.contains(student)) {
            participants.remove(student);
            student.cancelEvent(this); // 雙向維護
        }
    }

    // Getters 和 Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public List<Student> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Student> participants) {
        this.participants = participants;
    }

    public String getTempOrganizerId() {
        return tempOrganizerId;
    }

    public void setTempOrganizerId(String tempOrganizerId) {
        this.tempOrganizerId = tempOrganizerId;
    }

    public List<String> getTempParticipantIds() {
        return tempParticipantIds;
    }

    public void setTempParticipantIds(List<String> tempParticipantIds) {
        this.tempParticipantIds = tempParticipantIds;
    }

    /**
     * 輔助序列化：動態提取報名學生的 ID 清單
     */
    public List<String> getRegisteredStudentUsernames() {
        List<String> ids = new ArrayList<>();
        for (Student s : participants) {
            ids.add(s.getId());
        }
        if (ids.isEmpty() && tempParticipantIds != null) {
            return tempParticipantIds;
        }
        return ids;
    }
}
