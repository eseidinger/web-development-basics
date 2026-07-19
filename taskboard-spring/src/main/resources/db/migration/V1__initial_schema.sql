create table app_user (
    id uuid primary key,
    external_id varchar(255) not null unique,
    display_name varchar(255) not null,
    email varchar(255) not null unique,
    created_at timestamp not null
);

create table board (
    id uuid primary key,
    name varchar(255) not null,
    owner_id uuid not null references app_user(id),
    archived_at timestamp,
    created_at timestamp not null
);

create table board_membership (
    board_id uuid not null references board(id) on delete cascade,
    user_id uuid not null references app_user(id),
    role varchar(32) not null,
    joined_at timestamp not null,
    primary key (board_id, user_id)
);

create table board_column (
    id uuid primary key,
    board_id uuid not null references board(id) on delete cascade,
    name varchar(255) not null,
    position integer not null,
    created_at timestamp not null,
    constraint uq_board_column_position unique (board_id, position)
);

create table task (
    id uuid primary key,
    board_id uuid not null references board(id) on delete cascade,
    column_id uuid not null references board_column(id),
    created_by uuid not null references app_user(id),
    assignee_id uuid references app_user(id),
    title varchar(255) not null,
    description text,
    due_date date,
    archived_at timestamp,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table comment (
    id uuid primary key,
    task_id uuid not null references task(id) on delete cascade,
    author_id uuid not null references app_user(id),
    body text not null,
    created_at timestamp not null,
    updated_at timestamp
);

create table label (
    id uuid primary key,
    board_id uuid not null references board(id) on delete cascade,
    name varchar(255) not null,
    color varchar(64),
    constraint uq_label_name unique (board_id, name)
);

create table task_label (
    task_id uuid not null references task(id) on delete cascade,
    label_id uuid not null references label(id) on delete cascade,
    primary key (task_id, label_id)
);

create index idx_app_user_external_id on app_user(external_id);
create index idx_board_owner_id on board(owner_id);
create index idx_board_membership_user_id on board_membership(user_id);
create index idx_board_column_board_id on board_column(board_id);
create index idx_task_board_id on task(board_id);
create index idx_task_column_id on task(column_id);
create index idx_task_assignee_id on task(assignee_id);
create index idx_task_archived_at on task(archived_at);
create index idx_comment_task_id on comment(task_id);
create index idx_label_board_id on label(board_id);
