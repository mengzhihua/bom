<template>
  <div class="page">
    <div class="page-title"><h2>零件管理</h2><div><el-button @click="exportCsv">导出 CSV</el-button><el-button v-if="canWrite()" type="primary" @click="open()">新建零件</el-button></div></div>
    <el-card shadow="never" class="filter-card"><el-form inline><el-form-item label="关键字"><el-input v-model="filters.keyword" clearable /></el-form-item><el-form-item label="类型"><el-select v-model="filters.partType" clearable><el-option v-for="x in types" :key="x" :label="x" :value="x" /></el-select></el-form-item><el-form-item label="生命周期"><el-select v-model="filters.lifecycle" clearable><el-option v-for="x in life" :key="x" :label="x" :value="x" /></el-select></el-form-item><el-button type="primary" @click="refresh">查询</el-button></el-form></el-card>
    <el-card shadow="never">
      <el-table :data="rows" stripe v-loading="loading">
        <el-table-column prop="partNo" label="零件号" width="140" />
        <el-table-column prop="revision" label="版本" width="70" />
        <el-table-column prop="partName" label="名称" min-width="150" />
        <el-table-column prop="partType" label="类型" />
        <el-table-column prop="category" label="类别" />
        <el-table-column prop="uom" label="单位" width="70" />
        <el-table-column prop="material" label="材质" />
        <el-table-column prop="weightKg" label="重量(kg)" />
        <el-table-column prop="makeBuy" label="自制/外购" />
        <el-table-column prop="unitCost" label="成本" />
        <el-table-column label="生命周期"><template #default="{ row }"><el-tag :type="tagType(row.lifecycle)">{{ row.lifecycle }}</el-tag></template></el-table-column>
        <el-table-column prop="sapMaterial" label="SAP 物料号" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/parts/${row.id}`)">详情</el-button>
            <el-button v-if="canWrite()" link @click="open(row)">编辑</el-button>
            <el-dropdown v-if="canWrite()">
              <el-button link type="primary">生命周期</el-button>
              <template #dropdown><el-dropdown-menu><el-dropdown-item @click="act(row,'submit')">提交</el-dropdown-item><el-dropdown-item @click="act(row,'release')">发布</el-dropdown-item><el-dropdown-item @click="act(row,'obsolete')">作废</el-dropdown-item><el-dropdown-item @click="act(row,'revise')">升版</el-dropdown-item><el-dropdown-item @click="act(row,'sync-sap')">同步 SAP</el-dropdown-item></el-dropdown-menu></template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" layout="total, prev, pager, next" :total="total" v-model:current-page="filters.current" @current-change="refresh" />
    </el-card>
    <el-drawer v-model="visible" title="零件信息" size="560px"><el-form :model="form" label-width="110px"><el-form-item v-for="f in fields" :key="f.prop" :label="f.label"><el-select v-if="f.options" v-model="form[f.prop]" clearable><el-option v-for="x in f.options" :key="x" :label="x" :value="x" /></el-select><el-input v-else v-model="form[f.prop]" /></el-form-item></el-form><template #footer><el-button type="primary" @click="save">保存</el-button></template></el-drawer>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { parts } from '../../api'
import { canWrite } from '../../auth'
const $router = useRouter(); const loading = ref(false); const rows = ref([]); const total = ref(0); const visible = ref(false); const form = reactive({})
const filters = reactive({ current: 1, size: 20, keyword: '', partType: '', lifecycle: '' })
const types = ['ASSEMBLY', 'SUB_ASSEMBLY', 'PART', 'RAW', 'STANDARD', 'SOFTWARE']; const life = ['DRAFT', 'IN_REVIEW', 'RELEASED', 'OBSOLETE']
const fields = [{ prop: 'partNo', label: '零件号' }, { prop: 'revision', label: '版本' }, { prop: 'partName', label: '名称' }, { prop: 'partNameEn', label: '英文名称' }, { prop: 'partType', label: '类型', options: types }, { prop: 'category', label: '类别', options: ['BODY', 'CHASSIS', 'POWERTRAIN', 'ELECTRICAL', 'INTERIOR', 'EXTERIOR'] }, { prop: 'uom', label: '单位', options: ['EA', 'KG', 'M', 'L'] }, { prop: 'material', label: '材质' }, { prop: 'weightKg', label: '重量(kg)' }, { prop: 'makeBuy', label: '自制/外购', options: ['MAKE', 'BUY'] }, { prop: 'supplierId', label: '供应商' }, { prop: 'unitCost', label: '成本' }, { prop: 'leadTimeDays', label: '交期天数' }, { prop: 'drawingNo', label: '图号' }, { prop: 'drawingRev', label: '图纸版本' }, { prop: 'sapMaterial', label: 'SAP 物料号' }, { prop: 'remark', label: '备注' }]
function tagType(v) { return v === 'RELEASED' ? 'success' : v === 'OBSOLETE' ? 'info' : v === 'IN_REVIEW' ? 'warning' : '' }
async function refresh() { loading.value = true; try { const p = await parts.page(filters); rows.value = p.records || []; total.value = p.total || 0 } finally { loading.value = false } }
function open(row) { Object.keys(form).forEach((k) => delete form[k]); Object.assign(form, row || { revision: 'A', lifecycle: 'DRAFT', uom: 'EA', makeBuy: 'MAKE' }); visible.value = true }
async function save() { if (form.id) await parts.update(form.id, form); else await parts.create(form); visible.value = false; ElMessage.success('保存成功'); refresh() }
async function act(row, action) { await parts.action(row.id, action); ElMessage.success('操作成功'); refresh() }
function exportCsv() { window.open('/api/parts/export') }
onMounted(refresh)
</script>
