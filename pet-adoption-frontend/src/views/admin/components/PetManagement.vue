<template>
  <div class="pet-management">
    <div class="management-header">
      <el-button type="primary" @click="handleAdd">
        添加宠物
      </el-button>
      <el-upload
        ref="uploadRef"
        action="#"
        :show-file-list="false"
        :on-change="handleFileChange"
        :auto-upload="false"
        accept=".xlsx,.xls"
        style="margin-left: 10px"
      >
        <el-button type="success">
          批量上传
        </el-button>
      </el-upload>
      <el-button 
        type="info" 
        @click="handleExport" 
        style="margin-left: 10px"
      >
        导出数据
      </el-button>
      <el-button 
        type="warning" 
        @click="handleBatchOffShelve" 
        :disabled="selectedPets.length === 0"
        style="margin-left: 10px"
      >
        批量下架 ({{ selectedPets.length }})
      </el-button>
    </div>
    
    <el-table 
      :data="pets" 
      stripe 
      v-loading="loading" 
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image
            :src="row.images?.split(',')[0]"
            :preview-src-list="row.images?.split(',')"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="breed" label="品种" width="120" />
      <el-table-column prop="age" label="年龄" width="80">
        <template #default="{ row }">
          {{ row.age }}个月
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.publishTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="loadPets"
      style="margin-top: 20px; justify-content: center"
    />
    
    <!-- 添加/编辑宠物对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
    >
      <el-form :model="petForm" label-width="100px">
        <el-form-item label="宠物名称" prop="name">
          <el-input v-model="petForm.name" placeholder="请输入宠物名称" />
        </el-form-item>
        
        <el-form-item label="宠物类型" prop="type">
          <el-select v-model="petForm.type" placeholder="请选择宠物类型">
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="品种" prop="breed">
          <el-input v-model="petForm.breed" placeholder="请输入品种" />
        </el-form-item>
        
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="petForm.age" :min="0" :max="120" placeholder="请输入年龄（月）" />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-select v-model="petForm.gender" placeholder="请选择性别">
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="体重" prop="weight">
          <el-input-number v-model="petForm.weight" :min="0" :max="50" :step="0.1" placeholder="请输入体重（kg）" />
        </el-form-item>
        
        <el-form-item label="颜色" prop="color">
          <el-input v-model="petForm.color" placeholder="请输入颜色" />
        </el-form-item>
        
        <el-form-item label="健康状况" prop="healthStatus">
          <el-select v-model="petForm.healthStatus" placeholder="请选择健康状况">
            <el-option label="健康" value="健康" />
            <el-option label="轻微疾病" value="轻微疾病" />
            <el-option label="需要治疗" value="需要治疗" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="是否绝育" prop="sterilized">
          <el-switch v-model="petForm.sterilized" />
        </el-form-item>
        
        <el-form-item label="疫苗情况" prop="vaccineInfo">
          <el-input v-model="petForm.vaccineInfo" placeholder="请输入疫苗情况" />
        </el-form-item>
        
        <el-form-item label="宠物描述" prop="description">
          <el-input
            v-model="petForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入宠物描述"
          />
        </el-form-item>
        
        <el-form-item label="领养要求" prop="adoptionRequirements">
          <el-input
            v-model="petForm.adoptionRequirements"
            type="textarea"
            :rows="3"
            placeholder="请输入领养要求"
          />
        </el-form-item>
        
        <el-form-item label="图片上传">
          <el-upload
            v-model:file-list="imageFileList"
            action="/api/upload"
            multiple
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持 JPG、PNG 格式，单个文件不超过 5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPetList, deletePet, publishPet, updatePet, batchUploadPets, exportPets } from '@/api/pet'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const uploadRef = ref(null)
const pets = ref([])
const selectedPets = ref([])
const pagination = ref({
  current: 1,
  size: 10,
  total: 0
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('添加宠物')
const petForm = ref({
  id: null,
  name: '',
  type: '',
  breed: '',
  age: 0,
  gender: '',
  weight: 0,
  color: '',
  healthStatus: '',
  sterilized: false,
  vaccineInfo: '',
  description: '',
  adoptionRequirements: '',
  images: ''
})

// 图片上传
const imageFileList = ref([])

const loadPets = async () => {
  loading.value = true
  try {
    const res = await getPetList({
      current: pagination.value.current,
      size: pagination.value.size
    })
    pets.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载宠物列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加宠物'
  petForm.value = {
    id: null,
    name: '',
    type: '',
    breed: '',
    age: 0,
    gender: '',
    weight: 0,
    color: '',
    healthStatus: '',
    sterilized: false,
    vaccineInfo: '',
    description: '',
    adoptionRequirements: '',
    images: ''
  }
  imageFileList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑宠物'
  petForm.value = { ...row }
  if (row.images) {
    imageFileList.value = row.images.split(',').map(img => ({
      url: img,
      name: img.split('/').pop(),
      status: 'success'
    }))
  } else {
    imageFileList.value = []
  }
  dialogVisible.value = true
}

const handleSelectionChange = (selection) => {
  selectedPets.value = selection
}

const handleBatchOffShelve = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要下架选中的 ${selectedPets.value.length} 个宠物吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    for (const pet of selectedPets.value) {
      await updatePet(pet.id, { ...pet, status: 0 })
    }
    
    ElMessage.success('批量下架成功')
    selectedPets.value = []
    loadPets()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量下架失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这个宠物信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePet(row.id)
    ElMessage.success('删除成功')
    loadPets()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleImageChange = (file, fileList) => {
  imageFileList.value = fileList
}

const handleImageRemove = (file, fileList) => {
  imageFileList.value = fileList
}

const handleSubmit = async () => {
  // 验证表单
  if (!petForm.value.name || !petForm.value.type) {
    ElMessage.warning('请填写必填字段')
    return
  }
  
  // 处理图片
  const imageUrls = imageFileList.value.map(file => file.url).join(',')
  petForm.value.images = imageUrls
  
  try {
    if (petForm.value.id) {
      // 编辑
      await updatePet(petForm.value.id, petForm.value)
      ElMessage.success('编辑成功')
    } else {
      // 添加
      await publishPet(petForm.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadPets()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '已领养', 1: '待领养', 2: '审核中' }
  return texts[status] || '未知'
}

const handleFileChange = async (file) => {
  try {
    await ElMessageBox.confirm(
      `确定要上传文件 "${file.name}" 吗？\n\n注意：请确保Excel文件格式正确，包含以下列：\n宠物名称、类型、品种、年龄、性别、体重、颜色、健康状况、是否绝育、描述`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    loading.value = true
    const formData = new FormData()
    formData.append('file', file.raw)
    
    await batchUploadPets(formData)
    ElMessage.success('批量上传成功')
    loadPets()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量上传失败')
    }
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  try {
    loading.value = true
    const response = await exportPets()
    
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `宠物数据_${new Date().toISOString().split('T')[0]}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadPets()
})
</script>

<style scoped>
.pet-management {
  min-height: 500px;
}

.management-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
}
</style>