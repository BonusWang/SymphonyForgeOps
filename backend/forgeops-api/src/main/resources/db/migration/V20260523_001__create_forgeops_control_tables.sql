CREATE TABLE managed_projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_key VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    repo_url VARCHAR(500) NOT NULL,
    local_path VARCHAR(500),
    default_branch VARCHAR(120) NOT NULL DEFAULT 'main',
    stack_summary VARCHAR(500),
    status VARCHAR(40) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE command_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    command_key VARCHAR(80) NOT NULL,
    command_type VARCHAR(40) NOT NULL,
    command_text TEXT NOT NULL,
    working_directory VARCHAR(500),
    is_destructive BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_command_templates_project
        FOREIGN KEY (project_id) REFERENCES managed_projects (id),
    CONSTRAINT uk_command_templates_project_key
        UNIQUE (project_id, command_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE work_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_order_key VARCHAR(120) NOT NULL UNIQUE,
    project_id BIGINT NOT NULL,
    title VARCHAR(240) NOT NULL,
    status VARCHAR(40) NOT NULL,
    requirement_text TEXT,
    implementation_agent VARCHAR(120),
    test_command VARCHAR(1000),
    review_command VARCHAR(1000),
    human_approval_required BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_orders_project
        FOREIGN KEY (project_id) REFERENCES managed_projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE agent_runs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_key VARCHAR(120) NOT NULL UNIQUE,
    work_order_id BIGINT NOT NULL,
    agent_name VARCHAR(120) NOT NULL,
    status VARCHAR(40) NOT NULL,
    summary TEXT,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_runs_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE review_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_key VARCHAR(120) NOT NULL UNIQUE,
    work_order_id BIGINT NOT NULL,
    review_type VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    risk_level VARCHAR(40) NOT NULL,
    summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_items_work_order
        FOREIGN KEY (work_order_id) REFERENCES work_orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

