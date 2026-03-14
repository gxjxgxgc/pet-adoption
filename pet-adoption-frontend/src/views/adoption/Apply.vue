<template>
  <div class="apply-adoption">
    <el-card class="apply-card">
      <h2>申请领养</h2>
      
      <div v-if="pet" class="pet-preview">
        <el-row :gutter="20">
          <el-col :xs="24" :md="8">
            <img :src="pet.images?.split(',')[0]" :alt="pet.name" class="pet-image" />
          </el-col>
          <el-col :xs="24" :md="16">
            <h3>{{ pet.name }}</h3>
            <p class="pet-info">{{ pet.type }} · {{ pet.breed }} · {{ pet.age }}个月</p>
            <p class="pet-desc">{{ pet.description }}</p>
          </el-col>
        </el-row>
      </div>
      
      <!-- 步骤指示器 -->
      <el-steps :active="currentStep" finish-status="success" class="step-indicator">
        <el-step title="基本信息" />
        <el-step title="养宠经验" />
        <el-step title="个人情况" />
        <el-step title="确认提交" />
      </el-steps>
      
      <!-- 多步骤表单 -->
      <div class="step-content">
        <!-- 步骤1：基本信息 -->
        <div v-if="currentStep === 0" class="step-form">
          <el-form :model="applyForm" :rules="rules.step1" ref="applyFormRef" label-width="100px">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="applyForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="applyForm.phone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="居住地址" prop="address">
              <el-input v-model="applyForm.address" placeholder="请输入居住地址" />
            </el-form-item>
            <el-form-item label="住房类型" prop="housingType">
              <el-select v-model="applyForm.housingType" placeholder="请选择住房类型">
                <el-option label="自有住房" value="owned" />
                <el-option label="租赁住房" value="rented" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 步骤2：养宠经验 -->
        <div v-if="currentStep === 1" class="step-form">
          <el-form :model="applyForm" :rules="rules.step2" ref="applyFormRef" label-width="100px">
            <el-form-item label="养宠经验" prop="experience">
              <el-radio-group v-model="applyForm.experience">
                <el-radio :label="1">有经验</el-radio>
                <el-radio :label="0">无经验</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="applyForm.experience === 1" label="经验描述" prop="experienceDesc">
              <el-input
                v-model="applyForm.experienceDesc"
                type="textarea"
                :rows="4"
                placeholder="请描述你的养宠经验，包括养过什么宠物、养了多久等"
              />
            </el-form-item>
            <el-form-item label="养宠计划" prop="petPlan">
              <el-input
                v-model="applyForm.petPlan"
                type="textarea"
                :rows="4"
                placeholder="请描述你的养宠计划，包括如何照顾宠物、时间安排等"
              />
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 步骤3：个人情况 -->
        <div v-if="currentStep === 2" class="step-form">
          <el-form :model="applyForm" :rules="rules.step3" ref="applyFormRef" label-width="100px">
            <el-form-item label="工作情况" prop="workInfo">
              <el-input
                v-model="applyForm.workInfo"
                type="textarea"
                :rows="3"
                placeholder="请描述你的工作情况，包括工作性质、工作时间等"
              />
            </el-form-item>
            <el-form-item label="家庭情况" prop="familyInfo">
              <el-input
                v-model="applyForm.familyInfo"
                type="textarea"
                :rows="3"
                placeholder="请描述家庭成员情况、是否有小孩或其他宠物等"
              />
            </el-form-item>
            <el-form-item label="经济状况" prop="financialStatus">
              <el-input
                v-model="applyForm.financialStatus"
                type="textarea"
                :rows="3"
                placeholder="请描述你的经济状况，包括收入水平、是否有能力承担宠物的费用等"
              />
            </el-form-item>
            <el-form-item label="申请理由" prop="reason">
              <el-input
                v-model="applyForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请详细说明你的领养理由"
              />
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 步骤4：确认提交 -->
        <div v-if="currentStep === 3" class="step-form">
          <div class="confirmation-form">
            <h3>申请信息确认</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="联系人">{{ applyForm.contactName }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ applyForm.phone }}</el-descriptions-item>
              <el-descriptions-item label="居住地址">{{ applyForm.address }}</el-descriptions-item>
              <el-descriptions-item label="住房类型">{{ getHousingTypeText() }}</el-descriptions-item>
              <el-descriptions-item label="养宠经验">{{ applyForm.experience === 1 ? '有经验' : '无经验' }}</el-descriptions-item>
              <el-descriptions-item v-if="applyForm.experience === 1" label="经验描述">{{ applyForm.experienceDesc }}</el-descriptions-item>
              <el-descriptions-item label="养宠计划">{{ applyForm.petPlan }}</el-descriptions-item>
              <el-descriptions-item label="工作情况">{{ applyForm.workInfo }}</el-descriptions-item>
              <el-descriptions-item label="家庭情况">{{ applyForm.familyInfo }}</el-descriptions-item>
              <el-descriptions-item label="经济状况">{{ applyForm.financialStatus }}</el-descriptions-item>
              <el-descriptions-item label="申请理由">{{ applyForm.reason }}</el-descriptions-item>
            </el-descriptions>
            
            <el-form-item class="agreement">
              <el-checkbox v-model="applyForm.agreement">
                我承诺以上信息真实有效，并且会好好照顾领养的宠物，不会虐待或遗弃。
              </el-checkbox>
            </el-form-item>
          </div>
        </div>
      </div>
      
      <!-- 步骤导航按钮 -->
      <div class="step-buttons">
        <el-button v-if="currentStep > 0" @click="prevStep" size="large">上一步</el-button>
        <el-button v-if="currentStep < 3" type="primary" @click="nextStep" :loading="loading" size="large">
          下一步
        </el-button>
        <el-button v-if="currentStep === 3" type="primary" @click="handleSubmit" :loading="loading" size="large">
          提交申请
        </el-button>
        <el-button @click="goBack" size="large">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPetDetail } from '@/api/pet'
