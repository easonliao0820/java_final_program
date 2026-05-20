package controller;

import repository.FileRepository;
import model.Organizer;
import model.Event;
import model.User;
import model.Student;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 主辦者控制層 (Organizer/Admin Controller) - 規格書優化版
 * 提供活動之增、刪、改功能，支持純物件導向參與名單檢視，以及匯出 CSV 名冊功能。
 */
public class AdminController {
    private FileRepository repository;
    private Organizer currentOrganizer;
    private List<Event> allEvents;
    private List<User> allUsers; // 儲存所有使用者以利存檔
    private ObservableList<Event> observableEvents;
    private ObservableList<String> rosterItems;

    @FXML private Label welcomeLabel;

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> idCol;
    @FXML private TableColumn<Event, String> titleCol;
    @FXML private TableColumn<Event, String> locationCol; // 規格書新增：地點
    @FXML private TableColumn<Event, String> dateCol;     // 規格書新增：時間
    @FXML private TableColumn<Event, String> capacityCol;
    @FXML private TableColumn<Event, String> registeredCol;

    @FXML private TextArea descArea;
    @FXML private ListView<String> studentRosterList;

    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button exportCsvBtn; // 規格書進階功能：匯出名冊按鈕

    public AdminController() {
        this.observableEvents = FXCollections.observableArrayList();
        this.rosterItems = FXCollections.observableArrayList();
    }

