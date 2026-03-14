import { defineStore } from 'pinia'

export const useLoadingStore = defineStore('loading', {
  state: () => ({
    // 全局加载状态
    global: false,
    // 具体操作的加载状态
    operations: {
      login: false,
      register: false,
      fetchPets: false,
      fetchPetDetail: false,
      createPet: false,
      updatePet: false,
      deletePet: false,
      submitAdoption: false,
      fetchAdoptions: false
    }
  }),
  
  getters: {
    // 是否有任何加载操作正在进行
    isLoading: (state) => state.global || Object.values(state.operations).some(value => value),
    // 检查特定操作是否正在加载
    isOperationLoading: (state) => (operation) => state.operations[operation] || false
  },
  
  actions: {
    // 设置全局加载状态
    setGlobalLoading(status) {
      this.global = status
    },
    
    // 设置特定操作的加载状态
    setOperationLoading(operation, status) {
      if (this.operations.hasOwnProperty(operation)) {
        this.operations[operation] = status
      }
    },
    
    // 开始加载某个操作
    startLoading(operation) {
      this.setOperationLoading(operation, true)
    },
    
    // 结束加载某个操作
    endLoading(operation) {
      this.setOperationLoading(operation, false)
    },
    
    // 重置所有加载状态
    resetLoading() {
      this.global = false
      Object.keys(this.operations).forEach(key => {
        this.operations[key] = false
      })
    }
  }
})
