package model;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用者工廠 (OCP)
 * 以角色字串為 key 查表建立 User 物件，消除所有
 * "if Student else if Organizer" 的 if-else 鏈。
 *
 * 新增角色只需呼叫 register() 一次，無需修改任何現有程式碼。
 */
public class UserFactory {

    @FunctionalInterface
    public interface UserCreator {
        User create(String id, String password, String name, String extra);
    }

    private static final Map<String, UserCreator> registry = new HashMap<>();

    static {
        registry.put("Student",   (id, pw, name, extra) -> new Student(id, pw, name));
        registry.put("Organizer", (id, pw, name, extra) -> new Organizer(id, pw, name, extra));
    }

    /** 開放擴充：新增角色時呼叫此方法，無需修改工廠本身 */
    public static void register(String role, UserCreator creator) {
        registry.put(role, creator);
    }

    public static User create(String role, String id, String password, String name, String extra) {
        UserCreator creator = registry.get(role);
        if (creator == null) {
            throw new IllegalArgumentException("未知的使用者角色: " + role);
        }
        return creator.create(id, password, name, extra);
    }

    public static boolean isRegistered(String role) {
        return registry.containsKey(role);
    }
}
