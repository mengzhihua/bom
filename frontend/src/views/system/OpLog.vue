<template>
  <div class="page">
    <div class="page-title">
      <h2>操作日志</h2>
      <el-button @click="load">
        刷新
      </el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="rows" stripe>
        <el-table-column prop="createdAt" label="时间" />
        <el-table-column prop="username" label="用户" />
        <el-table-column prop="method" label="方法" />
        <el-table-column prop="path" label="路径" />
        <el-table-column prop="httpStatus" label="状态码" />
        <el-table-column prop="costMs" label="耗时(ms)" />
      </el-table>
      <el-pagination
        v-model:current-page="page"
        class="pager"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { system } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  const result = await system.oplog({
    current: page.value,
    size: 50
  })
  rows.value = result.records || []
  total.value = result.total || 0
}

onMounted(load)
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pager {
  margin-top: 16px;
}
</style>
