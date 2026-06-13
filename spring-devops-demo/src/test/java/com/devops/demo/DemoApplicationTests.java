package com.devops.demo;

import com.devops.demo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Test
    void contextLoads() {
        assertNotNull(taskService);
    }

    @Test
    void homePageReturns200() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"));
    }

    @Test
    void apiStatusReturns200() throws Exception {
        mockMvc.perform(get("/api/status"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void taskServiceHasDefaultTasks() {
        assertTrue(taskService.getAllTasks().size() > 0);
    }

//    @Test
//    void canAddTask() {
//        int before = taskService.getAllTasks().size();
//        taskService.addTask("Test Task", "Test Desc", "LOW");
//        assertEquals(before + 1, taskService.getAllTasks().size());
//    }
}
