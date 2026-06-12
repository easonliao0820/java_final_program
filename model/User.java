package model;

/**
 * 抽象類別：使用者 (User)
 * 父類別定義 id, name, password, role 核心欄位，
 * 並宣告兩個供序列化/反序列化使用的鉤子方法：
 *   - getSerializedExtra()  : 將子類別特有欄位序列化為字串（消除 saveUsers 的 instanceof）
 *   - applySerializedExtra(): 讀取檔案後還原子類別特有狀態（消除 loadUsers 的 instanceof）
 */
public abstract class User {
    protected String id;
    protected String name;
    protected String password;
    protected String role;

    public User(String id, String password, String name, String role) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public abstract void displayMenu();

    /**
     * OCP 序列化鉤子：將子類別特有資料轉為 extra 欄位字串。
     * Student  → 已報名活動 ID 以分號串接
     * Organizer → 主辦單位名稱
     */
    public abstract String getSerializedExtra();

    /**
     * OCP 反序列化鉤子：從 extra 字串還原子類別特有狀態。
     * 預設為空操作，子類別視需要覆寫。
     */
    public void applySerializedExtra(String extra) { /* 預設 no-op */ }

    // Getters 和 Setters
    public String getId()       { return id; }
    public void   setId(String id)       { this.id = id; }
    public String getName()     { return name; }
    public void   setName(String name)   { this.name = name; }
    public String getPassword() { return password; }
    public void   setPassword(String password) { this.password = password; }
    public String getRole()     { return role; }
    public void   setRole(String role)   { this.role = role; }

    @Override
    public String toString() {
        return name + " (" + id + " - " + role + ")";
    }
}
