<template><div class="page"><div class="page-title"><h2>BOM 版本对比</h2></div><el-card shadow="never"><el-form inline><el-form-item label="左侧 BOM"><el-select v-model="left"><el-option v-for="b in rows" :key="b.id" :label="b.bomNo" :value="b.id" /></el-select></el-form-item><el-form-item label="右侧 BOM"><el-select v-model="right"><el-option v-for="b in rows" :key="b.id" :label="b.bomNo" :value="b.id" /></el-select></el-form-item><el-button type="primary" @click="load">开始对比</el-button></el-form><el-table :data="diff"><el-table-column prop="type" label="差异"><template #default="{row}"><el-tag :type="row.type === 'ADDED' ? 'success' : row.type === 'REMOVED' ? 'danger' : 'warning'">{{ row.type }}</el-tag></template></el-table-column><el-table-column prop="key" label="父件-子件" /><el-table-column prop="left" label="变更前" /><el-table-column prop="right" label="变更后" /></el-table></el-card></div></template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { boms } from '../../api'
const route = useRoute(); const rows = ref([]); const left = ref(Number(route.query.leftId) || null); const right = ref(Number(route.query.rightId) || null); const diff = ref([])
async function load() { if (left.value && right.value) diff.value = await boms.compare(left.value, right.value) }
onMounted(async () => { rows.value = await boms.list(); if (!left.value && rows.value.length > 1) { left.value = rows.value[0].id; right.value = rows.value[1].id } load() })
</script>
