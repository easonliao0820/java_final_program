package controller;

import repository.DataContext;
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
 * 學生控制器 (Student Controller)
 * 依賴 DataContext（DIP），不直接耦合任何具體 Repository 實作。
 */
public class StudentController {

    private DataContext context;
    private Student currentStudent;
    private List<Event> allEvents;
    private List<User> allUsers;
    private final ObservableList<Event> observableEvents = FXCollections.observableArrayList();

    @FXML private Label       welcomeLabel;
    @FXML private RadioButton viewAllRadio;
    @FXML private RadioButton viewRegisteredRadio;
    @FXML private ToggleGroup viewGroup;
    @FXML private TextField   searchField;

    @FXML private TableView<Event>             eventTable;
    @FXML private TableColumn<Event, String>   idCol;
    @FXML private TableColumn<Event, String>   titleCol;
    @FXML private TableColumn<Event, String>   locationCol;
    @FXML private TableColumn<Event, String>   dateCol;
    @FXML private TableColumn<Event, String>   capacityCol;
    @FXML private TableColumn<Event, String>   statusCol;

    @FXML private TextArea descArea;
    @FXML private Button   registerBtn;
    @FXML private Button   cancelBtn;

    /** 注入 DataContext（DIP：依賴抽象協調者） */
    public void initData(DataContext context, Student student) {
        this.context  = context;
        this.allUsers = context.loadUsers();
        this.allEvents = context.loadEvents();
        context.resolveRelationships(allUsers, allEvents);

        for (User u : allUsers) {
            if (u.getId().equals(student.getId())) {
                this.currentStudent = (Student) u;
                break;
            }
        }
        if (this.currentStudent == null) this.currentStudent = student;

        welcomeLabel.setText("學生您好：" + currentStudent.getName() +
                             " (" + currentStudent.getId() + ")，歡迎使用本系統！");

        setupTableColumns();
        setupTableSelectionListener();

        viewAllRadio.setOnAction(e -> refreshTableData());
        viewRegisteredRadio.setOnAction(e -> refreshTableData());
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTableData());
        }

        refreshTableData();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getId()));
        titleCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitle()));
        locationCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLocation()));
        dateCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTime()));
        capacityCol.setCellValueFactory(cd -> {
            Event e = cd.getValue();
            return new SimpleStringProperty(e.getRegisteredCount() + " / " + e.getCapacity());
        });
        statusCol.setCellValueFactory(cd -> {
            Event e = cd.getValue();
            boolean registered = currentStudent.getRegisteredEvents().contains(e);
            String status = registered ? "已報名 ✓" : (e.isFull() ? "額滿" : "開放中");
            return new SimpleStringProperty(status);
        });
        eventTable.setItems(observableEvents);
    }

    private void setupTableSelectionListener() {
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                descArea.setText(
                    "【活動代碼】" + newVal.getId()       + "\n" +
                    "【活動主題】" + newVal.getTitle()     + "\n" +
                    "【活動地點】" + newVal.getLocation()  + "\n" +
                    "【舉辦時間】" + newVal.getTime()      + "\n" +
                    "【人數上限】" + newVal.getCapacity()  + " 人\n" +
                    "【已報名數】" + newVal.getRegisteredCount() + " 人\n" +
                    "【活動狀態】" + newVal.getStatus()    + "\n" +
                    "【主辦單位】" + (newVal.getOrganizer() != null
                                     ? newVal.getOrganizer().getOrganization() : "系學會") + "\n" +
                    "==================================\n" +
                    "【活動詳情】\n" + newVal.getDescription()
                );
            } else {
                descArea.setText("請選擇左側活動以檢視詳細介紹與內容。");
            }
            updateButtonStates(newVal);
        });
    }

    private void updateButtonStates(Event selectedEvent) {
        if (selectedEvent == null) {
            registerBtn.setDisable(true);
            cancelBtn.setDisable(true);
        } else {
            boolean isRegistered = currentStudent.getRegisteredEvents().contains(selectedEvent);
            boolean isFull = selectedEvent.isFull();
            registerBtn.setDisable(isRegistered || isFull);
            cancelBtn.setDisable(!isRegistered);
        }
    }

    private void refreshTableData() {
        Event prev = eventTable.getSelectionModel().getSelectedItem();
        observableEvents.clear();

        boolean showOnlyRegistered = viewRegisteredRadio.isSelected();
        String keyword = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";

        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            boolean registered = currentStudent.getRegisteredEvents().contains(e);
            if (showOnlyRegistered && !registered) continue;
            if (!keyword.isEmpty()) {
                boolean matchTitle    = e.getTitle().toLowerCase().contains(keyword);
                boolean matchLocation = e.getLocation().toLowerCase().contains(keyword);
                if (!matchTitle && !matchLocation) continue;
            }
            filtered.add(e);
        }

        observableEvents.addAll(filtered);
        eventTable.refresh();

        if (prev != null) {
            observableEvents.stream()
                    .filter(e -> e.getId().equals(prev.getId()))
                    .findFirst()
                    .ifPresent(e -> eventTable.getSelectionModel().select(e));
        }
        if (eventTable.getSelectionModel().getSelectedItem() == null && !observableEvents.isEmpty()) {
            eventTable.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleRegisterEvent(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "提示", "請先在左邊列表中選取欲報名的活動！");
            return;
        }
        ButtonType yesBtn = new ButtonType("是，確認報名", ButtonBar.ButtonData.YES);
        ButtonType noBtn  = new ButtonType("否，取消",     ButtonBar.ButtonData.NO);
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("確認報名");
        confirm.setHeaderText("您確定要加入這個活動嗎？");
        confirm.setContentText(
            "活動名稱：" + selected.getTitle() + "\n" +
            "活動地點：" + selected.getLocation() + "\n" +
            "舉辦時間：" + selected.getTime()
        );
        confirm.getButtonTypes().setAll(yesBtn, noBtn);
        Optional<ButtonType> confirmResult = confirm.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != yesBtn) return;

        try {
            selected.registerStudent(currentStudent);
            saveAllData();
            showAlert(AlertType.INFORMATION, "報名成功", "【" + selected.getTitle() + "】報名成功！");
            refreshTableData();
        } catch (CapacityFullException ex) {
            showAlert(AlertType.ERROR, "人數已滿",
                    "報名失敗！\n活動【" + ex.getEventTitle() + "】已經額滿囉！\n" + ex.getMessage());
        } catch (DuplicateRegisterException ex) {
            showAlert(AlertType.WARNING, "重複報名",
                    "報名失敗！\n您已經報名過【" + ex.getEventTitle() + "】活動了！\n" + ex.getMessage());
        }
    }

    @FXML
    private void handleCancelRegistration(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲取消的活動！");
            return;
        }
        if (!currentStudent.getRegisteredEvents().contains(selected)) {
            showAlert(AlertType.WARNING, "取消失敗", "您尚未報名此活動，無法取消！");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("確認取消報名");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要取消報名活動【" + selected.getTitle() + "】嗎？");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.cancelStudent(currentStudent);
            saveAllData();
            showAlert(AlertType.INFORMATION, "取消成功", "已成功取消報名【" + selected.getTitle() + "】！");
            refreshTableData();
        }
    }

    private void saveAllData() {
        context.saveEvents(allEvents);
        context.saveUsers(allUsers);
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
                lc.setContext(context);

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
