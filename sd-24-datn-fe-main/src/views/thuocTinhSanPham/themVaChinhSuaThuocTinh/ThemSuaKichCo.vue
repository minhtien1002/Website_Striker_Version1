<template>
  <ThuocTinhForm :config="THUOC_TINH.kichCo" />
</template>

<script setup>
import ThuocTinhForm from '@/components/thuocTinh/ThuocTinhForm.vue'
import { THUOC_TINH } from '@/components/thuocTinh/thuocTinhConfig.js'
</script>
