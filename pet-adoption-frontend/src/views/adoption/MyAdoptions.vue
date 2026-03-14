<template>
  <div class="my-adoptions">
    <h2>📋 我的领养申请</h2>
    
    <el-tabs v-model="activeTab">
      <el-tab-pane label="全部" name="all">
        <adoption-list :status="null" />
      </el-tab-pane>
      <el-tab-pane label="待审核" name="pending">
        <adoption-list :status="0" />
      </el-tab-pane>
      <el-tab-pane label="已通过" name="approved">
        <adoption-list :status="1" />
      </el-tab-pane>
      <el-tab-pane label="已拒绝" name="rejected">
        <adoption-list :status="2" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AdoptionList from './components/AdoptionList.vue'

const activeTab = ref('all')
</script>

<style scoped>
.my-adoptions {
  max-width: 1200px;
  margin: 0 auto;
}

.my-adoptions h2 {
  margin-bottom: 30px;
  color: #333;
}
</style>
