<template>
  <div class="page">
    <div class="page-title">
      <h2>集成日志</h2>
      <el-button @click="load">
        刷新
      </el-button>
    </div>
    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="方向">
          <el-select v-model="filters.direction" clearable>
            <el-option label="发出" value="OUT" />
            <el-option label="接入" value="IN" />
          </el-select>
        </el-form-item>
        <el-form-item label="系统">
          <el-input v-model="filters.system" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="filters.status" clearable />
        </el-form-item>
        <el-button type="primary" @click="load">
          查询
        </el-button>
      </el-form>
      <el-table :data="rows" stripe>
        <el-table-column type="expand">
          <template #default="{ row }">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="请求">
                <pre>{{ row.requestBody }}</pre>
              </el-descriptions-item>
              <el-descriptions-item label="响应">
                <pre>{{ row.responseBody }}</pre>
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" />
        <el-table-column prop="direction" label="方向" />
        <el-table-column prop="system" label="系统" />
        <el-table-column prop="action" label="动作" />
        <el-table-column prop="bizCode" label="业务号" />
        <el-table-column prop="status" label="状态" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button link @click="retry(row)">
              重试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { integration } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const filters = reactive({
  direction: '',
  system: '',
  status: ''
})

async function load() {
  const result = await integration.logs({
    current: page.value,
    size: 20,
    ...filters
  })
  rows.value = result.records || []
  total.value = result.total || 0
}

async function retry(row) {
  await integration.retry(row.id)
  ElMessage.success('已重试')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

pre {
  max-width: 500px;
  margin: 0;
  white-space: pre-wrap;
}

.pager {
  margin-top: 16px;
}
</style>
