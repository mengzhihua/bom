<template>
  <div class="page"><div class="page-title"><h2>BOM 管理</h2><el-button v-if="canWrite()" type="primary" @click="visible=true">新建 BOM</el-button></div><el-card shadow="never"><el-table :data="rows" stripe v-loading="loading"><el-table-column prop="bomNo" label="BOM 号" /><el-table-column prop="bomType" label="类型" /><el-table-column prop="rootPartId" label="顶层零件" /><el-table-column prop="vehicleModelId" label="车型" /><el-table-column prop="plantId" label="工厂" /><el-table-column prop="version" label="版本" /><el-table-column label="状态"><template #default="{row}"><StatusTag :value="row.status" /></template></el-table-column><el-table-column prop="effectiveFrom" label="生效日期" /><el-table-column prop="sourceBomId" label="来源 BOM" /><el-table-column prop="sapBomNo" label="SAP BOM 号" /><el-table-column label="操作" width="390" fixed="right"><template #default="{row}"><el-button link type="primary" @click="$router.push(`/boms/${row.id}`)">打开</el-button><el-button v-if="canWrite()" link @click="act(row,'release')">发布</el-button><el-button v-if="canWrite()" link @click="act(row,'freeze')">冻结</el-button><el-button v-if="canWrite()" link @click="act(row,'new-version')">新版本</el-button><el-button link @click="compare(row)">对比</el-button><el-button v-if="canWrite()" link @click="act(row,'sync-sap')">SAP 同步</el-button></template></el-table-column></el-table></el-card><el-dialog v-model="visible" title="新建 BOM"><el-form :model="form" label-width="110px"><el-form-item label="BOM 类型"><el-select v-model="form.bomType"><el-option v-for="x in ['EBOM','MBOM','SBOM']" :key="x" :label="x" :value="x" /></el-select></el-form-item><el-form-item label="顶层零件 ID"><el-input v-model="form.rootPartId" /></el-form-item><el-form-item label="车型 ID"><el-input v-model="form.vehicleModelId" /></el-form-item><el-form-item label="工厂 ID"><el-input v-model="form.plantId" /></el-form-item><el-form-item label="描述"><el-input v-model="form.description" /></el-form-item></el-form><template #footer><el-button type="primary" @click="create">创建</el-button></template></el-dialog></div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { boms } from '../../api'
import { canWrite } from '../../auth'
import StatusTag from '../../components/StatusTag.vue'
const $router = useRouter(); const rows = ref([]); const loading = ref(false); const visible = ref(false); const form = ref({ bomType: 'EBOM' })
async function load() { loading.value = true; try { rows.value = await boms.list() } finally { loading.value = false } }
async function act(row, action) { await boms.action(row.id, action); ElMessage.success('操作成功'); load() }
async function create() { await boms.create(form.value); visible.value = false; ElMessage.success('创建成功'); load() }
function compare(row) { const other = rows.value.find((x) => x.rootPartId === row.rootPartId && x.id !== row.id); if (other) $router.push(`/boms/compare?leftId=${row.id}&rightId=${other.id}`); else ElMessage.info('暂无同顶层零件的其他版本') }
onMounted(load)
</script>
