package com.devops.demo.service;

import com.devops.demo.model.Task;
import com.devops.demo.model.User;
import com.devops.demo.repository.TaskRepository;
import com.devops.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;

    @PostConstruct
    public void seedData() {
        if (taskRepository.count() > 0) return;

        // Seed tasks without assignees so they are visible to admin immediately.
        // Assignees can be set via the UI after users register.
        Task t1 = new Task(null, "Setup EC2 Instance",
                "Launch and configure AWS EC2", true, "HIGH");
        t1.setDueDate(LocalDate.now().minusDays(3));

        Task t2 = new Task(null, "Configure GitHub Actions",
                "Setup CI/CD pipeline YAML", true, "HIGH");
        t2.setDueDate(LocalDate.now().minusDays(1));

        Task t3 = new Task(null, "Deploy Spring App",
                "Deploy JAR to EC2 via SSH", false, "MEDIUM");
        t3.setDueDate(LocalDate.now());

        Task t4 = new Task(null, "Setup Health Checks",
                "Configure /actuator/health endpoint", false, "LOW");
        t4.setDueDate(LocalDate.now().plusDays(7));

        taskRepository.saveAll(List.of(t1, t2, t3, t4));
    }


    public List<Task> searchTasks(String search, String priority, String status,
                                  String assigneeUsername, String viewerUsername) {
        return taskRepository.search(search, priority, status, assigneeUsername, viewerUsername);
    }

    public long getCompletedCount() { return taskRepository.countByCompleted(true); }
    public long getTotalCount()     { return taskRepository.count(); }

    public List<Task> getTasksDueTomorrowWithAssignee() {
        return taskRepository.findTasksDueTomorrowWithAssignee(LocalDate.now().plusDays(1));
    }

    @Transactional
    public Task addTask(String title, String description, String priority,
                        LocalDate dueDate, Long assigneeId) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setCompleted(false);
        task.setDueDate(dueDate);

        if (assigneeId != null) {
            userRepository.findById(assigneeId).ifPresent(task::setAssignee);
        }
        return taskRepository.save(task);
    }

    @Transactional
    public boolean assignTask(Long taskId, Long assigneeId) {
        Optional<Task> opt = taskRepository.findById(taskId);
        if (opt.isEmpty()) return false;
        Task task = opt.get();
        if (assigneeId == null) {
            task.setAssignee(null);
        } else {
            userRepository.findById(assigneeId).ifPresent(task::setAssignee);
        }
        taskRepository.save(task);
        return true;
    }

    @Transactional
    public boolean toggleTask(Long id) {
        Optional<Task> opt = taskRepository.findById(id);
        if (opt.isEmpty()) return false;
        Task task = opt.get();
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);
        return true;
    }

    @Transactional
    public boolean deleteTask(Long id) {
        if (!taskRepository.existsById(id)) return false;
        taskRepository.deleteById(id);
        return true;
    }


    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }
}