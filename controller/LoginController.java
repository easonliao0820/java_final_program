package controller;

import repository.FileRepository;
import model.User;
import model.Student;
import model.Organizer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 登入控制器 (Login Controller)
 * 負責「登入畫面」與「註冊視窗」的 JavaFX 事件綁定與登入/註冊核心邏輯。
 */
public class LoginController {
    private FileRepository repository;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton studentRadio;
    @FXML private RadioButton organizerRadio;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // 無引數建構子：讓 JavaFX FXMLLoader 可以自動反射實例化
    public LoginController() {}

    /**
     * 注入 repository 資料持久層依賴
     */
    public void setRepository(FileRepository repository) {
        this.repository = repository;
    }

    @FXML
    public void initialize() {
        // 此方法在 FXML 載入完成後會自動被呼叫
    }

    /**
     * 處理登入動作
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String selectedRole = studentRadio.isSelected() ? "Student" : "Organizer";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "提示", "帳號與密碼欄位不得為空！");
            return;
        }

        List<User> users = repository.loadUsers();
        User matchedUser = null;

        for (User u : users) {
            if (u.getId().equals(username) && u.getPassword().equals(password)) {
                matchedUser = u;
                break;
            }
        }

        if (matchedUser == null) {
            showAlert(AlertType.ERROR, "登入失敗", "帳號或密碼輸入錯誤，請再試一次。");
        } else if (!matchedUser.getRole().equals(selectedRole)) {
            showAlert(AlertType.WARNING, "身份錯誤", "登入角色選擇不正確！請確認您的身份類型。");
        } else {
            try {
                // 1. 關閉目前的登入視窗
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

                // 2. 依身份載入並開啟對應的後續畫面
                if (matchedUser instanceof Student) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/student-view.fxml"));
                    Parent root = loader.load();
                    
                    StudentController sc = loader.getController();
                    sc.initData(repository, (Student) matchedUser);

                    Stage studentStage = new Stage();
                    studentStage.setTitle("活動報名管理系統 - 學生專區");
                    studentStage.setScene(new Scene(root, 850, 600));
                    studentStage.show();
                } else if (matchedUser instanceof Organizer) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/organizer-view.fxml"));
                    Parent root = loader.load();
                    
                    AdminController ac = loader.getController();
                    ac.initData(repository, (Organizer) matchedUser);

                    Stage adminStage = new Stage();
                    adminStage.setTitle("活動報名管理系統 - 主辦者後台");
                    adminStage.setScene(new Scene(root, 950, 620));
                    adminStage.show();
                }
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "載入失敗", "系統在載入主介面時發生錯誤！\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 以 JavaFX Dialog 顯示註冊視窗
     */
    @FXML
    private void handleRegisterDialog(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("註冊新帳號");
        dialog.setHeaderText("請輸入您的個人資訊以註冊帳號：");

        // 設定 Dialog 按鈕
        ButtonType registerButtonType = new ButtonType("註冊", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        // 建立註冊表單 GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 30));

        TextField regUserField = new TextField();
        regUserField.setPromptText("英文或數字帳號");
        PasswordField regPassField = new PasswordField();
        regPassField.setPromptText("密碼");
        TextField regNameField = new TextField();
        regNameField.setPromptText("真實姓名");

        RadioButton regStudentRadio = new RadioButton("學生");
        regStudentRadio.setSelected(true);
        RadioButton regOrgRadio = new RadioButton("主辦者");
        ToggleGroup regRoleGroup = new ToggleGroup();
        regStudentRadio.setToggleGroup(regRoleGroup);
        regOrgRadio.setToggleGroup(regRoleGroup);

        HBox roleBox = new HBox(15, regStudentRadio, regOrgRadio);

        TextField extraField = new TextField();
        extraField.setPromptText("主辦單位/學會/科系");
        extraField.setDisable(true); // 預設學生身分，不啟用主辦單位欄位

        // 單選鈕點擊切換事件
        regStudentRadio.setOnAction(e -> extraField.setDisable(true));
        regOrgRadio.setOnAction(e -> extraField.setDisable(false));

        grid.add(new Label("註冊帳號："), 0, 0);
        grid.add(regUserField, 1, 0);
        grid.add(new Label("註冊密碼："), 0, 1);
        grid.add(regPassField, 1, 1);
        grid.add(new Label("真實姓名："), 0, 2);
        grid.add(regNameField, 1, 2);
        grid.add(new Label("註冊身分："), 0, 3);
        grid.add(roleBox, 1, 3);
        grid.add(new Label("主辦單位："), 0, 4);
        grid.add(extraField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 防呆驗證（當按註冊按鈕時攔截）
        final Button regButton = (Button) dialog.getDialogPane().lookupButton(registerButtonType);
        regButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String rUser = regUserField.getText().trim();
            String rPass = regPassField.getText().trim();
            String rName = regNameField.getText().trim();
            boolean isStudent = regStudentRadio.isSelected();
            String rExtra = extraField.getText().trim();

            if (rUser.isEmpty() || rPass.isEmpty() || rName.isEmpty()) {
                showAlert(AlertType.WARNING, "註冊失敗", "請完整填寫所有必填欄位！");
                ae.consume(); // 阻擋對話框關閉
                return;
            }

            if (!isStudent && rExtra.isEmpty()) {
                showAlert(AlertType.WARNING, "註冊失敗", "主辦者註冊必須填寫主辦單位！");
                ae.consume();
                return;
            }

            // 檢查帳號是否重複
            List<User> userList = repository.loadUsers();
            for (User u : userList) {
                if (u.getId().equalsIgnoreCase(rUser)) {
                    showAlert(AlertType.ERROR, "註冊失敗", "此帳號已被註冊，請換一個帳號。");
                    ae.consume();
                    return;
                }
            }

            // 通過驗證，新增使用者
            User newUser;
            if (isStudent) {
                newUser = new Student(rUser, rPass, rName);
            } else {
                newUser = new Organizer(rUser, rPass, rName, rExtra);
            }

            userList.add(newUser);
            repository.saveUsers(userList);

            showAlert(AlertType.INFORMATION, "註冊成功", "恭喜您註冊成功！現在可以登入系統了。");
            
            // 自動在主畫面填入剛剛註冊的帳號
            usernameField.setText(rUser);
            passwordField.setText("");
            if (isStudent) {
                studentRadio.setSelected(true);
            } else {
                organizerRadio.setSelected(true);
            }
        });

        dialog.showAndWait();
    }

    /**
     * 輔助彈出訊息視窗
     */
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
