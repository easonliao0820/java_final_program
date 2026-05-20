package repository;

import model.User;
import model.Student;
import model.Organizer;
import model.Event;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 資料存取層 (Data Access Layer)
 * 負責「使用者」與「活動」的檔案讀寫持久化，並實作關聯性解析。
 * 使用 FileReader/BufferedReader 與 FileWriter/PrintWriter。
 */
public class FileRepository {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";
    private static final String EVENTS_FILE = DATA_DIR + "/events.txt";

    public FileRepository() {
        ensureDataDirectoryAndFilesExist();
    }

    private void ensureDataDirectoryAndFilesExist() {
        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) {
                usersFile.createNewFile();
                writeDefaultUsers();
            }

            File eventsFile = new File(EVENTS_FILE);
            if (!eventsFile.exists()) {
                eventsFile.createNewFile();
                writeDefaultEvents();
            }
        } catch (IOException e) {
            System.err.println("初始化資料檔案時發生錯誤: " + e.getMessage());
        }
    }

    private void writeDefaultUsers() {
        List<User> defaultUsers = new ArrayList<>();
        
        Student s1 = new Student("student1", "password", "王小明");
        s1.getTempRegisteredEventIds().add("E001");
        s1.getTempRegisteredEventIds().add("E003");
        
        Student s2 = new Student("student2", "password", "李婷婷");
        s2.getTempRegisteredEventIds().add("E003");

        defaultUsers.add(s1);
        defaultUsers.add(s2);
        
        defaultUsers.add(new Organizer("admin", "admin", "系學會會長", "資訊工程系學會"));
        defaultUsers.add(new Organizer("organizer1", "password", "陳老師", "課外活動指導組"));

        saveUsers(defaultUsers);
    }

    private void writeDefaultEvents() {
        List<Event> defaultEvents = new ArrayList<>();
        
        // E001
        Event e1 = new Event("E001", "Java 程式設計工作坊", "資工系一館 301 教室", "2026-06-15 13:30", 20, 
                "學習 Java 進階多型、IO 串流與 GUI 實戰的期末衝刺班！");
        e1.setTempOrganizerId("admin");
        e1.getTempParticipantIds().add("student1");
        
        // E002
        Event e2 = new Event("E002", "AI 專題成果發表會", "國際會議廳 A 廳", "2026-06-20 09:00", 30, 
                "全校最具代表性的 AI 創新專題展示與評分大會。");
        e2.setTempOrganizerId("admin");
        
        // E003
        Event e3 = new Event("E003", "校園職涯就業博覽會", "體育館二樓大禮堂", "2026-06-25 10:00", 2, 
                "超過 50 家頂尖企業現場徵才與諮詢，名額超稀有！");
        e3.setTempOrganizerId("organizer1");
        e3.getTempParticipantIds().add("student1");
        e3.getTempParticipantIds().add("student2");

        defaultEvents.add(e1);
        defaultEvents.add(e2);
        defaultEvents.add(e3);

        saveEvents(defaultEvents);
    }

    /**
     * 從 users.txt 載入所有使用者
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) continue;

                String role = parts[0];
                String id = parts[1];
                String password = parts[2];
                String name = parts[3];
                String lastCol = parts[4];

                if ("Student".equals(role)) {
                    Student s = new Student(id, password, name);
                    if (!lastCol.trim().isEmpty()) {
                        s.getTempRegisteredEventIds().addAll(Arrays.asList(lastCol.split(";")));
                    }
                    users.add(s);
                } else if ("Organizer".equals(role)) {
                    users.add(new Organizer(id, password, name, lastCol));
                }
            }
        } catch (IOException e) {
            System.err.println("載入使用者檔案錯誤: " + e.getMessage());
        }
        return users;
    }

    /**
     * 將使用者儲存至 users.txt
     */
    public void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                StringBuilder sb = new StringBuilder();
                sb.append(u.getRole()).append("|")
                  .append(u.getId()).append("|")
                  .append(u.getPassword()).append("|")
                  .append(u.getName()).append("|");

                if (u instanceof Student) {
                    Student s = (Student) u;
                    sb.append(String.join(";", s.getRegisteredEventIds()));
                } else if (u instanceof Organizer) {
                    Organizer o = (Organizer) u;
                    sb.append(o.getOrganization());
                }
                pw.println(sb.toString());
            }
        } catch (IOException e) {
            System.err.println("儲存使用者檔案錯誤: " + e.getMessage());
        }
    }

    /**
     * 從 events.txt 載入所有活動
     */
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EVENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) continue;

                String id = parts[0];
                String title = parts[1];
                String location = parts[2];
                String time = parts[3];
                int capacity = Integer.parseInt(parts[4]);
                String description = parts[5];
                String organizerId = parts[6];
                String participantsStr = parts[7];

                Event e = new Event(id, title, location, time, capacity, description);
                e.setTempOrganizerId(organizerId);
                
                if (!participantsStr.trim().isEmpty()) {
                    e.getTempParticipantIds().addAll(Arrays.asList(participantsStr.split(";")));
                }
                events.add(e);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("載入活動檔案錯誤: " + e.getMessage());
        }
        return events;
    }

    /**
     * 將活動儲存至 events.txt
     */
    public void saveEvents(List<Event> events) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EVENTS_FILE))) {
            for (Event e : events) {
                StringBuilder sb = new StringBuilder();
                sb.append(e.getId()).append("|")
                  .append(e.getTitle()).append("|")
                  .append(e.getLocation()).append("|")
                  .append(e.getTime()).append("|")
                  .append(e.getCapacity()).append("|")
                  .append(e.getDescription()).append("|")
                  .append(e.getOrganizer() != null ? e.getOrganizer().getId() : e.getTempOrganizerId()).append("|")
                  .append(String.join(";", e.getRegisteredStudentUsernames()));
                pw.println(sb.toString());
            }
        } catch (IOException ex) {
            System.err.println("儲存活動檔案錯誤: " + ex.getMessage());
        }
    }

    /**
     * 👑 核心亮點：關聯解析器 (In-Memory Relationship Resolver)
     * 將讀取出的扁平 Id 字串動態解析並連結為雙向的 OOP 物件參考，
     * 完美實現規格書要求的 List<Student> participants 與 List<Event> registeredEvents 物件導向對稱參考。
     */
    public void resolveRelationships(List<User> users, List<Event> events) {
        // 先清空所有舊關聯（避免重複執行時重複堆疊）
        for (User u : users) {
            if (u instanceof Student) {
                ((Student) u).getRegisteredEvents().clear();
            } else if (u instanceof Organizer) {
                ((Organizer) u).getHostedEvents().clear();
            }
        }
        for (Event e : events) {
            e.getParticipants().clear();
            e.setOrganizer(null);
        }

        // 開始解析關聯
        for (Event e : events) {
            // 1. 解析活動的 Organizer 物件
            String orgId = e.getTempOrganizerId();
            if (orgId != null && !orgId.isEmpty()) {
                for (User u : users) {
                    if (u instanceof Organizer && u.getId().equals(orgId)) {
                        e.setOrganizer((Organizer) u);
                        ((Organizer) u).getHostedEvents().add(e); // 雙向關聯
                        break;
                    }
                }
            }

            // 2. 解析活動的 Student 參與人 (participants) 物件
            List<String> partIds = e.getTempParticipantIds();
            if (partIds != null) {
                for (String pid : partIds) {
                    if (pid.trim().isEmpty()) continue;
                    for (User u : users) {
                        if (u instanceof Student && u.getId().equals(pid)) {
                            e.getParticipants().add((Student) u);
                            ((Student) u).getRegisteredEvents().add(e); // 雙向關聯
                            break;
                        }
                    }
                }
            }
        }
    }
}
