<script setup>
import EntityList from './EntityList.vue'
import { master } from '../../api'

const columns = [
  { prop: 'plantCode', label: '工厂编码' },
  { prop: 'plantName', label: '工厂名称' },
  { prop: 'sapPlant', label: 'SAP 工厂' },
  { prop: 'address', label: '地址' }
]
</script>

<template>
  <EntityList
    title="工厂管理"
    :columns="columns"
    :load="master.plants"
    :create="master.plant"
    :update="master.updatePlant"
  />
</template>
