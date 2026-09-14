<template>
  <div class="page">
    <div class="page-title"><h2>配置特征与选项</h2><el-button v-if="canWrite()" type="primary" @click="featureDialog=true">新增特征</el-button></div>
    <el-card shadow="never"><el-table :data="rows" stripe>
      <el-table-column type="expand"><template #default="{ row }"><div class="nested"><el-tag v-for="o in row.options" :key="o.id" closable @close="removeOption(row, o)">{{ o.optionCode }} / {{ o.optionName }}</el-tag><el-button v-if="canWrite()" link type="primary" @click="addOption(row)">添加选项</el-button></div></template></el-table-column>
      <el-table-column prop="feature" label="特征编码" /><el-table-column prop="name" label="特征名称" /><el-table-column prop="vehicleModelId" label="车型" />
    </el-table></el-card>
    <el-dialog v-model="featureDialog" title="新增配置特征"><el-form :model="feature"><el-form-item label="编码"><el-input v-model="feature.feature" /></el-form-item><el-form-item label="名称"><el-input v-model="feature.name" /></el-form-item></el-form><template #footer><el-button type="primary" @click="saveFeature">保存</el-button></template></el-dialog>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { master } from '../../api'
import { canWrite } from '../../auth'
const rows = ref([]); const featureDialog = ref(false); const feature = ref({})
async function refresh() { rows.value = await master.features(); for (const r of rows.value) r.options = await master.options(r.id) }
async function saveFeature() { await master.feature(feature.value); featureDialog.value = false; feature.value = {}; ElMessage.success('保存成功'); refresh() }
async function addOption(row) { const value = prompt('请输入选项编码'); if (value) { await master.option(row.id, { optionCode: value, optionName: value }); refresh() } }
async function removeOption(row, option) { await ElMessageBox.confirm('确认删除该选项？'); option.id && ElMessage.warning('后端当前仅提供新增接口，已保留数据') }
onMounted(refresh)
</script>
