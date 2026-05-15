# Design

## Source Of Truth

- Status: Active
- Last refreshed: 2026-05-15
- Primary product surfaces: 登录、患者管理、历史病历列表、录入新病历、轻量仪表盘、排班计划、设置。
- Evidence reviewed: `/Users/allenj/Documents/medical原型/Image 1.html`、`Image 3.html`、`Image 5.html`、`Image 7.html`、`Image 9.html`、`Image 11.html`、`Image 12.markdown` and paired PNG screenshots.

## Brand

- Personality: 专业、克制、清洁、可信，面向临床医生的工作台。
- Trust signals: 高对比标题、低噪音表面分层、明确操作状态、审计与账号安全。
- Avoid: 营销式 hero、过度装饰、深色大面积渐变、拥挤表单、过多边框。

## Product Goals

- Goals: 快速检索患者和病历；高效录入诊疗记录；管理员可管理医生账号；核心数据持久化。
- Non-goals: 真实短信/邮件、第三方登录、HIS/EMR 集成、生产级医疗合规审计。
- Success signals: 医生可在 4 个核心页面完成日常演示流程；管理员可创建医生账号；新增记录能被检索。

## Personas And Jobs

- Primary personas: 管理员、主治医生、副主任医师、护理/随访人员。
- User jobs: 登录系统、创建医生账号、管理患者档案、录入病历、筛选历史记录、查看运营摘要。
- Key contexts of use: 桌面端优先，移动端可读可操作。

## Information Architecture

- Primary navigation: 仪表盘、患者管理、病历记录、排班计划、设置，侧边栏固定展示。
- Core routes/screens: `/login`、`/dashboard`、`/patients`、`/records`、`/records/new`、`/schedule`、`/settings`、`/admin/users`。
- Content hierarchy: 顶部全局搜索和用户区；页面标题与主操作；筛选区；数据表/表单主体；辅助统计卡片。

## Design Principles

- Reduce cognitive load: 表单标签、状态和主操作必须清楚，避免信息噪音。
- Layer with tone: 用浅灰和白色表面区分层级，弱化实线边框。
- Keep clinical density usable: 数据表保持可扫描，表单保留充足留白。

## Visual Language

- Color: 主色 `#0069b4`/`#0075d5`，背景 `#f7f9fc`，容器 `#f2f4f7`，文本 `#191c1e`，辅助文本 `#64748b`。
- Typography: 标题使用 Manrope，正文和 UI 使用 Public Sans；中文环境回退到系统无衬线字体。
- Spacing/layout rhythm: 左侧栏 280px 左右，主体最大宽度 1440px，卡片内距 24-32px。
- Shape/radius/elevation: 大容器 12px，按钮 8-10px，输入 10-12px；静态元素少用阴影，以表面层级为主。
- Motion: 聚焦、hover、页面切换使用 150-200ms 过渡。
- Imagery/iconography: 使用 Material Symbols 或 lucide-react；医生头像和第三方登录图标可热链接原型 HTML 中图片。

## Components

- Existing components to reuse: 原型中的侧边导航、顶部搜索、筛选条、患者/病历表格、录入病历表单、登录卡片。
- New/changed components: 受保护 AppShell、Admin 用户管理、Toast/错误提示、分页控件。
- Variants and states: loading、empty、error、disabled、active nav、status badge、focus accent。
- Token/component ownership: 前端在 Tailwind theme 和 `src/styles.css` 中定义设计 token。

## Accessibility

- Target standard: WCAG 2.1 AA 的基础可用性。
- Keyboard/focus behavior: 表单和按钮可键盘访问，焦点状态清晰。
- Contrast/readability: 正文不使用纯灰低对比，按钮文字必须满足对比。
- Screen-reader semantics: 表单 label、表格 header、按钮 aria-label。
- Reduced motion: 不依赖动画表达必要信息。

## Responsive Behavior

- Supported breakpoints/devices: 桌面优先，平板和手机可操作。
- Layout adaptations: 小屏侧栏折叠为顶部导航/抽屉式布局；表格横向滚动或转换为紧凑列表。
- Touch/hover differences: 触屏保留足够点击面积，不要求 hover 才能发现操作。

## Interaction States

- Loading: 页面级 skeleton 或按钮 loading 文案。
- Empty: 明确说明无数据，并提供可执行主操作。
- Error: 表单字段内联错误和页面级错误提示。
- Success: 新增/更新成功后 toast，并刷新列表。
- Disabled: 禁用按钮降低透明度并保留文案。
- Offline/slow network: API 错误统一提示重试。

## Content Voice

- Tone: 简洁、专业、操作导向。
- Terminology: 使用“患者”“病历”“医生”“诊断”“就诊日期”“提交档案”等原型词汇。
- Microcopy rules: 不在页面内解释功能设计；提示文案只说明当前操作结果或必要输入。

## Implementation Constraints

- Framework/styling system: React + TypeScript + Vite + Tailwind CSS；后端 Spring Boot + Spring Security + JPA。
- Design-token constraints: 不新增重型 UI 框架；用自定义组件复刻原型。
- Performance constraints: 初版数据量使用分页；避免一次加载全量病历。
- Compatibility constraints: 开发期前端代理 `/api` 到 Spring Boot；数据库 PostgreSQL。
- Test/screenshot expectations: 核心流程通过单元、集成和 e2e 验证；桌面/移动截图人工对照。

## Open Questions

- [ ] 生产部署形态、域名、证书和真实邮件/短信服务待后续确定。
