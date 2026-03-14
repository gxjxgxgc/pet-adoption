<template>
  <div class="adoption-list" v-loading="loading">
    <div v-if="adoptions.length > 0">
      <el-card v-for="adoption in adoptions" :key="adoption.id" class="adoption-card">
        <div class="adoption-header">
          <div class="pet-info">
            <img :src="adoption.petImages?.split(',')[0]" :alt="adoption.petName">
            <div>
              <h3>{{ adoption.petName }}</h3>
              <p>{{ adoption.petType }} · {{ adoption.petBreed }}</p>
            </div>
          </div>
          <el-tag :type="getStatusType(adoption.status)">
            {{ getStatusText(adoption.status) }}
          </el-tag>
        </div>
        
        <div class="adoption-body">
          <div class="info-item">
            <span class="label">申请时间：</span>
            <span class="value">{{ formatDate(adoption.applyTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">申请理由：</span>
            <span class="value">{{ adoption.reason }}</span>
          </div>
          <div v-if="adoption.status === 2" class="info-item">
            <span class="label">拒绝原因：</span>
            <span class="value">{{ adoption.rejectReason }}</span>
          </div>
          <div v-if="adoption.reviewTime" class="info-item">
            <span class="label">审核时间：</span>
            <span class="value">{{ formatDate(adoption.reviewTime) }}</span>
          </div>
        </div>

        <div class="adoption-footer">
          <el-button 
            v-if="adoption.status === 0" 
            type="danger" 
            size="small"
            @click="handleCancel(adoption.id)"
          >
            取消申请
          </el-button>
          <el-button 
            type="primary" 
            size="small"
            @click="goToPetDetail(adoption.petId)"
          >
            查看宠物
          </el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无领养申请" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMyAdoptions } from '@/api/adoption'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  status: {
    type: Number,
    default: null
  }
})

const router = useRouter()
const loading = ref(false)
const adoptions = ref([])

const loadAdoptions = async () => {
  loading.value = true
  try {
    const params = {}
    if (props.status !== null) {
      params.status = props.status
    }
    const res = await getMyAdoptions(params)
    adoptions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载领养申请失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelAdoption(id)
    ElMessage.success('取消成功')
    loadAdoptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const goToPetDetail = (petId) => {
  router.push(`/pets/${petId}`)
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已取消'
  }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadAdoptions()
})

watch(() => props.status, () => {
  loadAdoptions()
})
</script>

<style scoped>
.adoption-list {
  min-height: 400px;
}

.adoption-card {
  margin-bottom: 20px;
}

.adoption-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pet-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.pet-info img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.pet-info h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.pet-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.adoption-body {
  padding: 15px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #666;
  margin-right: 10px;
  min-width: 80px;
}

.info-item .value {
  color: #333;
  flex: 1;
}

.adoption-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
