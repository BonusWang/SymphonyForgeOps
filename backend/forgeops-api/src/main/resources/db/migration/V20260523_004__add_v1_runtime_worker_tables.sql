CREATE TABLE command_runs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    work_order_id BIGINT NOT NULL,
    command_text TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    stdout_text MEDIUMTEXT,
    stderr_text MEDIUMTEXT,
    exit_code INT,
    destructive BOOLEAN NOT NULL DEFAULT FALSE,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_command_runs_project
        FOREIGN KEY (project_id) REFERENCES managed_projects (id),
    CONSTRAINT fk_command_runs_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worker_hosts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_key VARCHAR(120) NOT NULL UNIQUE,
    display_name VARCHAR(160) NOT NULL,
    protocol VARCHAR(40) NOT NULL,
    host_name VARCHAR(240) NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    status VARCHAR(40) NOT NULL DEFAULT 'offline',
    current_runs INT NOT NULL DEFAULT 0,
    available_capacity INT NOT NULL DEFAULT 0,
    last_heartbeat_at TIMESTAMP NULL,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worker_heartbeats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    available_capacity INT NOT NULL,
    current_runs INT NOT NULL,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_worker_heartbeats_worker
        FOREIGN KEY (worker_id) REFERENCES worker_hosts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE worker_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    work_order_id BIGINT NOT NULL,
    agent_run_id BIGINT,
    status VARCHAR(40) NOT NULL,
    exit_code INT,
    stdout_text MEDIUMTEXT,
    stderr_text MEDIUMTEXT,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP NULL,
    CONSTRAINT fk_worker_assignments_worker
        FOREIGN KEY (worker_id) REFERENCES worker_hosts (id),
    CONSTRAINT fk_worker_assignments_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders (id),
    CONSTRAINT fk_worker_assignments_agent_run
        FOREIGN KEY (agent_run_id) REFERENCES agent_runs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
