<template>
  <div class="diagnosis-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>🩺 智能简历诊断</span>
        </div>
      </template>

      <!-- 输入区域 -->
      <div class="input-section">
        <el-input
          v-model="resumeText"
          type="textarea"
          :rows="8"
          placeholder="请粘贴您的简历内容...&#10;&#10;包含：个人技能、项目经验、工作年限、求职意向等信息"
          maxlength="10000"
          show-word-limit
        />
        <div class="action-bar">
          <el-button type="primary" size="large" :loading="loading" @click="diagnose">
            <el-icon v-if="!loading"><MagicStick /></el-icon>
            {{ loading ? 'AI 诊断中...' : '开始诊断' }}
          </el-button>
          <el-button size="large" @click="clearAll" :disabled="loading">清空</el-button>
        </div>
        <p class="input-tip">💡 AI 将为您匹配最适合的岗位并生成详细的诊断报告（约需 3 分钟）</p>
      </div>

      <!-- 加载提示 -->
      <div v-if="loading" class="loading-section">
        <el-progress :percentage="progress" :stroke-width="10" :duration="3" />
        <p class="loading-text">🤖 AI 正在深度分析您的简历并与岗位库进行匹配，请耐心等待...</p>
      </div>

      <!-- 诊断结果 -->
      <div v-if="diagnosisResult && !loading" class="result-section">
        <el-divider />

        <!-- 推理过程（默认隐藏） -->
        <div v-if="thinkContent" class="think-section">
          <el-button text type="info" @click="showThink = !showThink">
            {{ showThink ? '隐藏推理过程' : '展示推理过程' }}
            <el-icon class="el-icon--right"><ArrowDown v-if="!showThink" /><ArrowUp v-else /></el-icon>
          </el-button>
          <el-collapse-transition>
            <div v-show="showThink" class="think-content">
              <pre>{{ thinkContent }}</pre>
            </div>
          </el-collapse-transition>
        </div>

        <!-- 匹配结果列表 -->
        <div v-if="matches.length > 0">
          <h3 class="result-title">📋 诊断报告 — 共匹配到 {{ matches.length }} 个岗位</h3>

          <div v-for="(match, index) in matches" :key="index" class="match-card">
            <el-card shadow="hover">
              <!-- 匹配头部 -->
              <div class="match-header">
                <div class="match-info">
                  <h3>{{ match.matched_job }}</h3>
                  <div class="match-meta">
                    <el-tag type="primary" effect="plain">{{ match.type || '全职' }}</el-tag>
                    <el-tag type="warning" effect="plain" v-if="match.company">{{ match.company }}</el-tag>
                    <el-tag effect="plain" v-if="match.city">{{ match.city }}</el-tag>
                  </div>
                </div>
                <div class="match-score">
                  <el-progress
                    type="circle"
                    :percentage="match.matched_score"
                    :color="getScoreColor(match.matched_score)"
                    :width="90"
                    :stroke-width="8"
                  />
                  <span class="score-label">匹配分</span>
                </div>
              </div>

              <!-- 薪资 -->
              <div class="salary-row" v-if="match.salary">
                <span class="salary-label">💰 薪资：</span>
                <span class="salary-value">{{ match.salary }}</span>
              </div>

              <!-- 匹配原因 -->
              <div class="section-block">
                <h4><el-icon color="#67c23a"><CircleCheck /></el-icon> 匹配原因</h4>
                <p>{{ match.matched_reason }}</p>
              </div>

              <!-- 差距点 -->
              <div class="section-block" v-if="match.gap_points && match.gap_points.length">
                <h4><el-icon color="#f56c6c"><Warning /></el-icon> 差距分析</h4>
                <ul class="gap-list">
                  <li v-for="(gap, i) in match.gap_points" :key="i">{{ gap }}</li>
                </ul>
              </div>

              <!-- 面试建议 -->
              <div class="section-block" v-if="match.interview_advice && match.interview_advice.length">
                <h4><el-icon color="#409eff"><ChatDotRound /></el-icon> 面试建议</h4>
                <ul class="advice-list">
                  <li v-for="(advice, i) in match.interview_advice" :key="i">{{ advice }}</li>
                </ul>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 如果返回的是纯文本 -->
        <div v-else-if="rawResult" class="raw-result">
          <h3>📋 诊断报告</h3>
          <div class="markdown-body" v-html="renderedResult"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onDeactivated } from 'vue'
