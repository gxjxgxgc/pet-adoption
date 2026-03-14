import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router'
import './assets/styles/global.css'

const app = createApp(App)

// 注册常用图标（不是全部）
const commonIcons = [
  'User', 'Lock', 'Home', 'View', 'Edit', 'Delete', 'Search',
  'Plus', 'Document', 'Picture', 'Star', 'ChatDotRound', 'Location'
]

import('./utils/icons.js').then(({ registerIcons }) => {
  registerIcons(app, commonIcons)
})

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')