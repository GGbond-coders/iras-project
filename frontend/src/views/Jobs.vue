<template>
  <div class="jobs-page">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>🔍 职位搜索</span>
        </div>
      </template>

      <el-form :model="searchForm" inline>
        <el-form-item label="职位名称">
          <el-input v-model="searchForm.jobName" placeholder="请输入职位名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="searchForm.city" placeholder="请输入城市" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-input v-model="searchForm.salaryMin" placeholder="最低" clearable style="width: 100px" />
          <span style="margin: 0 8px; color: #909399">—</span>
          <el-input v-model="searchForm.salaryMax" placeholder="最高" clearable style="width: 100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 结果列表 -->
    <el-card class="result-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>📊 搜索结果（共 {{ total }} 条）</span>
        </div>
      </template>

      <el-table :data="jobList" stripe style="width: 100%" v-loading="loading" empty-text="暂无职位数据">
        <el-table-column prop="jobName" label="职位名称" min-width="150" />
        <el-table-column prop="companyName" label="公司名称" min-width="150" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="salary" label="薪资" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">{{ row.salary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type || '全职' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.size"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchJobs"
          @size-change="fetchJobs"
        />
      </div>
    </el-card>

    <!-- 职位详情弹窗 -->
    <el-dialog v-model="detailVisible" title="职位详情" width="700px">
      <div v-if="currentJob" class="job-detail">
        <h2>{{ currentJob.jobName }}</h2>
        <el-descriptions :column="2" border style="margin-top: 16px">
          <el-descriptions-item label="公司名称">{{ currentJob.companyName }}</el-descriptions-item>
          <el-descriptions-item label="工作城市">{{ currentJob.city }}</el-descriptions-item>
          <el-descriptions-item label="薪资范围">{{ currentJob.salary }}</el-descriptions-item>
          <el-descriptions-item label="职位类型">{{ currentJob.type || '全职' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top: 20px; color: #303133">职位描述</h4>
        <div class="jd-content">{{ currentJob.jdText }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { jobApi } from '../api'

const loading = ref(false)
const jobList = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentJob = ref(null)

const searchForm = reactive({
  jobName: '',
  city: '',
  salaryMin: '',
  salaryMax: '',
  page: 1,
  size: 20
})

async function fetchJobs() {
  loading.value = true
  try {
    const res = await jobApi.search(searchForm)
    jobList.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  searchForm.page = 1
  fetchJobs()
}

function handleReset() {
  searchForm.jobName = ''
  searchForm.city = ''
  searchForm.salaryMin = ''
  searchForm.salaryMax = ''
  searchForm.page = 1
  fetchJobs()
}

function viewDetail(job) {
  currentJob.value = job
  detailVisible.value = true
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.jobs-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.job-detail h2 {
  color: #303133;
  font-size: 22px;
}

.jd-content {
  margin-top: 10px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}
</style>
