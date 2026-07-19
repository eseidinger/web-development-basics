# Task Board: API Reference

## Conventions

### Base Path

All endpoints are prefixed with a versioned base path:

```
/api/v1
```

### Authentication

Every endpoint requires a valid access token issued by the OIDC provider. The token must be included in the `Authorization` header on every request:

```
Authorization: Bearer <access_token>
```

Requests without a valid token receive `401 Unauthorized`. Requests with a valid token that lacks the required permission receive `403 Forbidden`.

### Content Type

All request and response bodies use `application/json`.

### Error Response Format

All error responses follow a consistent structure:

```json
{
  "status": 422,
  "code": "VALIDATION_ERROR",
  "message": "Human-readable description of the error.",
  "details": [
    { "field": "title", "issue": "must not be blank" }
  ]
}
```

The `details` array is omitted when there are no field-level errors.

### Common Status Codes

| Code | Meaning |
|---|---|
| `200 OK` | Request succeeded; response body contains the result |
| `201 Created` | Resource created; response body contains the new resource |
| `204 No Content` | Request succeeded; no response body |
| `400 Bad Request` | Request is malformed or missing required fields |
| `401 Unauthorized` | No valid access token provided |
| `403 Forbidden` | Authenticated user lacks permission for this operation |
| `404 Not Found` | The requested resource does not exist or is not visible to the caller |
| `409 Conflict` | The operation conflicts with the current state of a resource |
| `422 Unprocessable Entity` | Request is well-formed but violates a business rule |

---

## Resources

### Boards

#### List boards

Returns all boards the authenticated user is a member of.

```
GET /api/v1/boards
```

**Response `200`**

```json
[
  {
    "id": "string",
    "name": "string",
    "role": "owner | member | viewer",
    "archivedAt": "ISO 8601 timestamp | null",
    "createdAt": "ISO 8601 timestamp"
  }
]
```

---

#### Create a board

```
POST /api/v1/boards
```

**Required role:** authenticated user (becomes the owner)

**Request body**

```json
{
  "name": "string"
}
```

**Response `201`** — returns the created board.

---

#### Get a board

```
GET /api/v1/boards/{boardId}
```

**Response `200`**

```json
{
  "id": "string",
  "name": "string",
  "role": "owner | member | viewer",
  "archivedAt": "ISO 8601 timestamp | null",
  "createdAt": "ISO 8601 timestamp"
}
```

---

#### Update a board

Rename the board.

```
PATCH /api/v1/boards/{boardId}
```

**Required role:** owner

**Request body**

```json
{
  "name": "string"
}
```

**Response `200`** — returns the updated board.

---

#### Archive a board

```
POST /api/v1/boards/{boardId}/archive
```

**Required role:** owner

**Response `204`**

---

#### Delete a board

Permanently deletes the board and all its columns and tasks. This action is irreversible.

```
DELETE /api/v1/boards/{boardId}
```

**Required role:** owner

**Response `204`**

---

### Columns

#### List columns

Returns all columns for a board in display order.

```
GET /api/v1/boards/{boardId}/columns
```

**Response `200`**

```json
[
  {
    "id": "string",
    "name": "string",
    "position": "integer",
    "taskCount": "integer"
  }
]
```

---

#### Create a column

```
POST /api/v1/boards/{boardId}/columns
```

**Required role:** owner

**Request body**

```json
{
  "name": "string"
}
```

The new column is appended at the end of the current column order.

**Response `201`** — returns the created column.

---

#### Update a column

Rename the column.

```
PATCH /api/v1/boards/{boardId}/columns/{columnId}
```

**Required role:** owner

**Request body**

```json
{
  "name": "string"
}
```

**Response `200`** — returns the updated column.

---

#### Reorder columns

Sets the display order of all columns on a board. The request must include every column ID for the board.

```
PUT /api/v1/boards/{boardId}/columns/order
```

**Required role:** owner

**Request body**

```json
{
  "columnIds": ["string"]
}
```

**Response `204`**

---

#### Delete a column

If the column contains tasks, the request must specify what to do with them.

```
DELETE /api/v1/boards/{boardId}/columns/{columnId}
```

**Required role:** owner

**Query parameters**

| Parameter | Required | Description |
|---|---|---|
| `moveTasksTo` | conditional | ID of the column to move existing tasks into. Required if the column has tasks and `deleteTasks` is not set. |
| `deleteTasks` | conditional | `true` to permanently delete all tasks in the column. Required if `moveTasksTo` is not set and the column has tasks. |

**Response `204`**

**Errors**

- `409 Conflict` — column has tasks and neither `moveTasksTo` nor `deleteTasks` was provided

---

### Tasks

#### List tasks for a board

Returns all active (non-archived) tasks on a board.

```
GET /api/v1/boards/{boardId}/tasks
```

**Query parameters**

| Parameter | Description |
|---|---|
| `columnId` | Filter by column |
| `assigneeId` | Filter by assignee |
| `label` | Filter by label name |
| `dueBefore` | Filter tasks with due date before this date (ISO 8601) |
| `dueAfter` | Filter tasks with due date after this date (ISO 8601) |
| `archived` | `true` to return only archived tasks; defaults to `false` |

**Response `200`**

```json
[
  {
    "id": "string",
    "boardId": "string",
    "columnId": "string",
    "title": "string",
    "description": "string | null",
    "assignee": { "id": "string", "displayName": "string" } ,
    "dueDate": "ISO 8601 date | null",
    "labels": ["string"],
    "archivedAt": "ISO 8601 timestamp | null",
    "createdAt": "ISO 8601 timestamp",
    "createdBy": { "id": "string", "displayName": "string" }
  }
]
```

