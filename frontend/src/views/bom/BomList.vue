<template>
  <div class="page">
    <div class="page-title">
      <h2>BOM 管理</h2>
      <el-button
        v-if="canWrite()"
        type="primary"
        @click="openCreate"
      >
        新建 BOM
      </el-button>
    </div>

    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="类型">
          <el-select v-model="filters.bomType" clearable>
            <el-option label="EBOM" value="EBOM" />
            <el-option label="MBOM" value="MBOM" />
            <el-option label="SBOM" value="SBOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="RELEASED" />
            <el-option label="冻结" value="FROZEN" />
            <el-option label="作废" value="OBSOLETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="车型">
          <el-input v-model="filters.modelCode" clearable />
        </el-form-item>
        <el-button type="primary" @click="load">
          查询
        </el-button>
      </el-form>

      <el-table :data="filteredRows" stripe>
        <el-table-column prop="bomNo" label="BOM 号" width="170" />
        <el-table-column prop="bomType" label="类型" width="90" />
        <el-table-column label="顶层零件" min-width="180">
          <template #default="{ row }">
            {{ row.rootPartNo }} {{ row.rootPartName }}
          </template>
        </el-table-column>
        <el-table-column label="车型" min-width="130">
          <template #default="{ row }">
            {{ row.vehicleModelCode }} {{ row.vehicleModelName }}
          </template>
        </el-table-column>
        <el-table-column label="工厂" min-width="130">
          <template #default="{ row }">
            {{ row.plantCode || '-' }} {{ row.plantName || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <StatusTag :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="生效期" width="180">
          <template #default="{ row }">
            {{ row.effectiveFrom || '-' }} ~ {{ row.effectiveTo || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="sourceBomNo" label="来源 BOM" width="150" />
        <el-table-column prop="sapBomNo" label="SAP BOM 号" width="140" />
        <el-table-column label="操作" width="470" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">
              打开
            </el-button>
            <el-button
              v-if="canWrite() && row.status === 'DRAFT'"
              link
              @click="act(row, 'release')"
            >
              发布
            </el-button>
            <el-button
              v-if="canWrite() && row.status === 'RELEASED'"
              link
              @click="act(row, 'freeze')"
            >
              冻结
            </el-button>
            <el-button
              v-if="canWrite() && row.status !== 'OBSOLETE'"
              link
              @click="act(row, 'obsolete')"
            >
              作废
            </el-button>
            <el-button
              v-if="canWrite() && row.status !== 'DRAFT'"
              link
              @click="act(row, 'new-version')"
            >
              新版本
            </el-button>
            <el-button
              v-if="canWrite() && row.bomType === 'EBOM'"
              link
              @click="derive(row)"
            >
              派生 MBOM
            </el-button>
            <el-button link @click="compare(row)">
              对比
            </el-button>
            <el-button
              v-if="canWrite() && row.status === 'RELEASED'"
              link
              @click="act(row, 'sync-sap')"
            >
              SAP 同步
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="新建 BOM" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="BOM 类型" required>
          <el-select v-model="form.bomType">
            <el-option label="EBOM" value="EBOM" />
            <el-option label="MBOM" value="MBOM" />
            <el-option label="SBOM" value="SBOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="顶层零件 ID" required>
          <el-input v-model="form.rootPartId" />
        </el-form-item>
        <el-form-item label="车型 ID">
          <el-input v-model="form.vehicleModelId" />
        </el-form-item>
        <el-form-item label="工厂 ID">
          <el-input v-model="form.plantId" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button type="primary" @click="create">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { boms } from '../../api'
import { canWrite } from '../../auth'
import StatusTag from '../../components/StatusTag.vue'

const router = useRouter()
const rows = ref([])
const visible = ref(false)
const filters = reactive({
  bomType: '',
  status: '',
  modelCode: ''
})
const form = reactive({
  bomType: 'EBOM',
  rootPartId: null,
  vehicleModelId: null,
  plantId: null,
  description: ''
})

const filteredRows = computed(() => rows.value.filter((row) => {
  const typeMatched = !filters.bomType || row.bomType === filters.bomType
  const statusMatched = !filters.status || row.status === filters.status
  const modelMatched = !filters.modelCode
    || row.vehicleModelCode?.includes(filters.modelCode)
  return typeMatched && statusMatched && modelMatched
}))

async function load() {
  rows.value = await boms.list()
}

function openCreate() {
  Object.assign(form, {
    bomType: 'EBOM',
    rootPartId: null,
    vehicleModelId: null,
    plantId: null,
    description: ''
  })
  visible.value = true
}

function open(row) {
  router.push(`/boms/${row.id}`)
}

async function create() {
  await boms.create(form)
  visible.value = false
  ElMessage.success('创建成功')
  await load()
}

async function act(row, action) {
  await boms.action(row.id, action)
  ElMessage.success('操作成功')
  await load()
}

async function derive(row) {
  const plantId = window.prompt('请输入目标工厂 ID')
  if (!plantId) {
    return
  }
  await boms.action(row.id, 'derive-mbom', {
    plantId: Number(plantId),
    description: `${row.bomNo} 派生 MBOM`
  })
  ElMessage.success('MBOM 已创建')
  await load()
}

function compare(row) {
  const other = rows.value.find(
    (item) => item.rootPartId === row.rootPartId && item.id !== row.id
  )
  if (!other) {
    ElMessage.info('暂无同顶层零件的其他版本')
    return
  }
  router.push(`/boms/compare?leftId=${row.id}&rightId=${other.id}`)
}

onMounted(load)
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
