<template>
  <div class="page">
    <div class="page-title">
      <h2>ECR 工程变更申请</h2>
      <el-button
        v-if="canWrite()"
        type="primary"
        @click="openCreate"
      >
        新建 ECR
      </el-button>
    </div>

    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="status" clearable @change="load">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-table :data="filteredRows" stripe>
        <el-table-column prop="ecrNo" label="ECR 编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="reason" label="原因" width="100" />
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="requester" label="申请人" width="110" />
        <el-table-column label="操作" width="360">
          <template #default="{ row }">
            <el-button
              v-if="canWrite() && row.status === 'DRAFT'"
              link
              @click="act(row, 'submit')"
            >
              提交
            </el-button>
            <el-button
              v-if="canWrite() && row.status === 'SUBMITTED'"
              link
              @click="act(row, 'approve')"
            >
              审批
            </el-button>
            <el-button
              v-if="canWrite() && row.status === 'SUBMITTED'"
              link
              type="danger"
              @click="reject(row)"
            >
              驳回
            </el-button>
            <el-button
              v-if="canWrite() && row.status === 'APPROVED'"
              link
              @click="act(row, 'to-ecn')"
            >
              生成 ECN
            </el-button>
            <el-button
              v-if="canWrite() && row.status !== 'CLOSED'"
              link
              @click="act(row, 'close')"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="新建 ECR" width="600px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="变更原因">
          <el-select v-model="form.reason">
            <el-option label="设计" value="DESIGN" />
            <el-option label="质量" value="QUALITY" />
            <el-option label="成本" value="COST" />
            <el-option label="供应" value="SUPPLY" />
            <el-option label="法规" value="REGULATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority">
            <el-option label="低" value="LOW" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="车型 ID">
          <el-input v-model="form.vehicleModelId" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button type="primary" @click="create">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { changes } from '../../api'
import { canWrite } from '../../auth'

const router = useRouter()
const rows = ref([])
const status = ref('')
const visible = ref(false)
const form = reactive({
  title: '',
  reason: 'DESIGN',
  priority: 'NORMAL',
  vehicleModelId: null,
  description: '',
  affectedPartIds: '[]'
})

const filteredRows = computed(() => rows.value.filter(
  (row) => !status.value || row.status === status.value
))

async function load() {
  rows.value = await changes.ecrs()
}

function openCreate() {
  Object.assign(form, {
    title: '',
    reason: 'DESIGN',
    priority: 'NORMAL',
    vehicleModelId: null,
    description: '',
    affectedPartIds: '[]'
  })
  visible.value = true
}

async function create() {
  await changes.createEcr(form)
  visible.value = false
  ElMessage.success('ECR 已创建')
  await load()
}

async function act(row, action) {
  const result = await changes.ecrAction(row.id, action)
  ElMessage.success('操作成功')
  if (action === 'to-ecn' && result?.id) {
    await router.push(`/ecns/${result.id}`)
    return
  }
  await load()
}

async function reject(row) {
  const reason = await ElMessageBox.prompt(
    '请输入驳回原因',
    '驳回 ECR',
    { inputPattern: /\S+/, inputErrorMessage: '驳回原因不能为空' }
  )
  await changes.ecrAction(row.id, 'reject', {
    rejectReason: reason.value
  })
  ElMessage.success('ECR 已驳回')
  await load()
}

onMounted(load)
</script>
