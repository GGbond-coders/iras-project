<template>
  <div class="diagnosis-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>智能简历诊断</span>
        </div>
      </template>

      <!-- 文件上传区域 -->
      <div class="upload-section">
        <!-- 未选择文件时：显示上传区域 -->
        <el-upload
          v-if="!selectedFile"
          ref="uploadRef"
          class="resume-upload"
          drag
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          accept=".txt,.text,.pdf,.doc,.docx"
        >
          <div class="el-upload__text">
            将简历文件拖到此处，或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .txt / .text / .pdf / .doc / .docx 格式，文件大小不超过 10MB
            </div>
          </template>
        </el-upload>

        <!-- 已选择文件时：显示文件信息 + 重新上传按钮 -->
        <div v-else class="file-selected">
          <div class="file-info">
            <span class="file-name">{{ selectedFile.name }}</span>
            <span class="file-size">({{ (selectedFile.size / 1024).toFixed(1) }} KB)</span>
          </div>
          <el-button size="small" @click="reUpload" :disabled="loading">重新上传</el-button>
        </div>

        <div class="action-bar">
          <el-button type="primary" size="large" :loading="loading" :disabled="!selectedFile" @click="diagnose">
            {{ loading ? 'AI 诊断中...' : '开始诊断' }}
          </el-button>
          <el-button size="large" @click="clearAll" :disabled="loading">清空</el-button>
        </div>
        <p class="input-tip">上传简历文件后，AI 将为您匹配最适合的岗位并生成详细的诊断报告（约需 3 分钟）</p>
      </div>

      <!-- 加载提示 -->
      <div v-if="loading" class="loading-section">
        <p class="loading-text">AI 正在深度分析您的简历并与岗位库进行匹配，请耐心等待...</p>
      </div>

      <!-- 诊断结果 -->
      <div v-if="diagnosisResult && !loading" class="result-section">
        <el-divider />

        <!-- 推理过程（默认隐藏） -->
        <div v-if="thinkContent" class="think-section">
          <el-button text type="info" @click="showThink = !showThink">
            {{ showThink ? '隐藏推理过程' : '展示推理过程' }}
          </el-button>
          <el-collapse-transition>
            <div v-show="showThink" class="think-content">
              <pre>{{ thinkContent }}</pre>
            </div>
          </el-collapse-transition>
        </div>

        <!-- 匹配结果列表 -->
        <div v-if="matches.length > 0">
          <h3 class="result-title">诊断报告 — 共匹配到 {{ matches.length }} 个岗位</h3>

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
                  <span class="score-value" :style="{ color: getScoreColor(match.matched_score) }">{{ match.matched_score }}%</span>
                  <span class="score-label">匹配分</span>
                </div>
              </div>

              <!-- 薪资 -->
              <div class="salary-row" v-if="match.salary">
                <span class="salary-label">薪资：</span>
                <span class="salary-value">{{ match.salary }}</span>
              </div>

              <!-- 匹配原因 -->
              <div class="section-block">
                <h4>匹配原因</h4>
                <p>{{ match.matched_reason }}</p>
              </div>

              <!-- 差距点 -->
              <div class="section-block" v-if="match.gap_points && match.gap_points.length">
                <h4>差距分析</h4>
                <ul class="gap-list">
                  <li v-for="(gap, i) in match.gap_points" :key="i">{{ gap }}</li>
                </ul>
              </div>

              <!-- 面试建议 -->
              <div class="section-block" v-if="match.interview_advice && match.interview_advice.length">
                <h4>面试建议</h4>
                <ul class="advice-list">
                  <li v-for="(advice, i) in match.interview_advice" :key="i">{{ advice }}</li>
                </ul>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 如果返回的是纯文本 -->
        <div v-else-if="rawResult" class="raw-result">
          <h3>诊断报告</h3>
          <div class="markdown-body" v-html="renderedResult"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { difyApi } from '../api'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'

const uploadRef = ref(null)
const selectedFile = ref(null)
const loading = ref(false)
const diagnosisResult = ref(false)
const matches = ref([])
const rawResult = ref('')
const thinkContent = ref('')
const showThink = ref(false)

const renderedResult = computed(() => {
  return rawResult.value ? marked(rawResult.value) : ''
})

function getScoreColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

function handleFileChange(file) {
  selectedFile.value = file.raw
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

function reUpload() {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
}

async function diagnose() {
  if (!selectedFile.value) {
    ElMessage.warning('请先上传简历文件')
    return
  }

  loading.value = true
  diagnosisResult.value = false
  matches.value = []
  rawResult.value = ''
  thinkContent.value = ''

  try {
    const res = await difyApi.diagnoseResume(selectedFile.value)
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
    ElMessage.success('诊断完成！')
  } catch (e) {
    ElMessage.error('诊断失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function clearAll() {
  selectedFile.value = null
  uploadRef.value?.clearFiles()
  diagnosisResult.value = false
  matches.value = []
  rawResult.value = ''
  thinkContent.value = ''
}
</script>

<style scoped>
.diagnosis-page {
  max-width: 1000px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.upload-section {
  max-width: 600px;
  margin: 0 auto;
}

.resume-upload {
  width: 100%;
}

.resume-upload :deep(.el-upload-dragger) {
  padding: 40px 20px;
}

.file-selected {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.file-size {
  font-size: 13px;
  color: #909399;
}

.action-bar {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  justify-content: center;
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

.score-value {
  font-size: 28px;
  font-weight: 700;
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
  content: '';
  position: absolute;
  left: 0;
}

.advice-list li::before {
  content: '';
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
