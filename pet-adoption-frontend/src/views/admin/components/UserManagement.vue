<template>
  <div class="user-management">
    <!-- 筛选表单 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline @submit.prevent="loadUsers">
        <el-form-item label="搜索">
          <el-input v-model="filterForm.keyword" placeholder="用户名/邮箱/手机号" clearable />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="filterForm.userType" placeholder="全部" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="收容所" value="SHELTER" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="filterForm.createTime"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadUsers">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 标签管理 -->
    <el-card class="tag-card" v-if="selectedUser">
      <template #header>
        <div class="card-header">
          <span>标签管理 - {{ selectedUser.username }}</span>
          <el-button size="small" @click="selectedUser = null">关闭</el-button>
        </div>
      </template>
      <el-form :model="tagForm" @submit.prevent="saveTags">
        <el-form-item label="用户标签">
          <el-tag
            v-for="tag in selectedUser.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-model="tagForm.newTag"
            placeholder="输入新标签"
            @keyup.enter="addTag"
            style="width: 150px; margin-left: 10px"
          />
          <el-button type="primary" size="small" @click="addTag">添加</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveTags">保存标签</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="userType" label="用户类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getUserTypeColor(row.userType)">
            {{ getUserTypeText(row.userType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="120">
        <template #default="{ row }">
          <el-tag
            v-for="tag in row.tags"
            :key="tag"
            size="small"
            style="margin-right: 5px"
          >
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button 
            :type="row.status === 1 ? 'warning' : 'success'" 
            size="small"
            @click="handleToggleStatus(row)"
            style="margin-right: 5px"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button type="info" size="small" @click="handleManageTags(row)">
            标签管理
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="loadUsers"
      style="margin-top: 20px; justify-content: center"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, updateUserStatus, updateUserTags } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const users = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 筛选表单
const filterForm = ref({
  keyword: '',
  userType: '',
  status: '',
  createTime: []
})

// 标签管理
const selectedUser = ref(null)
const tagForm = ref({
  newTag: ''
})

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    
    // 添加筛选参数
    if (filterForm.value.keyword) {
      params.keyword = filterForm.value.keyword
    }
    if (filterForm.value.userType) {
      params.userType = filterForm.value.userType
    }
    if (filterForm.value.status) {
      params.status = filterForm.value.status
    }
    if (filterForm.value.createTime && filterForm.value.createTime.length === 2) {
      params.startTime = filterForm.value.createTime[0]
      params.endTime = filterForm.value.createTime[1]
    }
    
    const res = await getUserList(params)
    users.value = (res.data.records || []).map(user => ({
      ...user,
      tags: user.tags ? user.tags.split(',') : []
    }))
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterForm.value = {
    keyword: '',
    userType: '',
    status: '',
    createTime: []
  }
  loadUsers()
}

const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateUserStatus(row.id, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadUsers()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleManageTags = (row) => {
  selectedUser.value = {
    ...row,
    tags: row.tags || []
  }
}

const addTag = () => {
  if (tagForm.value.newTag && !selectedUser.value.tags.includes(tagForm.value.newTag)) {
    selectedUser.value.tags.push(tagForm.value.newTag)
    tagForm.value.newTag = ''
  }
}

const removeTag = (tag) => {
  const index = selectedUser.value.tags.indexOf(tag)
  if (index > -1) {
    selectedUser.value.tags.splice(index, 1)
  }
}

const saveTags = async () => {
  try {
    const tagsStr = selectedUser.value.tags.join(',')
    await updateUserTags(selectedUser.value.id, { tags: tagsStr })
    ElMessage.success('标签保存成功')
    loadUsers()
    selectedUser.value = null
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const getUserTypeColor = (type) => {
  const colors = { ADMIN: 'danger', SHELTER: 'warning', USER: 'success' }
  return colors[type] || 'info'
}

const getUserTypeText = (type) => {
  const texts = { ADMIN: '管理员', SHELTER: '收容所', USER: '普通用户' }
  return texts[type] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  min-height: 500px;
}
</style>
