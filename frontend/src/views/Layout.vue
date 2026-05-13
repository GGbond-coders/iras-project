<!--
  @file Layout.vue
  @description 主布局组件。
               采用 Element Plus 的 Container 布局，包含左侧边栏和右侧内容区。
               侧边栏包含导航菜单（职位检索、职能画像、智能诊断），
               顶部栏包含面包屑导航和用户下拉菜单（退出登录）。
               使用 keep-alive 缓存子页面组件，避免重复渲染。
  @author IRAS Team
  @since 1.0
-->
<template>
  <el-container class="layout-container">
    <!-- 左侧边栏 - 导航菜单 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <!-- 侧边栏标题（点击可折叠/展开） -->
      <div class="aside-header" @click="isCollapse = !isCollapse">
        <span class="aside-title">IRAS</span>
      </div>

      <!-- 导航菜单，router 模式下点击菜单项自动跳转对应路由 -->
      <el-menu
        :default-active="$route.path"
        router
        :collapse="isCollapse"
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#60a5fa"
        class="aside-menu"
      >
        <el-menu-item index="/jobs">
          <template #title>职位检索</template>
        </el-menu-item>
        <el-menu-item index="/job-profile">
          <template #title>职能画像</template>
        </el-menu-item>
        <el-menu-item index="/diagnosis">
          <template #title>智能诊断</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主内容区 -->
    <el-container>
      <!-- 顶部栏 - 面包屑导航 + 用户信息 -->
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 面包屑导航：显示当前页面路径 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.username }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 - 渲染子路由页面 -->
      <el-main class="layout-main">
        <!-- 使用 keep-alive 缓存页面组件，避免切换路由时重新渲染 -->
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
/**
 * 主布局页面逻辑。
 * <p>
 * 功能：
 * <ul>
 *   <li>侧边栏折叠/展开控制</li>
 *   <li>用户退出登录</li>
 * </ul>
 * </p>
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

/** 侧边栏折叠状态 */
const isCollapse = ref(false)

/**
 * 处理下拉菜单命令。
 * @param {string} command - 命令名称（'logout'）
 */
function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()        // 清除用户状态
    router.push('/login')     // 跳转到登录页
  }
}
</script>

<style scoped>
/* 整体布局容器 - 占满视口高度 */
.layout-container {
  height: 100vh;
}

/* 侧边栏样式 */
.layout-aside {
  background-color: #1e293b;   /* 深色背景 */
  transition: width 0.3s;      /* 折叠/展开动画 */
  overflow: hidden;
}

/* 侧边栏标题区域 */
.aside-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  gap: 10px;
  border-bottom: 1px solid #334155;
}

.aside-logo {
  font-size: 28px;
}

.aside-title {
  color: #f1f5f9;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 2px;
}

/* 隐藏菜单右边框 */
.aside-menu {
  border-right: none;
}

/* 顶部栏样式 */
.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 0 20px;
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

/* 主内容区域 */
.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>
