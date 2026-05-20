import repository.FileRepository;
import controller.LoginController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 整個 JavaFX 程式的啟動入口 (Main Entry Point)
 * 繼承自 javafx.application.Application，負責初始化資料庫並載入第一個 FXML 視圖。
 */
public class App extends Application {
    private static FileRepository repository;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. 初始化資料存取層（自動檢查資料夾與檔案，不存在則會建立預設測試資料）
            repository = new FileRepository();
            
            // 2. 載入登入畫面的 FXML 檔案
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();
            
            // 3. 取得 FXML 自動實例化的控制器，並注入資料庫依賴
            LoginController loginController = loader.getController();
            loginController.setRepository(repository);
            
            // 4. 設定視窗與場景
            primaryStage.setTitle("活動報名管理系統 - 登入");
            primaryStage.setScene(new Scene(root, 420, 390));
            primaryStage.setResizable(false);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("程式啟動時發生嚴重錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 啟動 JavaFX 應用程式 (使用顯式類別引導，相容性最高)
        Application.launch(App.class, args);
    }
}
