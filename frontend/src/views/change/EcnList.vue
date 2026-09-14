<template>
  <div class="page">
    <div class="page-title">
      <h2>ECN 工程变更通知</h2>
    </div>
    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="status" clearable>
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已实施" value="IMPLEMENTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="filteredRows" stripe>
        <el-table-column prop="ecnNo" label="ECN 编号" width="170" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="bomNo" label="目标 BOM" width="170" />
        <el-table-column prop="effectiveType" label="生效方式" width="110" />
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="router.push(`/ecns/${row.id}`)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { changes } from '../../api'

const router = useRouter()
const rows = ref([])
const status = ref('')

const filteredRows = computed(() => rows.value.filter(
  (row) => !status.value || row.status === status.value
))

onMounted(async () => {
  rows.value = await changes.ecns()
})
</script>
