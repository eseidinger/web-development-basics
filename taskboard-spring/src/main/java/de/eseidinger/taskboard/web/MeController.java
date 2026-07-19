package de.eseidinger.taskboard.web;

import de.eseidinger.taskboard.service.TaskboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final TaskboardService taskboardService;

    public MeController(TaskboardService taskboardService) {
        this.taskboardService = taskboardService;
    }

    @GetMapping("/tasks")
    List<ApiDtos.TaskResponse> listMyTasks(@RequestParam(required = false) LocalDate dueBefore,
                                           @RequestParam(required = false) LocalDate dueAfter) {
        return taskboardService.listMyTasks(dueBefore, dueAfter);
    }
}