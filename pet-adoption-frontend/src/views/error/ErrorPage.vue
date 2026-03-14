<template>
  <div class="error-page">
    <div class="error-container">
      <div class="error-code">{{ errorCode }}</div>
      <div class="error-message">{{ errorMessage }}</div>
      <div class="error-description">{{ errorDescription }}</div>
      <el-button type="primary" @click="goHome" class="home-button">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  code: {
    type: Number,
    default: 404
  },
  message: {
    type: String,
    default: ''
  }
})

const router = useRouter()

const errorCode = computed(() => props.code)

const errorMessage = computed(() => {
  if (props.message) {
    return props.message
  }
  
  switch (props.code) {
    case 404:
      return '页面不存在'
    case 403:
      return '权限不足'
    case 500:
      return '服务器内部错误'
    case 401:
      return '未授权'
    default:
      return '未知错误'
  }
})

const errorDescription = computed(() => {
  switch (props.code) {
    case 404:
      return '您访问的页面不存在或已被移除'
    case 403:
      return '您没有权限访问此页面'
    case 500:
      return '服务器内部出现错误，请稍后重试'
    case 401:
      return '请先登录后再访问'
    default:
      return '出现了一些问题，请稍后重试'
  }
})

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.error-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}

.error-container {
  text-align: center;
  padding: 60px 40px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 20px;
  line-height: 1;
}

.error-message {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 40px;
  line-height: 1.5;
}

.home-button {
  font-size: 16px;
  padding: 10px 30px;
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-message {
    font-size: 20px;
  }
  
  .error-description {
    font-size: 14px;
  }
  
  .error-container {
    padding: 40px 20px;
    margin: 0 20px;
  }
}
</style>
