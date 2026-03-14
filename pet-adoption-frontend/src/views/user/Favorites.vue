<template>
  <div class="favorites">
    <h2>❤️ 我的收藏</h2>
    
    <div class="favorites-toolbar">
      <div class="category-section">
        <el-select v-model="currentCategory" placeholder="选择分类" class="category-select">
          <el-option label="全部" value="all" />
          <el-option 
            v-for="category in categories" 
            :key="category.id" 
            :label="category.name" 
            :value="category.id"
          />
        </el-select>
        <el-button 
          type="primary" 
          size="small" 
          @click="showCategoryDialog"
        >
          管理分类
        </el-button>
      </div>
      <div class="action-section">
        <el-select 
          v-model="batchAction" 
          placeholder="批量操作" 
          class="batch-select"
          :disabled="selectedPets.length === 0"
        >
          <el-option label="移动到分类" value="move" />
          <el-option label="批量取消收藏" value="delete" />
        </el-select>
        <el-button 
          type="primary" 
          size="small" 
          @click="handleBatchAction"
          :disabled="!batchAction || selectedPets.length === 0"
        >
          执行
        </el-button>
      </div>
    </div>
    
    <div class="favorites-grid" v-loading="loading">
      <div v-if="favorites.length > 0">
        <el-card 
          v-for="pet in favorites" 
          :key="pet.id" 
          class="pet-card"
        >
          <div class="pet-header">
            <div class="pet-image-container">
              <img :src="pet.images?.split(',')[0]" :alt="pet.name" class="pet-image">
              <el-checkbox 
                v-model="selectedPets" 
                :label="pet.id"
                class="pet-checkbox"
              />
            </div>
            <div class="pet-info">
              <div class="pet-name-row">
                <h3>{{ pet.name }}</h3>
                <el-select 
                  v-model="pet.categoryId" 
                  placeholder="选择分类" 
                  size="small" 
                  class="pet-category-select"
                  @change="updatePetCategory(pet.id, pet.categoryId)"
                >
                  <el-option label="未分类" value="" />
                  <el-option 
                    v-for="category in categories" 
                    :key="category.id" 
                    :label="category.name" 
                    :value="category.id"
                  />
                </el-select>
              </div>
              <p class="pet-basic-info">
                {{ pet.type }} · {{ pet.breed }} · {{ pet.age }}个月
              </p>
              <p class="pet-description">{{ pet.description }}</p>
            </div>
          </div>
          <div class="pet-footer">
            <el-button 
              type="primary" 
              size="small"
              @click="goToPetDetail(pet.id)"
            >
              查看详情
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click="handleRemove(pet.id)"
            >
              取消收藏
            </el-button>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无收藏" />
    </div>
    
    <el-pagination
      v-if="favorites.length > 0"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @current-change="loadFavorites"
      style="margin-top: 30px; justify-content: center"
    />
    
    <!-- 分类管理对话框 -->
    <el-dialog v-model="categoryDialogVisible" title="分类管理" width="500px">
      <div class="category-management">
        <div class="category-list">
          <div 
            v-for="category in categories" 
            :key="category.id" 
            class="category-item"
          >
            <span class="category-name">{{ category.name }}</span>
            <div class="category-actions">
              <el-button 
                type="primary" 
                size="small" 
                @click="editCategory(category)"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteCategory(category.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
        <div class="category-form">
          <el-form :model="categoryForm" ref="categoryFormRef" label-width="80px">
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveCategory">保存</el-button>
              <el-button @click="resetCategoryForm">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-dialog>
    
    <!-- 批量移动对话框 -->
    <el-dialog v-model="moveDialogVisible" title="移动到分类" width="400px">
      <el-form :model="moveForm" ref="moveFormRef" label-width="80px">
        <el-form-item label="目标分类" prop="categoryId">
          <el-select v-model="moveForm.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="confirmMove">确定</el-button>
          <el-button @click="moveDialogVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyFavorites, removeFavorite } from '@/api/favorite'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const favorites = ref([])
const selectedPets = ref([])
const pagination = ref({
  page: 1,
  size: 12,
  total: 0
})

// 分类相关变量
const categories = ref([
  { id: 1, name: '狗狗' },
  { id: 2, name: '猫咪' },
  { id: 3, name: '其他宠物' }
])
const currentCategory = ref('all')
const categoryDialogVisible = ref(false)
const categoryForm = ref({ id: null, name: '' })
const categoryFormRef = ref(null)

// 批量操作相关变量
const batchAction = ref('')
const moveDialogVisible = ref(false)
const moveForm = ref({ categoryId: '' })
const moveFormRef = ref(null)

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites({
      page: pagination.value.page,
      size: pagination.value.size,
      categoryId: currentCategory.value === 'all' ? '' : currentCategory.value
    })
    favorites.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const handleRemove = async (petId) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这个宠物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await removeFavorite(petId)
    ElMessage.success('取消收藏成功')
    loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消收藏失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedPets.value.length === 0) {
    ElMessage.warning('请选择要取消收藏的宠物')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要取消收藏这 ${selectedPets.value.length} 个宠物吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 批量取消收藏
    for (const petId of selectedPets.value) {
      await removeFavorite(petId)
    }
    
    ElMessage.success('批量取消收藏成功')
    selectedPets.value = []
    loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量取消收藏失败')
    }
  }
}

