<template>
  <div class="profile-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>职能画像分析</span>
        </div>
      </template>

      <div class="input-section">
        <el-input
          v-model="jobName"
          placeholder="请输入职位名称，如：软件工程师、产品经理、数据分析师..."
          size="large"
          clearable
          @keyup.enter="analyze"
        >
          <template #prepend>职位名称</template>
          <template #append>
            <el-button type="primary" :loading="loading" @click="analyze">
              {{ loading ? 'AI 分析中...' : '开始分析' }}
            </el-button>
          </template>
        </el-input>
        <p class="input-tip">AI 将为您生成该职位的技能要求、工具清单、经验要求等完整画像（约需 2 分钟）</p>
      </div>

      <!-- 加载提示 -->
      <div v-if="loading" class="loading-section">
        <p class="loading-text">AI 正在深度分析职位画像，请耐心等待...</p>
      </div>

      <!-- 分析结果 -->
      <div v-if="profileData && !loading" class="result-section">
        <el-divider />

        <div class="profile-header">
          <h2>{{ profileData.job_title }}</h2>
          <el-tag type="success" size="large">分析完成</el-tag>
        </div>

        <el-row :gutter="20" style="margin-top: 24px">
          <!-- 硬技能 -->
          <el-col :span="12">
            <el-card class="skill-card" shadow="hover">
              <template #header>
                <div class="skill-header">
                  <span>硬技能要求</span>
                </div>
              </template>
              <ul class="skill-list">
                <li v-for="(skill, i) in profileData.hard_skills" :key="i">
                  {{ skill }}
                </li>
              </ul>
            </el-card>
          </el-col>

          <!-- 软技能 -->
          <el-col :span="12">
            <el-card class="skill-card" shadow="hover">
              <template #header>
                <div class="skill-header">
                  <span>软技能要求</span>
                </div>
              </template>
              <ul class="skill-list">
                <li v-for="(skill, i) in profileData.soft_skills" :key="i">
                  {{ skill }}
                </li>
              </ul>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <!-- 工具 -->
          <el-col :span="12">
            <el-card class="skill-card" shadow="hover">
              <template #header>
                <div class="skill-header">
                  <span>常用工具</span>
                </div>
              </template>
              <div class="tool-tags">
                <el-tag v-for="(tool, i) in profileData.tools" :key="i" type="info" effect="plain" class="tool-tag">
                  {{ tool }}
                </el-tag>
              </div>
            </el-card>
          </el-col>

          <!-- 经验与学历 -->
          <el-col :span="12">
            <el-card class="skill-card" shadow="hover">
              <template #header>
                <div class="skill-header">
                  <span>经验与学历</span>
                </div>
              </template>
              <div class="exp-section">
                <h4>工作经验</h4>
                <p>{{ profileData.experience }}</p>
              </div>
              <div class="exp-section" style="margin-top: 16px">
                <h4>学历要求</h4>
                <p>{{ profileData.education }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { difyApi } from '../api'
import { ElMessage } from 'element-plus'

const jobName = ref('')
const loading = ref(false)
const profileData = ref(null)

async function analyze() {
  if (!jobName.value.trim()) {
    ElMessage.warning('请输入职位名称')
    return
  }

  loading.value = true
  profileData.value = null

  try {
    const res = await difyApi.getJobProfile(jobName.value.trim())
    // 解析返回的 JSON 字符串
    const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
    profileData.value = data
    ElMessage.success('分析完成！')
  } catch (e) {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.input-section {
  max-width: 800px;
  margin: 0 auto;
}

.input-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 13px;
  text-align: center;
}

.loading-section {
  margin-top: 40px;
  text-align: center;
}

.loading-text {
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-header h2 {
  font-size: 24px;
  color: #303133;
}

.skill-card {
  height: 100%;
}

.skill-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.skill-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.skill-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.skill-list li:last-child {
  border-bottom: none;
}

.tool-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tool-tag {
  font-size: 13px;
}

.exp-section h4 {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}

.exp-section p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
</style>
