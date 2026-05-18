一张专业、干净的 **UML 时序图（sequence diagram）**，扁平化矢量信息图风格，浅色背景（淡灰白 #F7F8FA），顶部 4 个参与者头像方块，每个参与者向下引出竖直生命线（虚线），生命线之间是带文字标签的水平消息箭头，激活条用细长圆角矩形表示，配色为科技蓝 + 紫色 + 青色 + 中性灰，中文字体清晰可读、排版整齐、无乱码。

主题：场景 B —— 多域请求「开会 + 发邮件提醒」（Supervisor 编排核心展示）。

顶部从左到右 4 个参与者（带竖直生命线）：
1. 「👤 用户」
2. 「🧠 Supervisor」
3. 「📅 Calendar Agent」
4. 「📧 Email Agent」

按时间从上到下的消息箭头序列：
1. 用户 → Supervisor，实线箭头，文字：「下周二下午2点和设计团队开1小时会，并发邮件提醒审查设计稿」。
2. Supervisor → Supervisor（自调用小回环箭头），文字：「推理：识别两个子任务 → 顺序编排」。
3. Supervisor → Calendar Agent，实线箭头，文字：「① 调用 schedule_event」。
4. Calendar Agent → Calendar Agent（自调用回环），文字：「(可选) get_available_time_slots → ["09:00","14:00","16:00"]」。
5. Calendar Agent → Calendar Agent（自调用回环），文字：「create_calendar_event(下周二14:00~15:00, 设计团队)」。
6. Calendar Agent → Supervisor，**虚线返回箭头**，文字：「Event created ...」。
7. Supervisor → Email Agent，实线箭头，文字：「② 调用 manage_email」。
8. Email Agent → Email Agent（自调用回环），文字：「send_email(to=设计团队, subject=审查新设计稿, body=...)」。
9. Email Agent → Supervisor，虚线返回箭头，文字：「Email sent to ... - Subject: ...」。
10. Supervisor → 用户，虚线返回箭头，文字：「会议已安排，提醒邮件已发送」。

视觉强调：用 ① ② 序号标注两个子任务的顺序编排，体现“一句复合指令拆成有序多次子智能体调用并汇总”。竖版长图构图，时间自上而下，调用为实线箭头、返回为虚线箭头，生命线笔直对齐，专业 UML 文档质感，可加小标题「场景B：多域编排时序图」。
