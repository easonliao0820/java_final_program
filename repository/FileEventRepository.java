package repository;

import model.Event;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 活動檔案存取實作 (SRP)
 * 單一職責：只負責 data/events.txt 的讀寫，
 * 不處理使用者資料、不處理關聯解析。
 */
public class FileEventRepository implements IEventRepository {

    private static final String DATA_DIR   = "data";
    private static final String EVENTS_FILE = DATA_DIR + "/events.txt";

    public FileEventRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            new File(DATA_DIR).mkdirs();
            File file = new File(EVENTS_FILE);
            if (!file.exists()) {
                file.createNewFile();
                writeDefaultData();
            }
        } catch (IOException e) {
            System.err.println("初始化活動檔案錯誤: " + e.getMessage());
        }
    }

    @Override
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EVENTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) continue;

                String id           = parts[0];
                String title        = parts[1];
                String location     = parts[2];
                String time         = parts[3];
                int    capacity     = Integer.parseInt(parts[4]);
                String description  = parts[5];
                String organizerId  = parts[6];
                String participants = parts[7];
                String checkedIn    = parts.length > 8 ? parts[8] : "";

                Event e = new Event(id, title, location, time, capacity, description);
                e.setTempOrganizerId(organizerId);
                if (!participants.trim().isEmpty()) {
                    e.getTempParticipantIds().addAll(Arrays.asList(participants.split(";")));
                }
                if (!checkedIn.trim().isEmpty()) {
                    e.getTempCheckedInParticipantIds().addAll(Arrays.asList(checkedIn.split(";")));
                }
                events.add(e);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("載入活動檔案錯誤: " + e.getMessage());
        }
        return events;
    }

    @Override
    public void saveEvents(List<Event> events) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EVENTS_FILE))) {
            for (Event e : events) {
                String organizerId = e.getOrganizer() != null
                        ? e.getOrganizer().getId()
                        : e.getTempOrganizerId();
                pw.println(
                    e.getId()          + "|" +
                    e.getTitle()       + "|" +
                    e.getLocation()    + "|" +
                    e.getTime()        + "|" +
                    e.getCapacity()    + "|" +
                    e.getDescription() + "|" +
                    organizerId        + "|" +
                    String.join(";", e.getRegisteredStudentUsernames()) + "|" +
                    String.join(";", e.getCheckedInStudentUsernames())
                );
            }
        } catch (IOException ex) {
            System.err.println("儲存活動檔案錯誤: " + ex.getMessage());
        }
    }

    private void writeDefaultData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EVENTS_FILE))) {
            pw.println("E001|Java 程式設計工作坊|資工系一館 301 教室|2026-06-15 13:30|20|學習 Java 進階多型、IO 串流與 GUI 實戰的期末衝刺班！|admin|student1");
            pw.println("E002|AI 專題成果發表會|國際會議廳 A 廳|2026-06-20 09:00|30|全校最具代表性的 AI 創新專題展示與評分大會。|admin|");
            pw.println("E003|校園職涯就業博覽會|體育館二樓大禮堂|2026-06-25 10:00|2|超過 50 家頂尖企業現場徵才與諮詢，名額超稀有！|organizer1|student1;student2");
        } catch (IOException e) {
            System.err.println("寫入預設活動資料錯誤: " + e.getMessage());
        }
    }
}
