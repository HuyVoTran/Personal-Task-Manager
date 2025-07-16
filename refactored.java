import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class App {

    public static void main(String[] args) {
        // Khởi tạo Dependency
        ITaskRepository taskRepository = new JsonTaskRepository();
        PersonalTaskManager manager = new PersonalTaskManager(taskRepository);

        System.out.println("--- Minh họa thêm nhiệm vụ ---");
        // Thêm nhiệm vụ hợp lệ
        Optional<Task> task1 = manager.addNewTask(
                "Mua sách",
                "Sách Công nghệ phần mềm.",
                "2025-07-20",
                "Cao"
        );
        task1.ifPresent(task -> System.out.println("Nhiệm vụ đã thêm: " + task));

        // Thêm nhiệm vụ trùng lặp (minh họa kiểm tra trùng lặp)
        System.out.println("\n--- Minh họa thêm nhiệm vụ trùng lặp ---");
        manager.addNewTask(
                "Mua sách",
                "Sách Công nghệ phần mềm.",
                "2025-07-20",
                "Cao"
        );

        // Thêm nhiệm vụ với tiêu đề rỗng (minh họa xác thực)
        System.out.println("\n--- Minh họa thêm nhiệm vụ với tiêu đề rỗng ---");
        manager.addNewTask(
                "",
                "Nhiệm vụ không có tiêu đề.",
                "2025-07-22",
                "Thấp"
        );

        // Thêm nhiệm vụ với ngày không hợp lệ
        System.out.println("\n--- Minh họa thêm nhiệm vụ với ngày không hợp lệ ---");
        manager.addNewTask(
                "Lên kế hoạch",
                "Lên kế hoạch cho dự án mới.",
                "2025/07/23", // Định dạng sai
                "Trung bình"
        );

        // Thêm nhiệm vụ với mức độ ưu tiên không hợp lệ
        System.out.println("\n--- Minh họa thêm nhiệm vụ với mức độ ưu tiên không hợp lệ ---");
        manager.addNewTask(
                "Học Java",
                "Học các khái niệm OOP.",
                "2025-07-24",
                "Rất cao" // Mức độ ưu tiên không hợp lệ
        );

        // Thêm một vài nhiệm vụ khác để minh họa cập nhật/xóa
        System.out.println("\n--- Thêm các nhiệm vụ khác để minh họa cập nhật/xóa ---");
        Optional<Task> task2 = manager.addNewTask(
                "Viết báo cáo",
                "Báo cáo cuối kỳ.",
                "2025-07-25",
                "Cao"
        );
        Optional<Task> task3 = manager.addNewTask(
                "Gặp khách hàng",
                "Thảo luận dự án.",
                "2025-07-26",
                "Trung bình"
        );

        // Minh họa cập nhật trạng thái
        System.out.println("\n--- Minh họa cập nhật trạng thái nhiệm vụ ---");
        if (task2.isPresent()) {
            manager.updateTaskStatus(task2.get().getId(), "Đã hoàn thành");
        } else {
            System.out.println("Không thể cập nhật: Nhiệm vụ 2 không được tạo.");
        }

        // Minh họa xóa nhiệm vụ
        System.out.println("\n--- Minh họa xóa nhiệm vụ ---");
        if (task3.isPresent()) {
            manager.deleteTask(task3.get().getId());
        } else {
            System.out.println("Không thể xóa: Nhiệm vụ 3 không được tạo.");
        }

        // Hiển thị tất cả nhiệm vụ sau các thao tác
        System.out.println("\n--- Tất cả nhiệm vụ hiện có trong DB ---");
        List<Task> allTasks = taskRepository.getAllTasks();
        if (allTasks.isEmpty()) {
            System.out.println("Không có nhiệm vụ nào trong DB.");
        } else {
            allTasks.forEach(System.out::println);
        }
    }
}

