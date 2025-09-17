# 项目概览

本项目是基于 JeecgBoot 3.7.2 版本的能源管理系统 (EMS) 项目。JeecgBoot 是一个基于代码生成器的低代码开发平台，采用前后端分离架构，后端基于 SpringBoot 2.7.18 和 Mybatis-plus，前端基于 Vue3、Vite5 和 TypeScript。

该项目在标准 JeecgBoot 的基础上，扩展了能源管理相关的业务模块，主要集中在 `jeecg-module-energy` 模块中。

## 核心技术栈

- **后端**:
  - Java 8+
  - Spring Boot 2.7.18
  - Spring Cloud Alibaba (微服务, 可选)
  - MyBatis Plus 3.5.3.2
  - Maven
  - MySQL 5.7+ (主要数据库)
  - Redis (缓存)
  - Shiro + JWT (安全框架)
  - InfluxDB (用于实时数据存储, 通过 `influxdb-java` 客户端)
- **前端**:
  - Vue 3
  - Vite 5
  - TypeScript
  - Ant Design Vue 4
  - Pinia (状态管理)
  - ECharts (图表)
- **其他**:
  - Lombok (简化Java代码)
  - Swagger/Knife4j (API文档)
  - POI (Excel导出)
  - Hutool (工具库)

# 项目结构

```
jeecg-boot-parent (根POM)
├─jeecg-boot-base-core (核心共通模块)
├─jeecg-module-demo (示例模块)
├─jeecg-module-system (系统管理模块)
│  ├─jeecg-system-biz (系统业务实现)
│  ├─jeecg-system-start (单体启动项目)
│  └─jeecg-system-api (系统对外API)
└─jeecg-module-energy (能源管理模块 - 本项目核心业务)
jeecg-server-cloud (微服务模块, 可选)
```

# 核心业务模块: jeecg-module-energy

该模块是本项目的核心，实现了能源相关的业务逻辑。

## 主要功能

1.  **能源分析对比**: 提供设备能效对比功能，包括趋势图、汇总数据和明细表格。
    -   接口: `/energy/analysis/**`
    -   控制器: `EnergyAnalysisController`
    -   服务: `IEnergyAnalysisService` 及其实现 `EnergyAnalysisServiceImpl`
    -   数据来源: `tb_ep_equ_energy_*` 系列表 (日/月/年统计表), `tb_module` (仪表信息表)

2.  **实时数据监控**: (根据文件名推测) 可能包含实时数据的获取和展示。
    -   相关文件: `Real_Data_Monitor.md`, `Real_Monitor.md`, `Real_Run_Status.md` 等。

3.  **负荷查询**: (根据文件名推测) 可能包含对设备负荷的查询和分析。
    -   相关文件: `测试负荷查询修改_20250809.md`, `调试指南_负荷查询不一致问题.md` 等。

4.  **InfluxDB同步**: (根据文件名推测) 可能包含将数据从其他源同步到 InfluxDB 的任务。
    -   相关文件: `InfluxDBSyncJob.md`。

## 关键实体类 (Entity)

-   `TbModule`: 代表一个仪表或设备。
-   `TbEpEquEnergyDaycount`: 日粒度能耗统计数据。
-   `TbEpEquEnergyMonthcount`: 月粒度能耗统计数据。
-   `TbEpEquEnergyYearcount`: 年粒度能耗统计数据。

## 关键服务类 (Service)

-   `IEnergyAnalysisService`: 能源分析对比服务接口。
-   `EnergyAnalysisServiceImpl`: 能源分析对比服务实现，处理数据查询、计算和导出。

## 关键控制器类 (Controller)

-   `EnergyAnalysisController`: 提供能源分析对比相关的 RESTful API。

# 构建与运行

## 环境要求

-   Java 8+
-   Maven 3.6+
-   MySQL 5.7+
-   Node.js 16+ (前端)
-   npm 或 pnpm (前端)

## 后端构建与运行

1.  **初始化数据库**: 使用 `db/jeecgboot-mysql-5.7.sql` 脚本创建数据库和表结构。
2.  **配置**: 修改 `jeecg-module-system/jeecg-system-start/src/main/resources/application.yml` 文件中的数据库连接等配置。
3.  **Maven 构建**:
    ```bash
    cd jeecg-boot
    mvn clean install
    ```
4.  **运行单体应用**:
    ```bash
    cd jeecg-module-system/jeecg-system-start
    mvn spring-boot:run
    ```
    或者运行打包后的 jar 文件:
    ```bash
    java -jar target/jeecg-system-start-3.7.2.jar
    ```
5.  **运行微服务 (可选)**: 需要先启动 Nacos 等微服务组件，然后分别启动 `jeecg-server-cloud` 下的各个微服务模块。

## 前端构建与运行

1.  **安装依赖**:
    ```bash
    npm install
    ```
    或
    ```bash
    pnpm install
    ```
2.  **开发模式运行**:
    ```bash
    npm run dev
    ```
    或
    ```bash
    pnpm dev
    ```
3.  **构建生产版本**:
    ```bash
    npm run build
    ```
    或
    ```bash
    pnpm build
    ```

# 开发约定

-   **代码规范**: 遵循 Java 和 Spring Boot 的通用规范，使用 Lombok 简化代码。
-   **API 设计**: 使用 RESTful 风格，通过 Swagger 注解提供 API 文档。
-   **数据库**: 使用 MyBatis Plus 进行数据库操作，注意实体类与表结构的映射。
-   **前端**: 遵循 Vue3 和 TypeScript 的最佳实践，使用 Ant Design Vue 组件库。
-   **日志**: 使用 Slf4j 进行日志记录。
-   **安全**: 使用 Shiro 和 JWT 进行权限控制。