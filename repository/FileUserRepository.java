package repository;

import model.User;
import model.UserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用者檔案存取實作 (SRP)
 * 單一職責：只負責 data/users.txt 的讀寫，
 * 不處理活動資料、不處理關聯解析。
 */
public class FileUserRepository implements IUserRepository {

    private static final String DATA_DIR  = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.txt";

    public FileUserRepository() {
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            new File(DATA_DIR).mkdirs();
            File file = new File(USERS_FILE);
            if (!file.exists()) {
                file.createNewFile();
                writeDefaultData();
            }
        } catch (IOException e) {
            System.err.println("初始化使用者檔案錯誤: " + e.getMessage());
        }
    }

    /**
     * 載入所有使用者。
     * 使用 UserFactory 消除 if-else（OCP），
     * 透過 applySerializedExtra 鉤子還原子類別特有狀態（OCP）。
     */
    @Override
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) continue;

                String role     = parts[0];
                String id       = parts[1];
                String password = parts[2];
                String name     = parts[3];
                String extra    = parts[4];

                if (!UserFactory.isRegistered(role)) continue;

                User user = UserFactory.create(role, id, password, name, extra);
                user.applySerializedExtra(extra); // OCP 鉤子：還原子類別狀態
                users.add(user);
            }
        } catch (IOException e) {
            System.err.println("載入使用者檔案錯誤: " + e.getMessage());
        }
        return users;
    }

    /**
     * 儲存所有使用者。
     * 透過 getSerializedExtra 鉤子序列化子類別狀態，無 instanceof（OCP）。
     */
    @Override
    public void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                pw.println(u.getRole()     + "|" +
                           u.getId()       + "|" +
                           u.getPassword() + "|" +
                           u.getName()     + "|" +
                           u.getSerializedExtra()); // OCP 鉤子：序列化子類別狀態
            }
        } catch (IOException e) {
            System.err.println("儲存使用者檔案錯誤: " + e.getMessage());
        }
    }

    private void writeDefaultData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println("Student|student1|password|王小明|E001;E003");
            pw.println("Student|student2|password|李婷婷|E003");
            pw.println("Organizer|admin|admin|系學會會長|資訊工程系學會");
            pw.println("Organizer|organizer1|password|陳老師|課外活動指導組");
        } catch (IOException e) {
            System.err.println("寫入預設使用者資料錯誤: " + e.getMessage());
        }
    }
}
