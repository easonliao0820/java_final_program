package model;

/**
 * 抽象類別：使用者 (User)
 * 遵循規格書規範，作為「學生」與「主辦者」的父類別，定義 id, name 等核心欄位，
 * 並包含抽象方法 displayMenu() 以便子類別實現其專屬選單。
 */
public abstract class User {
    protected String id;         // 學號 或 員工編號
    protected String name;       // 真實姓名
    protected String password;   // 登入密碼 (進階驗證功能)
    protected String role;       // 角色 ("Student" 或 "Organizer")

    public User(String id, String password, String name, String role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // 規格書規定的抽象方法：顯示選單
    public abstract void displayMenu();

    // Getters 和 Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return name + " (" + id + " - " + role + ")";
    }
}
