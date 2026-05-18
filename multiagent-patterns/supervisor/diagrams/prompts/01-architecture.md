一张专业、干净的软件系统**整体架构图**，扁平化矢量信息图风格（flat technical architecture diagram / infographic），浅色背景（淡灰白 #F7F8FA），圆角矩形节点，细描边，柔和阴影，配色为科技蓝 + 青色 + 中性灰，箭头清晰带文字标签，整体自上而下分层布局，中文字体清晰可读、排版整齐、无乱码。

主题：AgentScope Supervisor 多智能体架构。

布局与节点（从上到下）：

1. 顶部：一个用户节点，图标 👤，文字「用户自然语言指令」，下方小字「例：下周二下午2点和设计团队开1小时会，并发邮件提醒他们审查设计稿」。

2. 第二层：一个大圆角容器，标题「🧠 Supervisor Agent（personal_assistant）」，容器内一个方块写「ReActAgent + SUPERVISOR_PROMPT + InMemoryMemory　职责：拆解请求 → 编排子智能体 → 汇总结果」。

3. 第三层：左右并排两个圆角容器：
   - 左：标题「📅 Calendar Agent（schedule_event）」，内含上方方块「ReActAgent + 日历Prompt + InMemoryMemory」，箭头向下指向方块「CalendarStubTools　• create_calendar_event　• get_available_time_slots」。
   - 右：标题「📧 Email Agent（manage_email）」，内含上方方块「ReActAgent + 邮件Prompt + InMemoryMemory」，箭头向下指向方块「EmailStubTools　• send_email」。

4. 右侧或底部：一个独立节点，图标 🤖，文字「共享 DashScope 模型（qwen-plus）」。

连线与标签：
- 用户 → Supervisor，实线箭头，标签「Msg(role=USER)」。
- Supervisor → Calendar Agent，实线箭头，标签「作为工具调用 schedule_event」。
- Supervisor → Email Agent，实线箭头，标签「作为工具调用 manage_email」。
- Calendar Agent → Supervisor，实线箭头，标签「结果回传」。
- Email Agent → Supervisor，实线箭头，标签「结果回传」。
- Supervisor → 用户，实线箭头，标签「汇总回复」。
- Supervisor、Calendar Agent、Email Agent 三者各有一条虚线箭头指向「共享 DashScope 模型」，标签「调用」。

要求：层次分明，箭头不交叉混乱，留白充足，专业技术文档插图质感，标题区可加小标题「整体架构图」。
