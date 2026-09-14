<template>
  <div class="page"><div class="page-title"><h2>零件详情 {{ part.partNo }}</h2><el-button @click="$router.back()">返回</el-button></div><el-card shadow="never"><el-descriptions :column="3" border><el-descriptions-item v-for="(v,k) in summary" :key="k" :label="labels[k] || k">{{ v }}</el-descriptions-item></el-descriptions></el-card><el-card shadow="never" class="mt"><template #header>Where-used 反查 <el-switch v-model="recursive" @change="loadWhere" /></template><el-table :data="where" stripe><el-table-column prop="bomNo" label="BOM 号" /><el-table-column prop="bomType" label="类型" /><el-table-column prop="parentPart" label="父件" /><el-table-column prop="rootPart" label="顶层件" /><el-table-column prop="qty" label="数量" /><el-table-column prop="path" label="层级路径" /></el-table></el-card><el-card shadow="never" class="mt"><template #header>文档清单</template><el-table :data="docs"><el-table-column prop="docType" label="类型" /><el-table-column prop="docNo" label="文档号" /><el-table-column prop="version" label="版本" /><el-table-column prop="url" label="链接" /></el-table></el-card></div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { parts } from '../../api'
const route = useRoute(); const $router = useRouter(); const part = ref({}); const where = ref([]); const docs = ref([]); const recursive = ref(true)
const labels = { partNo: '零件号', revision: '版本', partName: '名称', partType: '类型', category: '类别', uom: '单位', material: '材质', weightKg: '重量', makeBuy: '自制/外购', lifecycle: '生命周期', unitCost: '成本', sapMaterial: 'SAP 物料号' }
const summary = computed(() => Object.fromEntries(Object.keys(labels).map((k) => [k, part.value[k]])))
async function loadWhere() { where.value = await parts.whereUsed(route.params.id, recursive.value) }
onMounted(async () => { part.value = await parts.get(route.params.id); docs.value = await parts.documents(route.params.id); loadWhere() })
</script>
