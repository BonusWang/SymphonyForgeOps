CREATE TABLE workflow_contracts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    workflow_path VARCHAR(500) NOT NULL,
    tracker_kind VARCHAR(80) NOT NULL,
    active_states JSON,
    terminal_states JSON,
    polling_interval_ms INT NOT NULL DEFAULT 30000,
    max_concurrent_agents INT NOT NULL DEFAULT 1,
    max_turns INT NOT NULL DEFAULT 20,
    workspace_root VARCHAR(500) NOT NULL,
    prompt_body MEDIUMTEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_contracts_project
        FOREIGN KEY (project_id) REFERENCES managed_projects (id),
    CONSTRAINT uk_workflow_contracts_project_path
        UNIQUE (project_id, workflow_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE isolated_workspaces (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    workspace_key VARCHAR(160) NOT NULL,
    workspace_path VARCHAR(500) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'created',
    created_now BOOLEAN NOT NULL DEFAULT TRUE,
    last_checked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_isolated_workspaces_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders (id),
    CONSTRAINT uk_isolated_workspaces_key
        UNIQUE (workspace_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE run_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_run_id BIGINT NOT NULL,
    attempt_no INT NOT NULL DEFAULT 1,
    workspace_id BIGINT,
    status VARCHAR(40) NOT NULL,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_run_attempts_agent_run
        FOREIGN KEY (agent_run_id) REFERENCES agent_runs (id),
    CONSTRAINT fk_run_attempts_workspace
        FOREIGN KEY (workspace_id) REFERENCES isolated_workspaces (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE retry_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_id BIGINT NOT NULL,
    attempt_no INT NOT NULL,
    due_at TIMESTAMP NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'scheduled',
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_retry_queue_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE run_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agent_run_id BIGINT NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    message TEXT,
    payload JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_run_events_agent_run
        FOREIGN KEY (agent_run_id) REFERENCES agent_runs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

