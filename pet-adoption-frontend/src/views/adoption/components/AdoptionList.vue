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
          <el-button 
            type="info" 
            size="small"
            @click="viewStatusTimeline(adoption)"
          >
            查看状态
          </el-button>
          <el-button 
            type="success" 
            size="small"
            v-if="adoption.status === 1" 
            @click="handleAdoptionSuccess(adoption.id)"
          >
            确认领养
          </el-button>
          <el-button 
            type="warning" 
            size="small"
            v-if="adoption.status === 4" 
            @click="handleRateAdoption(adoption)"
          >
            评价
          </el-button>
        </div>

        <!-- 状态时间线对话框 -->
        <el-dialog
          v-model="timelineDialogVisible"
          :title="`${currentAdoption?.petName || ''} - 申请状态追踪`"
          width="600px"
        >
          <div v-if="currentAdoption" class="status-timeline">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="申请ID">{{ currentAdoption.id }}</el-descriptions-item>
              <el-descriptions-item label="宠物名称">{{ currentAdoption.petName }}</el-descriptions-item>
              <el-descriptions-item label="当前状态"><el-tag :type="getStatusType(currentAdoption.status)">{{ getStatusText(currentAdoption.status) }}</el-tag></el-descriptions-item>
            </el-descriptions>

            <h4 style="margin-top: 20px; margin-bottom: 10px">状态追踪</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in getStatusTimeline(currentAdoption)"
                :key="index"
                :timestamp="formatDate(item.time)"
                :type="item.type"
                :icon="item.icon"
              >
                <div class="timeline-content">
                  <div class="status">{{ item.status }}</div>
                  <div v-if="item.description" class="description">{{ item.description }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-dialog>

        <!-- 评价对话框 -->
        <el-dialog
          v-model="rateDialogVisible"
          title="评价领养体验"
          width="600px"
        >
          <div v-if="currentAdoption" class="rate-form">
            <el-form :model="rateForm" label-width="100px">
              <el-form-item label="宠物评分">
                <el-rate v-model="rateForm.rating" show-score text-color="#ff9900" />
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input
                  v-model="rateForm.comment"
                  type="textarea"
                  :rows="4"
                  placeholder="请分享您的领养体验..."
                />
              </el-form-item>
            </el-form>
          </div>
          <template #footer>
            <el-button @click="rateDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitRate">提交评价</el-button>
          </template>
        </el-dialog>
      </el-card>
    </div>
    <el-empty v-else description="暂无领养申请" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getMyAdoptions, cancelAdoption, confirmAdoption, rateAdoption } from '@/api/adoption'
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

const timelineDialogVisible = ref(false)
const currentAdoption = ref(null)
const rateDialogVisible = ref(false)
const rateForm = ref({
  rating: 5,
  comment: ''
})

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
  const types = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已取消', 4: '已完成' }
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

const viewStatusTimeline = (adoption) => {
  currentAdoption.value = adoption
  timelineDialogVisible.value = true
}

const getStatusTimeline = (adoption) => {
  const timeline = [
    {
      status: '提交申请',
      time: adoption.applyTime,
      type: 'primary',
      icon: 'Message',
      description: '已提交领养申请，等待审核'
    }
  ]

  if (adoption.status >= 1) {
    timeline.push({
      status: '审核通过',
      time: adoption.reviewTime || new Date().toISOString(),
      type: 'success',
      icon: 'Check',
      description: '申请已通过，进入面试安排阶段'
    })
  }

  if (adoption.status >= 2 && adoption.status !== 3) {
    timeline.push({
      status: '审核拒绝',
      time: adoption.reviewTime || new Date().toISOString(),
      type: 'danger',
      icon: 'Close',
      description: adoption.rejectReason || '申请未通过审核'
    })
  }

  if (adoption.status === 3) {
    timeline.push({
      status: '申请取消',
      time: new Date().toISOString(),
      type: 'info',
      icon: 'CloseCircle',
      description: '领养申请已取消'
    })
  }

  if (adoption.status === 4) {
    timeline.push({
      status: '面试安排',
      time: new Date(Date.now() - 345600000).toISOString(), // 4天前
      type: 'warning',
      icon: 'VideoCamera',
      description: '审核通过，已安排面试'
    })
    timeline.push({
      status: '面试完成',
      time: new Date(Date.now() - 259200000).toISOString(), // 3天前
      type: 'info',
      icon: 'CheckCircle',
      description: '面试已完成，确认领养'
    })
    timeline.push({
      status: '签约领养',
      time: new Date(Date.now() - 172800000).toISOString(), // 2天前
      type: 'success',
      icon: 'Document',
      description: '已完成签约，正式领养'
    })
    timeline.push({
      status: '领养完成',
      time: new Date(Date.now() - 86400000).toISOString(), // 1天前
      type: 'success',
      icon: 'Star',
      description: '领养流程已完成，感谢您的爱心'
    })
  }

  // 模拟后续状态
  if (adoption.status === 1) {
    timeline.push({
      status: '面试安排',
      time: new Date(Date.now() + 86400000).toISOString(), // 1天后
      type: 'warning',
      icon: 'VideoCamera',
      description: '审核通过，将安排面试'
    })
    timeline.push({
      status: '面试完成',
      time: new Date(Date.now() + 172800000).toISOString(), // 2天后
      type: 'info',
      icon: 'CheckCircle',
      description: '面试已完成，等待最终确认'
    })
    timeline.push({
      status: '签约领养',
      time: new Date(Date.now() + 259200000).toISOString(), // 3天后
      type: 'success',
      icon: 'Document',
      description: '已完成签约，正式领养'
    })
    timeline.push({
      status: '领养完成',
      time: new Date(Date.now() + 345600000).toISOString(), // 4天后
      type: 'success',
      icon: 'Star',
      description: '领养流程已完成，感谢您的爱心'
    })
  }

  return timeline
}

const handleAdoptionSuccess = async (id) => {
  try {
    await ElMessageBox.confirm('确定要确认领养这个宠物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await confirmAdoption(id)
    ElMessage.success('确认领养成功')
    loadAdoptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认领养失败')
    }
  }
}

const handleRateAdoption = (adoption) => {
  currentAdoption.value = adoption
  rateForm.value = {
    rating: 5,
    comment: ''
  }
  rateDialogVisible.value = true
}

const submitRate = async () => {
  try {
    await rateAdoption(currentAdoption.value.id, rateForm.value)
    ElMessage.success('评价提交成功')
    rateDialogVisible.value = false
    loadAdoptions()
  } catch (error) {
    ElMessage.error('评价提交失败')
  }
}
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

.status-timeline {
  line-height: 1.6;
}

.timeline-content {
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.timeline-content .status {
  font-weight: bold;
  margin-bottom: 5px;
}

.timeline-content .description {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}
</style>
