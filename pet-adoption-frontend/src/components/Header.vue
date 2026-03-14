<template>
  <el-header class="header">
    <div class="container">
      <div class="header-left">
        <router-link to="/" class="logo">
          🐾 宠物领养系统
        </router-link>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/pets">宠物列表</router-link>
        </nav>
      </div>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <span class="welcome">欢迎，{{ userStore.username }}</span>
          <router-link v-if="userStore.isShelter || userStore.isAdmin" to="/pets/publish">
            发布宠物
          </router-link>
          <router-link to="/adoptions">我的领养</router-link>
          <router-link to="/favorites">我的收藏</router-link>
          <router-link to="/profile">个人中心</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin">
            管理后台
          </router-link>
          <el-button @click="handleLogout" type="danger" size="small">退出</el-button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logoutAction()
  router.push('/')
}
</script>

<style scoped>
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  text-decoration: none;
}

.nav {
  display: flex;
  gap: 20px;
}

.nav a {
  color: #333;
  text-decoration: none;
  transition: color 0.3s;
}

.nav a:hover,
.nav a.router-link-active {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-right a {
  color: #333;
  text-decoration: none;
  font-size: 14px;
}

.header-right a:hover {
  color: #409eff;
}

.welcome {
  color: #666;
  font-size: 14px;
}
</style>
