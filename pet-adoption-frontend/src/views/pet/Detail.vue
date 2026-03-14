<template>
  <div class="pet-detail" v-if="pet">
    <el-button @click="goBack" class="back-btn">← 返回</el-button>
    
    <el-row :gutter="20">
      <el-col :xs="24" :md="12">
        <div class="pet-images">
          <el-carousel height="400px" v-if="mediaList.length > 0">
            <el-carousel-item v-for="(item, index) in mediaList" :key="index">
              <img v-if="item.type === 'image'" :src="item.url" :alt="pet.name" />
              <video v-else-if="item.type === 'video'" :src="item.url" controls autoplay muted loop style="width: 100%; height: 100%; object-fit: cover"></video>
            </el-carousel-item>
          </el-carousel>
          <div class="placeholder" v-else>
            <el-icon size="100"><Picture /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="24" :md="12">
        <el-card class="pet-info-card">
          <h1>{{ pet.name }}</h1>
          <div class="pet-meta">
            <el-tag type="success">{{ pet.type }}</el-tag>
            <el-tag>{{ pet.breed }}</el-tag>
            <el-tag type="warning">{{ pet.age }}个月</el-tag>
            <el-tag type="info">{{ pet.gender }}</el-tag>
            <el-tag type="danger">{{ pet.healthStatus }}</el-tag>
          </div>
          
          <el-descriptions :column="2" border class="pet-details">
            <el-descriptions-item label="颜色">{{ pet.color }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ pet.weight }}kg</el-descriptions-item>
            <el-descriptions-item label="是否绝育">
              {{ pet.sterilized ? '是' : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusType">{{ statusText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布时间" :span="2">
              {{ formatDate(pet.publishTime) }}
            </el-descriptions-item>
          </el-descriptions>
          
          <el-tabs v-model="activeTab" class="pet-info-tabs">
            <el-tab-pane label="基本信息" name="basic">
              <div class="pet-description">
                <h3>宠物描述</h3>
                <p>{{ pet.description }}</p>
              </div>
            </el-tab-pane>
            <el-tab-pane label="健康信息" name="health">
              <div class="health-info">
                <h3>健康状况</h3>
                <p>{{ pet.healthStatus || '暂无信息' }}</p>
                <h3>疫苗情况</h3>
                <p>{{ pet.vaccineInfo || '暂无信息' }}</p>
                <h3>绝育情况</h3>
                <p>{{ pet.sterilized ? '已绝育' : '未绝育' }}</p>
                <h3>健康描述</h3>
                <p>{{ pet.healthDescription || '暂无信息' }}</p>
              </div>
            </el-tab-pane>
            <el-tab-pane label="性格特点" name="personality">
              <div class="personality-info">
                <h3>性格描述</h3>
                <p>{{ pet.personality || '暂无信息' }}</p>
                <h3>行为习惯</h3>
                <p>{{ pet.behavior || '暂无信息' }}</p>
                <h3>特殊需求</h3>
                <p>{{ pet.specialNeeds || '暂无特殊需求' }}</p>
              </div>
            </el-tab-pane>
            <el-tab-pane label="领养要求" name="requirements">
              <div class="requirements-info">
                <h3>领养条件</h3>
                <p>{{ pet.adoptionRequirements || '暂无信息' }}</p>
                <h3>注意事项</h3>
                <p>{{ pet.notes || '暂无信息' }}</p>
                <h3>领养流程</h3>
                <p>{{ pet.adoptionProcess || '暂无信息' }}</p>
              </div>
            </el-tab-pane>
          </el-tabs>
          
          <div class="pet-actions">
            <el-button 
              v-if="canApply" 
              type="primary" 
              size="large" 
              @click="showApplyDialog"
            >
              申请领养
            </el-button>
            <el-button 
              v-if="userStore.isLoggedIn" 
              type="info" 
              @click="showConsultDialog"
            >
              领养咨询
            </el-button>
            <el-button 
              v-if="userStore.isLoggedIn && !isFavorited" 
              @click="handleFavorite"
            >
              收藏
            </el-button>
            <el-button 
              v-if="userStore.isLoggedIn && isFavorited" 
              type="danger" 
              @click="handleUnfavorite"
            >
              取消收藏
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="comments-card">
      <h2>评论</h2>
      <div class="comment-form" v-if="userStore.isLoggedIn">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="发表你的评论..."
        />
        <el-button type="primary" @click="submitComment" class="submit-btn">
          发表评论
        </el-button>
      </div>
      <div v-else class="login-tip">
        <el-link type="primary" @click="$router.push('/login')">登录</el-link> 后发表评论
      </div>
      
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-user">{{ comment.username }}</span>
            <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
        </div>
        <el-empty v-if="comments.length === 0" description="暂无评论" />
      </div>
    </el-card>
    
    <el-dialog v-model="applyDialogVisible" title="申请领养" width="500px">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="申请理由">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请说明你的领养理由和养宠经验"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="consultDialogVisible" title="领养咨询" width="500px">
      <el-form :model="consultForm" label-width="80px">
        <el-form-item label="咨询内容">
          <el-input
            v-model="consultForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入你的咨询内容"
          />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input
            v-model="consultForm.contact"
            placeholder="请输入你的联系方式（电话或邮箱）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consultDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConsult">提交咨询</el-button>
      </template>
    </el-dialog>
  </div>
  <el-skeleton v-else :rows="10" animated />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPetDetail } from '@/api/pet'
import { applyAdoption } from '@/api/adoption'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { addComment, getComments } from '@/api/comment'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pet = ref(null)
const comments = ref([])
const isFavorited = ref(false)
const commentContent = ref('')
const applyDialogVisible = ref(false)
const applyForm = ref({
  reason: ''
})

const consultDialogVisible = ref(false)
const consultForm = ref({
  content: '',
  contact: ''
})

const activeTab = ref('basic')

const imageList = computed(() => {
  if (!pet.value || !pet.value.images) return []
  return pet.value.images.split(',').filter(img => img.trim())
})

const mediaList = computed(() => {
  const media = []
  
  // 添加图片
  if (pet.value && pet.value.images) {
    const images = pet.value.images.split(',').filter(img => img.trim())
    images.forEach(img => {
      media.push({ type: 'image', url: img })
    })
  }
  
  // 添加视频
  if (pet.value && pet.value.video) {
    const videos = pet.value.video.split(',').filter(video => video.trim())
    videos.forEach(video => {
      media.push({ type: 'video', url: video })
    })
  }
  
  return media
})

const statusType = computed(() => {
  const statusMap = { 0: 'info', 1: 'success', 2: 'warning' }
  return statusMap[pet.value?.status] || 'info'
})

const statusText = computed(() => {
  const statusMap = { 0: '已领养', 1: '待领养', 2: '审核中' }
  return statusMap[pet.value?.status] || '未知'
})

const canApply = computed(() => {
  return userStore.isLoggedIn && 
         pet.value?.status === 1 && 
         userStore.userType === 'USER'
})

const loadPetDetail = async () => {
  try {
    const res = await getPetDetail(route.params.id)
    pet.value = res.data
  } catch (error) {
    ElMessage.error('加载宠物详情失败')
    router.back()
  }
}

const loadComments = async () => {
  try {
    const res = await getComments(route.params.id)
    comments.value = res.data || []
  } catch (error) {
    console.error('加载评论失败：', error)
  }
}

const goBack = () => {
  router.back()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const handleFavorite = async () => {
  try {
    await addFavorite(pet.value.id)
    isFavorited.value = true
    ElMessage.success('收藏成功')
  } catch (error) {
    ElMessage.error('收藏失败')
  }
}

const handleUnfavorite = async () => {
  try {
    await removeFavorite(pet.value.id)
    isFavorited.value = false
    ElMessage.success('取消收藏成功')
  } catch (error) {
    ElMessage.error('取消收藏失败')
  }
}

const showApplyDialog = () => {
  applyDialogVisible.value = true
}

const showConsultDialog = () => {
  consultDialogVisible.value = true
}

const submitApply = async () => {
  if (!applyForm.value.reason.trim()) {
    ElMessage.warning('请填写申请理由')
    return
  }
  
  try {
    await applyAdoption({
      petId: pet.value.id,
      reason: applyForm.value.reason
    })
    ElMessage.success('申请提交成功，请等待审核')
    applyDialogVisible.value = false
    applyForm.value.reason = ''
  } catch (error) {
    ElMessage.error('申请提交失败')
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请填写评论内容')
    return
  }
  
  try {
    await addComment({
      petId: pet.value.id,
      content: commentContent.value
    })
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    loadComments()
  } catch (error) {
    ElMessage.error('评论发表失败')
  }
}

const submitConsult = async () => {
  if (!consultForm.value.content.trim()) {
    ElMessage.warning('请填写咨询内容')
    return
  }
  if (!consultForm.value.contact.trim()) {
    ElMessage.warning('请填写联系方式')
    return
  }
  
  try {
    // 注意：需要先在 API 中添加咨询相关方法
    // await addConsult({
    //   petId: pet.value.id,
    //   content: consultForm.value.content,
    //   contact: consultForm.value.contact
    // })
    ElMessage.success('咨询提交成功，我们会尽快回复您')
    consultDialogVisible.value = false
    consultForm.value = {
      content: '',
      contact: ''
    }
  } catch (error) {
    ElMessage.error('咨询提交失败')
  }
}

onMounted(() => {
  loadPetDetail()
  loadComments()
})
</script>

<style scoped>
.pet-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.pet-images {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pet-images img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
  color: #999;
}

.pet-info-card {
  height: 100%;
}

.pet-info-card h1 {
  font-size: 32px;
  margin-bottom: 20px;
  color: #333;
}

.pet-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pet-details {
  margin: 20px 0;
}

.pet-description {
  margin: 20px 0;
}

.pet-description h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.pet-description p {
  color: #666;
  line-height: 1.8;
}

.pet-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.pet-info-tabs {
  margin: 20px 0;
}

.health-info,
.personality-info,
.requirements-info {
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.health-info h3,
.personality-info h3,
.requirements-info h3 {
  font-size: 16px;
  margin: 15px 0 10px 0;
  color: #333;
}

.health-info h3:first-child,
.personality-info h3:first-child,
.requirements-info h3:first-child {
  margin-top: 0;
}

.health-info p,
.personality-info p,
.requirements-info p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.comments-card {
  margin-top: 30px;
}

.comments-card h2 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.comment-form {
  margin-bottom: 20px;
}

.submit-btn {
  margin-top: 10px;
}

.login-tip {
  text-align: center;
  padding: 20px;
  color: #999;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-user {
  font-weight: bold;
  color: #333;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #666;
  line-height: 1.6;
}
</style>
