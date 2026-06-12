package controller;

import repository.DataContext;
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
 * 主辦者控制器 (Admin Controller)
 * 依賴 DataContext（DIP），不直接耦合任何具體 Repository 實作。
 */
public class AdminController {

    private DataContext context;
    private Organizer   currentOrganizer;
    private List<Event> allEvents;
    private List<User>  allUsers;
    private final ObservableList<Event>  observableEvents = FXCollections.observableArrayList();
    private final ObservableList<String> rosterItems      = FXCollections.observableArrayList();

    @FXML private Label welcomeLabel;

    @FXML private TableView<Event>           eventTable;
    @FXML private TableColumn<Event, String> idCol;
    @FXML private TableColumn<Event, String> titleCol;
    @FXML private TableColumn<Event, String> locationCol;
    @FXML private TableColumn<Event, String> dateCol;
    @FXML private TableColumn<Event, String> capacityCol;
    @FXML private TableColumn<Event, String> registeredCol;

    @FXML private TextArea         descArea;
    @FXML private ListView<String> studentRosterList;

    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button exportCsvBtn;

    /** 注入 DataContext（DIP：依賴抽象協調者） */
    public void initData(DataContext context, Organizer organizer) {
        this.context   = context;
        this.allUsers  = context.loadUsers();
        this.allEvents = context.loadEvents();
        context.resolveRelationships(allUsers, allEvents);

        for (User u : allUsers) {
            if (u.getId().equals(organizer.getId())) {
                this.currentOrganizer = (Organizer) u;
                break;
            }
        }
        if (this.currentOrganizer == null) this.currentOrganizer = organizer;

        welcomeLabel.setText("主辦者：已登入為 " + currentOrganizer.getName() +
                             " (" + currentOrganizer.getOrganization() + ")");

        setupTableColumns();
        setupSelectionListener();
        refreshTableData();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getId()));
        titleCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitle()));
        locationCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLocation()));
        dateCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTime()));
        capacityCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getCapacity())));
        registeredCol.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getRegisteredCount())));
        eventTable.setItems(observableEvents);
        studentRosterList.setItems(rosterItems);
    }

    private void setupSelectionListener() {
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                descArea.setText(
                    "【活動代碼】" + newVal.getId()      + "\n" +
                    "【活動主題】" + newVal.getTitle()    + "\n" +
                    "【活動地點】" + newVal.getLocation() + "\n" +
                    "【舉辦時間】" + newVal.getTime()     + "\n" +
                    "【名額限制】" + newVal.getCapacity() + " 人\n" +
                    "【活動狀態】" + newVal.getStatus()   +
                        " (已報名 " + newVal.getRegisteredCount() + " 人)\n" +
                    "==================================\n" +
                    "【活動介紹】\n" + newVal.getDescription()
                );
                updateRosterList(newVal);
            } else {
                descArea.setText("請選擇左側活動以檢視詳細介紹與內容。");
                rosterItems.clear();
                rosterItems.add("請先選擇一個活動");
            }
        });
    }

    private void updateRosterList(Event ev) {
        rosterItems.clear();
        List<Student> students = ev.getParticipants();
        if (students.isEmpty()) {
            rosterItems.add("目前尚無人報名此活動。");
            return;
        }
        int count = 1;
        for (Student s : students) {
            rosterItems.add(count++ + ". " + s.getName() + " (" + s.getId() + ")");
        }
    }

    private void refreshTableData() {
        Event prev = eventTable.getSelectionModel().getSelectedItem();
        observableEvents.clear();
        observableEvents.addAll(allEvents);
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
    private void handleCreateEvent(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("新增活動項目");
        dialog.setHeaderText("請輸入全新活動的詳細資訊：");

        ButtonType saveType = new ButtonType("儲存建立", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        applyTheme(dialog.getDialogPane());

        GridPane grid = buildEventFormGrid(null);
        dialog.getDialogPane().setContent(grid);

        TextField titleField    = (TextField) grid.getUserData();
        Object[]  fields        = (Object[]) grid.getProperties().get("fields");
        TextField locationField = (TextField)  fields[0];
        TextField dateField     = (TextField)  fields[1];
        Spinner<Integer> capSpinner = (Spinner<Integer>) fields[2];
        TextArea  descArea      = (TextArea)   fields[3];

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String title    = titleField.getText().trim();
            String location = locationField.getText().trim();
            String date     = dateField.getText().trim();
            int    cap      = capSpinner.getValue();
            String desc     = descArea.getText().trim();

            if (title.isEmpty() || location.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                showAlert(AlertType.WARNING, "提示", "所有欄位均為必填！");
                ae.consume(); return;
            }

            ButtonType yesBtn = new ButtonType("是，確認建立", ButtonBar.ButtonData.YES);
            ButtonType noBtn  = new ButtonType("否，返回修改", ButtonBar.ButtonData.NO);
            Alert confirm = new Alert(AlertType.CONFIRMATION);
            confirm.setTitle("確認建立活動");
            confirm.setHeaderText("您確定要建立這個活動嗎？");
            confirm.setContentText(
                "活動名稱：" + title + "\n" +
                "活動地點：" + location + "\n" +
                "舉辦時間：" + date + "\n" +
                "人數限制：" + cap + " 人"
            );
            confirm.getButtonTypes().setAll(yesBtn, noBtn);
            applyTheme(confirm.getDialogPane());
            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isEmpty() || confirmResult.get() != yesBtn) {
                ae.consume(); return;
            }

            Event newEv = new Event(generateNextEventId(), title, location, date, cap, desc);
            currentOrganizer.createEvent(newEv);
            allEvents.add(newEv);
            saveAllData();
            showAlert(AlertType.INFORMATION, "建立成功", "已成功建立新活動：【" + title + "】！");
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveType) refreshTableData();
    }

    @FXML
    private void handleEditEvent(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲修改的活動！");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("修改活動資料 - " + selected.getId());
        dialog.setHeaderText("請調整活動的資訊欄位：");

        ButtonType saveType = new ButtonType("更新修改", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        applyTheme(dialog.getDialogPane());

        GridPane grid = buildEventFormGrid(selected);
        dialog.getDialogPane().setContent(grid);

        TextField titleField    = (TextField) grid.getUserData();
        Object[]  fields        = (Object[]) grid.getProperties().get("fields");
        TextField locationField = (TextField)  fields[0];
        TextField dateField     = (TextField)  fields[1];
        Spinner<Integer> capSpinner = (Spinner<Integer>) fields[2];
        TextArea  dialogDescArea = (TextArea)   fields[3];

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String title    = titleField.getText().trim();
            String location = locationField.getText().trim();
            String date     = dateField.getText().trim();
            int    cap      = capSpinner.getValue();
            String desc     = dialogDescArea.getText().trim();

            if (title.isEmpty() || location.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                showAlert(AlertType.WARNING, "提示", "所有欄位均為必填！");
                ae.consume(); return;
            }
            if (cap < selected.getRegisteredCount()) {
                showAlert(AlertType.ERROR, "修改限制警告",
                        "新的人數限制 (" + cap + ") 不能低於已報名人數 (" +
                        selected.getRegisteredCount() + ")！");
                ae.consume(); return;
            }

            selected.setTitle(title);
            selected.setLocation(location);
            selected.setTime(date);
            selected.setCapacity(cap);
            selected.setDescription(desc);
            currentOrganizer.editEvent(selected);
            saveAllData();
            showAlert(AlertType.INFORMATION, "更新成功", "活動資料更新成功！");
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveType) refreshTableData();
    }

    @FXML
    private void handleDeleteEvent(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲刪除的活動！");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("危險動作確認");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要刪除活動【" + selected.getTitle() + "】嗎？\n這將會同步清除所有學生的報名記錄且無法復原！");
        applyTheme(confirm.getDialogPane());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allEvents.remove(selected);
            currentOrganizer.getHostedEvents().remove(selected);

            new ArrayList<>(selected.getParticipants())
                    .forEach(s -> s.cancelEvent(selected));

            saveAllData();
            showAlert(AlertType.INFORMATION, "刪除成功", "活動已成功刪除！");
            refreshTableData();
        }
    }

    @FXML
    private void handleExportCsv(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "提示", "請先在列表中選取欲匯出名單的活動！");
            return;
        }
        List<Student> students = selected.getParticipants();
        if (students.isEmpty()) {
            showAlert(AlertType.WARNING, "匯出提示", "該活動目前尚無學生報名，無須匯出！");
            return;
        }

        new File("exports").mkdirs();
        File file = new File("exports", selected.getId() + "_" + selected.getTitle() + "_報名名冊.csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("序號,學號(Id),姓名(Name)");
            int index = 1;
            for (Student s : students) {
                pw.println(index++ + "," + s.getId() + "," + s.getName());
            }
            showAlert(AlertType.INFORMATION, "匯出成功",
                    "【" + selected.getTitle() + "】報名名冊已成功匯出！\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "匯出失敗", "在寫入 CSV 檔案時發生錯誤！\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveAllData() {
        context.saveEvents(allEvents);
        context.saveUsers(allUsers);
    }

    private String generateNextEventId() {
        int max = allEvents.stream()
                .filter(e -> e.getId().startsWith("E"))
                .mapToInt(e -> {
                    try { return Integer.parseInt(e.getId().substring(1)); }
                    catch (NumberFormatException ex) { return 0; }
                }).max().orElse(0);
        return String.format("E%03d", max + 1);
    }

    /**
     * 建立活動表單 GridPane，並以 userData / properties 傳回各輸入元件
     * （避免在兩個 handle 方法中重複佈局程式碼）。
     */
    private GridPane buildEventFormGrid(Event existing) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 25, 10, 25));

        TextField titleField    = new TextField(existing != null ? existing.getTitle()    : "");
        TextField locationField = new TextField(existing != null ? existing.getLocation() : "");
        TextField dateField     = new TextField(existing != null ? existing.getTime()     : "2026-06-30 14:00");
        int       initCap       = existing != null ? existing.getCapacity() : 20;
        Spinner<Integer> capSpinner = new Spinner<>(1, 1000, initCap, 5);
        TextArea  dialogDescArea = new TextArea(existing != null ? existing.getDescription() : "");
        dialogDescArea.setPrefRowCount(4);
        dialogDescArea.setWrapText(true);

        titleField.setPromptText("例如：Java 程式設計工作坊");
        locationField.setPromptText("例如：資工系一館 301 教室");

        grid.add(new Label("活動主題："), 0, 0); grid.add(titleField,     1, 0);
        grid.add(new Label("活動地點："), 0, 1); grid.add(locationField,  1, 1);
        grid.add(new Label("舉辦日期："), 0, 2); grid.add(dateField,      1, 2);
        grid.add(new Label("人數限制："), 0, 3); grid.add(capSpinner,     1, 3);
        grid.add(new Label("活動描述："), 0, 4); grid.add(dialogDescArea, 1, 4);

        // 傳回元件的輕量方式（不需引入額外 DTO 類別）
        grid.setUserData(titleField);
        grid.getProperties().put("fields", new Object[]{ locationField, dateField, capSpinner, dialogDescArea });

        return grid;
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("安全登出確認");
        confirm.setHeaderText(null);
        confirm.setContentText("您確定要安全登出主辦者後台嗎？");
        applyTheme(confirm.getDialogPane());

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
