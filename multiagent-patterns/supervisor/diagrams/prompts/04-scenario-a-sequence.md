一张专业、干净的 **UML 时序图（sequence diagram）**，扁平化矢量信息图风格，浅色背景（淡灰白 #F7F8FA），顶部 4 个参与者头像方块，每个参与者向下引出竖直生命线（虚线），生命线之间是带文字标签的水平消息箭头，激活条用细长圆角矩形表示，配色为科技蓝 + 青色 + 中性灰，中文字体清晰可读、排版整齐、无乱码。

主题：场景 A —— 单域请求「安排明天上午9点的团队站会」。

顶部从左到右 4 个参与者（带竖直生命线）：
1. 「👤 用户」
2. 「🧠 Supervisor」
3. 「📅 Calendar Agent」
4. 「CalendarStubTools」

按时间从上到下的消息箭头序列：
1. 用户 → Supervisor，实线箭头，文字：「安排明天上午9点的团队站会」。
2. Supervisor → Supervisor（自调用小回环箭头），文字：「推理：仅涉及日历」。
3. Supervisor → Calendar Agent，实线箭头，文字：「调用工具 schedule_event(...)」。
4. Calendar Agent → Calendar Agent（自调用小回环箭头），文字：「“明天上午9点” → ISO（2026-05-19T09:00:00）」。
5. Calendar Agent → CalendarStubTools，实线箭头，文字：「create_calendar_event(团队站会, start, end, attendees)」。
6. CalendarStubTools → Calendar Agent，**虚线返回箭头**，文字：「Event created: 团队站会 ...」。
7. Calendar Agent → Supervisor，虚线返回箭头，文字：「确认结果」。
8. Supervisor → 用户，虚线返回箭头，文字：「已为你安排明天上午9点的团队站会」。

视觉要求：竖版长图构图，时间自上而下，调用为实线箭头、返回为虚线箭头，消息文字水平排列贴在箭头上方，生命线笔直对齐，专业 UML 文档质感，可加小标题「场景A：单域请求时序图」。
