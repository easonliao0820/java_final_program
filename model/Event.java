package model;

import exception.CapacityFullException;
import exception.DuplicateRegisterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 活動類別 (Event)
 * 欄位：id, title, location, time, capacity, organizer, participants。
 *
 * tempOrganizerId / tempParticipantIds 僅供 repository 層兩階段載入使用，
 * 控制器層不應存取這兩個欄位。
 */
public class Event {
    private String id;
    private String title;
    private String location;
    private String time;
    private int capacity;
    private String description;
    private Organizer organizer;
    private List<Student> participants;

    /** 僅供 FileEventRepository / RelationshipResolver 使用的序列化暫存 */
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

    public boolean isFull() {
        return participants.size() >= capacity;
    }

    public int getRegisteredCount() {
        return participants.size();
    }

    public String getStatus() {
        return isFull() ? "額滿" : "開放中";
    }

    public void registerStudent(Student student) throws CapacityFullException, DuplicateRegisterException {
        if (participants.contains(student)) {
            throw new DuplicateRegisterException("您已經報名過此活動囉！", this.title);
        }
        if (isFull()) {
            throw new CapacityFullException("很抱歉，此活動報名人數已滿！", this.title);
        }
        participants.add(student);
        student.registerEvent(this);
    }

    public void cancelStudent(Student student) {
        if (participants.contains(student)) {
            participants.remove(student);
            student.cancelEvent(this);
        }
    }

    /** 序列化輔助：從已解析的物件關聯提取學生 ID 清單（無 fallback，確保關係已解析後才呼叫） */
    public List<String> getRegisteredStudentUsernames() {
        List<String> ids = new ArrayList<>();
        for (Student s : participants) {
            ids.add(s.getId());
        }
        return ids;
    }

    // Getters 和 Setters
    public String getId()          { return id; }
    public void   setId(String id) { this.id = id; }

    public String getTitle()              { return title; }
    public void   setTitle(String title)  { this.title = title; }

    public String getLocation()                 { return location; }
    public void   setLocation(String location)  { this.location = location; }

    public String getTime()             { return time; }
    public void   setTime(String time)  { this.time = time; }

    public int  getCapacity()               { return capacity; }
    public void setCapacity(int capacity)   { this.capacity = capacity; }

    public String getDescription()                    { return description; }
    public void   setDescription(String description)  { this.description = description; }

    public Organizer getOrganizer()                   { return organizer; }
    public void      setOrganizer(Organizer organizer){ this.organizer = organizer; }

    public List<Student> getParticipants()                      { return participants; }
    public void          setParticipants(List<Student> list)    { this.participants = list; }

    public String getTempOrganizerId()                          { return tempOrganizerId; }
    public void   setTempOrganizerId(String tempOrganizerId)    { this.tempOrganizerId = tempOrganizerId; }

    public List<String> getTempParticipantIds()                 { return tempParticipantIds; }
    public void         setTempParticipantIds(List<String> ids) { this.tempParticipantIds = ids; }
}