import { difyApi } from '../api'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

const resumeText = ref('')
const loading = ref(false)
const progress = ref(0)
const diagnosisResult = ref(false)
const matches = ref([])
const rawResult = ref('')
const thinkContent = ref('')
const showThink = ref(false)
let progressTimer = null

const renderedResult = computed(() => {
  return rawResult.value ? marked(rawResult.value) : ''
})

function getScoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

async function diagnose() {
  if (!resumeText.value.trim()) {
    ElMessage.warning('请输入简历内容')
    return
  }

  loading.value = true
  diagnosisResult.value = false
  matches.value = []
  rawResult.value = ''
  thinkContent.value = ''
  progress.value = 0

  progressTimer = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.random() * 5
    }
  }, 2000)

  try {
    const res = await difyApi.diagnoseResume(resumeText.value.trim())
    let data = res.data

    // 提取 <think> 标签内容
    const thinkMatch = data.match(/<think>([\s\S]*?)<\/think>/)
    if (thinkMatch) {
      thinkContent.value = thinkMatch[1].trim()
      data = data.replace(/<think>[\s\S]*?<\/think>/, '').trim()
    }

    // 尝试解析 JSON
    try {
      const parsed = JSON.parse(data)
      if (Array.isArray(parsed)) {
        matches.value = parsed
      } else if (parsed.matches) {
        matches.value = parsed.matches
      } else if (parsed.result) {
        // 嵌套的 result 字段
        const inner = parsed.result
        const innerThink = inner.match(/<think>([\s\S]*?)<\/think>/)
        if (innerThink) {
          thinkContent.value = innerThink[1].trim()
        }
        const cleanJson = inner.replace(/<think>[\s\S]*?<\/think>/, '').trim()
        try {
          const innerParsed = JSON.parse(cleanJson)
          matches.value = Array.isArray(innerParsed) ? innerParsed : [innerParsed]
        } catch {
          rawResult.value = cleanJson
        }
      } else {
        matches.value = [parsed]
      }
    } catch {
      rawResult.value = data
    }

    diagnosisResult.value = true
    progress.value = 100
    ElMessage.success('诊断完成！')
  } catch (e) {
    ElMessage.error('诊断失败，请稍后重试')
  } finally {
    clearInterval(progressTimer)
    loading.value = false
  }
}

function clearAll() {
  resumeText.value = ''
  diagnosisResult.value = false
  matches.value = []
  rawResult.value = ''
  thinkContent.value = ''
}

onDeactivated(() => {
  if (progressTimer) clearInterval(progressTimer)
})
</script>

<style scoped>
.diagnosis-page {
  max-width: 1000px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.input-section {
  max-width: 800px;
  margin: 0 auto;
}

.action-bar {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.input-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 13px;
}

.loading-section {
  margin-top: 40px;
  text-align: center;
}

.loading-text {
  margin-top: 16px;
  color: #909399;
}

.result-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
}

.match-card {
  margin-bottom: 20px;
}

.match-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.match-info h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 8px;
}

.match-meta {
  display: flex;
  gap: 8px;
}

.match-score {
  text-align: center;
}

.score-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.salary-row {
  padding: 12px 16px;
  background: #fdf6ec;
  border-radius: 8px;
  margin-bottom: 20px;
}

.salary-label {
  color: #909399;
}

.salary-value {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.section-block {
  margin-top: 20px;
}

.section-block h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: #303133;
  margin-bottom: 12px;
}

.section-block > p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  padding-left: 24px;
}

.gap-list, .advice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.gap-list li, .advice-list li {
  position: relative;
  padding: 8px 0 8px 24px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  border-bottom: 1px solid #f5f5f5;
}

.gap-list li::before {
  content: '⚠️';
  position: absolute;
  left: 0;
}

.advice-list li::before {
  content: '💡';
  position: absolute;
  left: 0;
}

.think-section {
  margin-bottom: 20px;
}

.think-content {
  margin-top: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
}

.think-content pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 0;
}

.raw-result {
  margin-top: 20px;
}

.raw-result h3 {
  margin-bottom: 16px;
}

.markdown-body {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  line-height: 1.8;
}
</style>
