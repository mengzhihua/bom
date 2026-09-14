<template>
  <div class="page">
    <div class="page-title"><h2>{{ title }}</h2><el-button v-if="canWrite('master')" type="primary" @click="open()"><el-icon><Plus /></el-icon>新增</el-button></div>
    <el-card shadow="never">
      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label" min-width="130" />
        <el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="open(row)">编辑</el-button></template></el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="visible" :title="form.id ? '编辑' : '新增'" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item v-for="col in columns" :key="col.prop" :label="col.label"><el-input v-model="form[col.prop]" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { canWrite } from '../../auth'
const props = defineProps({
  title: String,
  columns: Array,
  load: Function,
  create: Function,
  update: Function
})
const rows = ref([]); const loading = ref(false); const visible = ref(false); const form = reactive({})
async function refresh() { loading.value = true; try { rows.value = await props.load() } finally { loading.value = false } }
function open(row) { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, row || {}); visible.value = true }
async function save() {
  if (form.id && props.update) {
    await props.update(form.id, { ...form })
  } else {
    await props.create({ ...form })
  }
  ElMessage.success('保存成功')
  visible.value = false
  refresh()
}
onMounted(refresh)
</script>
