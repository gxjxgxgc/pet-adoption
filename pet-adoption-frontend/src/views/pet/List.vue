<template>
  <div class="pet-list">
    <div class="filter-bar">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="宠物类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable>
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="性别">
          <el-select v-model="filterForm.gender" placeholder="全部" clearable>
            <el-option label="公" value="公" />
            <el-option label="母" value="母" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="年龄范围">
          <el-slider 
            v-model="filterForm.ageRange" 
            range 
            :min="0" 
            :max="120" 
            :marks="{ 0: '0个月', 60: '5岁', 120: '10岁' }"
            @change="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="体重范围">
          <el-slider 
            v-model="filterForm.weightRange" 
            range 
            :min="0" 
            :max="50" 
            :marks="{ 0: '0kg', 10: '10kg', 25: '25kg', 50: '50kg' }"
            @change="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="健康状况">
          <el-checkbox-group v-model="filterForm.healthStatus" @change="handleSearch">
            <el-checkbox label="健康">健康</el-checkbox>
            <el-checkbox label="已疫苗">已疫苗</el-checkbox>
            <el-checkbox label="已绝育">已绝育</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="pet-grid">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in pets" :key="pet.id">
          <PetCard :pet="pet" />
        </el-col>
      </el-row>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadPets"
        @current-change="loadPets"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPetList } from '@/api/pet'
import PetCard from '@/components/PetCard.vue'

const pets = ref([])
const filterForm = ref({
  type: '',
  gender: '',
  ageRange: [0, 120],
  weightRange: [0, 50],
  healthStatus: []
})
const pagination = ref({
  current: 1,
  size: 12,
  total: 0
})

const loadPets = async () => {
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.size,
      type: filterForm.value.type || undefined,
      gender: filterForm.value.gender || undefined,
      minAge: filterForm.value.ageRange[0],
      maxAge: filterForm.value.ageRange[1],
      minWeight: filterForm.value.weightRange[0],
      maxWeight: filterForm.value.weightRange[1],
      healthStatus: filterForm.value.healthStatus.length > 0 ? filterForm.value.healthStatus : undefined,
      status: 1
    }
    const res = await getPetList(params)
    pets.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    console.error('加载宠物列表失败：', error)
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  loadPets()
}

const handleReset = () => {
  filterForm.value = {
    type: '',
    gender: '',
    ageRange: [0, 120],
    weightRange: [0, 50],
    healthStatus: []
  }
  pagination.value.current = 1
  loadPets()
}

onMounted(() => {
  loadPets()
})
</script>

<style scoped>
.pet-list {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-form .el-form-item {
  margin-bottom: 15px;
}

.filter-form .el-slider {
  width: 200px;
}

.filter-form .el-checkbox-group {
  display: flex;
  gap: 15px;
}

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
  }
  
  .filter-form .el-slider {
    width: 100%;
  }
  
  .filter-form .el-checkbox-group {
    flex-direction: column;
    gap: 5px;
  }
}

.pet-grid {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>