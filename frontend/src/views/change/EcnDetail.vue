<template>
  <div class="page">
    <div class="page-title">
      <div>
        <el-button link @click="router.back()">
          返回
        </el-button>
        <h2>{{ ecn.ecnNo }} {{ ecn.title }}</h2>
      </div>
    </div>

    <el-card shadow="never">
      <el-descriptions :column="4" border>
        <el-descriptions-item label="状态">
          {{ ecn.status }}
        </el-descriptions-item>
        <el-descriptions-item label="目标 BOM">
          {{ ecn.bomNo || ecn.bomId }}
        </el-descriptions-item>
        <el-descriptions-item label="生效方式">
          {{ ecn.effectiveType }}
        </el-descriptions-item>
        <el-descriptions-item label="生效日期">
          {{ ecn.effectiveDate || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="生效 VIN">
          {{ ecn.effectiveVin || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button
          v-if="canWrite() && ecn.status === 'DRAFT'"
          @click="act('submit')"
        >
          提交
        </el-button>
        <el-button
          v-if="canWrite() && ecn.status === 'SUBMITTED'"
          @click="act('approve')"
        >
          审批
        </el-button>
        <el-button
          v-if="canWrite() && ecn.status === 'APPROVED'"
          type="primary"
          @click="act('implement')"
        >
          实施
        </el-button>
        <el-button
          v-if="ecn.implementedBomId"
          type="success"
          @click="router.push(`/boms/${ecn.implementedBomId}`)"
        >
          查看新版本 BOM
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="mt">
      <template #header>
        <div class="card-header">
          <span>ECN 变更明细</span>
          <el-button
            v-if="canWrite() && ecn.status === 'DRAFT'"
            type="primary"
            @click="openItem"
          >
            添加明细
          </el-button>
        </div>
      </template>
      <el-table :data="items" border>
        <el-table-column prop="action" label="动作" width="100" />
        <el-table-column prop="parentPartNo" label="父件" />
        <el-table-column prop="oldChildPartNo" label="旧子件" />
        <el-table-column prop="newChildPartNo" label="新子件" />
        <el-table-column prop="oldQty" label="旧数量" />
        <el-table-column prop="newQty" label="新数量" />
        <el-table-column prop="findNo" label="序号" />
        <el-table-column prop="usageCondition" label="配置条件" />
        <el-table-column prop="stationCode" label="工位" />
      </el-table>
    </el-card>

    <el-dialog v-model="itemVisible" title="添加 ECN 明细" width="620px">
      <el-form :model="itemForm" label-width="110px">
        <el-form-item label="动作">
          <el-select v-model="itemForm.action">
            <el-option label="新增" value="ADD" />
            <el-option label="删除" value="REMOVE" />
            <el-option label="修改" value="MODIFY" />
            <el-option label="替换" value="REPLACE" />
          </el-select>
        </el-form-item>
        <el-form-item label="父件 ID">
          <el-input v-model="itemForm.parentPartId" />
        </el-form-item>
        <el-form-item label="旧子件 ID">
          <el-input v-model="itemForm.oldChildPartId" />
        </el-form-item>
        <el-form-item label="新子件 ID">
          <el-input v-model="itemForm.newChildPartId" />
        </el-form-item>
        <el-form-item label="新数量">
          <el-input-number v-model="itemForm.newQty" :min="0" />
        </el-form-item>
        <el-form-item label="配置条件">
          <el-input v-model="itemForm.usageCondition" />
        </el-form-item>
        <el-form-item label="序号">
          <el-input-number v-model="itemForm.findNo" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemVisible = false">
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { changes } from '../../api'
import { canWrite } from '../../auth'

const route = useRoute()
const router = useRouter()
const ecn = ref({})
const items = ref([])
const itemVisible = ref(false)
const itemForm = reactive({
  action: 'REPLACE',
  parentPartId: null,
  oldChildPartId: null,
  newChildPartId: null,
  oldQty: null,
  newQty: 1,
  findNo: 10,
  usageCondition: '',
  stationCode: ''
})

async function load() {
  ecn.value = await changes.ecn(route.params.id)
  items.value = await changes.ecnItems(route.params.id)
}

async function act(action) {
  await changes.ecnAction(route.params.id, action)
  ElMessage.success('操作成功')
  await load()
}

function openItem() {
  Object.assign(itemForm, {
    action: 'REPLACE',
    parentPartId: null,
    oldChildPartId: null,
    newChildPartId: null,
    oldQty: null,
    newQty: 1,
    findNo: 10,
    usageCondition: '',
    stationCode: ''
  })
  itemVisible.value = true
}

async function saveItem() {
  await changes.addEcnItem(route.params.id, itemForm)
  itemVisible.value = false
  ElMessage.success('明细已添加')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.page-title > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title h2 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 8px;
  margin-top: 18px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mt {
  margin-top: 16px;
}
</style>