const goToPetDetail = (petId) => {
  router.push(`/pets/${petId}`)
}

// 分类管理方法
const showCategoryDialog = () => {
  resetCategoryForm()
  categoryDialogVisible.value = true
}

const saveCategory = () => {
  if (!categoryForm.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  if (categoryForm.value.id) {
    // 编辑分类
    const category = categories.value.find(c => c.id === categoryForm.value.id)
    if (category) {
      category.name = categoryForm.value.name
      ElMessage.success('分类编辑成功')
    }
  } else {
    // 新增分类
    const newCategory = {
      id: Date.now(),
      name: categoryForm.value.name
    }
    categories.value.push(newCategory)
    ElMessage.success('分类创建成功')
  }
  
  resetCategoryForm()
  categoryDialogVisible.value = false
}

const editCategory = (category) => {
  categoryForm.value = { ...category }
}

const deleteCategory = async (categoryId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    categories.value = categories.value.filter(c => c.id !== categoryId)
    ElMessage.success('分类删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('分类删除失败')
    }
  }
}

const resetCategoryForm = () => {
  categoryForm.value = { id: null, name: '' }
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

// 批量操作方法
const handleBatchAction = () => {
  if (batchAction.value === 'move') {
    moveDialogVisible.value = true
  } else if (batchAction.value === 'delete') {
    handleBatchDelete()
  }
}

const confirmMove = () => {
  if (!moveForm.value.categoryId) {
    ElMessage.warning('请选择目标分类')
    return
  }
  
  // 模拟批量移动到分类
  favorites.value.forEach(pet => {
    if (selectedPets.value.includes(pet.id)) {
      pet.categoryId = moveForm.value.categoryId
    }
  })
  
  ElMessage.success(`已将 ${selectedPets.value.length} 个宠物移动到分类`)
  moveDialogVisible.value = false
  selectedPets.value = []
  batchAction.value = ''
}

// 更新宠物分类
const updatePetCategory = (petId, categoryId) => {
  // 模拟更新宠物分类
  ElMessage.success('分类更新成功')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites {
  max-width: 1200px;
  margin: 0 auto;
}

.favorites h2 {
  margin-bottom: 30px;
  color: #333;
}

.favorites-toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.category-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-select {
  width: 150px;
}

.batch-select {
  width: 120px;
}

.pet-name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.pet-category-select {
  width: 120px;
}

/* 分类管理样式 */
.category-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.category-list {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  max-height: 200px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.category-item:last-child {
  border-bottom: none;
}

.category-name {
  font-size: 14px;
  font-weight: 500;
}

.category-actions {
  display: flex;
  gap: 5px;
}

.category-form {
  margin-top: 10px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.pet-card {
  transition: transform 0.3s ease;
}

.pet-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.pet-header {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.pet-image-container {
  position: relative;
}

.pet-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}

.pet-checkbox {
  position: absolute;
  top: 5px;
  right: 5px;
}

.pet-info {
  flex: 1;
}

.pet-info h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 18px;
}

.pet-basic-info {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.pet-description {
  margin: 0;
  color: #999;
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pet-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  
  .pet-header {
    flex-direction: column;
  }
  
  .pet-image {
    width: 100%;
    height: 200px;
  }
}
</style>