---

#### Create a task

```
POST /api/v1/boards/{boardId}/tasks
```

**Required role:** member or owner

**Request body**

```json
{
  "columnId": "string",
  "title": "string",
  "description": "string | null",
  "assigneeId": "string | null",
  "dueDate": "ISO 8601 date | null",
  "labels": ["string"]
}
```

**Response `201`** — returns the created task.

---

#### Get a task

```
GET /api/v1/tasks/{taskId}
```

**Response `200`** — returns the full task object (same structure as list response).

---

#### Update a task

Updates any combination of editable fields. Only the fields included in the request body are changed.

```
PATCH /api/v1/tasks/{taskId}
```

**Required role:** member or owner

**Request body** — all fields optional

```json
{
  "title": "string",
  "description": "string | null",
  "assigneeId": "string | null",
  "dueDate": "ISO 8601 date | null",
  "labels": ["string"]
}
```

**Response `200`** — returns the updated task.

---

#### Move a task

Moves a task to a different column.

```
POST /api/v1/tasks/{taskId}/move
```

**Required role:** member or owner

**Request body**

```json
{
  "columnId": "string"
}
```

**Errors**

- `422` — target column does not belong to the same board as the task

**Response `200`** — returns the updated task.

---

#### Archive a task

```
POST /api/v1/tasks/{taskId}/archive
```

**Required role:** member or owner

**Response `204`**

---

#### Restore a task

Moves an archived task back to the board. The caller must specify which column to restore it into.

```
POST /api/v1/tasks/{taskId}/restore
```

**Required role:** member or owner

**Request body**

```json
{
  "columnId": "string"
}
```

**Response `200`** — returns the restored task.

---

#### Delete a task

Permanently deletes a task and its comments. This action is irreversible.

```
DELETE /api/v1/tasks/{taskId}
```

**Required role:** member or owner

**Response `204`**

---

### Comments

#### List comments on a task

```
GET /api/v1/tasks/{taskId}/comments
```

**Response `200`**

```json
[
  {
    "id": "string",
    "taskId": "string",
    "body": "string",
    "author": { "id": "string", "displayName": "string" },
    "createdAt": "ISO 8601 timestamp",
    "updatedAt": "ISO 8601 timestamp | null"
  }
]
```

---

#### Add a comment

```
POST /api/v1/tasks/{taskId}/comments
```

**Required role:** member or owner

**Request body**

```json
{
  "body": "string"
}
```

**Response `201`** — returns the created comment.

---

#### Update a comment

A user may only edit their own comments.

```
PATCH /api/v1/tasks/{taskId}/comments/{commentId}
```

**Request body**

```json
{
  "body": "string"
}
```

**Response `200`** — returns the updated comment.

---

#### Delete a comment

A user may delete their own comments. A Board Owner may delete any comment on their board.

```
DELETE /api/v1/tasks/{taskId}/comments/{commentId}
```

**Response `204`**

---

### Board Members

#### List members

```
GET /api/v1/boards/{boardId}/members
```

**Response `200`**

```json
[
  {
    "userId": "string",
    "displayName": "string",
    "email": "string",
    "role": "owner | member | viewer",
    "joinedAt": "ISO 8601 timestamp"
  }
]
```

---

#### Add a member

```
POST /api/v1/boards/{boardId}/members
```

**Required role:** owner

**Request body**

```json
{
  "userId": "string",
  "role": "member | viewer"
}
```

**Errors**

- `409 Conflict` — user is already a member of this board

**Response `201`** — returns the membership record.

---

#### Update a member's role

```
PATCH /api/v1/boards/{boardId}/members/{userId}
```

**Required role:** owner

**Request body**

```json
{
  "role": "member | viewer"
}
```

**Errors**

- `422` — cannot change the role of the board owner without transferring ownership

**Response `200`** — returns the updated membership record.

---

#### Transfer ownership

```
POST /api/v1/boards/{boardId}/transfer-ownership
```

**Required role:** owner

**Request body**

```json
{
  "userId": "string"
}
```

The current owner becomes a member after the transfer.

**Response `204`**

---

#### Remove a member

```
DELETE /api/v1/boards/{boardId}/members/{userId}
```

**Required role:** owner

**Errors**

- `422` — cannot remove the board owner; transfer ownership first

**Response `204`**

---

### Labels

#### List labels for a board

```
GET /api/v1/boards/{boardId}/labels
```

**Response `200`**

```json
[
  {
    "id": "string",
    "name": "string",
    "color": "string | null"
  }
]
```

---

#### Create a label

```
POST /api/v1/boards/{boardId}/labels
```

**Required role:** member or owner

**Request body**

```json
{
  "name": "string",
  "color": "string | null"
}
```

**Response `201`** — returns the created label.

---

#### Delete a label

Removes the label from the board and from all tasks that carry it.

```
DELETE /api/v1/boards/{boardId}/labels/{labelId}
```

**Required role:** owner

**Response `204`**

---

### Personal View

#### List my tasks

Returns all active tasks assigned to the authenticated user across all boards they have access to.

```
GET /api/v1/me/tasks
```

**Query parameters**

| Parameter | Description |
|---|---|
| `dueBefore` | Filter tasks due before this date (ISO 8601) |
| `dueAfter` | Filter tasks due after this date (ISO 8601) |

**Response `200`** — same task object structure as the board task list, including `boardId` for context.