    /**
     * 載入並解析關係圖
     */
    public void initData(FileRepository repository, Organizer organizer) {
        this.repository = repository;
        
        // 1. 載入扁平資料並解析為雙向 OOP 關係圖
        this.allUsers = repository.loadUsers();
        this.allEvents = repository.loadEvents();
        repository.resolveRelationships(allUsers, allEvents);

        // 2. 獲取已關聯的主辦方實體
        for (User u : allUsers) {
            if (u.getId().equals(organizer.getId())) {
                this.currentOrganizer = (Organizer) u;
                break;
            }
        }
        if (this.currentOrganizer == null) {
            this.currentOrganizer = organizer; // 備用防呆
        }

        welcomeLabel.setText("主辦者：已登入為 " + currentOrganizer.getName() + " (" + currentOrganizer.getOrganization() + ")");

        setupTableColumns();
        setupSelectionListener();
        
        refreshTableData();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        locationCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime()));
        capacityCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getCapacity())));
        registeredCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getRegisteredCount())));

        eventTable.setItems(observableEvents);
        studentRosterList.setItems(rosterItems);
    }

    private void setupSelectionListener() {
        eventTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                descArea.setText(
                    "【活動代碼】" + newValue.getId() + "\n" +
                    "【活動主題】" + newValue.getTitle() + "\n" +
                    "【活動地點】" + newValue.getLocation() + "\n" +
                    "【舉辦時間】" + newValue.getTime() + "\n" +
                    "【名額限制】" + newValue.getCapacity() + " 人\n" +
                    "【活動狀態】" + newValue.getStatus() + " (已報名 " + newValue.getRegisteredCount() + " 人)\n" +
                    "==================================\n" +
                    "【活動介紹】\n" + newValue.getDescription()
                );
                updateRosterList(newValue);
            } else {
                descArea.setText("請選擇左側活動以檢視詳細介紹與內容。");
                rosterItems.clear();
                rosterItems.add("請先選擇一個活動");
            }
        });
    }

    /**
     * 以純 OOP 關聯方式獲取學生對象列表，動態印出名冊
     */
    private void updateRosterList(Event ev) {
        rosterItems.clear();
        
        // 直接取得 Student 物件清單！
        List<Student> students = ev.getParticipants();
        
        if (students.isEmpty()) {
            rosterItems.add("目前尚無人報名此活動。");
            return;
        }

        int count = 1;
        for (Student s : students) {
            rosterItems.add(count + ". " + s.getName() + " (" + s.getId() + ")");
            count++;
        }
    }

    private void refreshTableData() {
        Event prevSelection = eventTable.getSelectionModel().getSelectedItem();

        observableEvents.clear();
        observableEvents.addAll(allEvents);
        
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

    /**
     * 新增活動：包含地點與時間輸入
     */
    @FXML
    private void handleCreateEvent(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("新增活動項目");
        dialog.setHeaderText("請輸入全新活動的詳細資訊：");

        ButtonType saveButtonType = new ButtonType("儲存建立", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 25, 10, 25));

        TextField titleField = new TextField();
        titleField.setPromptText("例如：Java 程式設計工作坊");
        
        TextField locationField = new TextField();
        locationField.setPromptText("例如：資工系一館 301 教室");
        
        TextField dateField = new TextField("2026-06-30 14:00");
        dateField.setPromptText("例如：2026-06-30 14:00");
        
        Spinner<Integer> capSpinner = new Spinner<>(1, 1000, 20, 5);

        TextArea dialogDescArea = new TextArea();
        dialogDescArea.setPromptText("請輸入活動詳細介紹說明...");
        dialogDescArea.setPrefRowCount(4);
        dialogDescArea.setWrapText(true);

        grid.add(new Label("活動主題："), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("活動地點："), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("舉辦日期："), 0, 2);
        grid.add(dateField, 1, 2);
        grid.add(new Label("人數限制："), 0, 3);
        grid.add(capSpinner, 1, 3);
        grid.add(new Label("活動描述："), 0, 4);
        grid.add(dialogDescArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String title = titleField.getText().trim();
            String location = locationField.getText().trim();
            String date = dateField.getText().trim();
            int cap = capSpinner.getValue();
            String desc = dialogDescArea.getText().trim();

            if (title.isEmpty() || location.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                showAlert(AlertType.WARNING, "提示", "所有欄位均為必填！");
                ae.consume();
                return;
            }

            String nextId = generateNextEventId();

            Event newEv = new Event(nextId, title, location, date, cap, desc);
            
            // 呼叫主辦者的物件方法 (OOP 維護)
            currentOrganizer.createEvent(newEv);
            allEvents.add(newEv);
            
            saveAllData();
            
            showAlert(AlertType.INFORMATION, "建立成功", "已成功建立新活動：【" + title + "】！");
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            refreshTableData();
        }
    }

    /**
     * 修改活動項目
     */
    @FXML
    private void handleEditEvent(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲修改的活動！");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("修改活動資料 - " + selectedEvent.getId());
        dialog.setHeaderText("請調整活動的資訊欄位：");

        ButtonType saveButtonType = new ButtonType("更新修改", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 25, 10, 25));

        TextField titleField = new TextField(selectedEvent.getTitle());
        TextField locationField = new TextField(selectedEvent.getLocation());
        TextField dateField = new TextField(selectedEvent.getTime());
        Spinner<Integer> capSpinner = new Spinner<>(1, 1000, selectedEvent.getCapacity(), 5);

        TextArea dialogDescArea = new TextArea(selectedEvent.getDescription());
        dialogDescArea.setPrefRowCount(4);
        dialogDescArea.setWrapText(true);

        grid.add(new Label("活動主題："), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("活動地點："), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("舉辦日期："), 0, 2);
        grid.add(dateField, 1, 2);
        grid.add(new Label("人數限制："), 0, 3);
        grid.add(capSpinner, 1, 3);
        grid.add(new Label("活動描述："), 0, 4);
        grid.add(dialogDescArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String title = titleField.getText().trim();
            String location = locationField.getText().trim();
            String date = dateField.getText().trim();
            int cap = capSpinner.getValue();
            String desc = dialogDescArea.getText().trim();

            if (title.isEmpty() || location.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                showAlert(AlertType.WARNING, "提示", "所有欄位均為必填！");
                ae.consume();
                return;
            }

            if (cap < selectedEvent.getRegisteredCount()) {
                showAlert(AlertType.ERROR, "修改限制警告", 
                        "更新失敗！\n新的人數限制 (" + cap + " 人) 不能低於目前已報名人數 (" + selectedEvent.getRegisteredCount() + " 人)！");
                ae.consume();
                return;
            }

            // 修改欄位並透過 OOP 方法確認綁定
            selectedEvent.setTitle(title);
            selectedEvent.setLocation(location);
            selectedEvent.setTime(date);
            selectedEvent.setCapacity(cap);
            selectedEvent.setDescription(desc);
            
            currentOrganizer.editEvent(selectedEvent);

            saveAllData();
            showAlert(AlertType.INFORMATION, "更新成功", "活動資料更新成功！");
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            refreshTableData();
        }
    }

    /**
     * 刪除活動項目：雙向物件移除與防孤立更新
     */
    @FXML
    private void handleDeleteEvent(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲刪除的活動！");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("危險動作確認");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要刪除活動【" + selectedEvent.getTitle() + "】嗎？\n這將會同步清除所有學生的報名記錄且無法復原！");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1. 從總表移除，自 Organizer 託管移除
            allEvents.remove(selectedEvent);
            currentOrganizer.getHostedEvents().remove(selectedEvent);

            // 2. 串級連帶刪除 (純 OOP 物件操作)
            List<Student> participants = new ArrayList<>(selectedEvent.getParticipants());
            for (Student s : participants) {
                // 取消學生對活動的註冊 reference
                s.cancelEvent(selectedEvent);
            }

            saveAllData();

            showAlert(AlertType.INFORMATION, "刪除成功", "活動已成功刪除，相關報名歷史紀錄已全部清理完畢！");
            refreshTableData();
        }
    }

    /**
     * 👑 規格書進階功能實作：匯出報名名冊至 CSV 檔案 (Section 6)
     */
    @FXML
    private void handleExportCsv(ActionEvent event) {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲匯出名單的活動！");
            return;
        }

        List<Student> students = selectedEvent.getParticipants();
        if (students.isEmpty()) {
            showAlert(AlertType.WARNING, "匯出提示", "該活動目前尚無學生報名，無須匯出！");
            return;
        }

        // 建立匯出資料夾 exports/
        File exportDir = new File("exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        // 以活動代碼與名稱命名
        String fileName = selectedEvent.getId() + "_" + selectedEvent.getTitle() + "_報名名冊.csv";
        File file = new File(exportDir, fileName);

        // 使用 FileWriter 與 PrintWriter 寫入 CSV 欄位，符合規格書規範
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // 寫入 CSV 標題
            pw.println("序號,學號(Id),姓名(Name)");
            
            int index = 1;
            for (Student s : students) {
                // CSV 格式：序號,學號,真實姓名
                pw.println(index + "," + s.getId() + "," + s.getName());
                index++;
            }

            showAlert(AlertType.INFORMATION, "匯出成功", 
                    "【" + selectedEvent.getTitle() + "】報名名冊已成功匯出！\n檔案儲存於：" + file.getAbsolutePath());

        } catch (IOException e) {
            showAlert(AlertType.ERROR, "匯出失敗", "在寫入 CSV 檔案時發生錯誤！\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveAllData() {
        repository.saveEvents(allEvents);
        repository.saveUsers(allUsers); // 儲存包含報名 ID 提取之已解析 User 名單
    }

    private String generateNextEventId() {
        int maxNum = 0;
        for (Event e : allEvents) {
            String idStr = e.getId();
            if (idStr.startsWith("E")) {
                try {
                    int num = Integer.parseInt(idStr.substring(1));
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        int nextNum = maxNum + 1;
        return String.format("E%03d", nextNum);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("安全登出確認");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要安全登出主辦者後台嗎？");

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
