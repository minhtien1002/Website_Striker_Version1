<template>
  <ThuocTinhForm :config="THUOC_TINH.mauSac" />
</template>

<script setup>
import ThuocTinhForm from '@/components/thuocTinh/ThuocTinhForm.vue'
import { THUOC_TINH } from '@/components/thuocTinh/thuocTinhConfig.js'
</script>
