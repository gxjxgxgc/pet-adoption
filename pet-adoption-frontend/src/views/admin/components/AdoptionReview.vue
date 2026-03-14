<template>
  <div class="adoption-review" v-loading="loading">
    <div class="review-header">
      <el-button 
        type="success" 
        @click="handleBatchApprove" 
        :disabled="pendingAdoptions.length === 0"
      >
        批量通过 ({{ pendingAdoptions.length }})
      </el-button>
      <el-button 
        type="danger" 
        @click="handleBatchReject" 
        :disabled="pendingAdoptions.length === 0"
        style="margin-left: 10px"
      >
        批量拒绝 ({{ pendingAdoptions.length }})
      </el-button>
    </div>
    
    <el-table 
      :data="adoptions" 
      stripe 
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" :selectable="selectable" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="宠物信息" width="200">
        <template #default="{ row }">
          <div class="pet-cell">
            <img :src="row.petImages?.split(',')[0]" :alt="row.petName">
            <div>
              <div>{{ row.petName }}</div>
              <div class="text-small">{{ row.petType }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="申请人" width="120" />
      <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
      <el-table-column prop="applyTime" label="申请时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button type="success" size="small" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">
              拒绝
            </el-button>
          </template>
          <el-button type="info" size="small" @click="viewHistory(row)" style="margin-left: 5px">
            查看历史
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="loadAdoptions"
      style="margin-top: 20px; justify-content: center"
    />

    <!-- 审核历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="审核历史记录"
      width="600px"
    >
      <div v-if="currentAdoption" class="adoption-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请ID">{{ currentAdoption.id }}</el-descriptions-item>
          <el-descriptions-item label="宠物名称">{{ currentAdoption.petName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentAdoption.username }}</el-descriptions-item>
          <el-descriptions-item label="当前状态"><el-tag :type="getStatusType(currentAdoption.status)">{{ getStatusText(currentAdoption.status) }}</el-tag></el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 20px; margin-bottom: 10px">审核历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in currentAdoption.history"
            :key="index"
            :timestamp="formatDate(item.time)"
            :type="getHistoryType(item.action)"
          >
            <div class="timeline-content">
              <div class="action">{{ getHistoryActionText(item.action) }}</div>
              <div v-if="item.comment" class="comment">{{ item.comment }}</div>
              <div class="operator">操作人: {{ item.operator || '系统' }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPendingAdoptions, reviewAdoption } from '@/api/adoption'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const adoptions = ref([])
const selectedAdoptions = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const historyDialogVisible = ref(false)
const currentAdoption = ref(null)

const pendingAdoptions = ref([])

const selectable = (row) => {
  return row.status === 0
}

const handleSelectionChange = (selection) => {
  selectedAdoptions.value = selection
  pendingAdoptions.value = selection.filter(item => item.status === 0)
}

const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要通过选中的 ${pendingAdoptions.value.length} 个申请吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    for (const adoption of pendingAdoptions.value) {
      await reviewAdoption(adoption.id, { status: 1 })
    }
    
    ElMessage.success('批量审核通过成功')
    selectedAdoptions.value = []
    pendingAdoptions.value = []
    loadAdoptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审核通过失败')
    }
  }
}

const handleBatchReject = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    
    for (const adoption of pendingAdoptions.value) {
      await reviewAdoption(adoption.id, { status: 2, rejectReason: value })
    }
    
    ElMessage.success('批量拒绝成功')
    selectedAdoptions.value = []
    pendingAdoptions.value = []
    loadAdoptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量拒绝失败')
    }
  }
}

const loadAdoptions = async () => {
  loading.value = true
  try {
    const res = await getPendingAdoptions({
      page: pagination.value.page,
      size: pagination.value.size
    })
    adoptions.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过这个申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await reviewAdoption(row.id, { status: 1 })
    ElMessage.success('审核通过')
    loadAdoptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    
    await reviewAdoption(row.id, { status: 2, rejectReason: value })
    ElMessage.success('已拒绝')
    loadAdoptions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const viewHistory = (row) => {
  // 模拟审核历史数据
  currentAdoption.value = {
    ...row,
    history: [
      {
        action: 'apply',
        time: row.applyTime,
        comment: '提交领养申请',
        operator: row.username
      },
      ...(row.status === 1 ? [{
        action: 'approve',
        time: new Date().toISOString(),
        comment: '审核通过',
        operator: '管理员'
      }] : []),
      ...(row.status === 2 ? [{
        action: 'reject',
        time: new Date().toISOString(),
        comment: row.rejectReason || '审核拒绝',
        operator: '管理员'
      }] : [])
    ]
  }
  historyDialogVisible.value = true
}

const getHistoryType = (action) => {
  const types = {
    'apply': 'primary',
    'approve': 'success',
    'reject': 'danger',
    'interview': 'warning',
    'sign': 'info',
    'complete': 'success'
  }
  return types[action] || 'info'
}

const getHistoryActionText = (action) => {
  const texts = {
    'apply': '提交领养申请',
    'approve': '审核通过',
    'reject': '审核拒绝',
    'interview': '安排面试',
    'sign': '签约领养',
    'complete': '领养完成'
  }
  return texts[action] || '未知操作'
}

onMounted(() => {
  loadAdoptions()
})
</script>

<style scoped>
.review-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
}

.pet-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pet-cell img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.text-small {
  font-size: 12px;
}

.text-gray {
  color: #999;
}

.adoption-detail {
  line-height: 1.6;
}

.timeline-content {
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.timeline-content .action {
  font-weight: bold;
  margin-bottom: 5px;
}

.timeline-content .comment {
  margin: 5px 0;
  color: #666;
}

.timeline-content .operator {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
