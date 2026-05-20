/**
 * 專案的啟動代理器 (Launcher Bypass)
 * 用於繞過 Java 9+ 對於直接執行 Application 子類別時的 JavaFX 模組檢查。
 * 這是 JavaFX 免模組開發最推薦、最簡單的啟動方式！
 */
public class Launcher {
    public static void main(String[] args) {
        // 呼叫實際的 App 啟動主入口
        App.main(args);
    }
}
