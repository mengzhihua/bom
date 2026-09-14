<script setup>
import EntityList from './EntityList.vue'
import { master } from '../../api'

const columns = [
  { prop: 'modelCode', label: '车型编码' },
  { prop: 'modelName', label: '车型名称' },
  { prop: 'platform', label: '平台' },
  { prop: 'program', label: '阶段' },
  { prop: 'status', label: '状态' }
]
</script>

<template>
  <EntityList
    title="车型项目"
    :columns="columns"
    :load="master.models"
    :create="master.model"
  />
</template>
