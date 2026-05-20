# 🎓 Java 期末專題：活動報名管理系統 (Event Registration Management System) - JavaFX 版

本專案是一個採用 **MVC (Model-View-Controller) 設計模式** 與 **物件導向程式設計 (OOP)** 概念開發的活動報名與管理系統。
系統全面整合了現代化的 **JavaFX 與 FXML 圖形介面**、自訂例外處理（衝高分亮點）、資料持久化（以 `FileReader` / `PrintWriter` 實作文字檔讀寫）以及關聯完整性維護。

---

## 📁 系統目錄結構 (Project Directory Structure)

```text
java_final_program/
│
├── Launcher.java                # 專案啟動代理器 (Bypass 啟動器)
├── App.java                     # 整個程式的啟動點 (JavaFX Application 實體)
│
├── login-view.fxml              # 【視圖層】登入與註冊畫面的 FXML 版面配置
├── student-view.fxml            # 【視圖層】學生專區主畫面的 FXML 版面配置
├── organizer-view.fxml          # 【視圖層】主辦者管理後台畫面的 FXML 版面配置
│
├── model/                       # 【資料模型層】存放所有的資料實體類別
│   ├── User.java                # 抽象父類別：定義使用者共同屬性與方法
│   ├── Student.java             # 學生子類別：維護已報名活動 ID 清單
│   ├── Organizer.java           # 主辦者子類別：維護主辦單位名稱
│   └── Event.java               # 活動類別：維護活動詳情、報名名冊及報名業務邏輯
│
├── controller/                  # 【控制層】負責 FXML 事件綁定與業務資料處理
│   ├── LoginController.java     # 處理登入與帳號註冊邏輯
│   ├── StudentController.java   # 處理學生專區業務（報名、取消報名、篩選）邏輯
│   └── AdminController.java     # 處理主辦方管理業務（增刪改活動、報名名冊顯示）邏輯
│
├── repository/                  # 【資料存取層】負責與硬碟資料檔案進行讀寫
│   └── FileRepository.java      # 實作 FileReader/PrintWriter，具備自動建檔與預設測試資料注入
│
├── exception/                   # 【自訂例外處理】處理特定的商務邏輯衝突 (衝高分專用)
│   ├── CapacityFullException.java     # 活動額滿例外
│   └── DuplicateRegisterException.java # 重複報名例外
│
└── data/                        # 【資料庫資料夾】程式執行後自動產生
    ├── users.txt                # 使用者帳號與狀態持久化檔案 (包含註冊與報名關係)
    └── events.txt               # 活動清單與報名名單持久化檔案
```

---

## 🛠️ 技術實作亮點 (Technical Implementation Highlights)

### 1. 現代 Java 核心技術：JavaFX + FXML
- 採用 **FXML 宣告式視圖**，將 UI 佈局（XML）與後端控制邏輯（Java Controller）徹底分離，實現高度解耦的 MVC 架構。
- 整合了 JavaFX 特有的 **`TableView` 搭配 Lambda 欄位值工廠 (`CellValueFactory`)**，以動態、即時的方式計算活動容量百分比及學生的個別報名狀態，避免冗餘欄位存檔。
- 提供安全、優雅的**視窗轉換與重登流程**，並全面升級為 JavaFX 原生 `Alert` 彈出對話框，美觀度極高。

### 2. 完整物件導向 (OOP) 概念實作
- **繼承 (Inheritance)**：自訂抽象類別 `User`，並由 `Student` 與 `Organizer` 繼承，重用核心欄位（如帳號、密碼、姓名與角色），並擴充各自的角色特性。
- **封裝 (Encapsulation)**：所有類別的成員變數均宣告為 `private`，並對外提供封裝良好的 `getter`/`setter` 方法與業務操作介面。
- **多型 (Polymorphism)**：在登入驗證時，動態判斷 User 子類別類型，並在轉接視圖時透過多型化處理，將登入主體導向不同的控制器。

