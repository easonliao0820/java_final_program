package repository;

import model.User;
import model.Event;
import java.util.List;

/**
 * 向後相容薄包裝 (Facade)
 * 組合新的三個職責類別，並委派所有呼叫。
 * 新程式碼請直接使用 DataContext；此類別僅為相容舊呼叫保留。
 */
public class FileRepository {

    private final DataContext context;

    public FileRepository() {
        this.context = new DataContext(
            new FileUserRepository(),
            new FileEventRepository(),
            new RelationshipResolver()
        );
    }

    public DataContext getContext() { return context; }

    public List<User>  loadUsers()                          { return context.loadUsers(); }
    public void        saveUsers(List<User> users)          { context.saveUsers(users); }
    public List<Event> loadEvents()                         { return context.loadEvents(); }
    public void        saveEvents(List<Event> events)       { context.saveEvents(events); }
    public void        resolveRelationships(List<User> u, List<Event> e) {
        context.resolveRelationships(u, e);
    }
}
