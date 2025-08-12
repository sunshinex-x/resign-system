# 应用重签名管理系统前端

## 项目介绍

本项目是应用重签名服务的管理前端，基于Vue 3 + Vite + Pinia + Element Plus开发，提供了任务管理、统计分析等功能。

## 技术栈

- **Vue 3**：渐进式JavaScript框架
- **Vite**：下一代前端构建工具
- **Pinia**：Vue的状态管理库
- **Vue Router**：Vue.js官方路由管理器
- **Element Plus**：基于Vue 3的组件库
- **Axios**：基于Promise的HTTP客户端
- **ECharts**：数据可视化图表库

## 功能特性

- 任务管理：创建、查询、重试、删除重签名任务
- 数据统计：展示任务数量、成功率等统计信息
- 系统管理：日志查看、系统配置等

## 项目结构

```
resign-admin-vue3/
├── public/               # 静态资源
├── src/
│   ├── api/             # API接口
│   ├── assets/          # 资源文件
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── store/           # Pinia状态管理
│   ├── utils/           # 工具函数
│   ├── views/           # 页面组件
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html           # HTML模板
├── package.json         # 项目依赖
└── vite.config.js       # Vite配置
```

## 开发指南

### 安装依赖

```bash
cd resign-admin-vue3
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 接口说明

本项目默认连接到后端API，接口前缀为`/api`，可在`vite.config.js`中修改代理配置。

## 浏览器兼容性

- Chrome
- Firefox
- Safari
- Edge

## 贡献指南

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request