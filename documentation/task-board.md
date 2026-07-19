# Task Board Application

## Overview

The Task Board is a collaborative work management tool that helps teams organize, track, and progress tasks through defined stages. Teams use it to maintain a shared, real-time view of who is working on what and where each piece of work stands.

The application is intended for teams of any size who need a lightweight, structured way to coordinate ongoing work without relying on ad hoc communication.

## User Roles

### Board Owner

The Board Owner creates and administers the board. Their responsibilities include:

- creating and naming the board
- defining and reordering columns to reflect the team's workflow
- inviting and removing members
- archiving or deleting the board when it is no longer active

A board must have exactly one owner. Ownership can be transferred to another member.

### Member

Members are the primary users of the board. They can:

- create, edit, and delete tasks on any board they belong to
- move tasks between columns
- assign tasks to themselves or other members
- add descriptions, due dates, and labels to tasks
- comment on tasks

### Viewer

Viewers have read-only access. They can see all boards, columns, tasks, and comments they have been granted access to, but cannot make any changes.

## Core Concepts

### Board

A board is the top-level workspace, typically scoped to a project or team. It contains all the columns and tasks relevant to that scope. Boards are independent of each other; a task belongs to exactly one board.

### Column

A column represents a stage in the team's workflow (for example: *To Do*, *In Progress*, *Review*, *Done*). Columns are ordered and named by the Board Owner. The sequence of columns defines the expected progression of work.

### Task

A task is a discrete unit of work. Each task has:

- a **title** — a short description of what needs to be done
- a **description** — optional detail, context, or acceptance criteria
- an **assignee** — the member responsible for completing the task (optional)
- a **due date** — the target completion date (optional)
- one or more **labels** — free-form tags for grouping or filtering (optional)

A task always belongs to exactly one column within its board.

## Features

### Board Management

- Create a new board with a name and an initial set of columns
- Rename the board or individual columns at any time
- Reorder columns by dragging them to a new position
- Invite members by email or username
- Change a member's role between Member and Viewer
- Archive the board to preserve its history without keeping it active

### Task Management

- Add a task to any column on a board you are a member of
- Edit any field of a task: title, description, assignee, due date, or labels
- Move a task to a different column to reflect a change in status
- Delete a task (this action is permanent and requires confirmation)
- Archive a task to hide it from the board without deleting it

### Assignment and Collaboration

- Assign a task to any board member
- A task can have at most one assignee at a time
- Members can add comments to a task to communicate progress or raise questions
- Mentions in comments notify the mentioned member

### Filtering and Search

- Filter tasks on a board by assignee, label, or due date range
- Search for tasks by title keyword across all boards a user has access to

### Progress Tracking

- View a summary count of tasks per column to assess workload distribution
- See all tasks assigned to you across all boards in a personal task view

## Key Workflows

### Setting Up a New Board

1. A Board Owner creates a new board and gives it a name.
2. They define the columns that reflect their team's workflow stages, in order.
3. They invite team members by assigning each a role (Member or Viewer).
4. The board is ready for tasks to be added.

### Adding and Assigning a Task

1. A Member creates a task in the appropriate starting column (typically the first column).
2. They add a title, and optionally a description, due date, and labels.
3. They assign the task to the member who will work on it.
4. The assigned member is notified of the new assignment.

### Progressing a Task Through Stages

1. The assigned member begins work and moves the task to the next column (for example, from *To Do* to *In Progress*).
2. As the work progresses, the member continues moving the task through subsequent columns.
3. Once work is complete, the task is moved to the final column (for example, *Done*).
4. The Board Owner or Member can then archive the completed task.

### Archiving Completed Work

1. A Member or Board Owner selects one or more completed tasks.
2. They choose to archive the selected tasks.
3. Archived tasks are removed from the active board view but remain accessible in the archive for reference.
4. Archived tasks are read-only and cannot be moved or edited.

## Business Rules

- A task must always belong to exactly one board and exactly one column.
- A board must have at least one column before tasks can be added.
- Deleting a column that still contains tasks requires the user to either move those tasks to another column or confirm that they will be deleted along with the column.
- Only Board Owners may add or remove members, rename columns, or delete the board.
- Only Members and Board Owners may create, edit, move, or delete tasks.
- Viewers may not modify any content on a board.
- Archived tasks are read-only. To make changes to an archived task, it must first be restored to the board.
- A task can have at most one assignee at a time.
- Deleting a task is permanent and requires explicit confirmation. It cannot be undone.
- Board Owners cannot remove themselves from a board unless ownership is first transferred to another member.
