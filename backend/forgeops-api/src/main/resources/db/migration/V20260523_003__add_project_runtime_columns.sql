ALTER TABLE managed_projects
    ADD COLUMN workflow_path VARCHAR(500) NOT NULL DEFAULT 'WORKFLOW.md' AFTER default_branch,
    ADD COLUMN workspace_root VARCHAR(500) AFTER workflow_path;
