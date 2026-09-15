<script setup>
import EntityList from './EntityList.vue'
import { master } from '../../api'

const columns = [
  { prop: 'plantId', label: '工厂' },
  { prop: 'lineCode', label: '产线' },
  { prop: 'stationCode', label: '工位编码' },
  { prop: 'stationName', label: '工位名称' },
  { prop: 'seq', label: '顺序' },
  { prop: 'takt', label: '节拍(秒)' }
]
</script>

<template>
  <EntityList
    title="工位管理"
    :columns="columns"
    :load="master.workstations"
    :create="master.workstation"
    :update="master.updateWorkstation"
    module="workstations"
  />
</template>
