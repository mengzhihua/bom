<template><div class="page"><div class="page-title"><h2>{{ ecn.ecnNo }} {{ ecn.title }}</h2><el-button @click="$router.back()">返回</el-button></div><el-card shadow="never"><el-descriptions :column="4" border><el-descriptions-item label="状态">{{ ecn.status }}</el-descriptions-item><el-descriptions-item label="目标 BOM">{{ ecn.bomId }}</el-descriptions-item><el-descriptions-item label="生效方式">{{ ecn.effectiveType }}</el-descriptions-item><el-descriptions-item label="生效日期">{{ ecn.effectiveDate }}</el-descriptions-item></el-descriptions><div class="actions"><el-button v-if="canWrite()" @click="act('submit')">提交</el-button><el-button v-if="canWrite()" @click="act('approve')">审批</el-button><el-button v-if="canWrite()" type="primary" @click="act('implement')">实施</el-button><el-button v-if="ecn.implementedBomId" type="success" @click="$router.push(`/boms/${ecn.implementedBomId}`)">查看新版本 BOM</el-button></div></el-card><el-card shadow="never" class="mt"><template #header>ECN 变更明细</template><el-table :data="items"><el-table-column prop="action" label="动作" /><el-table-column prop="parentPartId" label="父件" /><el-table-column prop="oldChildPartId" label="旧子件" /><el-table-column prop="newChildPartId" label="新子件" /><el-table-column prop="oldQty" label="旧数量" /><el-table-column prop="newQty" label="新数量" /><el-table-column prop="findNo" label="序号" /><el-table-column prop="usageCondition" label="配置条件" /><el-table-column prop="stationCode" label="工位" /></el-table></el-card></div></template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changes } from '../../api'
import { canWrite } from '../../auth'
const route = useRoute(); const $router = useRouter(); const ecn = ref({}); const items = ref([])
async function load() { ecn.value = await changes.ecn(route.params.id); items.value = await changes.ecnItems(route.params.id) }
async function act(a) { await changes.ecnAction(route.params.id, a); ElMessage.success('操作成功'); load() }
onMounted(load)
</script>
