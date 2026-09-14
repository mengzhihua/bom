<template>
  <div class="page">
    <div class="page-title">
      <h2>工作台</h2>
      <el-button @click="load">
        刷新
      </el-button>
    </div>

    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :span="4">
        <el-card shadow="never" class="stat">
          <div class="label">
            {{ card.label }}
          </div>
          <div class="value">
            {{ card.value }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            零件生命周期分布
          </template>
          <div
            v-for="item in lifecycle"
            :key="item.name"
            class="life-row"
          >
            <span>{{ item.name }}</span>
            <el-progress
              :percentage="item.percent"
              :format="() => `${item.count} 个`"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            车型项目 EBOM / MBOM
          </template>
          <el-table :data="data.vehicleModels || []" size="small">
            <el-table-column prop="modelCode" label="车型" />
            <el-table-column prop="modelName" label="名称" />
            <el-table-column label="EBOM">
              <template #default="{ row }">
                <el-tag :type="statusType(row.ebomStatus)">
                  {{ row.ebomStatus || '暂无' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="MBOM">
              <template #default="{ row }">
                <el-tag :type="statusType(row.mbomStatus)">
                  {{ row.mbomStatus || '暂无' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            最近集成日志
          </template>
          <el-table :data="data.recentLogs || []" size="small">
            <el-table-column prop="createdAt" label="时间" />
            <el-table-column prop="system" label="系统" />
            <el-table-column prop="action" label="动作" />
            <el-table-column prop="status" label="状态" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            最近操作日志
          </template>
          <el-table :data="data.recentOpLogs || []" size="small">
            <el-table-column prop="createdAt" label="时间" />
            <el-table-column prop="username" label="用户" />
            <el-table-column prop="path" label="路径" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { dashboard } from '../api'

const data = ref({})

const cards = computed(() => [
  { label: '零件数', value: data.value.partCount || 0 },
  {
    label: 'RELEASED 零件',
    value: data.value.partsByLifecycle?.RELEASED || 0
  },
  { label: 'BOM 数', value: data.value.bomCount || 0 },
  { label: '待审 ECR', value: data.value.pendingEcr || 0 },
  { label: '待实施 ECN', value: data.value.pendingEcn || 0 },
  {
    label: '本月发布零件',
    value: data.value.releasedPartsThisMonth || 0
  }
])

const lifecycle = computed(() => {
  const values = data.value.partsByLifecycle || {}
  const total = Object.values(values).reduce(
    (sum, count) => sum + count,
    0
  ) || 1
  return Object.entries(values).map(([name, count]) => ({
    name,
    count,
    percent: Math.round((count * 100) / total)
  }))
})

function statusType(status) {
  return status === 'RELEASED' ? 'success' : 'info'
}

async function load() {
  data.value = await dashboard.summary()
}

onMounted(load)
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.stat {
  min-height: 100px;
}

.label {
  color: #909399;
}

.value {
  margin-top: 12px;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.mt {
  margin-top: 16px;
}

.life-row {
  display: grid;
  grid-template-columns: 100px 1fr;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
</style>