import { applyAdoption } from '@/api/adoption'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const applyFormRef = ref(null)
const loading = ref(false)
const pet = ref(null)
const currentStep = ref(0)

const applyForm = ref({
  contactName: '',
  phone: '',
  address: '',
  housingType: '',
  experience: 0,
  experienceDesc: '',
  petPlan: '',
  workInfo: '',
  familyInfo: '',
  financialStatus: '',
  reason: '',
  agreement: false
})

const rules = {
  step1: {
    contactName: [
      { required: true, message: '请输入联系人姓名', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入联系电话', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    address: [
      { required: true, message: '请输入居住地址', trigger: 'blur' }
    ],
    housingType: [
      { required: true, message: '请选择住房类型', trigger: 'change' }
    ]
  },
  step2: {
    experience: [
      { required: true, message: '请选择养宠经验', trigger: 'change' }
    ],
    experienceDesc: [
      { required: true, message: '请描述养宠经验', trigger: 'blur' },
      { min: 10, max: 200, message: '经验描述长度在10-200个字符', trigger: 'blur' }
    ],
    petPlan: [
      { required: true, message: '请描述养宠计划', trigger: 'blur' },
      { min: 20, max: 300, message: '养宠计划长度在20-300个字符', trigger: 'blur' }
    ]
  },
  step3: {
    workInfo: [
      { required: true, message: '请描述工作情况', trigger: 'blur' },
      { min: 10, max: 200, message: '工作情况长度在10-200个字符', trigger: 'blur' }
    ],
    familyInfo: [
      { required: true, message: '请描述家庭情况', trigger: 'blur' },
      { min: 10, max: 200, message: '家庭情况长度在10-200个字符', trigger: 'blur' }
    ],
    financialStatus: [
      { required: true, message: '请描述经济状况', trigger: 'blur' },
      { min: 10, max: 200, message: '经济状况长度在10-200个字符', trigger: 'blur' }
    ],
    reason: [
      { required: true, message: '请输入申请理由', trigger: 'blur' },
      { min: 20, max: 500, message: '申请理由长度在20-500个字符', trigger: 'blur' }
    ]
  }
}

const loadPetDetail = async () => {
  try {
    const res = await getPetDetail(route.params.id)
    pet.value = res.data
  } catch (error) {
    ElMessage.error('加载宠物信息失败')
    router.back()
  }
}

const nextStep = async () => {
  const valid = await applyFormRef.value.validate()
  if (!valid) return
  
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

const getHousingTypeText = () => {
  const housingTypes = {
    'owned': '自有住房',
    'rented': '租赁住房',
    'other': '其他'
  }
  return housingTypes[applyForm.value.housingType] || ''
}

const handleSubmit = async () => {
  if (!applyForm.value.agreement) {
    ElMessage.warning('请阅读并同意领养协议')
    return
  }

  loading.value = true
  try {
    await applyAdoption({
      petId: pet.value.id,
      reason: applyForm.value.reason,
      phone: applyForm.value.phone,
      address: applyForm.value.address,
      experience: applyForm.value.experience,
      familyInfo: applyForm.value.familyInfo
    })
    ElMessage.success('申请提交成功，请等待审核')
    clearFormData() // 提交成功后清除保存的表单数据
    router.push('/adoptions')
  } catch (error) {
    ElMessage.error('申请提交失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

// 保存表单数据到localStorage
const saveFormData = () => {
  const storageKey = `adoption_apply_${route.params.id}`
  try {
    localStorage.setItem(storageKey, JSON.stringify({
      form: applyForm.value,
      step: currentStep.value
    }))
  } catch (error) {
    console.error('保存表单数据失败:', error)
  }
}

// 从localStorage恢复表单数据
const loadFormData = () => {
  const storageKey = `adoption_apply_${route.params.id}`
  try {
    const savedData = localStorage.getItem(storageKey)
    if (savedData) {
      const parsedData = JSON.parse(savedData)
      applyForm.value = { ...applyForm.value, ...parsedData.form }
      currentStep.value = parsedData.step
      ElMessage.info('已恢复之前填写的表单数据')
    }
  } catch (error) {
    console.error('恢复表单数据失败:', error)
  }
}

// 清除保存的表单数据
const clearFormData = () => {
  const storageKey = `adoption_apply_${route.params.id}`
  try {
    localStorage.removeItem(storageKey)
  } catch (error) {
    console.error('清除表单数据失败:', error)
  }
}

// 监听表单数据变化，自动保存
watch(
  [applyForm, currentStep],
  () => {
    saveFormData()
  },
  { deep: true }
)

onMounted(() => {
  loadPetDetail()
  loadFormData()
})
</script>

<style scoped>
.apply-adoption {
  max-width: 800px;
  margin: 0 auto;
}

.apply-card {
  padding: 30px;
}

.apply-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.pet-preview {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.pet-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.pet-preview h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #333;
}

.pet-info {
  color: #666;
  margin-bottom: 10px;
}

.pet-desc {
  color: #999;
  line-height: 1.6;
}

/* 步骤指示器样式 */
.step-indicator {
  margin-bottom: 40px;
}

/* 步骤内容样式 */
.step-content {
  margin-bottom: 40px;
}

.step-form {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 确认表单样式 */
.confirmation-form {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.confirmation-form h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.agreement {
  margin-top: 20px;
  text-align: center;
}

/* 步骤按钮样式 */
.step-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .apply-card {
    padding: 20px;
  }
  
  .step-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .step-buttons button {
    width: 100%;
  }
}
</style>
