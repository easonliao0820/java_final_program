package repository;

import model.User;
import java.util.List;

/**
 * 使用者資料存取介面 (ISP + DIP)
 * 控制器依賴此介面而非具體 FileUserRepository，
 * 未來可輕鬆替換為資料庫或其他持久化實作。
 */
public interface IUserRepository {
    List<User> loadUsers();
    void saveUsers(List<User> users);
}
