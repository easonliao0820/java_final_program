package controller;

import repository.DataContext;
import model.User;
import model.Student;
import model.Organizer;
import model.UserFactory;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 登入控制器 (Login Controller)
 * 依賴 DataContext（介面），而非具體 FileRepository（DIP）。
 * 角色視圖派發改用 Map 查表，新增角色無需修改此類別（OCP）。
 */
public class LoginController {

    /** 視圖開啟策略介面（OCP：新增角色只需在 viewOpeners 中新增一筆） */
    @FunctionalInterface
    private interface ViewOpener {
        void open(Stage loginStage, User user) throws IOException;
    }

    private DataContext context;
    private final Map<String, ViewOpener> viewOpeners = new HashMap<>();

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton   studentRadio;
    @FXML private RadioButton   organizerRadio;
    @FXML private Button        loginButton;
    @FXML private Button        registerButton;

    public LoginController() {}

    /** 注入 DataContext（DIP：依賴抽象協調者，非具體實作） */
    public void setContext(DataContext context) {
        this.context = context;
        initViewOpeners();
    }

    /** 初始化角色→視圖對映表（OCP：擴充角色只需在此加一行） */
    private void initViewOpeners() {
        viewOpeners.put("Student",   (stage, user) -> openStudentView(stage, (Student) user));
        viewOpeners.put("Organizer", (stage, user) -> openAdminView(stage, (Organizer) user));
    }

    @FXML
    public void initialize() {}

    @FXML
    private void handleLogin(ActionEvent event) {
        String username     = usernameField.getText().trim();
        String password     = passwordField.getText().trim();
        String selectedRole = studentRadio.isSelected() ? "Student" : "Organizer";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "提示", "帳號與密碼欄位不得為空！");
            return;
        }

        List<User> users  = context.loadUsers();
        User matchedUser  = users.stream()
                .filter(u -> u.getId().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);

        if (matchedUser == null) {
            showAlert(AlertType.ERROR, "登入失敗", "帳號或密碼輸入錯誤，請再試一次。");
            return;
        }
        if (!matchedUser.getRole().equals(selectedRole)) {
            showAlert(AlertType.WARNING, "身份錯誤", "登入角色選擇不正確！請確認您的身份類型。");
            return;
        }

        ViewOpener opener = viewOpeners.get(matchedUser.getRole());
        if (opener == null) {
            showAlert(AlertType.ERROR, "系統錯誤", "未知的使用者角色：" + matchedUser.getRole());
            return;
        }

        try {
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            opener.open(loginStage, matchedUser);
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "載入失敗", "系統在載入主介面時發生錯誤！\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openStudentView(Stage loginStage, Student student) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/student-view.fxml"));
        Parent root = loader.load();
        StudentController sc = loader.getController();
        sc.initData(context, student);

        loginStage.close();
        Stage stage = new Stage();
        stage.setTitle("活動報名管理系統 - 學生專區");
        stage.setScene(new Scene(root, 850, 600));
        stage.show();
    }

    private void openAdminView(Stage loginStage, Organizer organizer) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/organizer-view.fxml"));
        Parent root = loader.load();
        AdminController ac = loader.getController();
        ac.initData(context, organizer);

        loginStage.close();
        Stage stage = new Stage();
        stage.setTitle("活動報名管理系統 - 主辦者後台");
        stage.setScene(new Scene(root, 950, 620));
        stage.show();
    }

    @FXML
    private void handleRegisterDialog(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("註冊新帳號");
        dialog.setHeaderText("請輸入您的個人資訊以註冊帳號：");

        ButtonType registerButtonType = new ButtonType("註冊", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        applyTheme(dialog.getDialogPane());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 30));

        TextField     regUserField = new TextField();
        PasswordField regPassField = new PasswordField();
        TextField     regNameField = new TextField();
        regUserField.setPromptText("英文或數字帳號");
        regPassField.setPromptText("密碼");
        regNameField.setPromptText("真實姓名");

        RadioButton regStudentRadio = new RadioButton("學生");
        RadioButton regOrgRadio     = new RadioButton("主辦者");
        regStudentRadio.setSelected(true);
        ToggleGroup regRoleGroup = new ToggleGroup();
        regStudentRadio.setToggleGroup(regRoleGroup);
        regOrgRadio.setToggleGroup(regRoleGroup);

        TextField extraField = new TextField();
        extraField.setPromptText("主辦單位/學會/科系");
        extraField.setDisable(true);

        regStudentRadio.setOnAction(e -> extraField.setDisable(true));
        regOrgRadio.setOnAction(e -> extraField.setDisable(false));

        grid.add(new Label("註冊帳號："), 0, 0); grid.add(regUserField, 1, 0);
        grid.add(new Label("註冊密碼："), 0, 1); grid.add(regPassField, 1, 1);
        grid.add(new Label("真實姓名："), 0, 2); grid.add(regNameField, 1, 2);
        grid.add(new Label("註冊身分："), 0, 3); grid.add(new HBox(15, regStudentRadio, regOrgRadio), 1, 3);
        grid.add(new Label("主辦單位："), 0, 4); grid.add(extraField, 1, 4);
        dialog.getDialogPane().setContent(grid);

        final Button regButton = (Button) dialog.getDialogPane().lookupButton(registerButtonType);
        regButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String  rUser      = regUserField.getText().trim();
            String  rPass      = regPassField.getText().trim();
            String  rName      = regNameField.getText().trim();
            boolean isStudent  = regStudentRadio.isSelected();
            String  rExtra     = extraField.getText().trim();

            if (rUser.isEmpty() || rPass.isEmpty() || rName.isEmpty()) {
                showAlert(AlertType.WARNING, "註冊失敗", "請完整填寫所有必填欄位！");
                ae.consume(); return;
            }
            if (!isStudent && rExtra.isEmpty()) {
                showAlert(AlertType.WARNING, "註冊失敗", "主辦者註冊必須填寫主辦單位！");
                ae.consume(); return;
            }

            List<User> userList = context.loadUsers();
            boolean duplicate = userList.stream()
                    .anyMatch(u -> u.getId().equalsIgnoreCase(rUser));
            if (duplicate) {
                showAlert(AlertType.ERROR, "註冊失敗", "此帳號已被註冊，請換一個帳號。");
                ae.consume(); return;
            }

            // OCP：使用 UserFactory 建立使用者，新增角色無需修改此處
            String role = isStudent ? "Student" : "Organizer";
            User newUser = UserFactory.create(role, rUser, rPass, rName, rExtra);
            userList.add(newUser);
            context.saveUsers(userList);

            showAlert(AlertType.INFORMATION, "註冊成功", "恭喜您註冊成功！現在可以登入系統了。");
            usernameField.setText(rUser);
            passwordField.setText("");
            if (isStudent) studentRadio.setSelected(true);
            else           organizerRadio.setSelected(true);
        });

        dialog.showAndWait();
    }

    private void applyTheme(DialogPane pane) {
        pane.getStylesheets().add(
            getClass().getResource("/styles.css").toExternalForm()
        );
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyTheme(alert.getDialogPane());
        alert.showAndWait();
    }
}
