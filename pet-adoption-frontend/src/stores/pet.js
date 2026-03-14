import { defineStore } from 'pinia'
import { getPetList, getPetDetail, publishPet, updatePet, deletePet, getHotPets } from '@/api/pet'
import { ElMessage } from 'element-plus'

export const usePetStore = defineStore('pet', {
  state: () => ({
    petList: [],
    hotPets: [],
    currentPet: null,
    loading: false,
    total: 0,
    page: 1,
    size: 10,
    filters: {
      type: '',
      status: 1
    }
  }),
  
  getters: {
    hasPets: (state) => state.petList.length > 0,
    currentPagePets: (state) => state.petList
  },
  
  actions: {
    async fetchPetList(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          current: params.page || this.page,
          size: params.size || this.size,
          type: params.type || this.filters.type,
          status: params.status || this.filters.status
        }
        
        const res = await getPetList(queryParams)
        this.petList = res.data.records || []
        this.total = res.data.total || 0
        this.page = queryParams.current
        this.size = queryParams.size
        
        if (params.type !== undefined) {
          this.filters.type = params.type
        }
        if (params.status !== undefined) {
          this.filters.status = params.status
        }
        
        return res
      } catch (error) {
        ElMessage.error('获取宠物列表失败')
        console.error('获取宠物列表失败:', error)
        return null
      } finally {
        this.loading = false
      }
    },
    
    async fetchHotPets(params = {}) {
      this.loading = true
      try {
        const queryParams = {
          current: params.page || 1,
          size: params.size || 4
        }
        
        const res = await getHotPets(queryParams)
        this.hotPets = res.data.records || []
        return res
      } catch (error) {
        ElMessage.error('获取热门宠物失败')
        console.error('获取热门宠物失败:', error)
        return null
      } finally {
        this.loading = false
      }
    },
    
    async fetchPetDetail(id) {
      this.loading = true
      try {
        const res = await getPetDetail(id)
        this.currentPet = res.data
        return res
      } catch (error) {
        ElMessage.error('获取宠物详情失败')
        console.error('获取宠物详情失败:', error)
        return null
      } finally {
        this.loading = false
      }
    },
    
    async createPet(petData) {
      this.loading = true
      try {
        const res = await publishPet(petData)
        ElMessage.success('发布宠物成功')
        // 重新获取宠物列表
        await this.fetchPetList()
        return res
      } catch (error) {
        ElMessage.error('发布宠物失败')
        console.error('发布宠物失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async updatePet(id, petData) {
      this.loading = true
      try {
        const res = await updatePet(id, petData)
        ElMessage.success('更新宠物信息成功')
        // 重新获取宠物详情
        await this.fetchPetDetail(id)
        // 重新获取宠物列表
        await this.fetchPetList()
        return res
      } catch (error) {
        ElMessage.error('更新宠物信息失败')
        console.error('更新宠物信息失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async deletePet(id) {
      this.loading = true
      try {
        await deletePet(id)
        ElMessage.success('删除宠物成功')
        // 重新获取宠物列表
        await this.fetchPetList()
      } catch (error) {
        ElMessage.error('删除宠物失败')
        console.error('删除宠物失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },
    
    clearCurrentPet() {
      this.currentPet = null
    },
    
    resetFilters() {
      this.filters = {
        type: '',
        status: 1
      }
      this.page = 1
    }
  }
})
