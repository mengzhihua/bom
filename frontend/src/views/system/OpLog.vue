<template><div class="page"><div class="page-title"><h2>操作日志</h2><el-button @click="load">刷新</el-button></div><el-card shadow="never"><el-table :data="rows" stripe><el-table-column prop="createdAt" label="时间" /><el-table-column prop="username" label="用户" /><el-table-column prop="method" label="方法" /><el-table-column prop="path" label="路径" /><el-table-column prop="httpStatus" label="状态码" /><el-table-column prop="costMs" label="耗时(ms)" /></el-table><el-pagination class="pager" :total="total" layout="total, prev, pager, next" /></el-card></div></template>
<script setup>
import { onMounted, ref } from 'vue'
import { system } from '../../api'
const rows = ref([]); const total = ref(0)
async function load() { const p = await system.oplog({ current: 1, size: 50 }); rows.value = p.records || []; total.value = p.total || 0 }
onMounted(load)
</script>
