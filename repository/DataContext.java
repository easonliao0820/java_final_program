package repository;

import model.User;
import model.Event;
import java.util.List;

/**
 * 資料協調者 (Data Coordinator)
 * 組合三個窄介面，為控制器提供統一的資料存取入口，
 * 但控制器仍可透過 getter 只依賴它真正需要的介面，符合 DIP 與 ISP。
 *
 * 控制器依賴 DataContext 而非任何具體實作，
 * 更換底層儲存（如改為資料庫）只需替換建構時注入的實作物件。
 */
public class DataContext {

    private final IUserRepository userRepository;
    private final IEventRepository eventRepository;
    private final IRelationshipResolver resolver;

    public DataContext(IUserRepository userRepository,
                       IEventRepository eventRepository,
                       IRelationshipResolver resolver) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.resolver = resolver;
    }

    // ── 窄介面 getter（ISP：客戶端可只取用它需要的介面）─────────────
    public IUserRepository getUserRepository()   { return userRepository; }
    public IEventRepository getEventRepository() { return eventRepository; }

    // ── 便捷委派方法（讓控制器程式碼保持簡潔）─────────────────────────
    public List<User>  loadUsers()                    { return userRepository.loadUsers(); }
    public void        saveUsers(List<User> users)    { userRepository.saveUsers(users); }
    public List<Event> loadEvents()                   { return eventRepository.loadEvents(); }
    public void        saveEvents(List<Event> events) { eventRepository.saveEvents(events); }

    public void resolveRelationships(List<User> users, List<Event> events) {
        resolver.resolveRelationships(users, events);
    }
}
