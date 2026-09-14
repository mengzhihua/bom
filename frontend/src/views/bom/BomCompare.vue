<template>
  <div class="page">
    <div class="page-title">
      <h2>BOM 版本对比</h2>
    </div>
    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="左侧 BOM">
          <el-select v-model="leftId" filterable>
            <el-option
              v-for="bom in rows"
              :key="bom.id"
              :label="bom.bomNo"
              :value="bom.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="右侧 BOM">
          <el-select v-model="rightId" filterable>
            <el-option
              v-for="bom in rows"
              :key="bom.id"
              :label="bom.bomNo"
              :value="bom.id"
            />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="loadDiff">
          开始对比
        </el-button>
      </el-form>

      <el-table :data="diff" border>
        <el-table-column label="差异" width="110">
          <template #default="{ row }">
            <el-tag :type="tagType(row.type)">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="key" label="父件-子件" width="220" />
        <el-table-column label="变更前">
          <template #default="{ row }">
            <pre>{{ formatValue(row.left) }}</pre>
          </template>
        </el-table-column>
        <el-table-column label="变更后">
          <template #default="{ row }">
            <pre>{{ formatValue(row.right) }}</pre>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { boms } from '../../api'

const route = useRoute()
const rows = ref([])
const leftId = ref(Number(route.query.leftId) || null)
const rightId = ref(Number(route.query.rightId) || null)
const diff = ref([])

function tagType(type) {
  if (type === 'ADDED') {
    return 'success'
  }
  if (type === 'REMOVED') {
    return 'danger'
  }
  return 'warning'
}

function formatValue(value) {
  return value ? JSON.stringify(value, null, 2) : '-'
}

async function loadDiff() {
  if (leftId.value && rightId.value) {
    diff.value = await boms.compare(leftId.value, rightId.value)
  }
}

onMounted(async () => {
  rows.value = await boms.list()
  if (!leftId.value && rows.value.length > 1) {
    leftId.value = rows.value[0].id
    rightId.value = rows.value[1].id
  }
  await loadDiff()
})
</script>

<style scoped>
pre {
  margin: 0;
  white-space: pre-wrap;
}
</style>
