<template>
  <el-container class="app-shell">
    <el-aside class="sidebar" width="228px">
      <div class="brand">
        <el-icon>
          <Van />
        </el-icon>
        <span>BOM 零部件管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item
          v-for="menu in sideMenus"
          :key="menu.path"
          :index="menu.path"
        >
          <el-icon>
            <component :is="menu.icon" />
          </el-icon>
          <span>{{ menu.name }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="topbar">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item
            v-for="crumb in crumbs"
            :key="crumb"
          >
            {{ crumb }}
          </el-breadcrumb-item>
        </el-breadcrumb>

        <el-dropdown @command="onCommand">
          <span class="account">
            <el-icon>
              <User />
            </el-icon>
            <span>{{ auth.user?.realName || auth.user?.username }}</span>
            <el-tag size="small" type="info">
              {{ ROLE_LABEL[auth.user?.role] || auth.user?.role }}
            </el-tag>
            <el-icon>
              <ArrowDown />
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">
                修改密码
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog
    v-model="pwdVisible"
    title="修改密码"
    width="420px"
    destroy-on-close
  >
    <el-form
      ref="pwdRef"
      :model="pwd"
      :rules="pwdRules"
      label-width="90px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="pwd.oldPassword"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="pwd.newPassword"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirm">
        <el-input
          v-model="pwd.confirm"
          type="password"
          show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdVisible = false">
        取消
      </el-button>
      <el-button
        type="primary"
        :loading="pwdSaving"
        @click="changePassword"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { auth, clearAuth, ROLE_LABEL } from '../auth'
import { menus, visibleMenus } from '../router'

const route = useRoute()
const router = useRouter()
const sideMenus = computed(() => visibleMenus())
const pwdVisible = ref(false)
const pwdSaving = ref(false)
const pwdRef = ref()
const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirm: ''
})
const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, min: 6, message: '新密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    {
      validator: (_, value, callback) => {
        callback(
          value === pwd.newPassword
            ? undefined
            : new Error('两次输入不一致')
        )
      },
      trigger: 'blur'
    }
  ]
}

const crumbs = computed(() => {
  const menu = menus.find((item) => item.path === route.path)
  return [route.meta.menu || menu?.name || route.name].filter(Boolean)
})

async function onCommand(command) {
  if (command === 'password') {
    Object.assign(pwd, {
      oldPassword: '',
      newPassword: '',
      confirm: ''
    })
    pwdVisible.value = true
    return
  }
  try {
    await authApi.logout()
  } catch (error) {
    // Expired sessions are still cleared locally.
  }
  clearAuth()
  router.replace('/login')
}

async function changePassword() {
  await pwdRef.value.validate()
  pwdSaving.value = true
  try {
    await authApi.changePassword({
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    ElMessage.success('密码已修改，请重新登录')
    clearAuth()
    router.replace('/login')
  } finally {
    pwdSaving.value = false
  }
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.sidebar {
  background: #1f2d3d;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 56px;
  padding: 0 16px;
  color: #ffffff;
  font-size: 17px;
  font-weight: 600;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.account {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  cursor: pointer;
}

.content {
  padding: 20px;
  overflow: auto;
  background: #f5f7fa;
}
</style>
