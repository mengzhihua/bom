<template><div class="page"><div class="page-title"><h2>ECR 工程变更申请</h2><el-button v-if="canWrite()" type="primary" @click="create">新建 ECR</el-button></div><el-card shadow="never"><el-table :data="rows" stripe><el-table-column prop="ecrNo" label="ECR 编号" /><el-table-column prop="title" label="标题" /><el-table-column prop="reason" label="原因" /><el-table-column prop="priority" label="优先级" /><el-table-column prop="status" label="状态" /><el-table-column prop="requester" label="申请人" /><el-table-column label="操作"><template #default="{row}"><el-button v-if="canWrite()" link @click="act(row,'submit')">提交</el-button><el-button v-if="canWrite()" link @click="act(row,'approve')">审批</el-button><el-button v-if="canWrite()" link @click="act(row,'to-ecn')">生成 ECN</el-button><el-button v-if="canWrite()" link @click="act(row,'close')">关闭</el-button></template></el-table-column></el-table></el-card></div></template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changes } from '../../api'
import { canWrite } from '../../auth'
const rows = ref([])
async function load() { rows.value = await changes.ecrs() }
async function create() { await changes.createEcr({ title: '新建工程变更', reason: 'DESIGN', priority: 'NORMAL', status: 'DRAFT' }); ElMessage.success('已创建'); load() }
async function act(row, action) { await changes.ecrAction(row.id, action); ElMessage.success('操作成功'); load() }
onMounted(load)
</script>
