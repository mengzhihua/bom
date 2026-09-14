<script setup>
import EntityList from './EntityList.vue'
import { master } from '../../api'

const columns = [
  { prop: 'supplierCode', label: '供应商编码' },
  { prop: 'name', label: '名称' },
  { prop: 'sapVendor', label: 'SAP 供应商' },
  { prop: 'status', label: '状态' }
]
</script>

<template>
  <EntityList
    title="供应商管理"
    :columns="columns"
    :load="master.suppliers"
    :create="master.supplier"
  />
</template>
