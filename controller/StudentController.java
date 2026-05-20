package controller;

import repository.FileRepository;
import model.Student;
import model.Event;
import model.User;
import exception.CapacityFullException;
import exception.DuplicateRegisterException;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 學生控制器 (Student Controller) - 規格書優化版
 * 採用純物件導向方法傳遞與篩選活動，支援即時關鍵字搜尋與狀態判定。
 */
public class StudentController {
    private FileRepository repository;
    private Student currentStudent;
    private List<Event> allEvents;
    private List<User> allUsers; // 儲存所有使用者以利存檔
    private ObservableList<Event> observableEvents;

    @FXML private Label welcomeLabel;
    @FXML private RadioButton viewAllRadio;
    @FXML private RadioButton viewRegisteredRadio;
    @FXML private ToggleGroup viewGroup;
    @FXML private TextField searchField; // 新增關鍵字搜尋框 (規格書進階功能)

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> idCol;
    @FXML private TableColumn<Event, String> titleCol;
    @FXML private TableColumn<Event, String> locationCol; // 規格書新增：地點欄位
    @FXML private TableColumn<Event, String> dateCol;     // 規格書新增：舉辦時間
    @FXML private TableColumn<Event, String> capacityCol;
    @FXML private TableColumn<Event, String> statusCol;   // 開放中 / 額滿 / 已報名

    @FXML private TextArea descArea;
    @FXML private Button registerBtn;
    @FXML private Button cancelBtn;

    public StudentController() {
        this.observableEvents = FXCollections.observableArrayList();
    }

