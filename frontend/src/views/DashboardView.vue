<script setup lang="ts">
import { onMounted } from 'vue'
import { CircleCheck, GitBranch, ListChecks, Play, ShieldCheck } from '@element-plus/icons-vue'
import { useDashboardStore } from '../stores/dashboard'

const dashboard = useDashboardStore()

onMounted(() => {
  dashboard.load()
})
</script>

<template>
  <main class="page-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">S</span>
        <div>
          <h1>SymphonyForgeOps</h1>
          <p>Personal development control console</p>
        </div>
      </div>
      <nav class="nav-list">
        <a class="nav-item active"><el-icon><CircleCheck /></el-icon> Dashboard</a>
        <a class="nav-item"><el-icon><GitBranch /></el-icon> Projects</a>
        <a class="nav-item"><el-icon><ListChecks /></el-icon> Work Orders</a>
        <a class="nav-item"><el-icon><Play /></el-icon> Agent Runs</a>
        <a class="nav-item"><el-icon><ShieldCheck /></el-icon> Reviews</a>
      </nav>
    </aside>

    <section class="content">
      <header class="topbar">
        <div>
          <p class="eyebrow">Mission Control</p>
          <h2>Independent agent operations for your projects</h2>
        </div>
        <el-button type="primary" @click="dashboard.load">Refresh</el-button>
      </header>

      <el-alert
        v-if="dashboard.error"
        :title="dashboard.error"
        type="error"
        show-icon
        class="alert"
      />

      <div v-if="dashboard.snapshot" class="metric-grid">
        <section class="metric-panel">
          <span>Projects</span>
          <strong>{{ dashboard.snapshot.projectCount }}</strong>
        </section>
        <section class="metric-panel">
          <span>Open Work Orders</span>
          <strong>{{ dashboard.snapshot.openWorkOrderCount }}</strong>
        </section>
        <section class="metric-panel">
          <span>Running Agents</span>
          <strong>{{ dashboard.snapshot.runningAgentCount }}</strong>
        </section>
        <section class="metric-panel">
          <span>Pending Reviews</span>
          <strong>{{ dashboard.snapshot.pendingReviewCount }}</strong>
        </section>
      </div>

      <div v-if="dashboard.snapshot" class="workspace-grid">
        <section class="panel">
          <div class="panel-header">
            <h3>Managed Projects</h3>
          </div>
          <el-table :data="dashboard.snapshot.projects" size="large">
            <el-table-column prop="name" label="Project" min-width="140" />
            <el-table-column prop="status" label="Status" width="120" />
            <el-table-column prop="stack" label="Stack" min-width="260" />
            <el-table-column prop="repoUrl" label="Repository" min-width="260" />
          </el-table>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h3>Work Orders</h3>
          </div>
          <el-table :data="dashboard.snapshot.workOrders" size="large">
            <el-table-column prop="title" label="Task" min-width="260" />
            <el-table-column prop="projectId" label="Project" width="160" />
            <el-table-column prop="status" label="Status" width="130" />
            <el-table-column prop="implementationAgent" label="Agent" width="120" />
          </el-table>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h3>Agent Runs</h3>
          </div>
          <el-table :data="dashboard.snapshot.agentRuns" size="large">
            <el-table-column prop="agentName" label="Agent" width="120" />
            <el-table-column prop="status" label="Status" width="120" />
            <el-table-column prop="summary" label="Summary" min-width="320" />
          </el-table>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h3>Review Queue</h3>
          </div>
          <el-table :data="dashboard.snapshot.reviewItems" size="large">
            <el-table-column prop="type" label="Type" width="130" />
            <el-table-column prop="riskLevel" label="Risk" width="120" />
            <el-table-column prop="status" label="Status" width="130" />
            <el-table-column prop="summary" label="Summary" min-width="320" />
          </el-table>
        </section>
      </div>
    </section>
  </main>
</template>

