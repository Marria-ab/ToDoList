import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// виды приоритетов
enum Priority {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий");

    private final String displayName;

    Priority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

// виды статуса
enum Status {
    TODO("К выполнению"),
    IN_PROGRESS("В процессе"),
    DONE("Выполнено");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


// инфа о задаче
class Task {
    private int id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private Status status;

    // дата
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Task(int id, String title, String description, LocalDate dueDate, Priority priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = Status.TODO;
    }

    // геттеры
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }

    // сеттеры
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setStatus(Status status) { this.status = status; }

    public String getFullInfo() {
        return "ID: " + id + "\n" +
                "Название: " + title + "\n" +
                "Описание: " + description + "\n" +
                "Приоритет: " + priority.getDisplayName() + "\n" +
                "Статус: " + status.getDisplayName() + "\n" +
                "Срок: " + dueDate.format(DATE_FORMATTER);
    }

    // проверка, если задача просрочена
    public boolean isOverdue() {
        return dueDate.isBefore(LocalDate.now()) && status != Status.DONE;
    }
}