    /**
     * 初始化傳參並解析 OOP 雙向連結
     */
    public void initData(FileRepository repository, Student student) {
        this.repository = repository;
        
        // 1. 自檔案載入所有使用者與活動，並解析為完整的 OOP 記憶體關聯圖
        this.allUsers = repository.loadUsers();
        this.allEvents = repository.loadEvents();
        repository.resolveRelationships(allUsers, allEvents);

        // 2. 在解析後的物件圖中，尋找對應的已連結 Student 實體
        for (User u : allUsers) {
            if (u.getId().equals(student.getId())) {
                this.currentStudent = (Student) u;
                break;
            }
        }
        if (this.currentStudent == null) {
            this.currentStudent = student; // 備用防呆
        }

        welcomeLabel.setText("學生您好：" + currentStudent.getName() + " (" + currentStudent.getId() + ")，歡迎使用本系統！");

        setupTableColumns();
        setupTableSelectionListener();
        
        // 3. 綁定篩選與搜尋框變更監聽器（當打字時，即時觸發表格刷新搜尋， Section 6 進階功能）
        viewAllRadio.setOnAction(e -> refreshTableData());
        viewRegisteredRadio.setOnAction(e -> refreshTableData());
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshTableData());
        }

        refreshTableData();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        locationCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime()));
        
        capacityCol.setCellValueFactory(cellData -> {
            Event e = cellData.getValue();
            return new SimpleStringProperty(e.getRegisteredCount() + " / " + e.getCapacity());
        });

        statusCol.setCellValueFactory(cellData -> {
            Event e = cellData.getValue();
            // 直接進行純物件導向清單 contains 判斷！ (OOP 優勢)
            boolean isRegistered = currentStudent.getRegisteredEvents().contains(e);
            String status = isRegistered ? "已報名 ✓" : (e.isFull() ? "額滿" : "開放中");
            return new SimpleStringProperty(status);
        });

        eventTable.setItems(observableEvents);
    }

    private void setupTableSelectionListener() {
        eventTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                descArea.setText(
                    "【活動代碼】" + newValue.getId() + "\n" +
                    "【活動主題】" + newValue.getTitle() + "\n" +
                    "【活動地點】" + newValue.getLocation() + "\n" +
                    "【舉辦時間】" + newValue.getTime() + "\n" +
                    "【人數上限】" + newValue.getCapacity() + " 人\n" +
                    "【已報名數】" + newValue.getRegisteredCount() + " 人\n" +
                    "【活動狀態】" + newValue.getStatus() + "\n" +
                    "【主辦單位】" + (newValue.getOrganizer() != null ? newValue.getOrganizer().getOrganization() : "系學會") + "\n" +
                    "==================================\n" +
                    "【活動詳情】\n" + newValue.getDescription()
                );
            } else {
                descArea.setText("請選擇左側活動以檢視詳細介紹與內容。");
            }
        });
    }

    private void refreshTableData() {
        Event prevSelection = eventTable.getSelectionModel().getSelectedItem();
        observableEvents.clear();

        boolean showOnlyRegistered = viewRegisteredRadio.isSelected();
        String keyword = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";

        List<Event> filteredList = new ArrayList<>();

        for (Event e : allEvents) {
            // 純物件導向狀態篩選
            boolean isRegistered = currentStudent.getRegisteredEvents().contains(e);
            if (showOnlyRegistered && !isRegistered) {
                continue;
            }

            // 關鍵字搜尋篩選 (活動主題或活動地點，Section 6 進階功能)
            if (!keyword.isEmpty()) {
                boolean matchTitle = e.getTitle().toLowerCase().contains(keyword);
                boolean matchLocation = e.getLocation().toLowerCase().contains(keyword);
                if (!matchTitle && !matchLocation) {
                    continue;
                }
            }

            filteredList.add(e);
        }

        observableEvents.addAll(filteredList);
        eventTable.refresh();

        if (prevSelection != null) {
            for (Event e : observableEvents) {
                if (e.getId().equals(prevSelection.getId())) {
                    eventTable.getSelectionModel().select(e);
                    break;
                }
            }
        }

        if (eventTable.getSelectionModel().getSelectedItem() == null && !observableEvents.isEmpty()) {
            eventTable.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleRegisterEvent(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "提示", "請先在左邊列表中選取欲報名的活動！");
            return;
        }

        try {
            // 核心物件導向報名方法呼叫 (將 Student 物件直接傳入，在 Event 內部會雙向維護清單)
            selectedEvent.registerStudent(currentStudent);

            saveAllData();

            showAlert(AlertType.INFORMATION, "報名成功", "【" + selectedEvent.getTitle() + "】報名成功！");
            refreshTableData();

        } catch (CapacityFullException ex) {
            showAlert(AlertType.ERROR, "人數已滿", 
                    "報名失敗！\n活動【" + ex.getEventTitle() + "】已經額滿囉！\n" + ex.getMessage());
        } catch (DuplicateRegisterException ex) {
            showAlert(AlertType.WARNING, "重複報名", 
                    "報名失敗！\n您已經報名過【" + ex.getEventTitle() + "】活動了，請勿重複報名！\n" + ex.getMessage());
        }
    }

    @FXML
    private void handleCancelRegistration(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲取消的活動！");
            return;
        }

        // 直接在物件中進行包含判定
        if (!currentStudent.getRegisteredEvents().contains(selectedEvent)) {
            showAlert(AlertType.WARNING, "取消失敗", "您尚未報名此活動，無法取消！");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("確認取消報名");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要取消報名活動【" + selectedEvent.getTitle() + "】嗎？");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 核心物件導向取消報名方法呼叫 (雙向維護)
            selectedEvent.cancelStudent(currentStudent);

            saveAllData();

            showAlert(AlertType.INFORMATION, "取消成功", "已成功取消報名【" + selectedEvent.getTitle() + "】！");
            refreshTableData();
        }
    }

    private void saveAllData() {
        repository.saveEvents(allEvents);
        repository.saveUsers(allUsers); // 直接將全部解析完畢的 User 儲存 (包含學生註冊 ID 提取)
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("安全登出");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要登出系統嗎？");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
                currentStage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login-view.fxml"));
                Parent root = loader.load();
                
                LoginController lc = loader.getController();
                lc.setRepository(repository);

                Stage loginStage = new Stage();
                loginStage.setTitle("活動報名管理系統 - 登入");
                loginStage.setScene(new Scene(root, 420, 390));
                loginStage.setResizable(false);
                loginStage.show();

            } catch (IOException e) {
                showAlert(AlertType.ERROR, "登出失敗", "無法重返登入畫面！");
                e.printStackTrace();
            }
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
