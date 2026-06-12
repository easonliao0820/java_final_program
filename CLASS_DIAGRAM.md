# UML 類別圖 — 活動報名管理系統

```mermaid
classDiagram
    direction TB

    %% ── Exception ──────────────────────────────────────────
    class Exception {
        <<Java Built-in>>
    }
    class CapacityFullException {
        -String eventTitle
        +CapacityFullException(String message)
        +CapacityFullException(String message, String eventTitle)
        +getEventTitle() String
    }
    class DuplicateRegisterException {
        -String eventTitle
        +DuplicateRegisterException(String message)
        +DuplicateRegisterException(String message, String eventTitle)
        +getEventTitle() String
    }
    Exception <|-- CapacityFullException
    Exception <|-- DuplicateRegisterException

    %% ── Model ──────────────────────────────────────────────
    class User {
        <<abstract>>
        #String id
        #String name
        #String password
        #String role
        +displayMenu()* void
        +getSerializedExtra()* String
        +applySerializedExtra(String extra) void
        +getId() String
        +getName() String
        +getPassword() String
        +getRole() String
    }
    class Student {
        -List~Event~ registeredEvents
        -List~String~ tempRegisteredEventIds
        +registerEvent(Event e) void
        +cancelEvent(Event e) void
        +getSerializedExtra() String
        +applySerializedExtra(String extra) void
        +getRegisteredEvents() List~Event~
        +getRegisteredEventIds() List~String~
        +getTempRegisteredEventIds() List~String~
        +displayMenu() void
    }
    class Organizer {
        -String organization
        -List~Event~ hostedEvents
        +createEvent(Event e) void
        +editEvent(Event e) void
        +getSerializedExtra() String
        +getOrganization() String
        +getHostedEvents() List~Event~
        +displayMenu() void
    }
    class Event {
        -String id
        -String title
        -String location
        -String time
        -int capacity
        -String description
        -Organizer organizer
        -List~Student~ participants
        -String tempOrganizerId
        -List~String~ tempParticipantIds
        +isFull() boolean
        +getRegisteredCount() int
        +getStatus() String
        +registerStudent(Student s) void
        +cancelStudent(Student s) void
        +getRegisteredStudentUsernames() List~String~
    }
    class UserFactory {
        -Map~String,UserCreator~ registry$
        +register(String role, UserCreator c)$ void
        +create(String role, String id, String pw, String name, String extra)$ User
        +isRegistered(String role)$ boolean
    }
    class UserCreator {
        <<interface>>
        +create(String id, String pw, String name, String extra) User
    }

    User <|-- Student
    User <|-- Organizer
    UserFactory ..> User : creates
    UserFactory +-- UserCreator

    Event "1" --> "0..1" Organizer : organizer
    Event "1" --> "*" Student : participants
    Student "*" --> "*" Event : registeredEvents
    Organizer "1" --> "*" Event : hostedEvents

    Event ..> CapacityFullException : throws
    Event ..> DuplicateRegisterException : throws

    %% ── Repository Interfaces ───────────────────────────────
    class IUserRepository {
        <<interface>>
        +loadUsers() List~User~
        +saveUsers(List~User~ users) void
    }
    class IEventRepository {
        <<interface>>
        +loadEvents() List~Event~
        +saveEvents(List~Event~ events) void
    }
    class IRelationshipResolver {
        <<interface>>
        +resolveRelationships(List~User~ users, List~Event~ events) void
    }

    %% ── Repository Implementations ─────────────────────────
    class FileUserRepository {
        -String DATA_DIR$
        -String USERS_FILE$
        +loadUsers() List~User~
        +saveUsers(List~User~ users) void
    }
    class FileEventRepository {
        -String DATA_DIR$
        -String EVENTS_FILE$
        +loadEvents() List~Event~
        +saveEvents(List~Event~ events) void
    }
    class RelationshipResolver {
        +resolveRelationships(List~User~ users, List~Event~ events) void
    }
    class DataContext {
        -IUserRepository userRepository
        -IEventRepository eventRepository
        -IRelationshipResolver resolver
        +getUserRepository() IUserRepository
        +getEventRepository() IEventRepository
        +loadUsers() List~User~
        +saveUsers(List~User~ users) void
        +loadEvents() List~Event~
        +saveEvents(List~Event~ events) void
        +resolveRelationships(List~User~ u, List~Event~ e) void
    }
    class FileRepository {
        <<Facade>>
        -DataContext context
        +getContext() DataContext
        +loadUsers() List~User~
        +saveUsers(List~User~ users) void
        +loadEvents() List~Event~
        +saveEvents(List~Event~ events) void
        +resolveRelationships(List~User~ u, List~Event~ e) void
    }

    IUserRepository <|.. FileUserRepository
    IEventRepository <|.. FileEventRepository
    IRelationshipResolver <|.. RelationshipResolver

    DataContext o-- IUserRepository
    DataContext o-- IEventRepository
    DataContext o-- IRelationshipResolver

    FileRepository *-- DataContext

    %% ── Controller ──────────────────────────────────────────
    class ViewOpener {
        <<interface>>
        +open(Stage loginStage, User user) void
    }
    class LoginController {
        -DataContext context
        -Map~String,ViewOpener~ viewOpeners
        +setContext(DataContext context) void
        +handleLogin() void
        +handleRegisterDialog() void
    }
    class StudentController {
        -DataContext context
        -Student currentStudent
        -List~Event~ allEvents
        -List~User~ allUsers
        +initData(DataContext context, Student student) void
        +handleRegisterEvent() void
        +handleCancelRegistration() void
        +handleLogout() void
    }
    class AdminController {
        -DataContext context
        -Organizer currentOrganizer
        -List~Event~ allEvents
        -List~User~ allUsers
        +initData(DataContext context, Organizer organizer) void
        +handleCreateEvent() void
        +handleEditEvent() void
        +handleDeleteEvent() void
        +handleExportCsv() void
        +handleLogout() void
    }

    LoginController +-- ViewOpener
    LoginController ..> DataContext : uses
    LoginController ..> StudentController : opens
    LoginController ..> AdminController : opens

    StudentController ..> DataContext : uses
    StudentController ..> Student : manages
    StudentController ..> Event : manages
    StudentController ..> CapacityFullException : catches
    StudentController ..> DuplicateRegisterException : catches

    AdminController ..> DataContext : uses
    AdminController ..> Organizer : manages
    AdminController ..> Event : manages

    %% ── App (Composition Root) ──────────────────────────────
    class App {
        <<Application>>
        +start(Stage primaryStage) void
        +main(String[] args)$ void
    }
    App ..> DataContext : creates
    App ..> LoginController : injects context
```

## 關係說明

| 符號 | 意義 |
|------|------|
| `<\|--` | 繼承 (Inheritance) |
| `<\|..` | 實作介面 (Realization) |
| `*--` | 組合 (Composition) |
| `o--` | 聚合 (Aggregation) |
| `-->` | 關聯 (Association) |
| `..>` | 依賴 (Dependency) |
| `+--` | 內部類別 (Nested Class) |

## 架構分層

| 層級 | 類別 |
|------|------|
| **App** | `App` |
| **controller** | `LoginController`、`StudentController`、`AdminController` |
| **repository** | `DataContext`、`FileRepository`、`FileUserRepository`、`FileEventRepository`、`RelationshipResolver` |
| **repository (interfaces)** | `IUserRepository`、`IEventRepository`、`IRelationshipResolver` |
| **model** | `User`、`Student`、`Organizer`、`Event`、`UserFactory` |
| **exception** | `CapacityFullException`、`DuplicateRegisterException` |
