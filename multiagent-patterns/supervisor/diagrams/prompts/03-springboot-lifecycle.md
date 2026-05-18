一张专业、干净的**流程图（flowchart）**，扁平化矢量信息图风格，浅色背景（淡灰白 #F7F8FA），圆角矩形与菱形判定节点，细描边，柔和阴影，配色为 Spring 绿 + 科技蓝 + 中性灰，箭头清晰带文字标签，竖向自上而下布局，含分支，中文字体清晰可读、排版整齐、无乱码。

主题：Spring Boot 集成与启动生命周期。

节点（从上到下）：

1. 起始圆角矩形：「SpringApplication.run()」。
2. 箭头向下到方块：「装配 SupervisorConfig 的 @Bean：Model → StubTools → calendarAgent / emailAgent → supervisorAgent」。
3. 从该方块分出两条线：
   a) 向下到菱形判定节点：「@ConditionalOnProperty　supervisor.run-examples = true ?」。
   b) 向右（或向下另一侧）到方块：「ApplicationReadyEvent → 打印 🎉 启动横幅」。
4. 菱形「是」分支（标签「是」）→ 方块：「SupervisorRunner.run()（ApplicationRunner，@Order 1）」。
5. 接着竖向两步：
   - 方块：「Demo1 单域：安排明天上午9点的团队站会」。
   - 箭头向下到方块：「Demo2 多域：下周二下午2点开会 + 发邮件提醒」。
6. 菱形「否」分支（标签「否」）→ 圆角矩形：「应用空跑，不调模型」。

视觉强调：判定菱形用强调色，「是」路径（跑 Demo）与「否」路径（空跑）用不同柔和底色区分。底部可加一行小字注释「@ConditionalOnProperty 开关控制是否跑 Demo；容器有 3 个 ReActAgent Bean，注入需 @Qualifier 消歧」。

要求：竖版构图，分支清晰不交叉，专业技术文档插图质感，可加小标题「Spring Boot 启动生命周期」。
