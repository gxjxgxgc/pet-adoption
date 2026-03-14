<template>
  <div class="home">
    <div class="banner">
      <h1>🐾 宠物领养系统</h1>
      <p>给每一个流浪动物一个温暖的家</p>
    </div>
    
    <div class="section">
      <h2>🔥 热门宠物</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in hotPets" :key="pet.id">
          <PetCard :pet="pet" />
        </el-col>
      </el-row>
      <div class="text-center mt-20">
        <el-button type="primary" @click="goToPetList">查看更多</el-button>
      </div>
    </div>

    <div class="section">
      <h2>📋 最新发布</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="pet in latestPets" :key="pet.id">
          <PetCard :pet="pet" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPetList, getHotPets } from '@/api/pet'
import PetCard from '@/components/PetCard.vue'

const router = useRouter()
const hotPets = ref([])
const latestPets = ref([])
const loading = ref(true)

const loadPets = async () => {
  loading.value = true
  try {
    // 获取热门宠物
    const hotRes = await getHotPets({ current: 1, size: 4 })
    hotPets.value = hotRes.data.records || []

    // 获取最新宠物
    const latestRes = await getPetList({ current: 1, size: 4, status: 1 })
    latestPets.value = latestRes.data.records || []
  } catch (error) {
    console.error('加载宠物列表失败：', error)
  } finally {
    loading.value = false
  }
}

const goToPetList = () => {
  router.push('/pets')
}

onMounted(() => {
  loadPets()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
  padding: 80px 20px;
  border-radius: 12px;
  margin-bottom: 40px;
}

.banner h1 {
  font-size: 48px;
  margin-bottom: 20px;
}

.banner p {
  font-size: 20px;
  opacity: 0.9;
}

.section {
  margin-bottom: 60px;
}

.section h2 {
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}
</style>
