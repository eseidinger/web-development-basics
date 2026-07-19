package de.eseidinger.taskboard.web;

import de.eseidinger.taskboard.service.TaskboardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {

    private final TaskboardService taskboardService;

    public BoardController(TaskboardService taskboardService) {
        this.taskboardService = taskboardService;
    }

    @GetMapping
    List<ApiDtos.BoardResponse> listBoards() {
        return taskboardService.listBoards();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiDtos.BoardResponse createBoard(@Valid @RequestBody ApiDtos.CreateBoardRequest request) {
        return taskboardService.createBoard(request);
    }

    @GetMapping("/{boardId}")
    ApiDtos.BoardResponse getBoard(@PathVariable UUID boardId) {
        return taskboardService.getBoard(boardId);
    }

    @PatchMapping("/{boardId}")
    ApiDtos.BoardResponse updateBoard(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.UpdateBoardRequest request) {
        return taskboardService.updateBoard(boardId, request);
    }

    @PostMapping("/{boardId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void archiveBoard(@PathVariable UUID boardId) {
        taskboardService.archiveBoard(boardId);
    }

    @DeleteMapping("/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBoard(@PathVariable UUID boardId) {
        taskboardService.deleteBoard(boardId);
    }

    @GetMapping("/{boardId}/columns")
    List<ApiDtos.ColumnResponse> listColumns(@PathVariable UUID boardId) {
        return taskboardService.listColumns(boardId);
    }

    @PostMapping("/{boardId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    ApiDtos.ColumnResponse createColumn(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.CreateColumnRequest request) {
        return taskboardService.createColumn(boardId, request);
    }

    @PatchMapping("/{boardId}/columns/{columnId}")
    ApiDtos.ColumnResponse updateColumn(@PathVariable UUID boardId,
                                        @PathVariable UUID columnId,
                                        @Valid @RequestBody ApiDtos.UpdateColumnRequest request) {
        return taskboardService.updateColumn(boardId, columnId, request);
    }

    @PutMapping("/{boardId}/columns/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reorderColumns(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.ReorderColumnsRequest request) {
        taskboardService.reorderColumns(boardId, request);
    }

    @DeleteMapping("/{boardId}/columns/{columnId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteColumn(@PathVariable UUID boardId,
                      @PathVariable UUID columnId,
                      @RequestParam(required = false) UUID moveTasksTo,
                      @RequestParam(required = false) Boolean deleteTasks) {
        taskboardService.deleteColumn(boardId, columnId, moveTasksTo, deleteTasks);
    }

    @GetMapping("/{boardId}/tasks")
    List<ApiDtos.TaskResponse> listTasks(@PathVariable UUID boardId,
                                         @RequestParam(required = false) UUID columnId,
                                         @RequestParam(required = false) UUID assigneeId,
                                         @RequestParam(required = false) String label,
                                         @RequestParam(required = false) java.time.LocalDate dueBefore,
                                         @RequestParam(required = false) java.time.LocalDate dueAfter,
                                         @RequestParam(defaultValue = "false") boolean archived) {
        return taskboardService.listTasks(boardId, columnId, assigneeId, label, dueBefore, dueAfter, archived);
    }

    @PostMapping("/{boardId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    ApiDtos.TaskResponse createTask(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.CreateTaskRequest request) {
        return taskboardService.createTask(boardId, request);
    }

    @GetMapping("/{boardId}/members")
    List<ApiDtos.MemberResponse> listMembers(@PathVariable UUID boardId) {
        return taskboardService.listMembers(boardId);
    }

    @PostMapping("/{boardId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    ApiDtos.MemberResponse addMember(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.AddMemberRequest request) {
        return taskboardService.addMember(boardId, request);
    }

    @PatchMapping("/{boardId}/members/{userId}")
    ApiDtos.MemberResponse updateMemberRole(@PathVariable UUID boardId,
                                            @PathVariable UUID userId,
                                            @Valid @RequestBody ApiDtos.UpdateMemberRoleRequest request) {
        return taskboardService.updateMemberRole(boardId, userId, request);
    }

    @PostMapping("/{boardId}/transfer-ownership")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void transferOwnership(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.TransferOwnershipRequest request) {
        taskboardService.transferOwnership(boardId, request);
    }

    @DeleteMapping("/{boardId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeMember(@PathVariable UUID boardId, @PathVariable UUID userId) {
        taskboardService.removeMember(boardId, userId);
    }

    @GetMapping("/{boardId}/labels")
    List<ApiDtos.LabelResponse> listLabels(@PathVariable UUID boardId) {
        return taskboardService.listLabels(boardId);
    }

    @PostMapping("/{boardId}/labels")
    @ResponseStatus(HttpStatus.CREATED)
    ApiDtos.LabelResponse createLabel(@PathVariable UUID boardId, @Valid @RequestBody ApiDtos.CreateLabelRequest request) {
        return taskboardService.createLabel(boardId, request);
    }

    @DeleteMapping("/{boardId}/labels/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLabel(@PathVariable UUID boardId, @PathVariable UUID labelId) {
        taskboardService.deleteLabel(boardId, labelId);
    }
}