// Lớp Task (POJO)
class Task {
    private String id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String priority; // "Thấp", "Trung bình", "Cao"
    private String status;   // "Chưa hoàn thành", "Đã hoàn thành"
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    // Constructor
    public Task(String id, String title, String description, LocalDate dueDate,
                String priority, String status, LocalDateTime createdAt, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Constructor for new tasks (ID, createdAt, lastUpdatedAt generated internally)
    public Task(String title, String description, LocalDate dueDate, String priority) {
        this.id = UUID.randomUUID().toString(); // Generate ID here
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = "Chưa hoàn thành"; // Default status
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    // Setters (for updatable fields)
    public void setTitle(String title) {
        this.title = title;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    public void setDescription(String description) {
        this.description = description;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    public void setPriority(String priority) {
        this.priority = priority;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    public void setStatus(String status) {
        this.status = status;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // Convert Task object to JSONObject for saving to JSON file
    @SuppressWarnings("unchecked") // Suppress warning for JSONObject.put
    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        obj.put("id", this.id);
        obj.put("title", this.title);
        obj.put("description", this.description);
        obj.put("due_date", this.dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        obj.put("priority", this.priority);
        obj.put("status", this.status);
        obj.put("created_at", this.createdAt.format(DateTimeFormatter.ISO_DATE_TIME));
        obj.put("last_updated_at", this.lastUpdatedAt.format(DateTimeFormatter.ISO_DATE_TIME));
        return obj;
    }

    // Create Task object from JSONObject loaded from JSON file
    public static Task fromJsonObject(JSONObject obj) {
        String id = (String) obj.get("id");
        String title = (String) obj.get("title");
        String description = (String) obj.get("description");
        LocalDate dueDate = LocalDate.parse((String) obj.get("due_date"), DateTimeFormatter.ISO_LOCAL_DATE);
        String priority = (String) obj.get("priority");
        String status = (String) obj.get("status");
        LocalDateTime createdAt = LocalDateTime.parse((String) obj.get("created_at"), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime lastUpdatedAt = LocalDateTime.parse((String) obj.get("last_updated_at"), DateTimeFormatter.ISO_DATE_TIME);

        return new Task(id, title, description, dueDate, priority, status, createdAt, lastUpdatedAt);
    }

    @Override
    public String toString() {
        return "Task{" +
               "id='" + id + '\'' +
               ", title='" + title + '\'' +
               ", dueDate=" + dueDate +
               ", priority='" + priority + '\'' +
               ", status='" + status + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

// Interface cho Task Repository để tuân thủ Dependency Inversion Principle
interface ITaskRepository {
    List<Task> getAllTasks();
    Optional<Task> findById(String id);
    void save(Task task); // Add or update
    void delete(String id);
    boolean existsByTitleAndDueDate(String title, String dueDateFormatted); // For duplicate check
}

// Triển khai Task Repository sử dụng file JSON
class JsonTaskRepository implements ITaskRepository {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // Use ISO format for consistency

    // Tải tất cả nhiệm vụ từ file JSON
    private JSONArray loadJsonArrayFromFile() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
            // Trả về JSONArray rỗng nếu file không tồn tại hoặc có lỗi
        }
        return new JSONArray();
    }

    // Lưu JSONArray vào file JSON
    private void saveJsonArrayToFile(JSONArray tasksData) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasksData.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }

    @Override
    public List<Task> getAllTasks() {
        JSONArray jsonTasks = loadJsonArrayFromFile();
        List<Task> tasks = new ArrayList<>();
        for (Object obj : jsonTasks) {
            tasks.add(Task.fromJsonObject((JSONObject) obj));
        }
        return tasks;
    }

    @Override
    public Optional<Task> findById(String id) {
        return getAllTasks().stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void save(Task task) {
        JSONArray jsonTasks = loadJsonArrayFromFile();
        boolean found = false;

        // Cập nhật nhiệm vụ nếu đã tồn tại
        for (int i = 0; i < jsonTasks.size(); i++) {
            JSONObject existingJsonTask = (JSONObject) jsonTasks.get(i);
            if (existingJsonTask.get("id").equals(task.getId())) {
                jsonTasks.set(i, task.toJsonObject()); // Thay thế bằng JSONObject mới
                found = true;
                break;
            }
        }

        // Thêm nhiệm vụ mới nếu chưa tồn tại
        if (!found) {
            jsonTasks.add(task.toJsonObject());
        }
        saveJsonArrayToFile(jsonTasks);
    }

    @Override
    public void delete(String id) {
        JSONArray jsonTasks = loadJsonArrayFromFile();
        // Lọc ra các nhiệm vụ không có ID cần xóa
        JSONArray updatedJsonTasks = jsonTasks.stream()
                .filter(obj -> !((JSONObject) obj).get("id").equals(id))
                .collect(Collectors.toCollection(JSONArray::new));
        
        if (updatedJsonTasks.size() < jsonTasks.size()) { // Chỉ lưu nếu có sự thay đổi (nhiệm vụ được tìm thấy và xóa)
            saveJsonArrayToFile(updatedJsonTasks);
        }
    }

    @Override
    public boolean existsByTitleAndDueDate(String title, String dueDateFormatted) {
        return getAllTasks().stream()
                .anyMatch(task -> task.getTitle().equalsIgnoreCase(title) &&
                                 task.getDueDate().format(DATE_FORMATTER).equals(dueDateFormatted));
    }
}

// Lớp tiện ích để xác thực đầu vào nhiệm vụ
class TaskValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final List<String> VALID_PRIORITIES = Arrays.asList("Thấp", "Trung bình", "Cao");

    public static boolean isValidTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi xác thực: Tiêu đề không được để trống.");
            return false;
        }
        return true;
    }

    public static boolean isValidDescription(String description) {
        // Có thể thêm logic xác thực cho mô tả nếu cần
        return true; // Hiện tại không có yêu cầu cụ thể, luôn trả về true
    }

    public static boolean isValidDueDate(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi xác thực: Ngày đến hạn không được để trống.");
            return false;
        }
        try {
            LocalDate.parse(dueDateStr, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi xác thực: Ngày đến hạn không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.");
            return false;
        }
    }

    public static boolean isValidPriority(String priorityLevel) {
        if (priorityLevel == null || priorityLevel.trim().isEmpty()) {
            System.out.println("Lỗi xác thực: Mức độ ưu tiên không được để trống.");
            return false;
        }
        if (!VALID_PRIORITIES.contains(priorityLevel)) {
            System.out.println("Lỗi xác thực: Mức độ ưu tiên không hợp lệ. Vui lòng chọn từ: Thấp, Trung bình, Cao.");
            return false;
        }
        return true;
    }
}

// Lớp chứa logic nghiệp vụ chính của ứng dụng quản lý nhiệm vụ
class PersonalTaskManager {

    private final ITaskRepository taskRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // Dependency Injection: Nhận ITaskRepository qua constructor
    public PersonalTaskManager(ITaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Thêm nhiệm vụ mới vào hệ thống.
     *
     * @param title Tiêu đề nhiệm vụ.
     * @param description Mô tả nhiệm vụ.
     * @param dueDateStr Ngày đến hạn (định dạng YYYY-MM-DD).
     * @param priorityLevel Mức độ ưu tiên ("Thấp", "Trung bình", "Cao").
     * @return Optional chứa nhiệm vụ đã thêm nếu thành công, hoặc Optional.empty() nếu có lỗi.
     */
    public Optional<Task> addNewTask(String title, String description,
                                    String dueDateStr, String priorityLevel) {

        // 1. Xác thực đầu vào
        if (!TaskValidator.isValidTitle(title) ||
            !TaskValidator.isValidDueDate(dueDateStr) ||
            !TaskValidator.isValidPriority(priorityLevel)) {
            return Optional.empty(); // Lỗi xác thực đã được in bên trong TaskValidator
        }

        LocalDate dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        String formattedDueDate = dueDate.format(DATE_FORMATTER);

        // 2. Kiểm tra trùng lặp
        if (taskRepository.existsByTitleAndDueDate(title, formattedDueDate)) {
            System.out.println(String.format("Lỗi: Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn (%s).", title, formattedDueDate));
            return Optional.empty();
        }

        // 3. Tạo đối tượng Task mới
        Task newTask = new Task(title, description, dueDate, priorityLevel);

        // 4. Lưu nhiệm vụ vào kho lưu trữ
        taskRepository.save(newTask);

        System.out.println(String.format("Đã thêm nhiệm vụ mới thành công với ID: %s", newTask.getId()));
        return Optional.of(newTask);
    }

    /**
     * Cập nhật trạng thái của một nhiệm vụ.
     * @param taskId ID của nhiệm vụ cần cập nhật.
     * @param newStatus Trạng thái mới ("Chưa hoàn thành" hoặc "Đã hoàn thành").
     * @return true nếu cập nhật thành công, false nếu không tìm thấy nhiệm vụ hoặc trạng thái không hợp lệ.
     */
    public boolean updateTaskStatus(String taskId, String newStatus) {
        if (!newStatus.equals("Chưa hoàn thành") && !newStatus.equals("Đã hoàn thành")) {
            System.out.println("Lỗi: Trạng thái không hợp lệ. Vui lòng chọn 'Chưa hoàn thành' hoặc 'Đã hoàn thành'.");
            return false;
        }

        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(newStatus);
            taskRepository.save(task); // Save will update if ID exists
            System.out.println(String.format("Đã cập nhật trạng thái nhiệm vụ ID '%s' thành '%s'.", taskId, newStatus));
            return true;
        } else {
            System.out.println(String.format("Lỗi: Không tìm thấy nhiệm vụ với ID '%s'.", taskId));
            return false;
        }
    }

    /**
     * Xóa một nhiệm vụ khỏi hệ thống.
     * @param taskId ID của nhiệm vụ cần xóa.
     * @return true nếu xóa thành công, false nếu không tìm thấy nhiệm vụ.
     */
    public boolean deleteTask(String taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            taskRepository.delete(taskId);
            System.out.println(String.format("Đã xóa nhiệm vụ với ID '%s'.", taskId));
            return true;
        } else {
            System.out.println(String.format("Lỗi: Không tìm thấy nhiệm vụ với ID '%s' để xóa.", taskId));
            return false;
        }
    }
}
