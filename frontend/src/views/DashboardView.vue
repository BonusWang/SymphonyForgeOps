<script setup lang="ts">
import { onMounted } from 'vue'
import {
  CircleCheck,
  Clock,
  Connection,
  DataBoard,
  Files,
  Management,
  Operation,
  Refresh,
  Share,
  Tickets,
  Timer,
  VideoPlay
} from '@element-plus/icons-vue'
import { useDashboardStore } from '../stores/dashboard'

const dashboard = useDashboardStore()

onMounted(() => {
  dashboard.load()
})
</script>

<template>
  <main class="layout-shell">
    <aside class="sidebar">
      <div class="sidebar-logo">
        <div class="logo-icon">S</div>
        <span class="brand-text">SymphonyForgeOps</span>
      </div>
      <nav class="sidebar-menu">
        <div class="menu-group-label">工作台</div>
        <a class="nav-item active"><el-icon><DataBoard /></el-icon> 控制台</a>
        <a class="nav-item"><el-icon><Share /></el-icon> 项目注册</a>
        <a class="nav-item"><el-icon><Tickets /></el-icon> 工作单</a>
        <a class="nav-item"><el-icon><VideoPlay /></el-icon> Agent 运行</a>
        <div class="menu-group-label">治理</div>
        <a class="nav-item"><el-icon><Management /></el-icon> 代码审核</a>
        <a class="nav-item"><el-icon><Files /></el-icon> 工作区</a>
        <a class="nav-item"><el-icon><Operation /></el-icon> 工作流契约</a>
        <a class="nav-item"><el-icon><Connection /></el-icon> Worker</a>
      </nav>
      <div class="sidebar-footer">v1.0 · Java 17</div>
    </aside>

    <section class="main">
      <header class="header">
        <span class="header-title">个人研发控制台</span>
        <el-button type="primary" :icon="Refresh" @click="dashboard.load">刷新</el-button>
      </header>

      <div class="content">
        <div class="welcome-bar">
          <div>
            <p class="eyebrow">Mission Control + Symphony Spec</p>
            <h1>管理项目，而不是盯着每个 Agent 回合</h1>
            <p class="welcome-desc">
              当前工作区根目录：{{ dashboard.snapshot?.workspaceRoot || 'D:/ForgeOps/workspaces' }}
            </p>
          </div>
          <el-tag v-if="dashboard.snapshot" type="success" effect="dark">
            {{ dashboard.snapshot.orchestratorStatus }}
          </el-tag>
        </div>

        <el-alert
          v-if="dashboard.error"
          :title="dashboard.error"
          type="error"
          show-icon
          class="alert"
        />

        <div v-if="dashboard.snapshot" class="stats-grid">
          <section class="stat-card">
            <div class="stat-icon success"><el-icon><Share /></el-icon></div>
            <div>
              <span>托管项目</span>
              <strong>{{ dashboard.snapshot.projectCount }}</strong>
            </div>
          </section>
          <section class="stat-card">
            <div class="stat-icon info"><el-icon><Tickets /></el-icon></div>
            <div>
              <span>开放工作单</span>
              <strong>{{ dashboard.snapshot.openWorkOrderCount }}</strong>
            </div>
          </section>
          <section class="stat-card">
            <div class="stat-icon success"><el-icon><VideoPlay /></el-icon></div>
            <div>
              <span>运行中 Agent</span>
              <strong>{{ dashboard.snapshot.runningAgentCount }}</strong>
            </div>
          </section>
          <section class="stat-card">
            <div class="stat-icon warning"><el-icon><Timer /></el-icon></div>
            <div>
              <span>重试队列</span>
              <strong>{{ dashboard.snapshot.retryQueueCount }}</strong>
            </div>
          </section>
          <section class="stat-card">
            <div class="stat-icon danger"><el-icon><Management /></el-icon></div>
            <div>
              <span>待审核</span>
              <strong>{{ dashboard.snapshot.pendingReviewCount }}</strong>
            </div>
          </section>
          <section class="stat-card">
            <div class="stat-icon info"><el-icon><Connection /></el-icon></div>
            <div>
              <span>在线 Worker</span>
              <strong>{{ dashboard.workers.filter((worker) => worker.status === 'online').length }}</strong>
            </div>
          </section>
        </div>

        <div v-if="dashboard.snapshot" class="workspace-grid">
          <section class="card">
            <div class="panel-header">
              <h2>托管项目</h2>
              <el-tag effect="dark">WORKFLOW.md</el-tag>
            </div>
            <el-table :data="dashboard.snapshot.projects" class="dark-table" size="large">
              <el-table-column prop="name" label="项目" min-width="140" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag size="small" effect="dark">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="workflowPath" label="契约" width="140" />
              <el-table-column prop="workspaceRoot" label="隔离工作区" min-width="260" />
              <el-table-column prop="stack" label="技术栈" min-width="260" />
            </el-table>
          </section>

          <section class="card">
            <div class="panel-header">
              <h2>工作单</h2>
              <el-tag type="info" effect="dark">Contract First</el-tag>
            </div>
            <el-table :data="dashboard.snapshot.workOrders" class="dark-table" size="large">
              <el-table-column prop="title" label="任务" min-width="300" />
              <el-table-column prop="projectId" label="项目" width="170" />
              <el-table-column prop="status" label="状态" width="140" />
              <el-table-column prop="implementationAgent" label="Agent" width="120" />
              <el-table-column prop="testCommand" label="验证命令" min-width="220" />
            </el-table>
          </section>

          <div class="two-column-grid">
            <section class="card">
              <div class="panel-header">
                <h2>Worker 运行层</h2>
                <el-icon><Connection /></el-icon>
              </div>
              <el-table :data="dashboard.workers" class="dark-table" size="large">
                <el-table-column prop="displayName" label="Worker" min-width="180" />
                <el-table-column prop="protocol" label="协议" width="90" />
                <el-table-column prop="status" label="状态" width="110" />
                <el-table-column prop="availableCapacity" label="可用" width="90" />
                <el-table-column prop="currentRuns" label="运行" width="90" />
                <el-table-column prop="host" label="主机" min-width="160" />
              </el-table>
            </section>

            <section class="card">
              <div class="panel-header">
                <h2>Agent 运行</h2>
                <el-icon><Clock /></el-icon>
              </div>
              <el-table :data="dashboard.snapshot.agentRuns" class="dark-table" size="large">
                <el-table-column prop="agentName" label="Agent" width="110" />
                <el-table-column prop="status" label="状态" width="120" />
                <el-table-column prop="summary" label="摘要" min-width="280" />
              </el-table>
            </section>

            <section class="card">
              <div class="panel-header">
                <h2>审核发现</h2>
                <el-icon><CircleCheck /></el-icon>
              </div>
              <el-table :data="dashboard.reviews?.findings || []" class="dark-table" size="large">
                <el-table-column prop="source" label="来源" width="110" />
                <el-table-column prop="severity" label="风险" width="110" />
                <el-table-column prop="status" label="状态" width="120" />
                <el-table-column prop="summary" label="结论" min-width="260" />
              </el-table>
            </section>

            <section class="card">
              <div class="panel-header">
                <h2>GitHub 链接</h2>
                <el-icon><Share /></el-icon>
              </div>
              <el-table :data="dashboard.githubLinks" class="dark-table" size="large">
                <el-table-column prop="workOrderId" label="工作单" width="170" />
                <el-table-column prop="checkStatus" label="检查" width="110" />
                <el-table-column prop="issueUrl" label="Issue" min-width="260" />
                <el-table-column prop="prUrl" label="PR" min-width="260" />
              </el-table>
            </section>

            <section class="card">
              <div class="panel-header">
                <h2>人工决策</h2>
                <el-icon><Management /></el-icon>
              </div>
              <el-table :data="dashboard.reviews?.humanDecisions || []" class="dark-table" size="large">
                <el-table-column prop="workOrderId" label="工作单" width="170" />
                <el-table-column prop="decision" label="决策" width="120" />
                <el-table-column prop="decider" label="决策人" width="130" />
                <el-table-column prop="reason" label="原因" min-width="260" />
              </el-table>
            </section>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>
