package repository;

import model.User;
import model.Event;
import java.util.List;

/**
 * 物件關聯解析介面 (SRP + DIP)
 * 將「把 ID 字串解析為物件參考」的職責從 FileRepository 中獨立出來。
 */
public interface IRelationshipResolver {
    void resolveRelationships(List<User> users, List<Event> events);
}