### 3. 自訂例外處理 (Custom Exceptions) —— 衝高分關鍵 🏆
系統沒有採用傳統以 `boolean` 回傳值來代表成功或失敗，而是導入了專業的**例外驅動開發 (Exception-Driven Development)**：
- **`CapacityFullException`**：當活動已達名額上限，學生再次嘗試報名時拋出。
- **`DuplicateRegisterException`**：當學生已存在於該活動的報名名冊中，重複報名時拋出。
- **核心報名邏輯的實作片段 (`model/Event.java`)**：
  ```java
  public void registerStudent(String username) throws CapacityFullException, DuplicateRegisterException {
      if (registeredStudentUsernames.contains(username)) {
          throw new DuplicateRegisterException("您已經報名過此活動囉！", this.title);
      }
      if (isFull()) {
          throw new CapacityFullException("很抱歉，此活動報名人數已滿！", this.title);
      }
      registeredStudentUsernames.add(username);
  }
  ```
  在 `StudentController` 中，會使用 `try-catch` 結構精確補獲這兩個異常，並將客製化錯誤以 JavaFX Alert 警告視窗呈現給使用者。

### 4. 資料持久化與防孤立級聯處理
- 使用 Java 核心的 **`FileReader` / `FileWriter`** 與高效的 **`BufferedReader` / `PrintWriter`** 進行純文字檔序列化。
- **資料完整性維護**：在主辦者刪除某項活動時，`AdminController` 會自動進行 **級聯清除 (Cascading Delete)**，尋找所有註冊的學生，將該活動 ID 從其「已報名清單」中剔除，從而防止產生懸空的孤立參考，展現精準的關聯式資料庫設計思維。

---

## 🔑 預設測試帳號資訊 (Test Credentials)

系統啟動時若發現沒有資料檔案，會**自動在 `data/` 中建立預設資料**，您可以使用以下帳號直接進行測試與評分：

| 身分角色 | 登入帳號 | 登入密碼 | 真實姓名 | 隸屬單位 / 備註 |
| :--- | :--- | :--- | :--- | :--- |
| **學生 (Student)** | `student1` | `password` | 王小明 | 已報名 Java 工作坊與就業博覽會 |
| **學生 (Student)** | `student2` | `password` | 李婷婷 | 已報名就業博覽會 |
| **主辦者 (Organizer)** | `admin` | `admin` | 系學會會長 | 資訊工程系學會 |
| **主辦者 (Organizer)** | `organizer1` | `password` | 陳老師 | 課外活動指導組 |

---

## 🏃 如何編譯與執行 (Compile & Run)

> [!IMPORTANT]
> **啟動核心須知**：由於 Java 9+ 的模組化啟動限制，直接執行 `App.java` (繼承了 Application) 會報出 `Unable to initialize main class` 的錯誤。
> 系統為你提供了專屬的啟動代理 **`Launcher.java`**，請**務必編譯並執行 `Launcher.java`** 以確保 JavaFX 能被 JVM 正確引導與啟動！

### 情況 A：使用傳統 Command Line (需具備 JavaFX SDK 環境)
1. **編譯檔案**：
   ```bash
   javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml Launcher.java App.java model/*.java controller/*.java repository/*.java exception/*.java
   ```
2. **執行程式** (執行不帶有模組限制的 `Launcher`)：
   ```bash
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml Launcher
   ```

### 情況 B：使用 IntelliJ IDEA, Eclipse 或 NetBeans (一鍵點擊，極力推薦 ⭐)
1. **新建專案**：在 IDE 中新建一個 `JavaFX` 專案 (Maven 或 Gradle 均可)。
2. **匯入原始碼**：將本目錄下的所有 `.java` 檔案 (含 `Launcher.java`) 匯入至專案原始碼目錄中，將 FXML 檔案放置於專案根目錄下（或視 IDE 設定放在 resources 資源目錄下）。
3. **新增依賴**：在 IDE 專案設定中加入 `javafx-controls` 與 `javafx-fxml` 軟體包。
4. **一鍵啟動**：直接在 **`Launcher.java`** 檔案上點擊滑鼠右鍵並執行 `main` 方法，即可開起專案視窗！
