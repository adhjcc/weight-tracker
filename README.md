# 体重记录

一款简洁的Android体重记录APP。

## 功能

### 体重记录
- 手动输入体重，自动记录对应日期
- 支持选择任意日期补充历史数据
- 快捷微调按钮（+0.1kg / -0.1kg）

### BMI计算
- 用户输入身高后自动计算BMI
- 根据BMI显示体型分类（偏瘦/正常/偏胖/肥胖）

### 折线图
- 有数据即可显示折线图
- 支持体重/BMI图表切换
- **缺失数据处理**：如果某天没有记录，绘制折线图时使用前一天的数据填充，如果前面没有数据，则使用后面最近一天的数据填充
- **时间范围选择**：支持7天、14天、30天、90天、180天、360天六个固定选项
- 图表默认显示完整的所选时间范围，无需横向滑动查看
- 折线图右端始终为今天的日期

### 数据统计
- **趋势显示**：
  - 较昨日体重变化
  - 较上周体重变化
- **30天统计**：
  - 平均体重
  - 最高体重
  - 最低体重
- 目标体重差距显示

### 提醒功能
- 可设置每日提醒时间
- 支持开机自启

### 数据管理
- 历史记录编辑/删除
- 数据导出/导入（CSV格式）

### UI设计
- 清新浅色主题
- 透明状态栏适配（通知栏不显示APP背景色）
- 简洁的卡片式布局
- 首次使用引导（提示设置身高和目标体重）

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **数据库**：Room
- **图表**：MPAndroidChart
- **状态管理**：ViewModel + StateFlow
- **数据存储**：DataStore Preferences

## 编译

```bash
export JAVA_HOME=/home/adhjc/jdk-17
./gradlew assembleDebug
```

APK位置：`app/build/outputs/apk/debug/app-debug.apk`

## 项目结构

```
app/src/main/java/com/weighttracker/
├── data/
│   ├── local/          # Room数据库、DataStore
│   └── repository/     # 数据仓库
├── domain/
│   └── model/          # 领域模型
├── receiver/           # 广播接收器（提醒、开机启动）
├── ui/
│   ├── screens/        # 页面组件
│   └── theme/          # 主题配置
└── util/               # 工具类
```
