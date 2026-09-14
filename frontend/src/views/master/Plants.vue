<script setup>
import EntityList from './EntityList.vue'
import { master } from '../../api'
</script>
<template><EntityList title="工厂管理" :columns="[{prop:'plantCode',label:'工厂编码'},{prop:'plantName',label:'工厂名称'},{prop:'sapPlant',label:'SAP 工厂'},{prop:'address',label:'地址'}]" :load="master.plants" :create="master.plant" /></template>
