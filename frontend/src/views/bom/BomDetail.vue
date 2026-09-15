<template>
  <div class="page">
    <div class="page-title">
      <div>
        <el-button link @click="router.back()">
          返回
        </el-button>
        <h2>{{ header.bomNo || 'BOM 详情' }}</h2>
      </div>
      <div class="actions">
        <el-button
          v-if="canWriteRole && header.status === 'DRAFT'"
          type="primary"
          @click="openAdd()"
        >
          添加子件
        </el-button>
        <el-button @click="downloadExport">
          导出 CSV
        </el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-descriptions :column="4" border>
        <el-descriptions-item label="BOM 类型">
          {{ header.bomType }}
        </el-descriptions-item>
        <el-descriptions-item label="版本">
          {{ header.version }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :value="header.status" />
        </el-descriptions-item>
        <el-descriptions-item label="顶层零件">
          {{ header.rootPartNo }} {{ header.rootPartName }}
        </el-descriptions-item>
        <el-descriptions-item label="车型">
          {{ header.vehicleModelCode }} {{ header.vehicleModelName }}
        </el-descriptions-item>
        <el-descriptions-item label="工厂">
          {{ header.plantCode || '-' }}
          {{ header.plantName || '' }}
        </el-descriptions-item>
        <el-descriptions-item label="生效日期">
          {{ header.effectiveFrom }} ~ {{ header.effectiveTo }}
        </el-descriptions-item>
        <el-descriptions-item label="来源 BOM">
          {{ header.sourceBomNo || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="mt">
      <el-tabs v-model="tab" @tab-change="loadTab">
        <el-tab-pane label="结构树" name="tree">
          <el-table
            :data="tree"
            row-key="itemId"
            border
            default-expand-all
            :tree-props="{ children: 'children' }"
          >
            <el-table-column prop="findNo" label="序号" width="80" />
            <el-table-column prop="partNo" label="零件号" width="150" />
            <el-table-column prop="partName" label="名称" min-width="160" />
            <el-table-column prop="revision" label="版本" width="80" />
            <el-table-column prop="partType" label="类型" width="110" />
            <el-table-column prop="qty" label="数量" width="90" />
            <el-table-column prop="extendedQty" label="累计数量" width="100" />
            <el-table-column prop="uom" label="单位" width="70" />
            <el-table-column prop="usageType" label="用途" width="90" />
            <el-table-column
              prop="usageCondition"
              label="配置条件"
              min-width="150"
            />
            <el-table-column prop="stationCode" label="工位" width="90" />
            <el-table-column
              prop="alternateGroup"
              label="替代组"
              width="90"
            />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="canWriteRole && header.status === 'DRAFT'"
                  link
                  type="primary"
                  @click="openAdd(row)"
                >
                  添加子件
                </el-button>
                <el-button
                  v-if="canWriteRole && header.status === 'DRAFT'"
                  link
                  @click="openEdit(row)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="canWriteRole && header.status === 'DRAFT'"
                  link
                  type="danger"
                  @click="removeItem(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="缩排展开" name="explode">
          <el-form inline>
            <el-form-item label="最大层级">
              <el-input-number
                v-model="level"
                :min="1"
                :max="20"
                @change="loadExplode"
              />
            </el-form-item>
          </el-form>
          <el-table :data="explode" border>
            <el-table-column prop="level" label="层级" width="80" />
            <el-table-column label="零件号" min-width="180">
              <template #default="{ row }">
                <span :style="{ paddingLeft: `${row.level * 18}px` }">
                  {{ row.partNo }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="partName" label="名称" />
            <el-table-column prop="qty" label="数量" />
            <el-table-column prop="extendedQty" label="累计数量" />
            <el-table-column prop="usageCondition" label="配置条件" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="汇总 BOM" name="summary">
          <el-table :data="summary" border>
            <el-table-column prop="partNo" label="零件号" />
            <el-table-column prop="partName" label="名称" />
            <el-table-column prop="partType" label="类型" />
            <el-table-column prop="extendedQty" label="汇总数量" />
            <el-table-column prop="uom" label="单位" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="成本/重量" name="rollup">
          <div class="metrics">
            <el-statistic
              title="总成本"
              :value="roll.totalCost || 0"
            />
            <el-statistic
              title="总重量"
              :value="roll.totalWeight || 0"
            />
          </div>
          <el-table :data="roll.details || []" border>
            <el-table-column prop="partNo" label="零件号" />
            <el-table-column prop="partName" label="名称" />
            <el-table-column prop="extendedQty" label="数量" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="配置 BOM" name="config">
          <el-form inline>
            <el-form-item
              v-for="feature in features"
              :key="feature.id"
              :label="feature.name"
            >
              <el-select
                v-model="selections[feature.feature]"
                clearable
                placeholder="请选择"
                style="width: 150px"
              >
                <el-option
                  v-for="option in feature.options"
                  :key="option.id"
                  :label="option.optionName"
                  :value="option.optionCode"
                />
              </el-select>
            </el-form-item>
            <el-button type="primary" @click="configure">
              解析 100% BOM
            </el-button>
          </el-form>
          <el-alert
            v-if="configured"
            class="mt"
            title="配置 BOM 解析成功"
            type="success"
            show-icon
          />
          <el-table
            v-if="configured"
            :data="configured.tree || []"
            row-key="itemId"
            border
            default-expand-all
            :tree-props="{ children: 'children' }"
          >
            <el-table-column prop="partNo" label="零件号" />
            <el-table-column prop="partName" label="名称" />
            <el-table-column prop="extendedQty" label="累计数量" />
            <el-table-column prop="usageCondition" label="条件" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane
          v-if="header.bomType === 'MBOM'"
          label="按工位"
          name="station"
        >
          <el-collapse v-if="Object.keys(stations).length">
            <el-collapse-item
              v-for="(rows, station) in stations"
              :key="station"
              :title="station"
              :name="station"
            >
              <el-table :data="rows" border>
                <el-table-column prop="partNo" label="零件号" />
                <el-table-column prop="partName" label="名称" />
                <el-table-column prop="qty" label="数量" />
                <el-table-column prop="uom" label="单位" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
          <el-empty v-else description="暂无工位数据" />
        </el-tab-pane>

        <el-tab-pane label="导入/导出" name="csv">
          <el-alert
            title="CSV 列：parentPartNo, childPartNo, findNo, qty, uom, usageCondition, stationCode"
            type="info"
            show-icon
          />
          <el-upload
            v-if="canWriteRole && header.status === 'DRAFT'"
            class="upload"
            :show-file-list="false"
            :http-request="uploadCsv"
            accept=".csv"
          >
            <el-button type="primary">
              导入 CSV
            </el-button>
          </el-upload>
          <el-button @click="downloadExport">
            导出 CSV
          </el-button>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="itemDialog"
      :title="editingItem ? '编辑 BOM 行' : '添加子件'"
      width="620px"
      destroy-on-close
    >
      <el-form :model="itemForm" label-width="100px">
        <el-form-item label="父件">
          <el-input :model-value="parentLabel" disabled />
        </el-form-item>
        <el-form-item label="子零件" required>
          <el-select
            v-model="itemForm.childPartId"
            filterable
            remote
            :remote-method="searchParts"
            placeholder="搜索零件号或名称"
            style="width: 100%"
          >
            <el-option
              v-for="part in partOptions"
              :key="part.id"
              :label="`${part.partNo} ${part.partName}`"
              :value="part.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="itemForm.qty" :min="0.0001" />
        </el-form-item>
        <el-form-item label="单位">
          <el-select v-model="itemForm.uom">
            <el-option label="EA" value="EA" />
            <el-option label="KG" value="KG" />
            <el-option label="M" value="M" />
          </el-select>
        </el-form-item>
        <el-form-item label="用途">
          <el-select v-model="itemForm.usageType">
            <el-option label="普通" value="NORMAL" />
            <el-option label="可选" value="OPTIONAL" />
            <el-option label="虚拟" value="PHANTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置条件">
          <el-input v-model="itemForm.usageCondition" />
        </el-form-item>
        <el-form-item v-if="header.bomType === 'MBOM'" label="工位">
          <el-select v-model="itemForm.stationCode">
            <el-option
              v-for="station in workstationOptions"
              :key="station.stationCode"
              :label="`${station.stationCode} ${station.stationName}`"
              :value="station.stationCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="替代组">
          <el-input v-model="itemForm.alternateGroup" />
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="itemForm.positionDesc" />
        </el-form-item>
        <el-form-item label="序号">
          <el-input-number v-model="itemForm.findNo" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog = false">
          取消
        </el-button>
        <el-button type="primary" @click="saveItem">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { boms, master, parts } from '../../api'
import StatusTag from '../../components/StatusTag.vue'
import { canWrite } from '../../auth'

const route = useRoute()
const router = useRouter()
const header = ref({})
const tab = ref('tree')
const tree = ref([])
const explode = ref([])
const summary = ref([])
const roll = ref({})
const stations = ref({})
const features = ref([])
const selections = reactive({})
const configured = ref(null)
const level = ref(6)
const itemDialog = ref(false)
const editingItem = ref(null)
const parentNode = ref(null)
const partOptions = ref([])
const workstationOptions = ref([])
const itemForm = reactive({
  parentPartId: null,
  childPartId: null,
  qty: 1,
  uom: 'EA',
  usageType: 'NORMAL',
  usageCondition: '',
  stationCode: '',
  alternateGroup: '',
  positionDesc: '',
  findNo: null
})

const canWriteRole = computed(() => canWrite(
  header.value.bomType === 'MBOM' ? 'mbom' : 'ebom'
))
const parentLabel = computed(() => {
  if (parentNode.value) {
    return `${parentNode.value.partNo} ${parentNode.value.partName}`
  }
  return `${header.value.rootPartNo || ''} ${header.value.rootPartName || ''}`
})

async function loadHeader() {
  header.value = await boms.get(route.params.id)
}

async function loadTree() {
  tree.value = await boms.tree(route.params.id)
}

async function loadExplode() {
  explode.value = await boms.explode(route.params.id, level.value)
}

async function loadTab(name = tab.value) {
  tab.value = name
  if (name === 'tree') {
    await loadTree()
  } else if (name === 'explode') {
    await loadExplode()
  } else if (name === 'summary') {
    summary.value = await boms.summarized(route.params.id)
  } else if (name === 'rollup') {
    roll.value = await boms.rollup(route.params.id)
  } else if (name === 'config') {
    await loadFeatures()
  } else if (name === 'station') {
    stations.value = await boms.byStation(route.params.id)
  }
}

async function loadFeatures() {
  const values = await master.features()
  features.value = values
  for (const feature of values) {
    feature.options = await master.options(feature.id)
  }
}

async function configure() {
  configured.value = await boms.configure(route.params.id, {
    selections: { ...selections }
  })
}

async function loadWorkstations() {
  workstationOptions.value = await master.workstations({
    plantId: header.value.plantId
  })
}

async function searchParts(keyword) {
  const result = await parts.page({
    keyword,
    current: 1,
    size: 30
  })
  partOptions.value = result.records || result
}

function openAdd(node = null) {
  editingItem.value = null
  parentNode.value = node
  Object.assign(itemForm, {
    parentPartId: node?.partId || header.value.rootPartId,
    childPartId: null,
    qty: 1,
    uom: 'EA',
    usageType: 'NORMAL',
    usageCondition: '',
    stationCode: '',
    alternateGroup: '',
    positionDesc: '',
    findNo: null
  })
  if (header.value.bomType === 'MBOM') {
    loadWorkstations()
  }
  itemDialog.value = true
}

function openEdit(node) {
  editingItem.value = node
  parentNode.value = null
  Object.assign(itemForm, {
    ...node,
    parentPartId: node.parentPartId,
    childPartId: node.partId || node.childPartId
  })
  partOptions.value = [{
    id: node.partId,
    partNo: node.partNo,
    partName: node.partName
  }]
  itemDialog.value = true
}

async function saveItem() {
  const payload = { ...itemForm }
  if (editingItem.value) {
    await boms.updateItem(
      route.params.id,
      editingItem.value.itemId,
      payload
    )
  } else {
    await boms.addItem(route.params.id, payload)
  }
  itemDialog.value = false
  ElMessage.success('BOM 行已保存')
  await loadTree()
}

async function removeItem(node) {
  await ElMessageBox.confirm(
    `确定删除 ${node.partNo} 吗？`,
    '删除确认',
    { type: 'warning' }
  )
  await boms.deleteItem(route.params.id, node.itemId)
  ElMessage.success('BOM 行已删除')
  await loadTree()
}

async function uploadCsv({ file }) {
  await boms.importCsv(route.params.id, file)
  ElMessage.success('CSV 导入成功')
  await loadTree()
}

async function downloadExport() {
  const blob = await boms.exportCsv(route.params.id)
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `${header.value.bomNo || 'bom'}.csv`
  anchor.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadHeader()
  await loadTree()
})
</script>

<style scoped>
.page {
  min-width: 1100px;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.page-title > div:first-child {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title h2 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 8px;
}

.mt {
  margin-top: 16px;
}

.metrics {
  display: flex;
  gap: 64px;
  margin-bottom: 24px;
}

.upload {
  display: inline-block;
  margin: 20px 12px 0 0;
}
</style>
