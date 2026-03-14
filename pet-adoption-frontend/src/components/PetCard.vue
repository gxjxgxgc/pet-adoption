<template>
  <el-card class="pet-card" :body-style="{ padding: '0px' }">
    <div class="pet-image" @click="goToDetail">
      <img :src="getImageUrl()" :alt="pet.name">
    </div>
    <div class="pet-info">
      <div class="pet-header">
        <h3>{{ pet.name }}</h3>
        <el-tag size="small" :type="getStatusType()">{{ getStatusText() }}</el-tag>
      </div>
      <p class="pet-type">{{ pet.type }} · {{ pet.breed }}</p>
      <p class="pet-desc">{{ pet.description }}</p>
      <div class="pet-details">
        <div class="detail-item">
          <span class="detail-label">性别：</span>
          <span class="detail-value">{{ getGenderText() }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">年龄：</span>
          <span class="detail-value">{{ pet.age }}个月</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">体重：</span>
          <span class="detail-value">{{ pet.weight || '未知' }}kg</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">颜色：</span>
          <span class="detail-value">{{ pet.color || '未知' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">绝育：</span>
          <span class="detail-value">{{ pet.sterilized ? '是' : '否' }}</span>
        </div>
      </div>
      <div class="pet-tags">
        <el-tag size="small" type="info">{{ getHealthStatusText() }}</el-tag>
        <el-tag v-if="pet.vaccineInfo" size="small" type="success">已疫苗</el-tag>
      </div>
      <div class="pet-actions">
        <el-button type="primary" size="small" @click="goToDetail">查看详情</el-button>
        <el-button 
          v-if="userStore.isLoggedIn" 
          :type="isFavorited ? 'danger' : 'default'" 
          size="small" 
          @click="toggleFavorite"
        >
          {{ isFavorited ? '已收藏' : '收藏' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'

const props = defineProps({
  pet: {
    type: Object,
    required: true
  },
  isFavorited: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['favorite-change'])

const router = useRouter()
const userStore = useUserStore()

const getImageUrl = () => {
  if (!props.pet.images) {
    return '/placeholder.png'
  }
  const images = props.pet.images.split(',')
  return images[0] || '/placeholder.png'
}

const getGenderText = () => {
  const genderMap = {
    0: '未知',
    1: '公',
    2: '母'
  }
  return genderMap[props.pet.gender] || '未知'
}

const getHealthStatusText = () => {
  const healthMap = {
    'healthy': '健康',
    'vaccinated': '已疫苗',
    'sterilized': '已绝育'
  }
  return healthMap[props.pet.healthStatus] || props.pet.healthStatus
}

const getStatusType = () => {
  const statusMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return statusMap[props.pet.status] || 'info'
}

const getStatusText = () => {
  const statusMap = {
    0: '已领养',
    1: '待领养',
    2: '审核中'
  }
  return statusMap[props.pet.status] || '未知'
}

const goToDetail = () => {
  router.push(`/pets/${props.pet.id}`)
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (props.isFavorited) {
      await removeFavorite(props.pet.id)
      ElMessage.success('取消收藏成功')
    } else {
      await addFavorite(props.pet.id)
      ElMessage.success('收藏成功')
    }
    emit('favorite-change', props.pet.id)
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.pet-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.pet-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.pet-image {
  height: 200px;
  overflow: hidden;
}

.pet-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-info {
  padding: 15px;
}

.pet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.pet-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.pet-details {
  margin: 10px 0;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 6px;
  font-size: 12px;
}

.detail-item {
  display: flex;
  margin-bottom: 5px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-weight: bold;
  color: #666;
  width: 50px;
}

.detail-value {
  color: #999;
  flex: 1;
}

.pet-type {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.pet-desc {
  color: #999;
  font-size: 13px;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pet-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.pet-actions {
  display: flex;
  gap: 10px;
}

.pet-actions .el-button {
  flex: 1;
}
</style>