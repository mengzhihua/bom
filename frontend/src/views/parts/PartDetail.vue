<template>
  <div class="page">
    <div class="page-title">
      <div>
        <el-button link @click="router.back()">
          返回
        </el-button>
        <h2>零件详情 {{ part.partNo }}</h2>
      </div>
    </div>

    <el-card shadow="never">
      <el-descriptions :column="3" border>
        <el-descriptions-item
          v-for="key in detailKeys"
          :key="key"
          :label="labels[key]"
        >
          {{ part[key] || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="mt">
      <template #header>
        <div class="card-header">
          <span>版本历史</span>
        </div>
      </template>
      <el-table :data="revisions" border>
        <el-table-column prop="revision" label="版本" />
        <el-table-column prop="changeNo" label="变更号" />
        <el-table-column prop="releasedBy" label="发布人" />
        <el-table-column prop="releasedAt" label="发布时间" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt">
      <template #header>
        <div class="card-header">
          <span>Where-used 反查</span>
          <el-switch
            v-model="recursive"
            active-text="递归"
            @change="loadWhere"
          />
        </div>
      </template>
      <el-table :data="where" border>
        <el-table-column prop="bomNo" label="BOM 号" />
        <el-table-column prop="bomType" label="类型" />
        <el-table-column prop="parentPartNo" label="父件号" />
        <el-table-column prop="parentPartName" label="父件名称" />
        <el-table-column prop="rootPartNo" label="顶层件" />
        <el-table-column prop="qty" label="数量" />
        <el-table-column prop="level" label="层级" />
        <el-table-column prop="path" label="层级路径" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt">
      <template #header>
        文档清单
      </template>
      <el-table :data="docs" border>
        <el-table-column prop="docType" label="类型" />
        <el-table-column prop="docNo" label="文档号" />
        <el-table-column prop="version" label="版本" />
        <el-table-column prop="url" label="链接" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { parts } from '../../api'

const route = useRoute()
const router = useRouter()
const part = ref({})
const revisions = ref([])
const where = ref([])
const docs = ref([])
const recursive = ref(true)
const labels = {
  partNo: '零件号',
  revision: '版本',
  partName: '名称',
  partType: '类型',
  category: '类别',
  uom: '单位',
  material: '材质',
  weightKg: '重量',
  makeBuy: '自制/外购',
  lifecycle: '生命周期',
  unitCost: '成本',
  sapMaterial: 'SAP 物料号'
}

const detailKeys = computed(() => Object.keys(labels))

async function loadWhere() {
  where.value = await parts.whereUsed(route.params.id, recursive.value)
}

onMounted(async () => {
  const detail = await parts.get(route.params.id)
  part.value = detail.part || detail
  revisions.value = detail.revisions || []
  docs.value = await parts.documents(route.params.id)
  await loadWhere()
})
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title h2 {
  margin: 0;
}

.mt {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
