package repository;

import model.User;
import model.Student;
import model.Organizer;
import model.Event;

import java.util.List;

/**
 * 物件關聯解析器 (SRP)
 * 單一職責：將從檔案讀入的 ID 字串解析並連結為雙向的 OOP 物件參考。
 * 不負責任何 I/O 操作。
 */
public class RelationshipResolver implements IRelationshipResolver {

    @Override
    public void resolveRelationships(List<User> users, List<Event> events) {
        clearAllAssociations(users, events);

        for (Event e : events) {
            resolveOrganizer(e, users);
            resolveParticipants(e, users);
            resolveCheckedInParticipants(e, users);
        }
    }

    private void clearAllAssociations(List<User> users, List<Event> events) {
        for (User u : users) {
            if (u instanceof Student)   ((Student) u).getRegisteredEvents().clear();
            else if (u instanceof Organizer) ((Organizer) u).getHostedEvents().clear();
        }
        for (Event e : events) {
            e.getParticipants().clear();
            e.getCheckedInParticipants().clear();
            e.setOrganizer(null);
        }
    }

    private void resolveOrganizer(Event e, List<User> users) {
        String orgId = e.getTempOrganizerId();
        if (orgId == null || orgId.isEmpty()) return;

        for (User u : users) {
            if (u instanceof Organizer && u.getId().equals(orgId)) {
                e.setOrganizer((Organizer) u);
                ((Organizer) u).getHostedEvents().add(e);
                return;
            }
        }
    }

    private void resolveParticipants(Event e, List<User> users) {
        List<String> partIds = e.getTempParticipantIds();
        if (partIds == null) return;

        for (String pid : partIds) {
            if (pid.trim().isEmpty()) continue;
            for (User u : users) {
                if (u instanceof Student && u.getId().equals(pid)) {
                    e.getParticipants().add((Student) u);
                    ((Student) u).getRegisteredEvents().add(e);
                    break;
                }
            }
        }
    }

    private void resolveCheckedInParticipants(Event e, List<User> users) {
        List<String> checkIds = e.getTempCheckedInParticipantIds();
        if (checkIds == null) return;

        for (String cid : checkIds) {
            if (cid.trim().isEmpty()) continue;
            for (User u : users) {
                if (u instanceof Student && u.getId().equals(cid)) {
                    e.getCheckedInParticipants().add((Student) u);
                    break;
                }
            }
        }
    }
}
