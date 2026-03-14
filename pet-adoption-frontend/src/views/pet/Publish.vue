<template>
  <div class="publish-pet">
    <el-card class="publish-card">
      <h2>📝 发布宠物信息</h2>
      <el-form :model="petForm" :rules="rules" ref="petFormRef" label-width="100px">
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
          <el-input-number v-model="petForm.age" :min="0" :max="200" />
          <span style="margin-left: 10px">个月</span>
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="petForm.gender">
            <el-radio label="公">公</el-radio>
            <el-radio label="母">母</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="颜色" prop="color">
          <el-input v-model="petForm.color" placeholder="请输入颜色" />
        </el-form-item>

        <el-form-item label="体重" prop="weight">
          <el-input-number v-model="petForm.weight" :min="0" :max="100" :precision="2" />
          <span style="margin-left: 10px">kg</span>
        </el-form-item>

        <el-form-item label="健康状况" prop="healthStatus">
          <el-input v-model="petForm.healthStatus" placeholder="请输入健康状况" />
        </el-form-item>

        <el-form-item label="是否绝育" prop="sterilized">
          <el-switch v-model="petForm.sterilized" />
        </el-form-item>

        <el-form-item label="宠物描述" prop="description">
          <el-input
            v-model="petForm.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述宠物的性格、习惯、疫苗接种情况等..."
          />
        </el-form-item>

        <el-form-item label="宠物图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            action="/api/upload"
            list-type="picture-card"
            :on-preview="handlePicturePreview"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-dialog v-model="previewVisible">
            <img w-full :src="previewImage" alt="Preview" style="width: 100%" />
          </el-dialog>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            发布
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishPet } from '@/api/pet'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const petFormRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const previewVisible = ref(false)
const previewImage = ref('')

const petForm = ref({
  name: '',
  type: '',
  breed: '',
  age: 12,
  gender: '公',
  color: '',
  weight: 5,
  healthStatus: '健康',
  sterilized: false,
  description: '',
  images: ''
})

const rules = {
  name: [
    { required: true, message: '请输入宠物名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择宠物类型', trigger: 'change' }
  ],
  breed: [
    { required: true, message: '请输入品种', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  color: [
    { required: true, message: '请输入颜色', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' }
  ],
  healthStatus: [
    { required: true, message: '请输入健康状况', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入宠物描述', trigger: 'blur' },
    { min: 10, message: '描述至少10个字符', trigger: 'blur' }
  ]
}

const handlePicturePreview = (file) => {
  previewImage.value = file.url
  previewVisible.value = true
}

const handleUploadSuccess = (response, file) => {
  petForm.value.images = fileList.value.map(f => f.response?.data || f.url).join(',')
}

const handleRemove = () => {
  petForm.value.images = fileList.value.map(f => f.response?.data || f.url).join(',')
}

const handleSubmit = async () => {
  const valid = await petFormRef.value.validate()
  if (!valid) return

  if (fileList.value.length === 0) {
    ElMessage.warning('请至少上传一张宠物图片')
    return
  }

  loading.value = true
  try {
      await publishPet(petForm.value)
      ElMessage.success('发布成功')
      router.push('/pets')
    } catch (error) {
      ElMessage.error('发布失败')
    } finally {
      loading.value = false
    }
}

const handleReset = () => {
  petFormRef.value.resetFields()
  fileList.value = []
  petForm.value.images = ''
}
</script>

<style scoped>
.publish-pet {
  max-width: 800px;
  margin: 0 auto;
}

.publish-card {
  padding: 30px;
}

.publish-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.el-form-item {
  margin-bottom: 25px;
}
</style>
