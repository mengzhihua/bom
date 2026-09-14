<template><div class="page"><div class="page-title"><h2>ECN 工程变更通知</h2></div><el-card shadow="never"><el-table :data="rows" stripe><el-table-column prop="ecnNo" label="ECN 编号" /><el-table-column prop="title" label="标题" /><el-table-column prop="bomId" label="目标 BOM" /><el-table-column prop="effectiveType" label="生效方式" /><el-table-column prop="effectiveDate" label="生效日期" /><el-table-column prop="status" label="状态" /><el-table-column label="操作"><template #default="{row}"><el-button link type="primary" @click="$router.push(`/ecns/${row.id}`)">详情</el-button></template></el-table-column></el-table></el-card></div></template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { changes } from '../../api'
const $router = useRouter(); const rows = ref([])
onMounted(async () => { rows.value = await changes.ecns() })
</script>
