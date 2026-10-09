<template>
  <ThuocTinhForm :config="THUOC_TINH.chatLieu" />
</template>

<script setup>
import ThuocTinhForm from '@/components/thuocTinh/ThuocTinhForm.vue'
import { THUOC_TINH } from '@/components/thuocTinh/thuocTinhConfig.js'
</script>
