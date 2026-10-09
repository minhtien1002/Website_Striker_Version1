<template>
  <ThuocTinhManager :config="THUOC_TINH.chatLieu" />
</template>

<script setup>
import ThuocTinhManager from '@/components/thuocTinh/ThuocTinhManager.vue'
import { THUOC_TINH } from '@/components/thuocTinh/thuocTinhConfig.js'
</script>
