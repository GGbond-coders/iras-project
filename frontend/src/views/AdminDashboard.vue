<!--
  @file AdminDashboard.vue
  @description 管理员仪表盘页面组件。
               展示系统核心统计数据（用户数、职位数、诊断数），
               以及近 7 天的用户注册趋势、诊断趋势和职位城市分布。
  @author IRAS Team
  @since 1.0
-->
<template>
  <div class="dashboard-page">
    <!-- 核心数据卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.userCount || 0 }}</div>
          <div class="stat-label">注册用户数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.jobCount || 0 }}</div>
          <div class="stat-label">职位总数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.diagnosisCount || 0 }}</div>
          <div class="stat-label">诊断总次数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 近 7 天用户注册趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">近 7 天用户注册趋势</div>
          </template>
          <div v-if="stats.userTrend && stats.userTrend.length" class="trend-list">
            <div v-for="item in stats.userTrend" :key="item.date" class="trend-item">
              <span class="trend-date">{{ item.date }}</span>
              <el-progress :percentage="getPercent(item.count, maxUserTrend)" :stroke-width="16" :text-inside="true" />
              <span class="trend-count">{{ item.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 近 7 天诊断趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">近 7 天诊断趋势</div>
          </template>
          <div v-if="stats.diagnosisTrend && stats.diagnosisTrend.length" class="trend-list">
            <div v-for="item in stats.diagnosisTrend" :key="item.date" class="trend-item">
              <span class="trend-date">{{ item.date }}</span>
              <el-progress :percentage="getPercent(item.count, maxDiagnosisTrend)" :stroke-width="16" :text-inside="true" color="#e6a23c" />
              <span class="trend-count">{{ item.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 城市分布 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div class="card-header">职位城市分布 Top 10</div>
      </template>
      <div v-if="stats.cityDistribution && stats.cityDistribution.length" class="city-list">
        <div v-for="(item, index) in stats.cityDistribution" :key="item.city" class="city-item">
          <span class="city-rank" :class="{ 'top3': index < 3 }">{{ index + 1 }}</span>
          <span class="city-name">{{ item.city }}</span>
          <el-progress :percentage="getPercent(item.count, maxCityCount)" :stroke-width="14" :text-inside="true" style="flex: 1" />
          <span class="city-count">{{ item.count }} 个</span>
        </div>
      </div>
      <el-empty v-else description="暂无数据" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminApi } from '../api'
import { ElMessage } from 'element-plus'

const stats = ref({})

const maxUserTrend = computed(() => {
  if (!stats.value.userTrend?.length) return 1
  return Math.max(...stats.value.userTrend.map(i => i.count), 1)
})

const maxDiagnosisTrend = computed(() => {
  if (!stats.value.diagnosisTrend?.length) return 1
  return Math.max(...stats.value.diagnosisTrend.map(i => i.count), 1)
})

const maxCityCount = computed(() => {
  if (!stats.value.cityDistribution?.length) return 1
  return Math.max(...stats.value.cityDistribution.map(i => i.count), 1)
})

function getPercent(value, max) {
  return Math.round((value / max) * 100)
}

async function fetchStats() {
  try {
    const res = await adminApi.getStatistics()
    stats.value = res.data
  } catch (e) {
    ElMessage.error('获取统计数据失败')
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.trend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trend-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.trend-date {
  width: 100px;
  font-size: 13px;
  color: #606266;
}

.trend-count {
  width: 40px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.city-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.city-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.city-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e4e7ed;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.city-rank.top3 {
  background: #409eff;
  color: #fff;
}

.city-name {
  width: 80px;
  font-size: 14px;
  color: #303133;
}

.city-count {
  width: 50px;
  text-align: right;
  font-size: 13px;
  color: #909399;
}
</style>
