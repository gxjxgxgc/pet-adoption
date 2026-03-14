<template>
  <div class="profile">
    <el-card class="profile-card">
      <h2>👤 个人中心</h2>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <div class="avatar-section">
            <div class="avatar-preview">
              <img :src="userForm.avatar || defaultAvatar" alt="头像" class="avatar-image">
              <div class="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  action="/api/upload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <el-button type="primary" size="small">更换头像</el-button>
                </el-upload>
              </div>
            </div>
          </div>
          
          <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" />
            </el-form-item>

            <el-form-item label="真实姓名">
              <el-input v-model="userForm.realName" />
            </el-form-item>

            <el-form-item label="性别">
              <el-radio-group v-model="userForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="年龄">
              <el-input-number v-model="userForm.age" :min="0" :max="150" />
            </el-form-item>

            <el-form-item label="地址">
              <el-input v-model="userForm.address" />
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input
                v-model="userForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下自己..."
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="我的统计" name="stats">
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ userStore.userInfo?.favoriteCount || 0 }}</div>
              <div class="stat-label">收藏数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userStore.userInfo?.adoptionCount || 0 }}</div>
              <div class="stat-label">领养数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userStore.userInfo?.creditScore || 100 }}</div>
              <div class="stat-label">信用分</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="实名认证" name="verification">
          <div class="verification-section">
            <div v-if="userStore.userInfo?.verificationStatus === 'VERIFIED'" class="verification-status verified">
              <el-icon class="status-icon"><Check /></el-icon>
              <div class="status-text">
                <h3>实名认证已通过</h3>
                <p>您的身份信息已验证，可正常使用所有功能</p>
              </div>
            </div>
            <div v-else-if="userStore.userInfo?.verificationStatus === 'PENDING'" class="verification-status pending">
              <el-icon class="status-icon"><Loading /></el-icon>
              <div class="status-text">
                <h3>认证审核中</h3>
                <p>您的认证信息正在审核中，请耐心等待</p>
              </div>
            </div>
            <div v-else class="verification-form">
              <el-form :model="verificationForm" ref="verificationFormRef" label-width="100px">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="verificationForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>

                <el-form-item label="身份证号" prop="idNumber">
                  <el-input v-model="verificationForm.idNumber" placeholder="请输入身份证号码" />
                </el-form-item>

                <el-form-item label="身份证正面">
                  <el-upload
                    class="id-uploader"
                    action="/api/upload"
                    :show-file-list="false"
                    :on-success="handleIdFrontSuccess"
                    :before-upload="beforeIdUpload"
                  >
                    <div v-if="verificationForm.idFront" class="id-preview">
                      <img :src="verificationForm.idFront" alt="身份证正面" class="id-image">
                      <el-button type="danger" size="small" @click="verificationForm.idFront = ''">删除</el-button>
                    </div>
                    <el-button v-else type="primary">上传身份证正面</el-button>
                  </el-upload>
                </el-form-item>

                <el-form-item label="身份证反面">
                  <el-upload
                    class="id-uploader"
                    action="/api/upload"
                    :show-file-list="false"
                    :on-success="handleIdBackSuccess"
                    :before-upload="beforeIdUpload"
                  >
                    <div v-if="verificationForm.idBack" class="id-preview">
                      <img :src="verificationForm.idBack" alt="身份证反面" class="id-image">
                      <el-button type="danger" size="small" @click="verificationForm.idBack = ''">删除</el-button>
                    </div>
                    <el-button v-else type="primary">上传身份证反面</el-button>
                  </el-upload>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" @click="submitVerification" :loading="verificationLoading">
                    提交认证
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Check, Loading } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('basic')
const userFormRef = ref(null)
const passwordFormRef = ref(null)
const verificationFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)
const verificationLoading = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userForm = ref({
  username: '',
  email: '',
  phone: '',
  realName: '',
  gender: '',
  age: 0,
  address: '',
  bio: '',
  avatar: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const verificationForm = ref({
  realName: '',
  idNumber: '',
  idFront: '',
  idBack: ''
})

const rules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadUserInfo = () => {
  if (userStore.userInfo) {
    userForm.value = {
      username: userStore.userInfo.username,
      email: userStore.userInfo.email || '',
      phone: userStore.userInfo.phone || '',
      realName: userStore.userInfo.realName || '',
      gender: userStore.userInfo.gender || '',
      age: userStore.userInfo.age || 0,
      address: userStore.userInfo.address || '',
      bio: userStore.userInfo.bio || '',
      avatar: userStore.userInfo.avatar || ''
    }
  }
}

const handleAvatarSuccess = (response, file) => {
  userForm.value.avatar = response.data.url
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const handleUpdate = async () => {
  const valid = await userFormRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    await updateProfile(userForm.value)
    ElMessage.success('保存成功')
    await userStore.getUserInfoAction()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate()
  if (!valid) return

  passwordLoading.value = true
  try {
    await updateProfile({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleIdFrontSuccess = (response, file) => {
  verificationForm.value.idFront = response.data.url
  ElMessage.success('身份证正面上传成功')
}

const handleIdBackSuccess = (response, file) => {
  verificationForm.value.idBack = response.data.url
  ElMessage.success('身份证反面上传成功')
}

const beforeIdUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPG) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
  }
  return isJPG && isLt5M
}

const submitVerification = async () => {
  if (!verificationForm.value.realName.trim()) {
    ElMessage.warning('请填写真实姓名')
    return
  }
  if (!verificationForm.value.idNumber.trim()) {
    ElMessage.warning('请填写身份证号码')
    return
  }
  if (!verificationForm.value.idFront) {
    ElMessage.warning('请上传身份证正面')
    return
  }
  if (!verificationForm.value.idBack) {
    ElMessage.warning('请上传身份证反面')
    return
  }
  
  verificationLoading.value = true
  try {
    // 注意：需要先在 API 中添加认证相关方法
    // await submitVerification(verificationForm.value)
    ElMessage.success('认证信息提交成功，等待审核')
    // 模拟审核状态更新
    userStore.userInfo.verificationStatus = 'PENDING'
    verificationForm.value = {
      realName: '',
      idNumber: '',
      idFront: '',
      idBack: ''
    }
  } catch (error) {
    ElMessage.error('认证提交失败')
  } finally {
    verificationLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  padding: 30px;
}

.profile-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  padding: 40px 0;
}

.stat-item {
  text-align: center;
  padding: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.stat-value {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  opacity: 0.9;
}

/* 头像上传样式 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.avatar-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #667eea;
}

.avatar-upload {
  margin-top: 10px;
}

.avatar-uploader {
  text-align: center;
}

/* 实名认证样式 */
.verification-section {
  padding: 20px 0;
}

.verification-status {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 30px;
}

.verification-status.verified {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.verification-status.pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.status-icon {
  font-size: 48px;
}

.status-text h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.status-text p {
  margin: 0;
  opacity: 0.9;
}

.verification-form {
  padding: 20px 0;
}

.id-uploader {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.id-preview {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #f9f9f9;
}

.id-image {
  width: 200px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-card {
    padding: 20px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .avatar-image {
    width: 100px;
    height: 100px;
  }
  
  .verification-status {
    flex-direction: column;
    text-align: center;
  }
  
  .id-preview {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .id-image {
    width: 100%;
    height: auto;
  }
}
</style>
