import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class TaskManagerApplication {
    private List<Task> taskList;
    private int nextTaskId;
    private JFrame mainWindow;
    private JTable tasksTable;
    private JTable overdueTasksTable;
    private DefaultTableModel tasksTableModel;
    private DefaultTableModel overdueTableModel;
    private JTextArea taskDetailsArea;

    public TaskManagerApplication() {
        taskList = new ArrayList<>();
        nextTaskId = 1;
        initializeGUI();
    }

    //создаётся граф. интерфейс
    private void initializeGUI() {
        // Главное окно
        mainWindow = new JFrame("Менеджер задач");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLayout(new BorderLayout());
        mainWindow.setSize(1200, 700);
        mainWindow.setLocationRelativeTo(null);

        // панель с кнопками
        JPanel controlPanel = new JPanel(new BorderLayout());

        // первая строка кнопок
        JPanel mainActionsPanel = new JPanel(new FlowLayout());
        JButton addTaskButton = createColoredButton("Добавить задачу", new Color(33, 150, 243));
        JButton editTaskButton = createColoredButton("Редактировать", new Color(33, 150, 243));
        JButton deleteTaskButton = createColoredButton("Удалить", new Color(244, 67, 54));
        JButton searchTasksButton = createColoredButton("Поиск", new Color(33, 150, 243));

        mainActionsPanel.add(addTaskButton);
        mainActionsPanel.add(editTaskButton);
        mainActionsPanel.add(deleteTaskButton);
        mainActionsPanel.add(searchTasksButton);

        // вторая строка кнопок
        JPanel taskStatePanel = new JPanel(new FlowLayout());
        JButton changeStatusButton = createColoredButton("Изменить статус", new Color(156, 39, 176));
        JButton changePriorityButton = createColoredButton("Изменить приоритет", new Color(156, 39, 176));

        taskStatePanel.add(changeStatusButton);
        taskStatePanel.add(changePriorityButton);

        // третья строка нопок
        JPanel sortingPanel = new JPanel(new FlowLayout());
        JButton sortByDateButton = createColoredButton("Сортировать по дате", new Color(76, 175, 80));
        JButton sortByPriorityButton = createColoredButton("Сортировать по приоритету", new Color(76, 175, 80));

        sortingPanel.add(sortByDateButton);
        sortingPanel.add(sortByPriorityButton);

        // кнопка выхода
        JPanel exitPanel = new JPanel(new FlowLayout());
        JButton exitButton = createColoredButton("Выход", new Color(158, 158, 158));
        exitButton.setPreferredSize(new Dimension(120, 35));
        exitPanel.add(exitButton);

        // Добавляем все панели в основную
        JPanel buttonsContainer = new JPanel(new GridLayout(4, 1));
        buttonsContainer.add(mainActionsPanel);
        buttonsContainer.add(taskStatePanel);
        buttonsContainer.add(sortingPanel);
        buttonsContainer.add(exitPanel);

        controlPanel.add(buttonsContainer, BorderLayout.CENTER);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // все задачи
        JPanel allTasksPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"ID", "Название", "Приоритет", "Статус", "Срок"};
        tasksTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tasksTable = new JTable(tasksTableModel);
        tasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tasksScrollPane = new JScrollPane(tasksTable);
        tasksScrollPane.setBorder(BorderFactory.createTitledBorder("Все задачи"));

        allTasksPanel.add(tasksScrollPane, BorderLayout.CENTER);

        // просроченные задачи
        JPanel overdueTasksPanel = new JPanel(new BorderLayout());

        String[] overdueColumnNames = {"ID", "Название", "Приоритет", "Просрочено дней"};
        overdueTableModel = new DefaultTableModel(overdueColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        overdueTasksTable = new JTable(overdueTableModel);
        overdueTasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane overdueScrollPane = new JScrollPane(overdueTasksTable);
        overdueScrollPane.setBorder(BorderFactory.createTitledBorder("Просроченные задачи"));

        overdueTasksPanel.add(overdueScrollPane, BorderLayout.CENTER);

        tablesPanel.add(allTasksPanel);
        tablesPanel.add(overdueTasksPanel);

        // детали задач
        JPanel detailsPanel = new JPanel(new BorderLayout());

        taskDetailsArea = new JTextArea(8, 80);
        taskDetailsArea.setEditable(false);
        taskDetailsArea.setLineWrap(true);
        taskDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScrollPane = new JScrollPane(taskDetailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Детали задачи"));

        detailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(tablesPanel, BorderLayout.CENTER);
        contentPanel.add(detailsPanel, BorderLayout.SOUTH);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controlPanel, contentPanel);
        mainSplitPane.setResizeWeight(0.1);

        mainWindow.add(mainSplitPane, BorderLayout.CENTER);

        // названия кнопок
        addTaskButton.addActionListener(e -> createTask());
        editTaskButton.addActionListener(e -> editTask());
        deleteTaskButton.addActionListener(e -> deleteTask());
        changeStatusButton.addActionListener(e -> changeStatus());
        changePriorityButton.addActionListener(e -> changePriority());
        sortByDateButton.addActionListener(e -> sortTasksByDate());
        sortByPriorityButton.addActionListener(e -> sortTasksByPriority());
        searchTasksButton.addActionListener(e -> searchTasks());
        exitButton.addActionListener(e -> exitApplication());

        // детали задачи
        tasksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedTaskDetails();
            }
        });

        // редактирование задачи, если нажать 2 раза
        tasksTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editTask();
                }
            }
        });

        refreshTasksTable();
        refreshOverdueTable();
        mainWindow.setVisible(true);
    }

    // создает цветную кнопку
    private JButton createColoredButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(180, 35));
        return button;
    }

    // создать новую задачу
    private void createTask() {
        JTextField titleField = new JTextField();
        JTextArea descriptionArea = new JTextArea(5, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        // можно выбрать приоритет
        JComboBox<String> priorityCombo = new JComboBox<>();
        for (Priority p : Priority.values()) {
            priorityCombo.addItem(p.getDisplayName());
        }

        // выбрать срок
        JTextField dateField = new JTextField(LocalDate.now().format(Task.DATE_FORMATTER));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название:"));
        panel.add(titleField);
        panel.add(new JLabel("Описание:"));
        panel.add(descriptionScroll);
        panel.add(new JLabel("Приоритет:"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Срок (дд.мм.гггг):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(mainWindow, panel, "Создание задачи",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(mainWindow, "Название задачи не может быть пустым!");
                    return;
                }

                LocalDate dueDate = LocalDate.parse(dateField.getText(), Task.DATE_FORMATTER);

                Priority priority = Priority.values()[priorityCombo.getSelectedIndex()];

                // обновляет таблицу
                Task newTask = new Task(nextTaskId++, title, descriptionArea.getText(), dueDate, priority);
                taskList.add(newTask);
                refreshTasksTable();
                refreshOverdueTable();
                JOptionPane.showMessageDialog(mainWindow, "Задача успешно создана!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainWindow, "Ошибка в формате даты! Используйте дд.мм.гггг");
            }
        }
    }

    // редактироват задачу
    private void editTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainWindow, "Выберите задачу для редактирования!");
            return;
        }

        int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
        Task task = findTaskById(taskId);
        if (task == null) return;

        JTextField titleField = new JTextField(task.getTitle());
        JTextArea descriptionArea = new JTextArea(task.getDescription(), 5, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        JComboBox<String> priorityCombo = new JComboBox<>();
        for (Priority p : Priority.values()) {
            priorityCombo.addItem(p.getDisplayName());
        }
        priorityCombo.setSelectedItem(task.getPriority().getDisplayName());

        JComboBox<String> statusCombo = new JComboBox<>();
        for (Status s : Status.values()) {
            statusCombo.addItem(s.getDisplayName());
        }
        statusCombo.setSelectedItem(task.getStatus().getDisplayName());

        JTextField dateField = new JTextField(task.getDueDate().format(Task.DATE_FORMATTER));

        // окно для редактирования
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Название:"));
        panel.add(titleField);
        panel.add(new JLabel("Описание:"));
        panel.add(descriptionScroll);
        panel.add(new JLabel("Приоритет:"));
        panel.add(priorityCombo);
        panel.add(new JLabel("Статус:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Срок (дд.мм.гггг):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(mainWindow, panel, "Редактирование задачи",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // обновляет задачу
                task.setTitle(titleField.getText());
                task.setDescription(descriptionArea.getText());
                task.setPriority(Priority.values()[priorityCombo.getSelectedIndex()]);
                task.setStatus(Status.values()[statusCombo.getSelectedIndex()]);
                task.setDueDate(LocalDate.parse(dateField.getText(), Task.DATE_FORMATTER));

                refreshTasksTable();
                refreshOverdueTable();
                showSelectedTaskDetails();
                JOptionPane.showMessageDialog(mainWindow, "Задача успешно обновлена!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainWindow, "Ошибка в формате даты! Используйте дд.мм.гггг");
            }
        }
    }

    // изменяет статус
    private void changeStatus() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainWindow, "Выберите задачу для изменения статуса!");
            return;
        }

        int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
        Task task = findTaskById(taskId);
        if (task == null) return;

        JComboBox<String> statusCombo = new JComboBox<>();
        for (Status s : Status.values()) {
            statusCombo.addItem(s.getDisplayName());
        }
        statusCombo.setSelectedItem(task.getStatus().getDisplayName());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Задача: " + task.getTitle()));
        panel.add(new JLabel("Текущий статус: " + task.getStatus().getDisplayName()));
        panel.add(new JLabel("Новый статус:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(mainWindow, panel, "Изменение статуса",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            task.setStatus(Status.values()[statusCombo.getSelectedIndex()]);
            refreshTasksTable();
            refreshOverdueTable();
            showSelectedTaskDetails();
            JOptionPane.showMessageDialog(mainWindow, "Статус задачи успешно изменен!");
        }
    }

    // изменяет приоритет
    private void changePriority() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainWindow, "Выберите задачу для изменения приоритета!");
            return;
        }

        int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
        Task task = findTaskById(taskId);
        if (task == null) return;

        JComboBox<String> priorityCombo = new JComboBox<>();
        for (Priority p : Priority.values()) {
            priorityCombo.addItem(p.getDisplayName());
        }
        priorityCombo.setSelectedItem(task.getPriority().getDisplayName());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Задача: " + task.getTitle()));
        panel.add(new JLabel("Текущий приоритет: " + task.getPriority().getDisplayName()));
        panel.add(new JLabel("Новый приоритет:"));
        panel.add(priorityCombo);

        int result = JOptionPane.showConfirmDialog(mainWindow, panel, "Изменение приоритета",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            task.setPriority(Priority.values()[priorityCombo.getSelectedIndex()]);
            refreshTasksTable();
            refreshOverdueTable();
            showSelectedTaskDetails();
            JOptionPane.showMessageDialog(mainWindow, "Приоритет задачи успешно изменен!");
        }
    }

    // сортировка по дате
    private void sortTasksByDate() {
        taskList.sort(Comparator.comparing(Task::getDueDate));
        refreshTasksTable();
        JOptionPane.showMessageDialog(mainWindow, "Задачи отсортированы!");
    }

    // сортировка по приоритету
    private void sortTasksByPriority() {
        taskList.sort((task1, task2) -> {
            // HIGH (0) -> MEDIUM (1) -> LOW (2) - по УБЫВАНИЮ приоритета
            return Integer.compare(task2.getPriority().ordinal(), task1.getPriority().ordinal());
        });
        refreshTasksTable();
        JOptionPane.showMessageDialog(mainWindow, "Задачи отсортированы!");
    }

    // удалить
    private void deleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainWindow, "Выберите задачу для удаления!");
            return;
        }

        int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
        Task task = findTaskById(taskId);
        if (task == null) return;

        int result = JOptionPane.showConfirmDialog(mainWindow,
                "Вы действительно хотите удалить задачу:\n" + task.getTitle(),
                "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            taskList.remove(task);
            refreshTasksTable();
            refreshOverdueTable();
            taskDetailsArea.setText("");
            JOptionPane.showMessageDialog(mainWindow, "Задача успешно удалена!");
        }
    }

    // поиск
    private void searchTasks() {
        String[] options = {"По названию/описанию", "По приоритету", "По статусу", "Просроченные"};
        String choice = (String) JOptionPane.showInputDialog(mainWindow,
                "Выберите тип поиска:", "Поиск задач",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice == null) return;

        List<Task> searchResults = new ArrayList<>();

        switch (choice) {
            case "По названию/описанию":
                String keyword = JOptionPane.showInputDialog(mainWindow, "Введите ключевое слово:");
                if (keyword != null && !keyword.trim().isEmpty()) {
                    searchResults = taskList.stream()
                            .filter(task -> task.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                    task.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                            .collect(Collectors.toList());
                }
                break;

            case "По приоритету":
                String[] priorityOptions = Arrays.stream(Priority.values())
                        .map(Priority::getDisplayName)
                        .toArray(String[]::new);

                String priorityChoice = (String) JOptionPane.showInputDialog(mainWindow,
                        "Выберите приоритет:", "Поиск по приоритету",
                        JOptionPane.QUESTION_MESSAGE, null, priorityOptions, priorityOptions[0]);

                if (priorityChoice != null) {
                    for (Priority p : Priority.values()) {
                        if (p.getDisplayName().equals(priorityChoice)) {
                            searchResults = taskList.stream()
                                    .filter(task -> task.getPriority() == p)
                                    .collect(Collectors.toList());
                            break;
                        }
                    }
                }
                break;

            case "По статусу":
                String[] statusOptions = Arrays.stream(Status.values())
                        .map(Status::getDisplayName)
                        .toArray(String[]::new);

                String statusChoice = (String) JOptionPane.showInputDialog(mainWindow,
                        "Выберите статус:", "Поиск по статусу",
                        JOptionPane.QUESTION_MESSAGE, null, statusOptions, statusOptions[0]);

                if (statusChoice != null) {
                    for (Status s : Status.values()) {
                        if (s.getDisplayName().equals(statusChoice)) {
                            searchResults = taskList.stream()
                                    .filter(task -> task.getStatus() == s)
                                    .collect(Collectors.toList());
                            break;
                        }
                    }
                }
                break;

            case "Просроченные":
                searchResults = getOverdueTasks();
                break;
        }

        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "Задачи не найдены.");
        } else {
            showSearchResults(searchResults);
        }
    }

    // результат поиска
    private void showSearchResults(List<Task> results) {
        JDialog resultsDialog = new JDialog(mainWindow, "Результаты поиска", true);
        resultsDialog.setLayout(new BorderLayout());
        resultsDialog.setSize(600, 400);
        resultsDialog.setLocationRelativeTo(mainWindow);

        String[] columnNames = {"ID", "Название", "Приоритет", "Статус", "Срок"};
        DefaultTableModel resultsModel = new DefaultTableModel(columnNames, 0);

        for (Task task : results) {
            resultsModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getPriority().getDisplayName(),
                    task.getStatus().getDisplayName(),
                    task.getDueDate().format(Task.DATE_FORMATTER)
            });
        }

        JTable resultsTable = new JTable(resultsModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> resultsDialog.dispose());

        resultsDialog.add(scrollPane, BorderLayout.CENTER);
        resultsDialog.add(closeButton, BorderLayout.SOUTH);
        resultsDialog.setVisible(true);
    }

    // обновляются просроченные задачи
    private void refreshOverdueTable() {
        overdueTableModel.setRowCount(0);
        List<Task> overdueTasks = getOverdueTasks();

        for (Task task : overdueTasks) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(task.getDueDate(), LocalDate.now());
            overdueTableModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getPriority().getDisplayName(),
                    daysOverdue + " дн."
            });
        }
    }

    private List<Task> getOverdueTasks() {
        return taskList.stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    // выход из окна
    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(mainWindow,
                "Вы действительно хотите выйти?", "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            mainWindow.dispose();
            System.exit(0);
        }
    }

    // обновляются все задачи
    private void refreshTasksTable() {
        tasksTableModel.setRowCount(0);
        for (Task task : taskList) {
            tasksTableModel.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getPriority().getDisplayName(),
                    task.getStatus().getDisplayName(),
                    task.getDueDate().format(Task.DATE_FORMATTER)
            });
        }
    }

    // описание всей задачи
    private void showSelectedTaskDetails() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow != -1) {
            int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
            Task task = findTaskById(taskId);
            if (task != null) {
                String taskInfo = task.getFullInfo();
                taskDetailsArea.setText(taskInfo);
            }
        }
    }

    private Task findTaskById(int id) {
        for (Task task : taskList) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TaskManagerApplication();
            }
        });
    }
}