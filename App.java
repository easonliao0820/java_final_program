import repository.DataContext;
import repository.FileUserRepository;
import repository.FileEventRepository;
import repository.RelationshipResolver;
import controller.LoginController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 程式啟動入口 (Composition Root)
 * 唯一知道具體實作類別的地方：在此組裝依賴，其他所有類別只依賴介面。
 *
 *   FileUserRepository  ─┐
 *   FileEventRepository ─┼─► DataContext ─► Controller
 *   RelationshipResolver ┘
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Composition Root：組裝具體實作，注入 DataContext
            DataContext context = new DataContext(
                new FileUserRepository(),
                new FileEventRepository(),
                new RelationshipResolver()
            );

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setContext(context);   // DIP：注入抽象協調者

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
        Application.launch(App.class, args);
    }
}
