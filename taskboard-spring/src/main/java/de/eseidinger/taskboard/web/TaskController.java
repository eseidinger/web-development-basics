package de.eseidinger.taskboard.web;

import de.eseidinger.taskboard.service.TaskboardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskboardService taskboardService;

    public TaskController(TaskboardService taskboardService) {
        this.taskboardService = taskboardService;
    }

    @GetMapping("/{taskId}")
    ApiDtos.TaskResponse getTask(@PathVariable UUID taskId) {
        return taskboardService.getTask(taskId);
    }

    @PatchMapping("/{taskId}")
    ApiDtos.TaskResponse updateTask(@PathVariable UUID taskId, @RequestBody ApiDtos.UpdateTaskRequest request) {
        return taskboardService.updateTask(taskId, request);
    }

    @PostMapping("/{taskId}/move")
    ApiDtos.TaskResponse moveTask(@PathVariable UUID taskId, @Valid @RequestBody ApiDtos.MoveTaskRequest request) {
        return taskboardService.moveTask(taskId, request);
    }

    @PostMapping("/{taskId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void archiveTask(@PathVariable UUID taskId) {
        taskboardService.archiveTask(taskId);
    }

    @PostMapping("/{taskId}/restore")
    ApiDtos.TaskResponse restoreTask(@PathVariable UUID taskId, @Valid @RequestBody ApiDtos.RestoreTaskRequest request) {
        return taskboardService.restoreTask(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTask(@PathVariable UUID taskId) {
        taskboardService.deleteTask(taskId);
    }

    @GetMapping("/{taskId}/comments")
    List<ApiDtos.CommentResponse> listComments(@PathVariable UUID taskId) {
        return taskboardService.listComments(taskId);
    }

    @PostMapping("/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    ApiDtos.CommentResponse addComment(@PathVariable UUID taskId, @Valid @RequestBody ApiDtos.CreateCommentRequest request) {
        return taskboardService.addComment(taskId, request);
    }

    @PatchMapping("/{taskId}/comments/{commentId}")
    ApiDtos.CommentResponse updateComment(@PathVariable UUID taskId,
                                          @PathVariable UUID commentId,
                                          @Valid @RequestBody ApiDtos.UpdateCommentRequest request) {
        return taskboardService.updateComment(taskId, commentId, request);
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(@PathVariable UUID taskId, @PathVariable UUID commentId) {
        taskboardService.deleteComment(taskId, commentId);
    }
}
