package repository;

import model.Event;
import java.util.List;

/**
 * 活動資料存取介面 (ISP + DIP)
 * 與 IUserRepository 分離，符合介面隔離原則——
 * 只需要使用者操作的客戶端不必依賴活動相關方法。
 */
public interface IEventRepository {
    List<Event> loadEvents();
    void saveEvents(List<Event> events);
}
