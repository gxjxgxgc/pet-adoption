<template>
  <div class="admin-dashboard">
    <h2>🎛️ 管理后台</h2>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.petCount }}</div>
            <div class="stat-label">宠物总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.adoptionCount }}</div>
            <div class="stat-label">领养申请</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingCount }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据统计图表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>领养趋势分析</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="loadChartData"
              />
            </div>
          </template>
          <div class="chart-container">
            <v-chart ref="adoptionChart" :option="adoptionChartOption" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>用户增长趋势</span>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="loadChartData"
              />
            </div>
          </template>
          <div class="chart-container">
            <v-chart ref="userChart" :option="userChartOption" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>宠物类型分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart ref="petTypeChart" :option="petTypeChartOption" />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>领养状态分布</span>
            </div>
          </template>
          <div class="chart-container">
            <v-chart ref="adoptionStatusChart" :option="adoptionStatusChartOption" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="content-tabs">
      <el-tab-pane label="领养审核" name="adoptions">
        <adoption-review />
      </el-tab-pane>
      <el-tab-pane label="宠物管理" name="pets">
        <pet-management />
      </el-tab-pane>
      <el-tab-pane label="用户管理" name="users">
        <user-management />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { User, Star, Document, Clock } from '@element-plus/icons-vue'
import { getAdminStats } from '@/api/user'
import AdoptionReview from './components/AdoptionReview.vue'
import PetManagement from './components/PetManagement.vue'
import UserManagement from './components/UserManagement.vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册必要的组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DatasetComponent,
  TransformComponent
])

const activeTab = ref('adoptions')
const stats = ref({
  userCount: 0,
  petCount: 0,
  adoptionCount: 0,
  pendingCount: 0
})

const dateRange = ref([
  new Date(new Date().setMonth(new Date().getMonth() - 3)),
  new Date()
])

// 模拟图表数据
const adoptionChartOption = ref({
  title: {
    text: '领养趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['领养申请', '领养成功'],
    bottom: 10
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '领养申请',
      type: 'line',
      stack: 'Total',
      data: [120, 132, 101, 134, 90, 230],
      smooth: true
    },
    {
      name: '领养成功',
      type: 'line',
      stack: 'Total',
      data: [820, 932, 901, 934, 1290, 1330],
      smooth: true
    }
  ]
})

const userChartOption = ref({
  title: {
    text: '用户增长',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['新用户注册'],
    bottom: 10
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '15%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['1月', '2月', '3月', '4月', '5月', '6月']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: '新用户注册',
      type: 'line',
      data: [120, 190, 300, 500, 700, 900],
      smooth: true,
      areaStyle: {}
    }
  ]
})

const petTypeChartOption = ref({
  title: {
    text: '宠物类型分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '宠物类型',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 60, name: '猫' },
        { value: 30, name: '狗' },
        { value: 10, name: '其他' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

const adoptionStatusChartOption = ref({
  title: {
    text: '领养状态分布',
    left: 'center'
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '领养状态',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 40, name: '待审核' },
        { value: 30, name: '已通过' },
        { value: 20, name: '已拒绝' },
        { value: 10, name: '已完成' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

const loadStats = async () => {
  try {
    const res = await getAdminStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

const loadChartData = () => {
  // 这里可以根据日期范围加载实际的图表数据
  console.log('加载图表数据:', dateRange.value)
  // 暂时使用模拟数据
}

onMounted(() => {
  loadStats()
  loadChartData()
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.admin-dashboard h2 {
  margin-bottom: 30px;
  color: #333;
}

.stats-row {
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.content-tabs {
  background: white;
  padding: 20px;
  border-radius: 12px;
}

.charts-row {
  margin-bottom: 30px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  width: 100%;
}

@media (max-width: 768px) {
  .chart-container {
    height: 250px;
  }
}
</style>
