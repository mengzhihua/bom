<template><div class="page"><div class="page-title"><h2>集成日志</h2><el-button @click="load">刷新</el-button></div><el-card shadow="never"><el-table :data="rows" stripe><el-table-column prop="createdAt" label="时间" /><el-table-column prop="direction" label="方向" /><el-table-column prop="system" label="系统" /><el-table-column prop="action" label="动作" /><el-table-column prop="bizCode" label="业务号" /><el-table-column prop="status" label="状态" /><el-table-column label="操作"><template #default="{row}"><el-button link @click="retry(row)">重试</el-button></template></el-table-column></el-table><el-pagination class="pager" layout="total, prev, pager, next" :total="total" /></el-card></div></template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { integration } from '../../api'
const rows = ref([]); const total = ref(0)
async function load() { const p = await integration.logs({ current: 1, size: 20 }); rows.value = p.records || []; total.value = p.total || 0 }
async function retry(row) { await integration.retry(row.id); ElMessage.success('已重试'); load() }
onMounted(load)
</script>
