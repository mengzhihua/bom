<script setup>
import EntityList from './EntityList.vue'
import { master } from '../../api'
</script>
<template><EntityList title="工位管理" :columns="[{prop:'plantId',label:'工厂'},{prop:'lineCode',label:'产线'},{prop:'stationCode',label:'工位编码'},{prop:'stationName',label:'工位名称'},{prop:'seq',label:'顺序'},{prop:'takt',label:'节拍(秒)'}]" :load="master.workstations" :create="master.workstation" /></template>
