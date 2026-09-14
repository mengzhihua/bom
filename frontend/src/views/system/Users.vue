<template>
  <div class="page">
    <div class="page-title">
      <h2>用户管理</h2>
      <el-button
        v-if="canWrite('system')"
        type="primary"
        @click="open()"
      >
        新增用户
      </el-button>
    </div>
    <el-card shadow="never">
      <el-table :data="rows">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="supplierCode" label="供应商" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button
              v-if="canWrite('system')"
              link
              @click="open(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="visible" title="用户">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option
              v-for="role in roles"
              :key="role"
              :label="role"
              :value="role"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="save">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { system } from '../../api'
import { canWrite } from '../../auth'

const rows = ref([])
const visible = ref(false)
const form = reactive({})
const roles = ['ADMIN', 'ENGINEER', 'PLANNER', 'VIEWER']

async function load() {
  const result = await system.users.list({
    current: 1,
    size: 50
  })
  rows.value = result.records || []
}

function open(row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row || {
    role: 'VIEWER',
    status: 1
  })
  visible.value = true
}

async function save() {
  if (form.id) {
    await system.users.update(form.id, form)
  } else {
    await system.users.create(form)
  }
  visible.value = false
  ElMessage.success('保存成功')
  await load()
}

onMounted(load)
</script>
