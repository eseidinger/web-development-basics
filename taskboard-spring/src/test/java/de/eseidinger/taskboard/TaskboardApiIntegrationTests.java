package de.eseidinger.taskboard;

import de.eseidinger.taskboard.domain.AppUser;
import de.eseidinger.taskboard.repository.AppUserRepository;
import de.eseidinger.taskboard.repository.BoardMembershipRepository;
import de.eseidinger.taskboard.repository.BoardRepository;
import de.eseidinger.taskboard.repository.CommentRepository;
import de.eseidinger.taskboard.repository.LabelRepository;
import de.eseidinger.taskboard.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskboardApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardMembershipRepository membershipRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    void cleanDatabase() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        membershipRepository.deleteAll();
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void boardAndTaskWorkflowWorksEndToEnd() throws Exception {
        AppUser bob = userRepository.save(new AppUser(
                UUID.randomUUID(),
                "bob-subject",
                "Bob Member",
                "bob@taskboard.local",
                Instant.now()));

        UUID boardId = extractUuid(mockMvc.perform(post("/api/v1/boards")
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Platform Board"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("owner"))
                .andReturn(), "id");

        UUID columnId = extractUuid(mockMvc.perform(post("/api/v1/boards/{boardId}/columns", boardId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"To Do"}
                                """))
                .andExpect(status().isCreated())
                .andReturn(), "id");

        mockMvc.perform(post("/api/v1/boards/{boardId}/members", boardId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"userId":"%s","role":"member"}
                                                                """.formatted(bob.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("member"));

        mockMvc.perform(post("/api/v1/boards/{boardId}/labels", boardId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"backend","color":"blue"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("backend"));

        UUID taskId = extractUuid(mockMvc.perform(post("/api/v1/boards/{boardId}/tasks", boardId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "columnId":"%s",
                                                                                                                                        "title":"Build API",
                                                                                                                                        "description":"Implement Spring endpoints",
                                                                                                                                        "assigneeId":"%s",
                                                                                                                                        "labels":["backend"]
                                                                                                                                }
                                                                                                                                """.formatted(columnId, bob.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Build API"))
                .andExpect(jsonPath("$.labels[0]").value("backend"))
                .andReturn(), "id");

        mockMvc.perform(get("/api/v1/boards/{boardId}/tasks", boardId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId.toString()));

        UUID commentId = extractUuid(mockMvc.perform(post("/api/v1/tasks/{taskId}/comments", taskId)
                        .with(taskUser("bob-subject", "bob@taskboard.local", "Bob Member"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"body":"Started implementation"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author.displayName").value("Bob Member"))
                .andReturn(), "id");

        mockMvc.perform(patch("/api/v1/tasks/{taskId}", taskId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Build full API"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Build full API"));

        mockMvc.perform(get("/api/v1/tasks/{taskId}/comments", taskId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId.toString()));

        mockMvc.perform(get("/api/v1/me/tasks")
                        .with(taskUser("bob-subject", "bob@taskboard.local", "Bob Member")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId.toString()));
    }

    @Test
    void nonOwnerCannotAddMembers() throws Exception {
        UUID boardId = extractUuid(mockMvc.perform(post("/api/v1/boards")
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Secure Board"}
                                """))
                .andExpect(status().isCreated())
                .andReturn(), "id");

        AppUser bob = userRepository.save(new AppUser(
                UUID.randomUUID(),
                "bob-subject",
                "Bob Member",
                "bob@taskboard.local",
                Instant.now()));
        AppUser charlie = userRepository.save(new AppUser(
                UUID.randomUUID(),
                "charlie-subject",
                "Charlie Viewer",
                "charlie@taskboard.local",
                Instant.now()));

        mockMvc.perform(post("/api/v1/boards/{boardId}/members", boardId)
                        .with(taskUser("alice-subject", "alice@taskboard.local", "Alice Admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"userId":"%s","role":"member"}
                                                                """.formatted(bob.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/boards/{boardId}/members", boardId)
                        .with(taskUser("bob-subject", "bob@taskboard.local", "Bob Member"))
                        .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"userId":"%s","role":"viewer"}
                                                                """.formatted(charlie.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private UUID extractUuid(MvcResult result, String field) throws Exception {
                String response = result.getResponse().getContentAsString();
                String token = "\"" + field + "\":\"";
                int start = response.indexOf(token);
                if (start < 0) {
                        throw new IllegalStateException("Field not found: " + field + " in response " + response);
                }
                int valueStart = start + token.length();
                int valueEnd = response.indexOf('"', valueStart);
                return UUID.fromString(response.substring(valueStart, valueEnd));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor taskUser(String subject, String email, String name) {
        return jwt().jwt(jwt -> jwt.subject(subject).claim("email", email).claim("name", name));
    }
}