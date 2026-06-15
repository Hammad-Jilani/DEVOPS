package com.devops.demo.service;

import com.devops.demo.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailReminderService {

    private static final Logger log = LoggerFactory.getLogger(EmailReminderService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Autowired private JavaMailSender mailSender;
    @Autowired private TaskService    taskService;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Scheduled(cron = "${app.reminder.cron:0 0 8 * * *}")
    public void sendDueTomorrowReminders() {
        List<Task> tasks = taskService.getTasksDueTomorrowWithAssignee();

        if (tasks.isEmpty()) {
            log.info("[Reminder] No tasks due tomorrow — nothing to send.");
            return;
        }

        log.info("[Reminder] Found {} task(s) due tomorrow — sending reminders.", tasks.size());

        for (Task task : tasks) {
            String assigneeEmail = task.getAssignee().getEmail();
            String assigneeName  = task.getAssignee().getUsername();

            try {
                SimpleMailMessage msg = buildReminderEmail(task, assigneeName, assigneeEmail);
                mailSender.send(msg);
                log.info("[Reminder] Sent reminder to {} for task \"{}\"",
                        assigneeEmail, task.getTitle());
            } catch (MailException ex) {
                // Log and continue — don't let one failed email abort the batch
                log.error("[Reminder] Failed to send to {} for task \"{}\": {}",
                        assigneeEmail, task.getTitle(), ex.getMessage());
            }
        }
    }


    private SimpleMailMessage buildReminderEmail(Task task, String name, String to) {
        String dueFormatted = task.getDueDate().format(DATE_FMT);
        String priority     = task.getPriority();

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject("[DevOps Demo] Reminder: \"" + task.getTitle() + "\" is due tomorrow");
        msg.setText(
                "Hi " + name + ",\n\n" +
                        "This is a friendly reminder that the following task is due tomorrow (" + dueFormatted + "):\n\n" +
                        "  Task     : " + task.getTitle() + "\n" +
                        "  Priority : " + priority + "\n" +
                        (task.getDescription() != null && !task.getDescription().isBlank()
                                ? "  Details  : " + task.getDescription() + "\n"
                                : "") +
                        "\nPlease make sure to complete it on time.\n\n" +
                        "— Spring DevOps Demo\n"
        );
        return msg;
    }

    public int triggerNow() {
        List<Task> tasks = taskService.getTasksDueTomorrowWithAssignee();
        int sent = 0;
        for (Task task : tasks) {
            try {
                mailSender.send(buildReminderEmail(task,
                        task.getAssignee().getUsername(),
                        task.getAssignee().getEmail()));
                sent++;
            } catch (MailException ex) {
                log.error("[Reminder][Manual] Failed for task \"{}\": {}", task.getTitle(), ex.getMessage());
            }
        }
        return sent;
    }
}