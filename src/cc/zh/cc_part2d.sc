// ============================================================================
// cc_part2d.sc — 第2d部分: 菜单系统 / 服务选择 / 主循环 SCRIPT
// 对应 C# Draw*Menu / HandleMenuSelect / OnTick / Idle 跟随段
// ============================================================================

// ── 菜单项数量 (上下文相关) ────────────────────────────────────────────
FUNC INT CC_MenuCount(INT level)
    IF level = ML_MAIN
        // ★ v0.61：搭讪模式（NOT sandboxMode）且 A-Life 开着时，**主菜单不再放「服务」**。
        //   原因（用户实测）：主菜单「服务」和「互动 → 服务」是两条**各自独立**的入场路径，
        //   从主菜单放过动画之后，再走「互动 → 服务」会让女伴直接消失
        //   （两条路都改 g_state/g_menuLevel、都发动画任务，状态互相踩）。
        //   搭讪模式本来就该"先搭讪，再从互动里点服务" ⇒ 搭讪模式只留 互动/设置。
        //   ⚠ A-Life 关掉时没有「互动」这一层，此时**保留**「服务」，否则菜单就废了。
        IF NOT g_sandboxMode AND g_aLifeMode
            RETURN 2
        ENDIF
        INT c = 2
        IF g_aLifeMode
            c = 3
        ENDIF
        RETURN c
    ENDIF
    IF level = ML_SERVICES
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            RETURN 2
        ENDIF
        RETURN 3
    ENDIF
    IF level = ML_SANDBOXCAR
        RETURN 2
    ENDIF
    IF level = ML_STANDINGSVC
        RETURN 5
    ENDIF
    IF level = ML_SITTINGSVC
        RETURN 3
    ENDIF
    IF level = ML_LAYINGSVC
        RETURN 4
    ENDIF
    IF level = ML_SITDOWNSUB
        RETURN 2
    ENDIF
    IF level = ML_FINISH
        RETURN 2
    ENDIF
    IF level = ML_SETTINGS
        // ★ 2026-09-25：12 -> 13（新增第 13 项「警察无视」）
        // ★ 2026-09-25晚 v0.57：13 -> 14（再新增第 14 项「热咖啡模组 总开关」）
        // ★ 2026-09-26 v0.58：14 -> 15（新增「AI 模式」；为保持"总开关永远在最后一项"
        //   的用户习惯，AI 模式插在第 14 位、把总开关顺延到第 15 位）
        //   设置页的下标散落在 CC_MenuLabel / CC_MenuSelect 两处，改一处必漏一处 ——
        //   每次增删都要**同时**核对这两个函数（本次改动点见这两处 v0.58 注释）。
        // ★ 2026-09-26 v0.69：15 -> 16（新增第 7 项「女伴嗓音」；总开关仍最后）
        RETURN 16
    ENDIF
    IF level = ML_COMPAT
        RETURN 3
    ENDIF
    IF level = ML_ACTIONS
        // ★ v0.61：7 -> 8（新增第 8 项「让她离开（变普通NPC）」）
        // ★ v0.75：8 -> 9（新增第 9 项「表白 / 分手」）
        RETURN 9
    ENDIF
    IF level = ML_CONVERSATION
        // ★ v0.77：7 -> 8（C# 的 Flirt 分支是 8 项：7 个普通问题 + **「我爱你」**）
        RETURN 8
    ENDIF
    IF level = ML_INTIMACY
        // ★ v0.76：2 -> 3（C# 是「问偏好[>] / 试探[>] / 搞得亲密一点」）
        RETURN 3
    ENDIF
    IF level = ML_INTIMACYSUB
        // ★ v0.76：分支 0 八项、分支 1 三项（C# `DrawIntimacySubMenu`）
        IF g_intSubBranch = 0
            RETURN 8
        ENDIF
        RETURN 3
    ENDIF
    IF level = ML_ESCALATE
        // ★ 2026-09-25: 5 → 3。C# DrawEscalateMenu 的 !_escalateConsented 分支只有 3 项。
        RETURN 3
    ENDIF
    IF level = ML_INVITEVEH
        RETURN 1
    ENDIF
    IF level = ML_GIVEMONEY
        RETURN 9
    ENDIF
    IF level = ML_LEAVE
        RETURN 5
    ENDIF
    IF level = ML_APPROACH
        RETURN 5
    ENDIF
    IF level = ML_APPROACHLINE
        // ★ 2026-09-25: 原来固定 6。现在按"簇 + 初次/回头"取条数（初次 4 / 回头 3）。
        RETURN CC_AppLineCount()
    ENDIF
    IF level = ML_LEAVELINE
        IF g_curAlIdx >= 0
            IF NOT g_alHooker[g_curAlIdx] AND g_leaveCluster = 3
                RETURN 4
            ENDIF
        ENDIF
        RETURN 3
    ENDIF
    RETURN 1
ENDFUNC

FUNC STRING CC_MenuLabel(INT level, INT i)
    IF level = ML_MAIN
        IF NOT g_sandboxMode AND g_aLifeMode
            // 搭讪模式：0=互动 1=设置（没有「服务」—— 服务在互动里）
            IF i = 0
                RETURN "互动"
            ELSE
                RETURN "设置"
            ENDIF
            RETURN ""
        ENDIF
        IF i = 0
            // ★ v0.74 去妓女化：原来叫「服务」，改成「做爱」
            //   （不能叫「亲密」—— 那是 ML_INTIMACY 那层菜单的名字，会重名）
            RETURN "做爱"
        ELIF i = 1 AND g_aLifeMode
            RETURN "互动"
        ELSE
            RETURN "设置"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_SERVICES
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            IF i = 0
                RETURN "口交"
            ELSE
                RETURN "性爱"
            ENDIF
        ELSE
            IF i = 0
                // ★ v0.74 去妓女化：菜单名 站立服务/坐姿服务/躺姿服务 → 站着做/坐着做/躺着做
                RETURN "站着做"
            ELIF i = 1
                RETURN "坐着做"
            ELSE
                RETURN "躺着做"
            ENDIF
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_SANDBOXCAR
        IF i = 0
            RETURN "口交"
        ELSE
            RETURN "性爱"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_STANDINGSVC
        IF i = 0
            RETURN "亲吻"
        ELIF i = 1
            RETURN "口交"
        ELIF i = 2
            RETURN "靠墙式"
        ELIF i = 3
            RETURN "后入"
        ELSE
            RETURN "粗暴后入"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_SITTINGSVC
        IF i = 0
            RETURN "口交"
        ELIF i = 1
            RETURN "女上位"
        ELSE
            RETURN "反向女上位"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_LAYINGSVC
        IF i = 0
            RETURN "颜骑"
        ELIF i = 1
            RETURN "口交"
        ELIF i = 2
            RETURN "女上位"
        ELSE
            RETURN "反向女上位"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_SITDOWNSUB
        IF i = 0
            RETURN "坐着做"
        ELSE
            RETURN "躺着做"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_FINISH
        IF i = 0
            RETURN "结束（拔出）"
        ELSE
            RETURN "结束（内射）"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_SETTINGS
        // ══════════════════════════════════════════════════════════════════
        // ★★★ v0.65 设置菜单**重新排序**（用户要求："模式选择在最上面，
        //   震动和假体放最下面"）。**当前顺序（0 起）**：
        //     0 游戏模式   1 AI 模式   2 警察无视
        //     3 服务语音   4 呻吟语音   5 结束语音
        //     6 教学提示   7 街坐动画   8 车引擎   9 可驾驶车  10 车内灯
        //     6 女伴嗓音   7 教学提示   8 街坐动画   9 车引擎  10 可驾驶车
        //    11 车内灯    12 假体      13 口交震动   14 拍打震动
        //    15 热咖啡模组（**必须永远最后**，走 ELSE 兜底）
        //
        //   ⚠ 这个顺序在**三处**同时存在，改一处必漏两处：
        //       · 本函数（CC_MenuLabel）的 CASE 号
        //       · CC_MenuSelect 的 IF/ELIF 号
        //       · CC_MenuSelect 末尾「改设置回显」里的跳过清单（v0.69 起跳过 0 / 1 / 6 / 15）
        //     总项数仍是 15（CC_MenuCount(ML_SETTINGS)）。
        // ══════════════════════════════════════════════════════════════════
        SWITCH i
            CASE 0
                // C# 原版标签（CinnamonCoffee.cs:18886）"Game Mode: Casual/Prostitution"，
                // 对应 HandleMenuSelect 的 `case 4`。守卫照抄 C#：`if (!(hasGirl && aLifeMode))`
                // —— 带着女伴、且 A-Life 开着时不让切，避免"她正按 A-Life 规则被处理着"时
                // 半路换规则、把条目和菜单对错位。
                IF g_sandboxMode
                    RETURN "游戏模式: 沙盒"
                ELSE
                    RETURN "游戏模式: 搭讪"
                ENDIF
                BREAK
            CASE 1
                // ★★★ 2026-09-26 v0.58 新增（设置第 14 项）：**AI 模式**（C# `aLifeMode`）。
                //   C# 原版标签 "AI Mode: A-Life/Sandbox"（CinnamonCoffee.cs:18882），
                //   对应 HandleMenuSelect 的 `case 3`。这是本端口**曾经整条丢掉**的开关 ——
                //   补回来之后，主菜单才会出现「互动」项，声望/话题/收费 那一整套才开始运转。
                IF g_aLifeMode
                    RETURN "AI 模式: A-Life"
                ELSE
                    RETURN "AI 模式: 基础"
                ENDIF
                BREAK
            CASE 2
                // ★ 2026-09-25 新增：警察无视开关。
                //   真凶是女伴加入时那句 SET_EVERYONE_IGNORE_PLAYER(TRUE)（C# 原版也有），
                //   它让**所有 ped 含警察**无视玩家 ⇒ 用户报的"触发警星警察也不打我"。
                //   默认"开启"= 保持原版行为；想被通缉/攻击就改成"关闭"。
                IF g_policeIgnore
                    RETURN "警察无视: 开"
                ELSE
                    RETURN "警察无视: 关"
                ENDIF
                BREAK
            CASE 3
                IF g_voiceSvc
                    RETURN "服务语音: 开"
                ELSE
                    RETURN "服务语音: 关"
                ENDIF
                BREAK
            CASE 4
                IF g_voiceMoan
                    RETURN "呻吟语音: 开"
                ELSE
                    RETURN "呻吟语音: 关"
                ENDIF
                BREAK
            CASE 5
                IF g_voiceEnd
                    RETURN "结束语音: 开"
                ELSE
                    RETURN "结束语音: 关"
                ENDIF
                BREAK
            CASE 6
                // ★★★ v0.69 新增：手动挑女伴嗓音（环形）。见 CC_MenuSelect 的 i = 6。
                //   用户报"有的女伴就是没声音"；嗓音名表与 C# 逐字一致 ⇒
                //   差异只可能在"本机音频数据里某几个嗓子取不到"。自动轮换又依赖
                //   `IS_AMBIENT_SPEECH_PLAYING`（未证明 native）⇒ 必须留一个手动开关。
                // ★ v0.70：看的是 `g_voiceFix`（跨女伴保留的"钉死值"），
                //   不是 `g_voiceIdx`（那是"当前这位女伴"的值，换人就变）。
                // ★ v0.79：坏嗓子（BLACK_FULL_02）**已从表里删掉** ⇒ 总数 9 → 8，
                //   标签一律 "/8"，也不再需要"（本机无音频）"那种提示。
                IF g_voiceFix < 0
                    RETURN "女伴嗓音: 随机（每人不同）"
                ELIF g_voiceFix = 0
                    RETURN "女伴嗓音: 1/8"
                ELIF g_voiceFix = 1
                    RETURN "女伴嗓音: 2/8"
                ELIF g_voiceFix = 2
                    RETURN "女伴嗓音: 3/8"
                ELIF g_voiceFix = 3
                    RETURN "女伴嗓音: 4/8"
                ELIF g_voiceFix = 4
                    RETURN "女伴嗓音: 5/8"
                ELIF g_voiceFix = 5
                    RETURN "女伴嗓音: 6/8"
                ELIF g_voiceFix = 6
                    RETURN "女伴嗓音: 7/8"
                ENDIF
                RETURN "女伴嗓音: 8/8"
                BREAK
            CASE 7
                IF g_showTut
                    RETURN "教学提示: 开"
                ELSE
                    RETURN "教学提示: 关"
                ENDIF
                BREAK
            CASE 8
                IF g_streetSitAnimOn
                    RETURN "街坐动画: 开"
                ELSE
                    RETURN "街坐动画: 关"
                ENDIF
                BREAK
            CASE 9
                IF g_carEngineOn
                    RETURN "车引擎: 开"
                ELSE
                    RETURN "车引擎: 关"
                ENDIF
                BREAK
            CASE 10
                IF g_driveable
                    RETURN "可驾驶车: 开"
                ELSE
                    RETURN "可驾驶车: 关"
                ENDIF
                BREAK
            CASE 11
                IF g_intLightOn
                    RETURN "车内灯: 开"
                ELSE
                    RETURN "车内灯: 关"
                ENDIF
                BREAK
            CASE 12
                // ★ 2026-09-25 修：原来第一行是 `RETURN "假体"`，
                //   把下面的开关状态判断全变成了**死代码** —— 标签永远只显示"假体"，
                //   不带开/关，看起来根本不像一个开关（用户反馈"假体没有开关选项"）。
                IF g_dildoOn
                    RETURN "假体: 开"
                ELSE
                    RETURN "假体: 关"
                ENDIF
                BREAK
            CASE 13
                IF g_bjSoundsOn
                    RETURN "口交震动(手柄): 开"
                ELSE
                    RETURN "口交震动(手柄): 关"
                ENDIF
                BREAK
            CASE 14
                IF g_slapSoundsOn
                    RETURN "拍打震动(手柄): 开"
                ELSE
                    RETURN "拍打震动(手柄): 关"
                ENDIF
                BREAK
            CASE 15
                // ★★★ 2026-09-25晚 v0.57 新增：热咖啡模组**总开关**。
                //   v0.58 起从第 14 项顺延到第 15 项（AI 模式插到它前面），
                //   **保持"设置菜单最后一项 = 总开关"** 这个用户已经形成的操作习惯。
                //   关闭 = 整个模组休眠（CC_MainTick 一开头就 EXIT），并把当前女伴
                //   "放生"成普通 NPC（清任务 / 交还所有权 / 移掉标点）。
                //   所以不需要"杀掉 NPC"来让她停止跟随。
                //   ⚠ 休眠后菜单打不开 ⇒ 重新开启走 **B + 十字键右**（见 cc_main.sc）。
                IF g_modOn
                    RETURN "热咖啡模组: 开"
                ELSE
                    RETURN "热咖啡模组: 关"
                ENDIF
                BREAK
        ENDSWITCH
        RETURN ""
    ENDIF
    IF level = ML_COMPAT
        IF i = 0
            IF g_sinkraLay
                RETURN "Sinkra躺女上: 开"
            ELSE
                RETURN "Sinkra躺女上: 关"
            ENDIF
        ELIF i = 1
            IF g_sinkraRevLay
                RETURN "Sinkra反向: 开"
            ELSE
                RETURN "Sinkra反向: 关"
            ENDIF
        ELSE
            IF g_sinkraFace
                RETURN "Sinkra颜骑: 开"
            ELSE
                RETURN "Sinkra颜骑: 关"
            ENDIF
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_ACTIONS
        IF i = 0
            RETURN "聊天"
        ELIF i = 1
            RETURN "亲密"
        ELIF i = 2
            RETURN "升温"
        ELIF i = 3
            // ★ v0.74 去妓女化：原来叫「服务」
            RETURN "做爱"
        ELIF i = 4
            RETURN "邀请上车"
        ELIF i = 5
            RETURN "给钱"
        ELIF i = 6
            RETURN "道别（按风格说再见）"
        ELIF i = 7
            // ★ v0.61 新增：直接放生，不用先回主菜单、也不用关总开关。
            RETURN "让她离开（变普通NPC）"
        ELSE
            // ★ v0.75 新增第 9 项：关系动作。
            //   已经是女友 ⇒「分手」；否则 ⇒「表白」（C# 的 `Make Her Mine`）
            // ★ v0.76：用 `>=` —— 否则「痴迷」(7) 会显示成「表白」而不是「分手」，
            //   点进去还只能听到"我说过了…"（没有分手出口）。
            // ☠ 审查改成**嵌套 IF**：原来是 `CC_AlOk() AND g_alRSt[g_curAlIdx]`，
            //   万一 SanScript 的 `AND` 不短路，`g_curAlIdx = -1` 时就是**越界读**
            //   （不报错、只给垃圾）。嵌套没有这个风险。
            IF CC_AlOk()
                IF g_alRSt[g_curAlIdx] >= RST_GIRLFRIEND
                    RETURN "分手（结束这段关系）"
                ENDIF
            ENDIF
            RETURN "表白（让她成为我的）"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_CONVERSATION
        IF i = 0
            RETURN "问她的情况"
        ELIF i = 1
            RETURN "问她的心情"
        ELIF i = 2
            RETURN "问她的目标"
        ELIF i = 3
            RETURN "夸夸她"
        ELIF i = 4
            RETURN "调情"
        ELIF i = 5
            RETURN "聊聊这座城市"
        ELIF i = 6
            RETURN "问她的名字"
        ELSE
            // ★ v0.77：C# 里「我爱你」是**调情分支的第 8 项**
            //   （`GetConvSubItemCount` 的 `case 3: return 8`，注释写着 "7 standard + I love you"）
            RETURN "我爱你"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_INTIMACY
        // ★ v0.76：拆回两层（C# `DrawIntimacyMenu`，18419-18427）
        // ★ v0.79：原来写的是 "问她的偏好 [>]" / "试探一下 [>]" —— 那两个 ASCII
        //   方括号+大于号在本机 HUD 字体里渲染成**怪符号**（用户反馈）。
        //   改成本工程既有的写法：中文括号里说明"点进去还有一层"。
        IF i = 0
            RETURN "问她的偏好（更多问题）"
        ELIF i = 1
            RETURN "试探一下（挑一句说）"
        ENDIF
        RETURN "搞得亲密一点"
    ENDIF
    IF level = ML_INTIMACYSUB
        // ★ v0.76：C# `DrawIntimacySubMenu` 的 8 + 3 项
        IF g_intSubBranch = 0
            IF i = 0
                RETURN "亲不亲？"
            ELIF i = 1
                RETURN "公开还是私下？"
            ELIF i = 2
                RETURN "你喜欢什么？"
            ELIF i = 3
                RETURN "你愿意用嘴吗？"
            ELIF i = 4
                RETURN "你喜欢粗暴吗？"
            ELIF i = 5
                RETURN "你会主动吗？"
            ELIF i = 6
                RETURN "你绝对不接受什么？"
            ENDIF
            // 第 8 项 —— **炮友关系的确认**（C# "Do you want this casual?"）
            RETURN "保持这种随便的关系？"
        ENDIF
        IF i = 0
            RETURN "你让人难以抗拒。"
        ELIF i = 1
            RETURN "我想和你更近一点。"
        ENDIF
        RETURN "你也想要吗？"
    ENDIF
    // ── ESCALATE (C# DrawEscalateMenu, !_escalateConsented 分支) ──────────
    //   ★ 2026-09-25 修：原来这里和 APPROACH **共用**一份"5 句人话"，
    //     其中「还能再挤一个吗？」在 C# 原版里**根本不存在** —— 是移植时
    //     凭空写的（用户问"这个什么意思，我搞不懂"）。原版这一层只有 3 项：
    //       night ? "Stay with me tonight. What do you say?" : "Let's have some fun."
    //       "Mind if I... come closer?.."
    //       "Wanna fuck?"
    //     本端口没有昼夜判定，统一用白天那句。
    IF level = ML_ESCALATE
        IF i = 0
            RETURN "咱们找点乐子。"
        ELIF i = 1
            RETURN "介意我……靠近点吗？"
        ELSE
            RETURN "想做吗？"
        ENDIF
        RETURN ""
    ENDIF
    // ── APPROACH (C# DrawApproachMenu) ────────────────────────────────────
    //   ★ 2026-09-25 修：原版这一层显示的是**5 个搭讪风格簇名**，
    //     点进去才看该风格专属的台词。移植版一度把它写成 5 句人话，
    //     结果"5 个选项点进去全都一样"（用户反馈）。
    IF level = ML_APPROACH
        IF i = 0
            RETURN "温柔"
        ELIF i = 1
            RETURN "自信"
        ELIF i = 2
            RETURN "高冷"
        ELIF i = 3
            RETURN "奉承"
        ELSE
            RETURN "俏皮"
        ENDIF
        RETURN ""
    ENDIF
    // ── APPROACHLINE (C# DrawApproachLineMenu) ────────────────────────────
    //   ★ 2026-09-25 修：原版台词随"所选风格簇 + 初次/回头"变化
    //     （卖淫模式 / 沙盒模式各一套）。原来是一张与所选簇无关的固定
    //     6 行通用表（「今晚过得怎么样？」「我只是路过。」等）⇒ 整表删除，
    //     改查 cc_applines.sc 的 CC_AppLine()。
    IF level = ML_APPROACHLINE
        RETURN CC_AppLine(i)
    ENDIF
    IF level = ML_INVITEVEH
        RETURN "邀请她上车"
        RETURN ""
    ENDIF
    IF level = ML_GIVEMONEY
        SWITCH i
            CASE 0 RETURN "给 $1"
            CASE 1 RETURN "给 $10"
            CASE 2 RETURN "给 $100"
            CASE 3 RETURN "给 $1,000"
            CASE 4 RETURN "给 $1 万"
            CASE 5 RETURN "给 $10 万"
            CASE 6 RETURN "给 $100 万"
            CASE 7 RETURN "给 $1000 万"
            CASE 8 RETURN "给 $1 亿"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF level = ML_LEAVE
        IF i = 0
            RETURN "温柔"
        ELIF i = 1
            RETURN "自信"
        ELIF i = 2
            RETURN "高冷"
        ELIF i = 3
            RETURN "奉承"
        ELSE
            RETURN "俏皮"
        ENDIF
        RETURN ""
    ENDIF
    IF level = ML_LEAVELINE
        // 玩家自己的离开台词 (C# DrawLeaveLineMenu: prostLeaveLines / leaveLines)
        // ★ v0.79：原来是 `IF g_alHooker[g_curAlIdx]` —— **裸读数组**（没守卫）。
        //   这个函数**每帧、每行**都跑，而 `g_curAlIdx` 可以是 -1（没跟人/菜单复用时）
        //   ⇒ 读的是数组前面那块内存，结果不可控：可能让她走"卖淫那套"道别词。
        //   不报错、只出错菜单，正是本工程反复踩过的那类越界读。
        BOOL lvIsProst = FALSE
        IF CC_AlOk()
            lvIsProst = g_alHooker[g_curAlIdx]
        ENDIF
        IF lvIsProst
            IF g_leaveCluster = 0
                IF i = 0
                    RETURN "行了，我走了。"
                ELIF i = 1
                    RETURN "回见。"
                ELSE
                    RETURN "保重。"
                ENDIF
            ELIF g_leaveCluster = 1
                IF i = 0
                    RETURN "跟你总是很愉快。"
                ELIF i = 1
                    RETURN "你让我还想再来。"
                ELSE
                    RETURN "回头见。"
                ENDIF
            ELIF g_leaveCluster = 2
                IF i = 0
                    RETURN "你很靠谱，真的。"
                ELIF i = 1
                    RETURN "你什么样我心里有数。"
                ELSE
                    RETURN "不玩虚的，佩服。"
                ENDIF
            ELIF g_leaveCluster = 3
                IF i = 0
                    RETURN "在外面小心点。"
                ELIF i = 1
                    RETURN "说真的，保重。"
                ELSE
                    RETURN "你值得比这街角更好的。"
                ENDIF
            ELSE
                IF i = 0
                    RETURN "这下我真舍不得走了。不公平。"
                ELIF i = 1
                    RETURN "一次比一次难走。"
                ELSE
                    RETURN "你知道你很危险，对吧？"
                ENDIF
            ENDIF
        ELSE
            IF g_leaveCluster = 0
                IF i = 0
                    RETURN "这对我意义重大。"
                ELIF i = 1
                    RETURN "我得走了，但很高兴认识你。"
                ELSE
                    RETURN "在外面保重，好吗？"
                ENDIF
            ELIF g_leaveCluster = 1
                IF i = 0
                    RETURN "回头见。"
                ELIF i = 1
                    RETURN "你知道我在哪。"
                ELSE
                    RETURN "感觉很好。别等太久。"
                ENDIF
            ELIF g_leaveCluster = 2
                IF i = 0
                    RETURN "我自己出去就行。"
                ELIF i = 1
                    RETURN "没什么大不了。回见。"
                ELSE
                    RETURN "别搞得很奇怪。再见。"
                ENDIF
            ELIF g_leaveCluster = 3
                IF i = 0
                    RETURN "你让人难以离开。"
                ELIF i = 1
                    RETURN "我已经在想下次了。"
                ELIF i = 2
                    RETURN "你是我今天最美好的事。"
                ELSE
                    RETURN "我爱你。"
                ENDIF
            ELSE
                IF i = 0
                    RETURN "好了，趁我还没后悔。"
                ELIF i = 1
                    RETURN "你很危险，知道吗？"
                ELSE
                    RETURN "别想我。不——还是想我吧。"
                ENDIF
            ENDIF
        ENDIF
        RETURN ""
    ENDIF
    RETURN ""
ENDFUNC

// ── 菜单绘制 (C# DrawMenu 布局, 10 帧降频) ──────────────────────────────
PROC CC_DrawMenuItem(STRING msg, INT i, FLOAT x, FLOAT &y, FLOAT lh)
    CONST_FLOAT MW 0.216
    CONST_FLOAT MPAD 0.007
    IF i = g_menuIndex
        CC_DrawRect(x, y, MW, 0.034, 79, 38, 142, 210)
    ENDIF
    CC_DrawText(msg, x + MPAD, y + 0.005, MW - MPAD * 2.0, 0.31, FALSE, 255, 255, 255, 255, 0)
    y = y + lh
ENDPROC

PROC CC_DrawMenuFrame(FLOAT totalH, FLOAT footH)
    CONST_FLOAT MPX 0.025
    CONST_FLOAT MTOP 0.045
    CONST_FLOAT MW 0.216
    CONST_FLOAT MHDR 0.047
    CC_DrawRect(MPX, MTOP, MW, totalH, 0, 0, 0, 190)
    CC_DrawRect(MPX, MTOP, MW, MHDR, 79, 38, 142, 230)
    // ★ 2026-09-25 修（用户反馈"热咖啡三个字太靠左，热字跑到菜单外面"）：
    //   原为 `MPX + 0.007`。CC_DrawText 在 cen=TRUE 时把 x 当**字符串中心**，
    //   0.032 离面板左沿(0.025)只有 0.007 ⇒ 三个字一半甩到面板外。
    //   改成面板水平中心，居中才落在标题条里。
    CC_DrawText("热咖啡", MPX + MW * 0.5, MTOP + (MHDR * 0.5) - 0.014, MW - 0.014, 0.50, TRUE, 210, 125, 45, 255, 0)
    CC_DrawRect(MPX, MTOP + totalH - footH, MW, footH, 30, 30, 30, 210)
    // ★ v0.57：确认键除了 A，**十字键左右也算**（用户要求），底栏说明同步改掉。
    CC_DrawText("[↑↓]移动  [A/←→]确认  [B]返回", MPX + 0.007, MTOP + totalH - footH + 0.006, MW - 0.014, 0.19, FALSE, 200, 200, 200, 255, 0)
ENDPROC

PROC CC_DrawMenu()
    CONST_FLOAT MPX 0.025
    CONST_FLOAT MTOP 0.045
    CONST_FLOAT MHDR 0.047
    CONST_FLOAT MLH 0.034
    CONST_FLOAT MFOOT 0.029
    INT count
    count = CC_MenuCount(g_menuLevel)
    // ★ v0.73：有"她"的时候，头部下方要多留两行（名字+性格 / 声望）。
    //   原来只留一行(0.016)给名字。没她的时候保持原样，不白占垂直空间。
    //   ⚠ 普通 `FLOAT` 变量声明必须写 `=`（`CONST_FLOAT` 才没有 `=`）——
    //     写成 `FLOAT hdrPad 0.016` 会 parse error，而且**报错行号飘到几十行外**。
    FLOAT hdrPad = 0.016
    IF CC_AlOk()
        hdrPad = 0.032
    ENDIF
    // ★ 先画底框再画行: SanScript HUD 按绘制顺序叠加, 后画的盖前面
    FLOAT totalH = MHDR + hdrPad + (TO_FLOAT(count) * MLH) + MFOOT
    CC_DrawMenuFrame(totalH, MFOOT)
    FLOAT y = MTOP + MHDR + hdrPad
    INT i = 0
    WHILE i < count
        CC_DrawMenuItem(CC_MenuLabel(g_menuLevel, i), i, MPX, y, MLH)
        i = i + 1
    ENDWHILE
    // 服务列表顶部显示名字/价格信息
    IF g_curAlIdx >= 0 AND g_aLifeMode
        TEXT_LABEL_63 info = g_alName[g_curAlIdx]
        CC_DrawText(info, MPX + 0.007, MTOP + MHDR + 0.002, 0.2, 0.24, FALSE, 255, 210, 0, 255, 0)
    ENDIF
    // ══════════════════════════════════════════════════════════════════════
    //  ★★★ v0.73 新增：她是谁 —— 名字旁加**性格**，下面加**声望分档**
    //
    //  【为什么加】用户原话："女性性格是不显示的吗？……你怎么在自说自话"。
    //    确实不显示：`CC_PERS_NAME()` 当时**零调用**（死代码，而且返回英文）。
    //    而 B 类（给钱接受率）和 D 类（RISKY/NOKISS/NOBJ*/NORO*/WALKWAY）
    //    的触发条件**全在"性格 + 声望"上** ⇒ 不显示就**没法验收**，
    //    只能靠反复撞运气 —— 这正是"你在自说自话"的根因。
    //
    //  【口径】纯观察窗：只读 CC_Pers()/CC_Rep()，**不参与任何判定**、
    //    **不写任何全局量**（避免变成"每帧重设顶掉别人设置"那种轮询）。
    //  【配色】声望按档上色：≤ -1 红（已在躲你 = AVOID / WALKWAY 档）；
    //    ≥ 25 绿（NOKISS 情感闸之上）；其余灰白。
    // ══════════════════════════════════════════════════════════════════════
    IF CC_AlOk()
        INT pz = CC_Pers()
        INT rz = CC_Rep()
        INT cr = 200
        INT cg = 200
        INT cb = 200
        IF rz <= -1
            cr = 255
            cg = 110
            cb = 110
        ELIF rz >= 25
            cr = 140
            cg = 255
            cb = 140
        ENDIF
        // 第一行右侧：性格（中文名，每项 ≤ 4 字）
        CC_DrawText(CC_PERS_NAME_ZH(pz), MPX + 0.098, MTOP + MHDR + 0.004, 0.10, 0.20, FALSE, 190, 210, 255, 255, 0)
        // 第二行：**关系状态**（★ v0.75 新增；颜色仍是声望分档：≤ -1 红 / ≥ 25 绿）
        //   v0.73 这行显示的是「声望 一般/交好…」—— 现在关系状态更重要
        //   （它决定「表白」能不能成、告别菜单有几项），所以把状态名放上来、
        //   声望退成**颜色**，不再单独占一行。
        CC_DrawText("关系", MPX + 0.007, MTOP + MHDR + 0.016, 0.05, 0.19, FALSE, 150, 170, 210, 255, 0)
        CC_DrawText(CC_RST_NAME_ZH(g_alRSt[g_curAlIdx]), MPX + 0.044, MTOP + MHDR + 0.016, 0.10, 0.19, FALSE, cr, cg, cb, 255, 0)
    ENDIF
ENDPROC

// ── 服务可用性 (C# IsServiceAvailable) ──────────────────────────────────
FUNC BOOL CC_SvcAvail(INT carFlag, INT idx)
    // 随机不可用没有任何解锁途径, 只会让人永远玩不了 -> 全部可用 (价格照常)
    RETURN TRUE
ENDFUNC

// 付款 (C# HandleMenuSelect 付款段)
FUNC BOOL CC_Pay(INT price)
    // ══════════════════════════════════════════════════════════════════════
    // ★★★ v0.62 用户要求：「既然是搭讪，为什么要钱呢？删掉扣费，保留给钱功能。」
    //
    //   原实现只在「搭讪 + A-Life」下真扣钱（沙盒 / 无 A-Life 本来就免费），
    //   现在**四个模式一律免费**：本函数只负责"放行"，不再 CC_AddMoney(-price)。
    //
    //   ⚠ 关键：**赠与（给钱）是另一条独立链路**，不经过本函数 ——
    //     `ML_GIVEMONEY` → `CC_AddMoney(-amt)`（cc_part2d 的 GIVEMONEY 段）。
    //     所以这里改成"永远免费"**完全不会**影响"给钱"功能。
    //
    //   顺带效果：原来那套 abort 退款全靠 `g_lastPaid`（cc_part2c 的
    //   `IF g_lastPaid > 0 → CC_AddMoney(g_lastPaid)`），现在永远为 0
    //   ⇒ 不再出现"扣了又退"的残留状态。
    //
    //   参数 price 保留（调用点很多，签名不动），只是不再使用。
    // ══════════════════════════════════════════════════════════════════════
    g_lastPaid = 0
    RETURN TRUE
ENDFUNC

// ── 菜单选择 (C# HandleMenuSelect 精简版 — 保留全部流程分支) ────────────
PROC CC_MenuSelect(INT level, INT i)
    // MAIN
    IF level = ML_MAIN
        IF NOT g_sandboxMode AND g_aLifeMode
            // 搭讪模式：0=互动 1=设置
            IF i = 0
                g_menuLevel = ML_ACTIONS
            ELSE
                g_menuLevel = ML_SETTINGS
            ENDIF
            g_menuIndex = 0
            EXIT
        ENDIF
        IF i = 0
            g_menuLevel = ML_SERVICES
            g_menuIndex = 0
            CC_PlayServiceVoice()
        ELIF i = 1 AND g_aLifeMode
            g_menuLevel = ML_ACTIONS
            g_menuIndex = 0
        ELSE
            g_menuLevel = ML_SETTINGS
            g_menuIndex = 0
        ENDIF
        EXIT
    ENDIF
    // SERVICES
    IF level = ML_SERVICES
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            INT price = PR_GetPrice(g_curAlIdx, 1, i)
            IF NOT CC_SvcAvail(1, i)
                CC_HudStatus("~r~她不做这个！", 2000)
                EXIT
            ENDIF
            IF CC_Pay(price)
                g_carPropActive = FALSE
                CC_StartCarAction(i)
                // ══════════════════════════════════════════════════════════
                // ★★★ v0.62 修「每次调动画速度都会重播动画（并且扣钱）」：
                //
                //   根因：C# 选完服务会把菜单**关掉**（`menuLevel = None`），
                //   本端口这里**漏了这一步** —— 菜单还停在 ML_SERVICES。
                //   于是动画期间 `CC_MenuNav()` 仍在跑（它只要 g_menuLevel != ML_NONE
                //   就工作），而它的"确认键"就是 **A / 十字键左 / 十字键右**
                //   （cc_part2d 的 CC_MenuNav 确认段）—— **和动画调速键一模一样**。
                //   ⇒ 玩家想按 ←/→ 调速，菜单却把那一按当成"再确认一次当前服务"
                //     ⇒ 服务重跑（动画重播）+ 再扣一次钱。
                //
                //   修法：动作一开始就把菜单收掉（g_menuLevel = ML_NONE）。
                //   这样：
                //     · CC_MenuNav 立刻停摆（动画期 ←/→ 不再被当成菜单确认）
                //     · 调速闸 `g_menuLevel = ML_NONE` 打开（cc_part2c 888）
                //   再给 g_speedCooldown 一个初始值，避免"确认后仍按着 ←/→"
                //   的那一帧立刻把速度拉走。
                // ══════════════════════════════════════════════════════════
                // ★ 只在**动作真的开始**时关菜单（g_state 变 ST_ANIMATING 才关）。
                //   若 CC_StartCarAction 因前置条件不足提前 EXIT（例如她没上车），
                //   菜单保持打开，玩家可以重选 —— 与 C#"成功才关"一致。
                IF g_state = ST_ANIMATING
                    g_menuLevel = ML_NONE
                    g_menuIndex = 0
                    g_speedCooldown = 10
                ENDIF
            ENDIF
        ELSE
            IF g_sitPropActive AND g_sitPropPhase = 2
                // 坐/躺 proposition 已就位 → 直接进动作
                IF i = 0
                    g_menuLevel = ML_SITTINGSVC
                ELIF i = 1
                    g_menuLevel = ML_LAYINGSVC
                ELSE
                    g_menuLevel = ML_SITDOWNSUB
                ENDIF
                g_menuIndex = 0
            ELSE
                IF i = 0
                    g_menuLevel = ML_STANDINGSVC
                ELIF i = 1
                    // ★★ 2026-09-25 用户要求：站姿 与 坐/躺 **单向隔离** ——
                    //   站姿动作进行中，不许直接切到坐姿/躺姿。
                    //   原因：坐/躺的定位走的是"proposition 绝对高度 g_sitOrigin.z + zOffset"，
                    //   而站姿动作进行中 g_sitPropActive = FALSE ⇒ 会掉进"当前位置累加 zOffset"
                    //   的旧路径 ⇒ 人物高度错乱（用户实测"切过去不自然、高度变奇怪"）。
                    //   必须先让站姿动作**完整结束、回到可控状态**，再回来选坐/躺。
                    IF CC_StandingActionActive()
                        CC_HudStatus("~r~先结束当前站姿动作！", 2500)
                        EXIT
                    ENDIF
                    g_svcSubParent = ML_SERVICES
                    CC_EnterStreetSitProp(ML_SITTINGSVC)
                    g_menuLevel = ML_NONE
                ELSE
                    IF CC_StandingActionActive()
                        CC_HudStatus("~r~先结束当前站姿动作！", 2500)
                        EXIT
                    ENDIF
                    g_svcSubParent = ML_SERVICES
                    CC_EnterStreetSitProp(ML_LAYINGSVC)
                    g_menuLevel = ML_NONE
                ENDIF
                g_menuIndex = 0
            ENDIF
        ENDIF
        EXIT
    ENDIF
    // 车 Actions (Blowjob/Sex)
    IF level = ML_SANDBOXCAR
        INT price2 = PR_GetPrice(g_curAlIdx, 1, i)
        IF CC_Pay(price2)
            CC_StartCarAction(i)
            // ★ v0.62：同 ML_SERVICES —— 开工即关菜单，否则调速度会被当成"再确认一次"。
            IF g_state = ST_ANIMATING
                g_menuLevel = ML_NONE
                g_menuIndex = 0
                g_speedCooldown = 10
            ENDIF
        ENDIF
        EXIT
    ENDIF
    // 街道服务子菜单 → 扁平 actionIdx
    IF level = ML_STANDINGSVC OR level = ML_SITTINGSVC OR level = ML_LAYINGSVC
        INT act = -1
        IF level = ML_STANDINGSVC
            IF i = 0
                act = 0
            ELIF i = 1
                act = 2
            ELIF i = 2
                act = 5
            ELIF i = 3
                act = 10
            ELSE
                act = 11
            ENDIF
        ELIF level = ML_SITTINGSVC
            IF i = 0
                act = 3
            ELIF i = 1
                act = 6
            ELSE
                act = 7
            ENDIF
        ELSE
            IF i = 0
                act = 1
            ELIF i = 1
                act = 4
            ELIF i = 2
                act = 8
            ELSE
                act = 9
            ENDIF
        ENDIF
        // ★★★ v0.72（D 类接线）：**开演前的性格闸**（C# 8622-8696）
        //   口径照抄 C#：口交偏好（NOBJK/NOBJN）→ 粗暴偏好（NOROK/NORON）
        //   → 户外风险（RISKY）→ 亲吻情感闸（NOKISS）。
        //   被拦下时 CC_SvcGate 已经说过台词、记过声望 ⇒ 这里直接退出。
        //   （沙盒模式 / 没有 A-Life 条目 ⇒ 直接放行，行为与以前完全一致。）
        IF CC_SvcGate(act)
            EXIT
        ENDIF
        IF NOT CC_SvcAvail(0, act)
            CC_HudStatus("~r~她不做这个！", 2000)
            // 性格台词: 服务不可用 (生成表 SVCUNAV)
            // ★ v0.72：原来写死 `_1` 变体、且用 `g_curAlIdx >= 0` 手工守卫；
            //   改成安全入口 + 变体调度器（这条目前仍走不到，见 cc_dlgnav 注释）。
            IF CC_AlOk()
                TEXT_LABEL_15 nm8
                CC_GetGirlName(nm8)
                CC_ShowLine2(CC_SVCUNAV_1_H(CC_Pers()), CC_SVCUNAV_T(CC_DlgPick(CC_SVCUNAV_N), CC_Pers()), nm8, 3000)
            ENDIF
            EXIT
        ENDIF
        INT price3 = PR_GetPrice(g_curAlIdx, 0, act)
        IF CC_Pay(price3)
            CC_StartStreetAction(act)
            // ★ v0.62：同上 —— 街面动作开工也立刻关菜单。
            IF g_state = ST_ANIMATING
                g_menuLevel = ML_NONE
                g_menuIndex = 0
                g_speedCooldown = 10
            ENDIF
        ENDIF
        EXIT
    ENDIF
    IF level = ML_SITDOWNSUB
        IF i = 0
            g_menuLevel = ML_SITTINGSVC
        ELSE
            g_menuLevel = ML_LAYINGSVC
        ENDIF
        g_menuIndex = 0
        EXIT
    ENDIF
    // FINISH
    IF level = ML_FINISH
        IF i = 0
            CC_RequestFinish(FALSE)
        ELSE
            // ══════════════════════════════════════════════════════════════════
            // ★★★ v0.72（D 类接线）：内射前**开口说话**
            //   FINUNASK / FINACC / FINFORCED / FCFHOOK 四张表。
            //
            //   这套状态（`g_alCim` / `g_finFreePass` / `g_finForced` / `g_finBroke`）
            //   **本来就在跑**（cc_part1 建条目、cc_part2b/2c 收尾），
            //   缺的只是"她开口" ⇒ 所以这一处**只加台词，不改任何玩法**。
            //   C# 对应（7516-7560）：她自己享受 → FINUNASK / 答应 → FINACC /
            //   强行内射 → FINFORCED（沙盒）或 FCFHOOK（妓女翻脸）。
            // ══════════════════════════════════════════════════════════════════
            BOOL finSpoke = FALSE
            IF g_aLifeMode AND CC_AlOk()
                IF g_alCim[g_curAlIdx] = 1
                    TEXT_LABEL_15 fnA
                    CC_GetGirlName(fnA)
                    CC_ShowLine2C(CC_FINUNASK_1_H(CC_Pers()), CC_FINUNASK_T(CC_DlgPick(CC_FINUNASK_N), CC_Pers()), fnA, 3500)
                    finSpoke = TRUE
                ENDIF
            ENDIF
            // A-Life: Ask 未用过 → 计费链路 (C# Finish 菜单 A-Life 分支)
            IF g_aLifeMode AND CC_AlOk() AND NOT g_finAsked AND NOT g_finFreePass
                IF g_alHooker[g_curAlIdx]
                    INT finPrice = 100
                    g_aLifeFinPrice = finPrice
                    IF g_alCim[g_curAlIdx] = 1
                        // 她自己也享受 — 免费 (C# CimPreference)
                        g_finFreePass = TRUE
                    ELIF NOT CC_Pay(finPrice)
                        // 没钱 → 强行内射会触发 forced
                        g_finInside = TRUE
                        g_finForced = TRUE
                        g_finBroke = TRUE
                        TEXT_LABEL_15 fnB
                        CC_GetGirlName(fnB)
                        IF g_alHooker[g_curAlIdx]
                            // 妓女被强行内射 → 翻脸走人（C# 7592 GetForcedFinishHookerLines）
                            CC_ShowLine2(CC_FCFHOOK_1_H(CC_Pers()), CC_FCFHOOK_T(CC_DlgPick(CC_FCFHOOK_N), CC_Pers()), fnB, 4000)
                        ELSE
                            CC_ShowLine2(CC_FINFORCED_1_H(CC_Pers()), CC_FINFORCED_T(CC_DlgPick(CC_FINFORCED_N), CC_Pers()), fnB, 3500)
                        ENDIF
                        g_menuLevel = ML_NONE
                        EXIT
                    ELSE
                        // 她收了钱、答应了 → FINACC（C# 7373-7411 各 accept 分支）
                        IF NOT finSpoke
                            TEXT_LABEL_15 fnC
                            CC_GetGirlName(fnC)
                            CC_ShowLine2C(CC_FINACC_1_H(CC_Pers()), CC_FINACC_T(CC_DlgPick(CC_FINACC_N), CC_Pers()), fnC, 3000)
                            finSpoke = TRUE
                        ENDIF
                    ENDIF
                ELSE
                    // 非妓女（平民向）A-Life：本端口没有单独的"请求内射"这一步，
                    // 等同"她默认接受" ⇒ FINACC。
                    IF NOT finSpoke
                        TEXT_LABEL_15 fnD
                        CC_GetGirlName(fnD)
                        CC_ShowLine2C(CC_FINACC_1_H(CC_Pers()), CC_FINACC_T(CC_DlgPick(CC_FINACC_N), CC_Pers()), fnD, 3000)
                        finSpoke = TRUE
                    ENDIF
                ENDIF
                g_finAsked = TRUE
            ENDIF
            CC_RequestFinish(TRUE)
        ENDIF
        EXIT
    ENDIF
    // SETTINGS
    IF level = ML_SETTINGS
        // ★★★ v0.65：设置项顺序已重排，**编号必须与 CC_MenuLabel 的 CASE 一一对应**。
        //   当前顺序：0 游戏模式 / 1 AI 模式 / 2 警察无视 / 3 服务语音 / 4 呻吟语音 /
        //   5 结束语音 / 6 教学提示 / 7 街坐动画 / 8 车引擎 / 9 可驾驶车 / 10 车内灯 /
        //   11 假体 / 12 口交震动 / 13 拍打震动 / 14(ELSE) 热咖啡模组总开关。
        //   （用户要求"模式选择在最上面、震动和假体放最下面"。）
        IF i = 0
            // ★ 2026-09-26 v0.58：补上 C# 的守卫（HandleMenuSelect `case 4`）：
            //     if (!(hasGirl && aLifeMode)) sandboxMode = !sandboxMode;
            //   带着女伴、且 A-Life 开着时**不允许**切游戏模式 —— 她这条 A-Life 数据
            //   是按旧规则建的，中途换规则会让收费/关系判定前后不一致。
            //   ★ 一定要给可见反馈：否则用户只会觉得"这个开关坏了"（记忆里"留可见状态"那条）。
            IF g_hasGirl AND g_aLifeMode
                CC_HudNote("~r~先让她离开才能切换游戏模式（互动菜单最后一项：让她离开）", 4000)
            ELSE
                g_sandboxMode = NOT g_sandboxMode
                IF g_sandboxMode
                    // ★★★ v0.72：一并去掉"服务免费"（"提示里别提钱"的要求，见上面搭讪那条）。
                    CC_HudNote("~g~游戏模式：沙盒（走近即上场，不用搭讪）", 3000)
                ELSE
                    // ★★★ v0.72：文案里**不再提钱**（用户反馈"搭讪模式的提示没删干净，还提到钱"）。
                    //   v0.62 起服务已经全免费（`CC_Pay` 恒 TRUE），"要付钱"是残留的旧文案。
                    CC_HudNote("~o~游戏模式：搭讪（要先主动搭讪，她才会停下来）", 3500)
                ENDIF
            ENDIF
        ELIF i = 1
            // ★★★ 2026-09-26 v0.58 新增（第 14 项）：**AI 模式**（C# `aLifeMode`）。
            //   守卫照抄 C#（HandleMenuSelect `case 3`）：`if (!hasGirl) aLifeMode = !aLifeMode;`
            //   —— 只有**没带着女伴**时才能切。她一旦在场，性格/声望/话题/收费这套
            //   A-Life 数据就已经建好了；把整层中途关掉会让数据结构和菜单对不上。
            //   所以这里不存在"切一半炸掉"，最坏就是切不动 + 一行红字提示。
            IF g_hasGirl
                CC_HudNote("~r~先让她离开才能切换 AI 模式（互动菜单最后一项：让她离开）", 4000)
            ELSE
                g_aLifeMode = NOT g_aLifeMode
                IF g_aLifeMode
                    // ★★★ v0.72：去掉"收费"二字（服务早已全免费，见 CC_Pay）。
                    CC_HudNote("~g~AI 模式：A-Life（完整个性 / 声望 / 话题 / 关系推进）", 3500)
                ELSE
                    CC_HudNote("~y~AI 模式：基础（无互动菜单、无声望、无个性差异）", 3500)
                ENDIF
            ENDIF
        ELIF i = 2
            // ★ 2026-09-25 新增（原第 13 项）：警察无视开关。
            //   只翻标志位；真正的 SET_EVERYONE_IGNORE_PLAYER 由 CC_ApplyPoliceIgnore()
            //   在下一帧按"变化驱动"补上（不在这里直接发 native，避免与别的调用点打架）。
            g_policeIgnore = NOT g_policeIgnore
        ELIF i = 3
            g_voiceSvc = NOT g_voiceSvc
        ELIF i = 4
            g_voiceMoan = NOT g_voiceMoan
        ELIF i = 5
            g_voiceEnd = NOT g_voiceEnd
        ELIF i = 6
            // ══════════════════════════════════════════════════════════════
            // ★★★ v0.69 新增 / v0.70 改成"钉死一个序号"（用户报"有的女伴就是没声音"）
            //
            //   为什么要手动：嗓音名表与 C# 原版**逐字一致**（9 个 hooker 嗓子），
            //   所以差异只可能在"这台机器的音频数据里，某几个嗓子拿不到语音"。
            //   已有的自动轮换（cc_main 的 CC_VoiceProbeTick）依赖
            //   `IS_AMBIENT_SPEECH_PLAYING` —— 本端口**未证明**的 native：
            //   它要是恒 TRUE，就会在第一句就把**坏嗓子锁死** ⇒ 这个女伴永远没声音。
            //   ⇒ 给玩家一个不依赖任何猜测的开关：
            //
            //   ★ v0.70 语义（对应用户"每个人单独随机"的要求）：
            //     `g_voiceFix = -1`（默认）＝ **随机：每个女伴单独摇一个**；
            //     按一下 → 1/8 → 2/8 → … → 8/8 → 回到"随机"。
            //     钉住某个数字 ⇒ **所有女伴统一用它**（用途：试出能出声的那几个之后钉住）。
            //     ★ 注意改的是 `g_voiceFix`（会跨女伴保留），**不是** `g_voiceIdx`
            //       （那是"当前这位女伴"的值，换人时 CC_FullReset 会清成 -1）。
            //   ★ v0.79：坏嗓子删掉后总数是 8（0..7）⇒ 回绕值从 8 改成 7。
            // ══════════════════════════════════════════════════════════════
            g_voiceFix = g_voiceFix + 1
            IF g_voiceFix > 7
                g_voiceFix = -1
            ENDIF
            g_voiceProbeShown = TRUE
            // ★★★ v0.79：**这一段的三条屏幕提示全部删掉**（用户要求）。
            //   原来会打：① "女伴嗓音：随机（每个女伴单独摇一个）"
            //             ② 嗓子名本身（`g_girlVoice`，试听时）
            //             ③ "女伴嗓音已设定（现在没有女伴，下次生效）"
            //   现在**唯一的反馈就是菜单标签本身**（`女伴嗓音: 随机 / 1/8..8/8` 会立刻变），
            //   这已经满足"改设置要把新值回显"的纪律，且不再往屏幕下方那座字幕区加字。
            //   试听的声音**保留**（钉死某一号时仍会念一句 SEX_GENERIC，用来确认这个嗓子能出声）。
            IF g_voiceFix < 0
                // 回到"随机"：当场给当前这位摇一个新的
                g_voiceIdx = CC_VoiceRandOk()
                g_voiceLocked = FALSE
            ELSE
                // 钉死这个序号：当前这位立刻改过去
                g_voiceIdx = g_voiceFix
                g_voiceLocked = TRUE
                g_voiceRotate = 0
            ENDIF
            g_girlVoice = CC_VOICE_NAME(g_voiceIdx)
            IF CC_EXISTS(g_girl)
                SET_AMBIENT_VOICE_NAME(g_girl, g_girlVoice)
                g_voiceReapplyAt = GET_GAME_TIMER() + 3000
                IF g_voiceFix >= 0
                    CC_PlaySpeech("SEX_GENERIC")     // 试听（不出声说明这个嗓子取不到语音）
                ENDIF
            ENDIF
        ELIF i = 7
            g_showTut = NOT g_showTut
        ELIF i = 8
            g_streetSitAnimOn = NOT g_streetSitAnimOn
        ELIF i = 9
            g_carEngineOn = NOT g_carEngineOn
        ELIF i = 10
            g_driveable = NOT g_driveable
        ELIF i = 11
            g_intLightOn = NOT g_intLightOn
        ELIF i = 12
            g_dildoOn = NOT g_dildoOn
        ELIF i = 13
            g_bjSoundsOn = NOT g_bjSoundsOn
        ELIF i = 14
            g_slapSoundsOn = NOT g_slapSoundsOn
        ELSE
            // ★★★ 2026-09-25晚 v0.57 新增（原第 14 项；v0.58 起顺延为第 15 项）：
            //   热咖啡模组**总开关**。
            //   ★ 这一项**故意留在最后**并用 `ELSE` 兜底 —— 用户的习惯是"最后一项 = 总开关"，
            //     而且以后再加菜单项时，只要插在它前面、并把这个 ELSE 保持为最后，
            //     总开关的下标会**自动顺延**，不用改这里。
            g_modOn = NOT g_modOn
            IF NOT g_modOn
                // CC_FullReset(FALSE) 做的事正好就是 C# 的 ReleaseGirl：
                //   清任务 / SET_PED_AS_NO_LONGER_NEEDED / 移掉地图标点 /
                //   复位警察无视 / 关菜单 / 回 ST_IDLE。
                // ⇒ 她从此是普通 NPC：不跟随、不被打标点，**不需要杀她**。
                //   （它会顺手把 g_menuLevel 置 ML_NONE —— 菜单当场关掉，是期望行为。）
                CC_FullReset(FALSE)
                // 用**不受门控**的 CC_HudNote：把"怎么再开回来"一并告诉用户，
                // 否则模组休眠后菜单打不开，用户会以为坏了。
                CC_HudNote("~y~热咖啡模组：已关闭（女伴已变回普通 NPC）｜ 按 B + 十字键右 可重新开启", 6000)
            ELSE
                CC_HudNote("~g~热咖啡模组：已开启", 2500)
            ENDIF
        ENDIF
        // ══════════════════════════════════════════════════════════════════
        // ★★★ v0.64：**每次改设置都把新状态打到屏幕上**。
        //
        //   为什么：这些开关是"电平"的 —— 按 A、**按 ← / → 也会翻**（用户要求过
        //   "十字键左右也能切换"）。于是很容易被蹭到而自己完全不知道。
        //   用户报的「有的人有声音、有的没有」，最后就落在设置菜单里的
        //   「呻吟语音: 开/关」被翻掉了 —— 那是**全局**开关，换女伴不会自己恢复，
        //   所以看起来像"这个女伴没声音"。打一行出来，改了什么都一目了然。
        //
        //   ⚠ 以下三项**跳过**，因为它们自己已经打了更详细的提示，别被这行盖掉：
        //       i = 0 「游戏模式」—— 被守卫拦住时有红字"先让她离开才能切换游戏模式"
        //       i = 1 「AI 模式」 —— 同上
        //       i = 14「热咖啡模组总开关」—— 有"关了怎么再开回来"的长提示
        //     **以后往设置菜单加项，记得把这里的比较值一起核对**
        //     （总开关的下标 = CC_MenuCount(ML_SETTINGS) - 1；v0.69 起是 15）。
        IF i != 0 AND i != 1 AND i != 6 AND i != 15
            CC_HudNote(CC_MenuLabel(ML_SETTINGS, i), 1800)
        ENDIF
        EXIT
    ENDIF
    IF level = ML_COMPAT
        IF i = 0
            g_sinkraLay = NOT g_sinkraLay
        ELIF i = 1
            g_sinkraRevLay = NOT g_sinkraRevLay
        ELSE
            g_sinkraFace = NOT g_sinkraFace
        ENDIF
        EXIT
    ENDIF
    // ACTIONS
    IF level = ML_ACTIONS
        // ══════════════════════════════════════════════════════════════════
        // ★★★ v0.79：**没有她在场时，互动里的动作一个也不该能点**
        //
        //   用户报的："附近根本没有女伴的时候，互动里的聊天还会冒出一句
        //   「她：xxxx」在回复"。
        //   根因是 v0.69 那条纪律（"记账看条目、显示无条件"）走过头了：
        //   各子分支在 A-Life 条目缺失时用**兜底值**（`CC_Pers()`→0、
        //   `CC_GetGirlName()`→"她"）照样把台词渲染出来
        //   ⇒ **空气里也有个"她"在回话**，比"点了没反应"更糟。
        //
        //   ⇒ 在导航之前统一拦一道，并给一句人能看懂的说明。
        //   判据与 cc_main 每帧那道 reconcile 一致：**有条目** 或 **真有一个她在场**。
        //   （"有条目但 ped 还没 stream"这种中间态仍然放行 —— 那种情况下她确实存在。）
        //   ⚠ 沙盒那条路（主菜单 →「做爱」→ 服务）不经过这里，不受影响。
        //   ⚠ 用 `CC_HudNote`（不受"教学提示"开关门控）—— 这是功能性反馈，不是教学。
        // ══════════════════════════════════════════════════════════════════
        BOOL ccAnyGirl = FALSE
        IF CC_AlOk()
            ccAnyGirl = TRUE
        ELIF g_hasGirl AND CC_EXISTS(g_girl)
            ccAnyGirl = TRUE
        ENDIF
        IF NOT ccAnyGirl
            CC_HudNote("~y~现在没有可以互动的对象 —— 先搭讪一个，或者让她跟着你。", 3000)
            g_menuIndex = 0
            EXIT
        ENDIF
        IF i = 0
            g_menuLevel = ML_CONVERSATION
        ELIF i = 1
            g_menuLevel = ML_INTIMACY
        ELIF i = 2
            g_menuLevel = ML_ESCALATE
        ELIF i = 3
            // ══════════════════════════════════════════════════════════════════
            // ★★★ v0.79：**「做爱」按关系门控**（用户报："搭讪模式里无论什么关系
            //   貌似都能直接做爱，是不是有点奇怪"）
            //
            //   C# 里这条走 `IsServiceAvailable`（8863 调用点）—— 那个函数**不按关系门控**，
            //   它按**性格**闸（Riskiness<0.40 野外不做、Attachment≥0.65 不亲）。
            //   那些性格闸端口已经有了（`CC_SvcGate`）。C# 之所以不需要关系门槛，是因为
            //   C# 那个模式下**钱就是通行证**（收费街女，掏钱就能上）。
            //   端口已经去妓女化 + 免费（v0.74）⇒ 再不给关系门槛，这一项就**零门槛**。
            //   ⇒ 规则：**搭讪模式要求「暧昧」以上**（= 她对你表示过兴趣）；
            //     **沙盒模式完全不门控**（"上来就能开干"是你的硬要求，一个字没动）。
            //   ⚠ 想放宽/收紧就改 `RST_FLIRTY` 这一个词（例如 RST_FRIENDLY = 只要聊熟）。
            // ══════════════════════════════════════════════════════════════════
            IF g_aLifeMode AND NOT g_sandboxMode
                BOOL svcOK = FALSE
                IF CC_AlOk()
                    IF g_alRSt[g_curAlIdx] >= RST_FLIRTY
                        svcOK = TRUE
                    ENDIF
                ELSE
                    svcOK = TRUE      // 条目缺失（普通 NPC）⇒ 不拦，交给后面的既有判定
                ENDIF
                IF NOT svcOK
                    CC_HudStatus("~y~她跟你还没到那一步 —— 先聊天、调情，把关系拉起来。", 3500)
                    g_menuIndex = 0
                    EXIT
                ENDIF
            ENDIF
            g_menuLevel = ML_SERVICES
        ELIF i = 4
            g_menuLevel = ML_INVITEVEH
        ELIF i = 5
            g_menuLevel = ML_GIVEMONEY
        ELIF i = 6
            g_menuLevel = ML_LEAVE
        ELIF i = 7
            // ★★★ v0.61：直接放生 —— 她当场变回普通 NPC，不跟随、无标点，**不用杀她**。
            //   加这个是因为设置菜单两条守卫（切游戏模式 / 切 AI 模式）都要求
            //   "先让她离开"，而原来的「离开」要走 C# 的道别流程
            //   （先选风格 → 再说台词 → 最后才放生）—— 用户找不到，以为没这个选项。
            //   CC_FullReset(FALSE) 与 C# 的 ReleaseGirl 等价：清任务 /
            //   SET_PED_AS_NO_LONGER_NEEDED / 移标点 / 复位警察无视 / 关菜单 / 回 ST_IDLE。
            //   ★ v0.59 起它还会顺手把玩家侧的动作/位置收尾（动作中按也不会卡半空）。
            IF g_hasGirl
                CC_FullReset(FALSE)
                CC_HudNote("~g~她已离开（变回普通 NPC）。现在可以切换游戏模式 / AI 模式了。", 4000)
            ELSE
                CC_HudNote("~y~现在没有跟着你的女伴。", 2500)
            ENDIF
        ELIF i = 8
            // ══════════════════════════════════════════════════════════════════
            //  ★★★ v0.75：「表白 / 分手」—— 关系推进
            //    （C# `Make Her Mine` / `Break Up`：`GetConvSubResponse` case 4
            //      + 菜单处理 CinnamonCoffee.cs:8236-8270 + `TryGfPassiveBreakup` 16538-16579）
            //
            //  【C# 原逻辑】
            //    · 关系**不是**"暧昧/女友" ⇒ 只回一句"太快了/我们不熟/我当你是朋友"，**不改状态**
            //    · 关系 = 暧昧 ⇒ 成功 ⇒ **女友**（同时 IsHooker=false、清价格表、声望 +70）
            //                    失败 ⇒ **朋友区**
            //    · 关系 = 女友 ⇒ 这一项在 C# 里变成「分手」
            //
            //  ★ 关于"暧昧态必定成功"：C# 那里 `isPositive = IsPositive(convResp)`，
            //    而 case 4 里**只写了"非暧昧/女友"和"女友"两种回应** —— 暧昧态没有任何
            //    分支命中，函数返回空串，`IsPositive("")` 恒为真 ⇒ **C# 里就是必定成功**。
            //    这里照抄这个行为。（要改成"要掷骰子"的话说一声，那属于改功能。）
            // ══════════════════════════════════════════════════════════════════
            IF NOT CC_AlOk()
                CC_HudStatus("~y~现在没有可表白的对象。", 2000)
                EXIT
            ENDIF
            INT st8 = g_alRSt[g_curAlIdx]
            TEXT_LABEL_15 nm8
            CC_GetGirlName(nm8)
            IF st8 >= RST_GIRLFRIEND
                // ── 分手 ──────────────────────────────────────────────────────
                //   ★ v0.76：用 `>=`，这样「痴迷」(7) 也有分手出口。
                //   C#：rep < 40 且掷中 40% ⇒ 硬分手（GFHARD）；
                //       否则 rep < 60 且掷中 25% ⇒ 软分手（GFSOFT，落到"朋友区"）。
                //   两种都当场放生（FullReset + Wander）。都没掷中 ⇒ 这次没分掉。
                BOOL broke8 = FALSE
                IF CC_Rep() < 40 AND CC_ROLL(40)
                    CC_ShowLine2(CC_GFHARD_1_H(CC_Pers()), CC_GFHARD_T(CC_DlgPick(CC_GFHARD_N), CC_Pers()), nm8, 5500)
                    // C#：50% 直接把声望打到 -1（她躲你），否则按声望落回基础态
                    IF CC_ROLL(50)
                        g_alRep[g_curAlIdx] = -1
                    ELSE
                        g_alRSt[g_curAlIdx] = RST_STRANGER
                    ENDIF
                    broke8 = TRUE
                ELIF CC_Rep() < 60 AND CC_ROLL(25)
                    CC_ShowLine2(CC_GFSOFT_1_H(CC_Pers()), CC_GFSOFT_T(CC_DlgPick(CC_GFSOFT_N), CC_Pers()), nm8, 5000)
                    g_alRSt[g_curAlIdx] = RST_FRIENDZONE
                    broke8 = TRUE
                ENDIF
                IF broke8
                    AL_UpdateRel(g_curAlIdx)
                    CC_FullReset(FALSE)
                    IF CC_EXISTS(g_girl)
                        TASK_WANDER_STANDARD(g_girl, 0.0, INT_TO_ENUM(EWDR_SCRIPT_FLAGS, 0))
                    ENDIF
                    CC_HudNote("~y~这段关系结束了。她已经离开。", 4000)
                ELSE
                    CC_HudStatus("~y~她没接这句话。等会儿再说？", 2500)
                ENDIF
                g_menuIndex = 0
                EXIT
            ENDIF
            IF st8 = RST_FLIRTY
                // ── 表白成功 ⇒ 女友（C#：Flirty + 正面 ⇒ Girlfriend）──
                g_alRSt[g_curAlIdx] = RST_GIRLFRIEND
                g_alHooker[g_curAlIdx] = FALSE        // C#：IsHooker = false（不再按交易口径）
                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 70
                CC_ClampRep(g_curAlIdx)
                AL_UpdateRel(g_curAlIdx)
                CC_ShowLine2(CC_ILYRESP_1_H(CC_Pers()), CC_ILYRESP_T(CC_DlgPick(CC_ILYRESP_N), CC_Pers()), nm8, 5000)
                CC_HudNote("~g~你们现在是情侣了。", 4500)
                g_menuIndex = 0
                EXIT
            ENDIF
            // ── 还没到"暧昧"：只给一句拒绝（C# 的 朋友区 / 不熟 / 太快了 三分支）──
            IF st8 = RST_FRIENDZONE
                CC_ShowLine2("~r~", ":~s~ 我说过了。我对你不是那种感觉。", nm8, 4000)
            ELIF CC_Rep() < 10
                CC_ShowLine2("~r~", ":~s~ 我还不认识你。", nm8, 4000)
            ELSE
                CC_ShowLine2("~r~", ":~s~ 太快了。我们还没到那一步。", nm8, 4000)
            ENDIF
            g_menuIndex = 0
            EXIT
        ENDIF
        g_menuIndex = 0
        EXIT
    ENDIF
    // CONVERSATION (效果逻辑 1:1: rep/mood/话题位; 台词为浓缩版)
    IF level = ML_CONVERSATION
        // ══════════════════════════════════════════════════════════════════
        // ★★★ v0.69 修「有时候选什么对话女伴都不会回应」：
        //
        //   原来**整段**套在 `IF g_curAlIdx >= 0` 里 ⇒ 一旦 A-Life 条目不可用
        //   （`g_curAlIdx = -1`：条目还没建 / 建的时候 ped 没 stream 进来），
        //   这里**一个字都不打印** —— 而"服务"那条路不依赖 A-Life 条目，
        //   ⇒ 用户看到的现象正是"对话没反应，但能推进到可服务状态"。
        //
        //   现在：**记账**（话题位/声望/见过面）只在条目可用时做；
        //        **回应一律显示**（CC_Pers()/CC_GirlName() 会兜底，不会越界）。
        //   另外 cc_main 每帧有一道 reconcile：有女伴却没有 A-Life 条目就补建一条。
        // ══════════════════════════════════════════════════════════════════
        // ★★★ v0.77：「我爱你」（i=7）—— C# 里是**调情分支的第 8 项**
        //   端口原来只有一个入口：道别 → 奉承 → 第 4 句（v0.74 去妓女化之后才出现）。
        //   现在聊天菜单也有正式入口。
        //   【C# 口径】CinnamonCoffee.cs:8216-8221（拒绝罚）与 8740（女友加声望）
        //     · 女友 / 痴迷 ⇒ 正面回应 `ILYRESP`（`GetGfILYResponseLines`）+ 小幅加声望
        //     · 其它      ⇒ 拒绝 `ILYREJ`，声望 **-10**
        //   ☠ 这一段放在 `IF CC_AlOk()` **之前** —— 回应必须**无条件显示**
        //     （工程纪律："记账看条目、显示无条件"）。
        IF i = 7
            TEXT_LABEL_15 ilyNm
            CC_GetGirlName(ilyNm)
            BOOL isGF7 = FALSE
            IF CC_AlOk()
                IF g_alRSt[g_curAlIdx] >= RST_GIRLFRIEND
                    isGF7 = TRUE
                ENDIF
                IF isGF7
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 5
                ELSE
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 10
                ENDIF
                CC_ClampRep(g_curAlIdx)
                AL_UpdateRel(g_curAlIdx)
            ENDIF
            IF isGF7
                CC_ShowLine2(CC_ILYRESP_1_H(CC_Pers()), CC_ILYRESP_T(CC_DlgPick(CC_ILYRESP_N), CC_Pers()), ilyNm, 5000)
            ELSE
                // ★ 只用变体 1~3：ILYREJ 的变体 4~6 在数据里缺 11 个性格（见 cc_dlgnav 的说明）
                CC_ShowLine2(CC_ILYREJ_1_H(CC_Pers()), CC_ILYREJ_T(CC_DlgPick(3), CC_Pers()), ilyNm, 5000)
            ENDIF
            EXIT
        ENDIF
        IF CC_AlOk()
            // ★★★ v0.72（D 类接线）：**补上 C# 的"重复问"惩罚 + 走开**
            //
            //   C#（7881-7928）里，问话题的声望是**分首次/重复**的：
            //       首次问      → +1
            //       重复问      → -1（"问心情"这一支）/ -2（其余支）
            //   而本端口原来是**无条件 +1** ⇒ 声望只会涨 ⇒ 永远到不了 -1
            //   ⇒ 「她已经在躲你」这个状态根本维持不住，WALKAWAY 也就永远触发不了。
            //
            //   现在按 C# 记账，并接上 **WALKAWAY**（C# 7915-7928）：
            //       她已经在躲你（问之前 rep <= -1）**且**你明知故问又问了一遍
            //     ⇒ 说一句"走开"的话 + 当场放生（她是普通 NPC 了，任务改 Wander）。
            //   第一次问（哪怕在躲你）只会冷冷回一句 —— 不会当场走人。
            INT cbit = CC_BIT(i)
            // ★ 用 `|` 反推"这一位原本就有了没有" —— 本工程已证明 `|` 可用，
            //   而 SanScript 的位与 `&` 在源码里从没用过（`&` 又是引用符号，风险大）。
            BOOL wasAsked = FALSE
            IF (g_alTopics[g_curAlIdx] | cbit) = g_alTopics[g_curAlIdx]
                wasAsked = TRUE
            ENDIF
            BOOL wasAvoiding = FALSE
            IF g_alRep[g_curAlIdx] <= -1
                wasAvoiding = TRUE
            ENDIF
            g_alTopics[g_curAlIdx] = g_alTopics[g_curAlIdx] | cbit
            IF wasAsked
                IF i = 1
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 1
                ELSE
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 2
                ENDIF
            ELIF i <> 4
                // ★ v0.75：「调情」(i=4) 的加声望改走 CC_FlirtPivot（C# 是 +10，不是 +1）
                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
            ENDIF
            CC_ClampRep(g_curAlIdx)
            AL_UpdateRel(g_curAlIdx)
            // ★★★ v0.75：「调情」的关系跳档（C# 的 Flirt 分支，8171-8208）
            //   ⇒ 认识 65% / 熟识 35% / 友好 10% 会让关系跳到「暧昧」
            IF i = 4
                CC_FlirtPivot()
            ENDIF
            IF i = 6
                g_alMet[g_curAlIdx] = TRUE
            ENDIF
            IF wasAvoiding AND wasAsked
                TEXT_LABEL_15 waNm
                CC_GetGirlName(waNm)
                CC_ShowLine2(CC_WALKAWAY_1_H(CC_Pers()), CC_WALKAWAY_T(CC_DlgPick(CC_WALKAWAY_N), CC_Pers()), waNm, 4500)
                // C# 7924-7926：FullReset + 把她改成 Wander（不给她新任务她会自己走回来）。
                // 本工程的 CC_FullReset(FALSE) 已经包含"放生 + 清任务"，
                // 额外再给一个 Wander 任务，和 C# 一致（见记忆里"放生 NPC 会自己回来"那条）。
                CC_FullReset(FALSE)
                IF CC_EXISTS(g_girl)
                    TASK_WANDER_STANDARD(g_girl, 0.0, INT_TO_ENUM(EWDR_SCRIPT_FLAGS, 0))
                ENDIF
                EXIT
            ENDIF
        ENDIF
        TEXT_LABEL_63 p9[9]
        TEXT_LABEL_15 nm9
        CC_GetGirlName(nm9)
        p9[0] = "~g~"
        p9[1] = nm9
        // ★★★ v0.61 修「7 个话题点哪个都是同一句」：
        //   原来这里是一句**写死的占位台词**（"……谢谢你还来问这个。"），
        //   上面那行注释自己也写了"真正按分支/话题分化的版本待办" —— 就是这一条。
        //   C# 是 GetConvResponse(branch, item, d)：按 话题 + 性格 + 声望 + 心情
        //   + 天气/昼夜 出专属回应。本端口没有昼夜/天气判定，
        //   所以按 话题(7) × 性格语气(冷/中/暖) 出回应 —— 见 cc_applines.CC_ConvResp。
        // ★★★ v0.69 修：原来第二个参数传的是 `g_curAlIdx`（**A-Life 槽位号**），
        //   而 CC_ToneBucket/CC_ConvResp 要的是**性格索引**（同文件 1095 行那条
        //   `CC_TestWaterLine(g_alPers[g_curAlIdx], ...)` 才是对的写法）。
        //   传槽位号 ⇒ 语气按"第几个女伴"而不是"她是什么性格" ⇒ 张冠李戴。
        p9[2] = CC_ConvResp(i, CC_Pers())
        BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
        INT ci = 0
        WHILE ci < 3
            ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(p9[ci])
            ci = ci + 1
        ENDWHILE
        END_TEXT_COMMAND_PRINT(3000, TRUE)
        EXIT
    ENDIF
    IF level = ML_INTIMACY
        // ★★★ v0.76：这层改成**纯导航**。
        //   C# 的 `MenuLevel.Intimacy` 本来就是 3~4 项、前两项带 `[>]` 进子菜单
        //   （`DrawIntimacyMenu`，CinnamonCoffee.cs:18419-18427）：
        //     「问她的偏好 [>]」「试探一下 [>]」「搞得亲密一点」「后座（条件）」
        //   端口原来把两层**压成了一层 2 项** ⇒ 「问偏好」8 个问题、「试探」3 句
        //   全都听不到，其中**第 8 个问题是「要不要保持这种随便的关系」**（炮友关系确认）。
        //   ⇒ 现在拆回两层：本层只管跳转，实际效果在 ML_INTIMACYSUB。
        IF i = 0
            g_intSubBranch = 0
            g_menuLevel = ML_INTIMACYSUB
            g_menuIndex = 0
        ELIF i = 1
            g_intSubBranch = 1
            g_menuLevel = ML_INTIMACYSUB
            g_menuIndex = 0
        ELSE
            // 「搞得亲密一点」= C# 的 `Make it Sexual` ⇒ 直接进升温菜单（C# 同款跳转）
            g_menuLevel = ML_ESCALATE
            g_menuIndex = 0
        ENDIF
        EXIT
    ENDIF
    // ══════════════════════════════════════════════════════════════════════════
    //  ★★★ v0.76 新增：ML_INTIMACYSUB（C# `DrawIntimacySubMenu`，CinnamonCoffee.cs:18434-18468）
    //    · 分支 0「问她的偏好」**8 项**：亲不亲 / 公开还是私下 / 你喜欢什么 /
    //      用嘴 / 粗暴 / 你会主动吗 / 你绝对不接受什么 / **要不要保持这种随便的关系**
    //    · 分支 1「试探一下」**3 项**
    //  【记账口径】
    //    · 分支 0 的「用嘴」(i=3) 与「粗暴」(i=4) **真的定下她的偏好** ——
    //      这就是 v0.72 原来挂在「问她的偏好」上的那段逻辑，现在按 C# 拆成两个问题。
    //    · 「要不要保持随便」(i=7) ⇒ 置 `g_alCasualOK` + 声望 +1（C# 里这句是关系确认）
    //    · 分支 1 的第 1 项用 `CC_TwAccPct`（C# 8432-8440 的按关系档接受率，女友 0.95）；
    //      后两项只按"正面/负面"加减 1 点。
    //  ☠ 回答文案是**语气档 × 条目**（33 句，见 cc_dlgnav 的 CC_INTASK_T / CC_INTTEST_T），
    //    **不是** C# 的逐性格版（那要 220 句）。
    // ══════════════════════════════════════════════════════════════════════════
    IF level = ML_INTIMACYSUB
        TEXT_LABEL_15 subNm
        CC_GetGirlName(subNm)
        INT subTone = CC_ToneBucket(CC_Pers())
        IF g_intSubBranch = 0
            IF i = 3
                IF CC_AlOk()
                    INT ipP = CC_Pers()
                    g_alPrefBJ[g_curAlIdx] = 1
                    IF NOT CC_ROLL(30 + FRIENDLINESS[ipP] / 4 + RISKINESS[ipP] / 4)
                        g_alPrefBJ[g_curAlIdx] = 2
                    ENDIF
                    g_alTopics[g_curAlIdx] = g_alTopics[g_curAlIdx] | CC_BIT(16)
                ENDIF
            ELIF i = 4
                IF CC_AlOk()
                    INT ipP2 = CC_Pers()
                    g_alPrefRough[g_curAlIdx] = 1
                    IF NOT CC_ROLL(15 + RISKINESS[ipP2] / 2)
                        g_alPrefRough[g_curAlIdx] = 2
                    ENDIF
                ENDIF
            ELIF i = 7
                IF CC_AlOk()
                    g_alCasualOK[g_curAlIdx] = 1
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
                    CC_ClampRep(g_curAlIdx)
                    AL_UpdateRel(g_curAlIdx)
                ENDIF
                // ★ v0.79：原来这里打的是「你们说清了：保持这种随便的关系。」
                //   —— 和下面她那句回答（`CC_INTASK_T(7,…)`：
                //      "行啊。我不喜欢被绑着。" / "可以。但别把我当随便的人。"）
                //   说的是同一件事 ⇒ 用户报"下面的提示和女伴回答重复了"。
                //   改成**纯机械反馈**（只报"状态变了 + 声望动了"），
                //   flavor 全部留给她那句台词。
                CC_HudNote("~g~她说可以（关系：保持随便 / 声望 +1）", 3000)
            ENDIF
            CC_ShowLine2("~g~", CC_INTASK_T(i, subTone), subNm, 3500)
        ELSE
            IF i = 0
                // C# 8432：这一项"高风险高回报"，接受率**按关系档**（女友/痴迷 0.95）
                BOOL twOk2 = FALSE
                IF CC_AlOk()
                    twOk2 = CC_ROLL(CC_TwAccPct(CC_Pers()))
                    IF twOk2
                        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 2
                        g_alMood[g_curAlIdx] = 1
                    ELSE
                        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 1
                        g_alMood[g_curAlIdx] = 5
                    ENDIF
                    CC_ClampRep(g_curAlIdx)
                    g_alTopics[g_curAlIdx] = g_alTopics[g_curAlIdx] | CC_BIT(17)
                    AL_UpdateRel(g_curAlIdx)
                    CC_ApplyMoodFace(g_girl, g_alMood[g_curAlIdx])
                ENDIF
                CC_ShowLine2("~g~", CC_TestWaterLine(CC_Pers(), twOk2), subNm, 3500)
            ELSE
                BOOL okSm = CC_RespPositive(CC_Pers())
                IF CC_AlOk()
                    IF okSm
                        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
                    ELSE
                        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 1
                    ENDIF
                    CC_ClampRep(g_curAlIdx)
                    AL_UpdateRel(g_curAlIdx)
                ENDIF
                CC_ShowLine2("~g~", CC_INTTEST_T(i, subTone), subNm, 3500)
            ENDIF
        ENDIF
        EXIT
    ENDIF
    // ESCALATE (提议性关系)
    IF level = ML_ESCALATE
        // ══════════════════════════════════════════════════════════════════════
        // ★★★ v0.72 修两个真 bug：「升级菜单用错了表」+「完全没用 i」
        //
        //  【1】表错了。C# 的 `MenuLevel.Escalate` 用的是
        //        `GetEscalateAcceptLine` / `GetEscalateRejectLine`（C# 8575 / 8582）；
        //        而 `GetProposition*`（PROPACC / PROPDECL）在 C# 里是
        //        **Conversation 的「提议」分支**用的（调用点 7943 / 8052 / 8058），
        //        跟这个菜单**不是一回事**。
        //        旧代码接的是 PROPACC/PROPDECL ⇒ 说的是"提议"的口气。
        //        ⇒ 现在改用 `src/cc_esc.sc` 的 CC_ESCACC / CC_ESCREJ。
        //
        //  【2】`i` 被完全忽略。这个菜单有 **3 句**（lineIdx 0/1/2）：
        //          0「咱们找点乐子。」  1「介意我……靠近点吗？」  2「想做吗？」
        //        C# 里选哪句**同时影响接受率与台词**（C# 8566-8568 + 8575/8582）。
        //        旧代码三句走同一条路 ⇒ 三个选项完全等价。
        //        ⇒ 现在把 `i` 当 lineIdx，台词按它选、接受率也按它微调。
        //
        //  【3】顺手改掉"整段套在 `IF g_curAlIdx < 0` 里"的老毛病
        //        （v0.69 的纪律：**记账**看条目、**显示无条件**）。
        //        取值一律走安全入口 CC_Pers() / CC_Rep()，
        //        免得 `g_curAlIdx = -1` 时 `g_alPers[-1]` / `g_alRep[-1]` 越界读到垃圾
        //        （不报错，只让接受率变成随机数）。
        // ══════════════════════════════════════════════════════════════════════
        INT escLine = i                    // 0 / 1 / 2 = 选的是哪一句
        INT escPers = CC_Pers()            // 性格（安全取值；无条目时 0）
        // ★★★ v0.76：接受率改成**按关系档**（C# 8559-8569 逐条照抄，见 cc_part1 的 CC_EscAccPct）
        //   原来这里是 `15 + 声望/2 + 性格修正` —— **完全没用关系档**，
        //   女友和陌生人差不了多少，和"炮友向"的体感不符。
        //   现在：痴迷 92 / **女友 85** / 暧昧 52+冒险×0.28 / 友好 22+… / 熟识 8+… /
        //         认识 3+… / 陌生 1+…；再叠加"说了哪一句"的修正 + 声望每点 0.3%，夹到 2..98。
        INT accPct = CC_EscAccPct(escLine, escPers)
        // ★ 名字无条件取（无条目时 CC_GetGirlName 会给兜底"她"），保证**永远有反馈**
        TEXT_LABEL_15 escNm
        CC_GetGirlName(escNm)
        IF CC_ROLL(accPct)
            // 「升级」被接受 —— 用 ESC 表（按 lineIdx × 性格）
            CC_ShowLine2(CC_ESCACC_H(), CC_ESCACC_T(escLine, escPers), escNm, 3000)
            IF CC_AlOk()                   // 记账只在条目可用时做
                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
                IF g_alRep[g_curAlIdx] > CC_REP_MAX
                    g_alRep[g_curAlIdx] = CC_REP_MAX
                ENDIF
                AL_UpdateRel(g_curAlIdx)
            ENDIF
            // 打开 Services (A-Life hooker 或 casual → escalate 接受后可用)
            g_aLifeHookerSvc = TRUE
            PR_GenerateALifeHooker(g_curAlIdx)
            // ★ v0.64：原来是 WAIT(3200) —— WAIT 冻住脚本线程 ⇒ 菜单停止重画 ⇒
            //   玩家看到"菜单自己关闭了"。改成**不阻塞**导航锁，并且从 3.2 秒缩到 0.6 秒。
            g_navLockUntil = GET_GAME_TIMER() + 600
            g_menuLevel = ML_SERVICES
            g_menuIndex = 0
            CC_PlayServiceVoice()
        ELSE
            // 「升级」被拒 —— 用 ESC 表（按 lineIdx × 性格）
            CC_ShowLine2(CC_ESCREJ_H(), CC_ESCREJ_T(escLine, escPers), escNm, 3000)
            IF CC_AlOk()
                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 2
                IF g_alRep[g_curAlIdx] < CC_REP_MIN
                    g_alRep[g_curAlIdx] = CC_REP_MIN
                ENDIF
                AL_UpdateRel(g_curAlIdx)
            ENDIF
            // ★ v0.64：WAIT(3200) → 非阻塞导航锁，并缩到 0.6 秒（用户嫌硬直久）
            g_navLockUntil = GET_GAME_TIMER() + 600
            g_menuLevel = ML_ACTIONS
            g_menuIndex = 0
        ENDIF
        EXIT
    ENDIF
    IF level = ML_INVITEVEH
        IF CC_EXISTS(g_girl)
            IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
                g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
                // ══════════════════════════════════════════════════════════════
                // ★★★ v0.79：她**已经上车了**就别再"请"一遍（用户实测的 bug）
                //   【现象】她已经在车上时再点这一项 ⇒ **她下车绕到后排去**。
                //   【根因】下面的座位算法是"副驾(0) → 被占就退到 2(后排)"，
                //     她已经在副驾 ⇒ `IS_VEHICLE_SEAT_FREE(0)` = FALSE ⇒ seat = 2
                //     ⇒ 发 `TASK_ENTER_VEHICLE(…, 2, …)` ⇒ 引擎让她"换到那个座位"，
                //     表现出来就是下车再钻后排。
                //   【C# 怎么处理】`ShowInviteVehicleItem`（17453-17456）：两人同车时
                //     **视同已邀请**（`_invitedToVehicle = true`）并且**不显示这一项**。
                //     端口菜单项数固定、索引不能动 ⇒ 保留条目，但点了只给提示。
                // ══════════════════════════════════════════════════════════════
                IF IS_PED_IN_ANY_VEHICLE(g_girl, FALSE)
                    g_vehInvited = TRUE      // 同车 ⇒ 她已"同意过"，之后一路免请
                    CC_HudStatus("~y~她已经在车上了。", 2500)
                    g_menuLevel = ML_NONE
                    EXIT
                ENDIF
                // ★ v0.61 补四条守卫（原来一条都没有 ⇒ 用户"邀请好几次才上车"）：
                //   ① 座位必须真的空着 —— 原来是"后排取 1，否则取 0"，副驾有人也照发
                //      （TASK 立刻失败，她就站着不动 / 走开）。
                //   ② 把 seat 回退到 2 再检查一次，没位子就明说，别发一个必败的任务。
                //   ③ 距离太远（>12m）不发 —— TASK_ENTER_VEHICLE 太远会直接中断。
                //   ④ 记下 g_playerWasInVeh + 开重试窗口，避免 CC_IdleFollowTick
                //      把这一帧当成"玩家刚上车"再发一个进车任务（两个任务打架）。
                FLOAT dCar = VDIST(GET_ENTITY_COORDS(g_girl), GET_ENTITY_COORDS(g_car))
                INT seatI = 0
                IF CC_InBackSeat(g_car)
                    seatI = 1
                ENDIF
                IF NOT IS_VEHICLE_SEAT_FREE(g_car, INT_TO_ENUM(VEHICLE_SEAT, seatI))
                    seatI = 2
                ENDIF
                IF NOT IS_VEHICLE_SEAT_FREE(g_car, INT_TO_ENUM(VEHICLE_SEAT, seatI))
                    CC_HudStatus("~r~车里没空位了！", 2000)
                    EXIT
                ENDIF
                IF dCar > 12.0
                    CC_HudStatus("~r~她离车太远了，先把车开过去", 2500)
                    EXIT
                ENDIF
                // ══════════════════════════════════════════════════════════════
                //  ★★★ v0.76：**她可以拒绝**（C# 7682-7731）—— 原来端口**无条件接受**
                //    C# 的接受率按关系档给：暧昧 0.65+冒险×0.15 / 友好 0.44+… /
                //    熟识 0.20+… / 认识 0.08+… / 陌生 0.03+…，再 + 声望×0.003，夹 0.03..0.90；
                //    而**女友 / 朋友区 / 痴迷**在 C# 里根本不做这个判定（直接放行）⇒ 端口给 95。
                //    C# 被拒时**留在菜单里**，可以换句话再试 —— 这里照做（`EXIT` 不改 g_menuLevel）。
                //    ★ 这条补上了 D 类里最后一个"场景就在眼前、只差接线"的缺口（`VEHINVREJ`）。
                // ══════════════════════════════════════════════════════════════
                IF CC_AlOk() AND NOT CC_ROLL(CC_VehAccPct(CC_Pers()))
                    INT vvR = CC_DlgPick(CC_VEHINVREJ_N)
                    TEXT_LABEL_15 vrNm
                    CC_GetGirlName(vrNm)
                    CC_ShowLine2(CC_VEHINVREJ_H(vvR, CC_Pers()), CC_VEHINVREJ_T(vvR, CC_Pers()), vrNm, 3000)
                    g_menuIndex = 0
                    EXIT
                ENDIF
                // ★ v0.62：邀请上车前**再认领一次**（幂等）。
                //   用户报「再邀请上车的时候有概率也会上车后又跑走」——
                //   若她此刻只是"普通 NPC 带任务"（例如上一次被放生后又重新搭上），
                //   `SET_BLOCKING_OF_NON_TEMPORARY_EVENTS` 可能是 FALSE ⇒ 她一上车
                //   遇到任何"非临时事件"（引擎的路人惊慌 / 碰撞 / 枪声）就当场逃开。
                //   CC_ClaimGirl 做两件事：标 mission entity + 挡非临时事件，且幂等。
                CC_ClaimGirl()
                // ★★★ v0.78：记下"她已同意上车"（C# `_invitedToVehicle = true`，7709）。
                //   一旦置上，`CC_VehEntryGated()` 就一路放行 ⇒ 之后你上下车她都自动跟。
                //   注意只有**接受**分支会走到这里；被拒时就停在上面 `EXIT` 了
                //   （C# 同：拒绝不置标记，留在菜单里可以再试）。
                g_vehInvited = TRUE
                g_playerWasInVeh = TRUE
                g_vehInviteUntil = GET_GAME_TIMER() + 20000
                g_vehInviteAt = GET_GAME_TIMER() + 2000
                TASK_ENTER_VEHICLE(g_girl, g_car, -1, INT_TO_ENUM(VEHICLE_SEAT, seatI), 1.0, INT_TO_ENUM(ENTER_EXIT_VEHICLE_FLAGS, 0))
                IF g_curAlIdx >= 0
                    TEXT_LABEL_63 pv[8]

                    INT ivw
                    CC_ShowLine2(CC_VEHINVACC_1_H(g_alPers[g_curAlIdx]), CC_VEHINVACC_1_T(g_alPers[g_curAlIdx]), g_alName[g_curAlIdx], 3000)
                ENDIF
                g_menuLevel = ML_NONE
            ELSE
                CC_HudStatus("~r~先上车！", 2000)
            ENDIF
        ENDIF
        EXIT
    ENDIF
    // GIVEMONEY (C# GiveMoney: tier 台词 + 现金转移)
    IF level = ML_GIVEMONEY
        INT amt = 1
        IF i = 1
            amt = 10
        ELIF i = 2
            amt = 100
        ELIF i = 3
            amt = 1000
        ELIF i = 4
            amt = 10000
        ELIF i = 5
            amt = 100000
        ELIF i = 6
            amt = 1000000
        ELIF i = 7
            amt = 10000000
        ELIF i = 8
            amt = 100000000
        ENDIF
        IF CC_GetMoney() < amt
            CC_HudStatus("~r~钱不够！", 2000)
            EXIT
        ENDIF
        // ★★★ v0.72（B 类接线）：**给钱要能被拒绝**
        //
        //   以前这里**无条件接受** ⇒ C# 的 180 条拒绝台词 + 那套"接受率公式"
        //   在本端口里全是死代码。用户给多少她都说"谢了"，且钱必然出手。
        //
        //   现在照抄 C#（C# 7773-7835）：
        //     ① 先算档位 tier（越贪婪门槛越高，阈值已对齐本模组实际面额，见下注释）
        //     ② 按 tier / 性格 / 声望 算"接受率"（C# 7779-7797）
        //     ③ 摇一次 CC_ROLL(接受率)
        //          · 接受 → 钱才真的转出去（CC_AddMoney + g_alCash）+ GMACC 台词 + 声望
        //          · 拒绝 → **钱不出去、她的现金也不涨** + GMREJ 台词
        //   这样三档金额的"接受/拒绝"两套台词才都真的能听到。
        IF NOT CC_AlOk()
            // 没有 A-Life 条目 ⇒ 不知道该给谁、也没法按性格算接受率。
            // 宁可明说，也不能像旧代码那样"钱被扣掉、她一句话不说"（静默吞钱）。
            CC_HudStatus("~y~现在没有可给钱的对象。", 2000)
            EXIT
        ENDIF
        INT gmPers = CC_Pers()
        // ── 档位：越贪婪门槛越高 ──────────────────────────────────────────────
        // ★★★ v0.61 修「给钱只剩两档反应」：
        //   C# 的 GiveMoney 是一个**面额累加器**（9 行 × 数量），总价能凑出任意值，
        //   所以它那三个 tier 是用连续阈值切的（C# 1206 GetGiveMoneyTier）：
        //       medThresh  = 100 + greed*400   （greed 先归一化到 0..1）
        //       highThresh = 500 + greed*5000
        //   本端口把它简化成"从 9 个固定面额里挑一个" ⇒ 面额是跳的
        //   （1/10/100/1千/1万/10万/100万/1000万/1亿），
        //   而阈值又照搬了连续版 ⇒ 实测 **$1000 就已经越过 highThresh**
        //   （greed=20 时 highThresh=1500，但 1000 已接近；$1万 直接进 tier2）
        //   ⇒ **tier1（中档）永远取不到** —— 用户看到的正是
        //   "1~1000 一个反应、1万以上另一个反应"，中间那套台词白写了。
        //   修法：保留"越贪婪门槛越高"的形状，把两个阈值**对齐到我们实际有的面额**
        //   之间（中档 ≈ 1千~1万，高档 ≈ 10万 起），让三套台词都真的能听到。
        // ★★★ v0.73b 修一个**我自己在 v0.72 埋的 bug**：
        //   这里原来是**两档近似** —— `greedF = 0.20`，只有拜金/操纵改成 0.90。
        //   而 v0.72 明明已经把 `GREED[20]`（照抄 C# 162-181）补进来了，**却没读它**。
        //   后果（都在这一个变量上分叉）：
        //     · 接受率公式里 `greedF` 只有 0.20 / 0.90 两种取值 ⇒
        //       Classy(70) / StreetSmart(65) / Aggressive(60) / Cold(55) / Chaotic(55) /
        //       Unstable(55) 这些**本应有区别**的性格，全被压成"不贪"那一档；
        //     · `medThresh` / `highThresh` 也跟着只剩两种取值 ⇒ 档位判断失真。
        //   C# 用的是 `profGive.Greed`（PersonalityProfile 真值，见 CinnamonCoffee.cs:7766）
        //   ⇒ 改成直读数组。数组百分数 ÷100 即 C# 的 0..1。
        //   （阈值那两行 `500+greed*1500` / `50000+greed*150000` 是**有意偏离** C# 的
        //     `100+greed*400 / 500+greed*5000`：因为我们的面额是离散跳档的，见上面注释。）
        FLOAT greedF = TO_FLOAT(GREED[gmPers]) / 100.0
        INT medThresh = ROUND(500.0 + greedF * 1500.0)       // 800 / 1850
        INT highThresh = ROUND(50000.0 + greedF * 150000.0)  // 8 万 / 18.5 万
        INT tier = 0
        IF amt >= highThresh
            tier = 2
        ELIF amt >= medThresh
            tier = 1
        ENDIF
        // ── 接受率（C# 7779-7797，0..1 浮点 → ×100 交给 CC_ROLL）────────────
        //   tier0（小额） 0.40 + friendliness*0.20 - greed*0.15   ← 贪婪的姑娘最看不上小钱
        //   tier1（中额） 0.65 + friendliness*0.15 + greed*0.10
        //   tier2（大额） 0.85 + friendliness*0.05 + greed*0.05   ← 大钱几乎都收
        //   · "交易型"（g_alHooker）再加 greed*0.05*tier —— 越贵越肯收
        //   · 声望每点 +0.3%；rep <= -1（记恨）硬置 0.10，几乎必拒
        //   · 最终夹到 0.15 .. 0.95
        FLOAT friF = TO_FLOAT(FRIENDLINESS[gmPers]) / 100.0
        FLOAT accF = 0.0
        IF tier = 0
            accF = 0.40 + friF * 0.20 - greedF * 0.15
        ELIF tier = 1
            accF = 0.65 + friF * 0.15 + greedF * 0.10
        ELSE
            accF = 0.85 + friF * 0.05 + greedF * 0.05
        ENDIF
        IF g_alHooker[g_curAlIdx]
            accF = accF + greedF * 0.05 * TO_FLOAT(tier)
        ENDIF
        accF = accF + TO_FLOAT(CC_Rep()) * 0.003
        IF CC_Rep() <= -1
            accF = 0.10
        ENDIF
        IF accF < 0.15
            accF = 0.15
        ENDIF
        IF accF > 0.95
            accF = 0.95
        ENDIF
        IF CC_Rep() <= -1
            accF = 0.10          // hard override（C# 7797 是夹紧之后再压一次，逐字照抄）
        ENDIF
        INT accPctG = ROUND(accF * 100.0)
        // 变体号 = **档位 × 3 + (1..3)**（生成表 _1.._9 是 3 档 × 每档 3 条；
        // _10.._18 是生成器的补位副本，不用。见 cc_dlgnav.sc。）
        INT gmv = tier * 3 + CC_DlgPick(3)
        IF CC_ROLL(accPctG)
            // ════════════════ 接受 ════════════════
            CC_AddMoney(-amt)
            g_alCash[g_curAlIdx] = g_alCash[g_curAlIdx] + amt
            // ★★★ v0.68 修一个真 bug：变体号 = **档位 × 3 + (1..3)**
            //   旧代码是 `tier 0/1/2 → _1/_2/_3` —— 也就是**小额档的三条**，
            //   ⇒ 给十万块她说的是"这……谢了"，三档金额听到的语气完全一样。
            CC_ShowLine3(CC_GMACC_1_H(gmPers), CC_GMACC_T(gmv, gmPers), amt, CC_GMACC_S(gmv, gmPers), g_alName[g_curAlIdx], 3000)
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + (tier + 1) * 2
            IF g_alRep[g_curAlIdx] > CC_REP_MAX
                g_alRep[g_curAlIdx] = CC_REP_MAX
            ENDIF
            AL_UpdateRel(g_curAlIdx)
        ELSE
            // ════════════════ 拒绝（钱不出去、她的现金也不涨）════════════════
            IF CC_GMREJ_I(gmv, gmPers) = 1
                // 这条台词里带金额（"：$X？拜托，别侮辱我。"）⇒ 三段式
                CC_ShowLine3(CC_GMREJ_1_H(gmPers), CC_GMREJ_T(gmv, gmPers), amt, CC_GMREJ_S(gmv, gmPers), g_alName[g_curAlIdx], 3000)
            ELSE
                // 纯文字（"：不。"）⇒ 两段式，别硬塞金额
                CC_ShowLine2(CC_GMREJ_1_H(gmPers), CC_GMREJ_T(gmv, gmPers), g_alName[g_curAlIdx], 3000)
            ENDIF
        ENDIF
        EXIT
    ENDIF
    // LEAVE (C# MenuLevel.Leave: 选离开簇 → LeaveLine)
    IF level = ML_LEAVE
        IF g_curAlIdx >= 0
            g_leaveCluster = i
            g_menuLevel = ML_LEAVELINE
            g_menuIndex = 0
        ELSE
            g_menuLevel = ML_NONE
            CC_FullReset(FALSE)
        ENDIF
        EXIT
    ENDIF
    // APPROACH (A-Life 搭讪簇 → 台词簇)
    IF level = ML_APPROACH
        // ★ 2026-09-25: 记住玩家选的风格簇（0..4），下一层台词靠它取（cc_applines.sc）。
        g_appCluster = i
        g_menuLevel = ML_APPROACHLINE
        g_menuIndex = 0
        EXIT
    ENDIF
    IF level = ML_APPROACHLINE
        // 台词选择 → 接受/拒绝判定
        // ★★ v0.69：原来这里 `IF g_curAlIdx < 0 → EXIT` ⇒ 条目缺失时**整层没反应**
        //   （点了台词什么都不发生）。改成"声望取不到就按 0 算"，判定照常走。
        INT accRep2 = 0
        IF CC_AlOk()
            accRep2 = g_alRep[g_curAlIdx]
        ENDIF
        INT accPct2 = 20 + accRep2
        IF FRIENDLINESS[CC_Pers()] >= 70
            accPct2 = accPct2 + 25
        ENDIF
        // ★★★ v0.79：**风格对不对她的胃口**要影响成败（C# `clusterBonus`，7195-7197）
        //   C#：`clusterBonus = (pickedCluster == expectedCluster) ? 0.25 : -0.20`
        //        `expectedCluster = GetApproachCluster(d.Personality)`
        //   端口原来**完全没有这一项** ⇒ 5 个风格只是 5 套词，选哪个都一样。
        //   ⇒ 现在：对味 +25%，不对味 −20%（换成百分点），并把结果夹在 5..95。
        IF g_appCluster = CC_AppClusterFor(CC_Pers())
            accPct2 = accPct2 + 25
        ELSE
            accPct2 = accPct2 - 20
        ENDIF
        IF accPct2 < 5
            accPct2 = 5
        ENDIF
        IF accPct2 > 95
            accPct2 = 95
        ENDIF
        IF CC_ROLL(accPct2)
            TEXT_LABEL_63 ps[8]

            INT ivs
            TEXT_LABEL_15 nmA
            CC_GetGirlName(nmA)
            // ★★★ v0.69：**接受**的话分模式取表（与 C# 一致）：
            //   · 沙盒模式 → `GetSandboxAcceptLines`（SBXACC，按性格 3 条）★ v0.68 已接线
            //   · 搭讪模式 → `GetApproachLineResponseOptions(..., accepted: TRUE)`
            //     = **跟着你刚说的那句台词**给回应（C# 7222 ShowApproachLineAcceptResponse）。
            //   原来两种模式都用 SBXACC ⇒ 搭讪模式下"她答应的话"跟你说了什么完全无关。
            IF g_sandboxMode
                INT vSbx = CC_DlgPick(3)
                CC_ShowLine2(CC_SBXACC_1_H(CC_Pers()), CC_SBXACC_T(vSbx, CC_Pers()), nmA, 3000)
            ELSE
                CC_ShowLine2("~g~", CC_AppRespYes(g_appCluster, i), nmA, 3500)
            ENDIF
            // ★★★ v0.79：**"见过面"标记 + 搭讪成功的声望** —— C# 7225-7266
            //   C# 原文：
            //       if (d.HasMet) d.HasMetSecondTime = true;
            //       d.HasMet = true;
            //       … 沙盒：rep += 2
            //       … 搭讪：风格**对味** ⇒ rep 直接抬到"认识"底线(10)；不对味 ⇒ +1
            //
            //   ☠☠ 这里原来是个真 bug：端口把 `g_alMet[...] = TRUE` 挂在
            //      「聊天 → 第 7 项「问她的名字」」上（本文件 `IF i = 6` 那处）
            //      ⇒ **光搭讪成功永远不会置位** ⇒ `g_appFirstEnc` 恒 TRUE
            //      ⇒ 每次搭讪都念"初次见面那 4 句"。
            //      用户报的"关系都刷到痴迷了、道别后重新搭讪还像第一次见面"，
            //      根子就是这一行挂错了动作（C# 的注释写得很清楚：
            //      `HasMet` = "true after first accept or reject dialogue"）。
            IF g_curAlIdx >= 0
                IF g_alMet[g_curAlIdx]
                    g_alMet2[g_curAlIdx] = TRUE      // 第二次招到她 ⇒ 解锁"私人调情"那几句
                ENDIF
                g_alMet[g_curAlIdx] = TRUE
            ENDIF
            IF CC_AlOk()
                IF g_sandboxMode
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 2
                ELSE
                    IF g_alRep[g_curAlIdx] >= 0
                        IF g_appCluster = CC_AppClusterFor(CC_Pers())
                            // C#：`Math.Max(rep, 10)` —— 直接抬到"认识"这一档的地板
                            IF g_alRep[g_curAlIdx] < 10
                                g_alRep[g_curAlIdx] = 10
                            ENDIF
                        ELSE
                            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
                        ENDIF
                    ENDIF
                ENDIF
                CC_ClampRep(g_curAlIdx)
                AL_UpdateRel(g_curAlIdx)
            ENDIF
            // ★ v0.64：**不加导航锁**（用户要求"我要可以随时选择其他对话选项"）。
            //   台词照播，菜单立刻可操作。
            g_menuLevel = ML_NONE
            g_hasGirl = TRUE
            // ★ v0.61：搭讪模式的"接受"分支原来**没有**认领这一步（只有沙盒路径有）
            //   ⇒ 她仍归环境 AI 管 ⇒ "答应了还在自己走 / 上车任务被顶掉"。
            //   见 cc_part2a.sc 的 CC_ClaimGirl 说明。
            CC_ClaimGirl()
            CC_AttachGirlBlip()
            // ★ 2026-09-25：同 CC_AppPhase3 —— 走开关（默认开启 = 原行为）。
            CC_ApplyPoliceIgnore()
            IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
                g_playerWasInVeh = TRUE
            ELSE
                TASK_FOLLOW_TO_OFFSET_OF_ENTITY(g_girl, g_player, <<0.0, -1.0, 0.0>>, 1.0, -1, 1.5, TRUE)
                g_playerWasInVeh = FALSE
            ENDIF
            g_state = ST_IDLE
            // ★★★ v0.63：搭讪成功后**直接落在「互动」页，且菜单保持打开**。
            //   原来是落到 ML_MAIN —— 玩家想继续聊天/选服务得再点两层；而且中途
            //   菜单是关着的（WAIT 期间停画），所以他感觉"选完一句话菜单就没了，
            //   还要重新打开"。现在菜单一直开着，直接停在能继续操作的这一层。
            IF g_aLifeMode
                g_menuLevel = ML_ACTIONS
            ELSE
                g_menuLevel = ML_MAIN
            ENDIF
            g_menuIndex = 0
        ELSE
            TEXT_LABEL_63 pd2[8]

            INT ivd
            TEXT_LABEL_15 nmR
            CC_GetGirlName(nmR)
            // ★★★ v0.69 修「搭讪被拒每个角色就那么一两句」：
            //   原来这里用的是 **AVOID** —— 那是"**她已经在躲你**"那张表
            //   （C# 里只在 `d.Reputation == -1` 时才用，CinnamonCoffee.cs 7303）。
            //   真正的搭讪被拒（C# 7278 ShowApproachLineRejectResponse）用的是
            //   `GetApproachLineResponseOptions(cluster, lineIndex, …, accepted: false)`
            //   —— **按你用的风格簇 × 哪一句** 给回应。
            //   现在：声望 ≤ -1 才用 AVOID（与 C# 一致），否则用新的回应表
            //   （cc_appresp.sc：5 风格 × 4 句 × 2 条 = 40 条，比原来 2 条多 20 倍，
            //     而且回应跟着你刚说的那句话走）。
            IF CC_AlOk() AND g_alRep[g_curAlIdx] <= -1
                INT vAvd = CC_DlgPick(2)
                CC_ShowLine2("~r~", CC_AVOID_T(vAvd, CC_Pers()), nmR, 3000)
            ELSE
                CC_ShowLine2("~r~", CC_AppRespNo(g_appCluster, i), nmR, 3500)
            ENDIF
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 2
            IF g_alRep[g_curAlIdx] < CC_REP_MIN
                g_alRep[g_curAlIdx] = CC_REP_MIN
            ENDIF
            AL_UpdateRel(g_curAlIdx)
            // ★★★ v0.64（用户要求："选完一句被拒之后我要可以马上再选别的对话"）：
            //   ① **不加任何导航锁** —— 台词还在播的时候菜单就能用；
            //   ② **留在「台词」页**（不回风格页）⇒ 直接按 ↑↓ 换下一句；
            //      想换风格就按 B 回上一层（CC_MenuNav 的返回映射本来就支持
            //      ML_APPROACHLINE → ML_APPROACH）。
            //   （v0.63 这里加了 3.2 秒导航锁 + 回风格页，用户嫌"硬直太久"。）
            g_menuLevel = ML_APPROACHLINE
        ENDIF
        EXIT
    ENDIF
    IF level = ML_LEAVELINE
        IF g_curAlIdx >= 0
            INT lnIdx
            lnIdx = CC_RAND(0, 2)
            STRING hv
            STRING tv
            IF g_alHooker[g_curAlIdx]
                // ── Prostitution A-Life (C# 8712-8731) ──
                IF g_leaveCluster = 3
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 2
                ELIF g_leaveCluster = 1 OR g_leaveCluster = 4
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
                ENDIF
                IF g_alRep[g_curAlIdx] > CC_REP_MAX
                    g_alRep[g_curAlIdx] = CC_REP_MAX
                ENDIF
                AL_UpdateRel(g_curAlIdx)
                INT relIdx
                relIdx = g_alRel[g_curAlIdx]
                INT tIdx
                tIdx = relIdx * 3 + lnIdx
                IF g_leaveCluster = 0
                    hv = CC_LEAVEPROST_1_H(relIdx)
                    tv = CC_LEAVEPROST_1_T(tIdx)
                ELIF g_leaveCluster = 1
                    hv = CC_LEAVEPROST_2_H(relIdx)
                    tv = CC_LEAVEPROST_2_T(tIdx)
                ELIF g_leaveCluster = 2
                    hv = CC_LEAVEPROST_3_H(relIdx)
                    tv = CC_LEAVEPROST_3_T(tIdx)
                ELIF g_leaveCluster = 3
                    hv = CC_LEAVEPROST_4_H(relIdx)
                    tv = CC_LEAVEPROST_4_T(tIdx)
                ELSE
                    hv = CC_LEAVEPROST_5_H(relIdx)
                    tv = CC_LEAVEPROST_5_T(tIdx)
                ENDIF
                CC_ShowLine2C(hv, tv, g_alName[g_curAlIdx], 4000)
            ELSE
                // ── Casual A-Life (C# 8733-8760) ──
                IF g_leaveCluster = 3 AND i = 3
                    // "I love you." — 无 GF 关系 → 拒绝分支, 声望 -10
                    g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 10
                    IF g_alRep[g_curAlIdx] < CC_REP_MIN
                        g_alRep[g_curAlIdx] = CC_REP_MIN
                    ENDIF
                    AL_UpdateRel(g_curAlIdx)
                    // ══════════════════════════════════════════════════════════
                    // ★★★ v0.68 修一个真 bug：ILYREJ 表的索引是**性格**（0..19），
                    //   不是 `性格 × 3 + 行号`。
                    //
                    //   旧代码 `mIdx = g_alPers[...] * 3 + lnIdx` 有两个后果：
                    //     · 性格 0~6  ⇒ mIdx 落在 0..20 ⇒ **索引到别人身上**，
                    //       她说的话不是她这个性格该说的；
                    //     · 性格 7~19 ⇒ mIdx ≥ 21，**超出表的 CASE 0..19**
                    //       ⇒ 表函数 `RETURN ""` ⇒ **她一整句话是空的**（屏幕只有名字）。
                    //       20 个性格里有 13 个中招。
                    //
                    //   另外 C# 侧是 `ilyOpts[rng.Next(ilyOpts.Length)]`
                    //   （CinnamonCoffee.cs:17395）⇒ 变体是**随机**挑的，
                    //   不是"按菜单行号取"（旧代码把它当 lnIdx 也是误会）。
                    // ══════════════════════════════════════════════════════════
                    INT mIdx
                    mIdx = g_alPers[g_curAlIdx]
                    hv = CC_ILYREJ_1_H(mIdx)
                    tv = CC_ILYREJ_T(CC_DlgPick(3), mIdx)
                    CC_ShowLine2(hv, tv, g_alName[g_curAlIdx], 5000)
                ELSE
                    // 簇匹配 → +1 (gentle/confident/cool/flattering/playful)
                    BOOL matched
                    INT persIdx
                    persIdx = g_alPers[g_curAlIdx]
                    matched = FALSE
                    IF g_leaveCluster = 0
                        IF persIdx = 0 OR persIdx = 4 OR persIdx = 8 OR persIdx = 10
                            matched = TRUE
                        ENDIF
                    ELIF g_leaveCluster = 1
                        IF persIdx = 2 OR persIdx = 6 OR persIdx = 11 OR persIdx = 15
                            matched = TRUE
                        ENDIF
                    ELIF g_leaveCluster = 2
                        IF persIdx = 3 OR persIdx = 9 OR persIdx = 17 OR persIdx = 18
                            matched = TRUE
                        ENDIF
                    ELIF g_leaveCluster = 3
                        IF persIdx = 5 OR persIdx = 12 OR persIdx = 14 OR persIdx = 19
                            matched = TRUE
                        ENDIF
                    ELSE
                        IF persIdx = 1 OR persIdx = 7 OR persIdx = 13 OR persIdx = 16
                            matched = TRUE
                        ENDIF
                    ENDIF
                    IF matched
                        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
                        IF g_alRep[g_curAlIdx] > CC_REP_MAX
                            g_alRep[g_curAlIdx] = CC_REP_MAX
                        ENDIF
                    ENDIF
                    AL_UpdateRel(g_curAlIdx)
                    INT lvIdx
                    lvIdx = persIdx * 3 + lnIdx
                    IF matched
                        hv = CC_LEAVEM_H(persIdx)
                        tv = CC_LEAVEM_T(lvIdx)
                    ELSE
                        hv = CC_LEAVEN_H(persIdx)
                        tv = CC_LEAVEN_T(lvIdx)
                    ENDIF
                    CC_ShowLine2C(hv, tv, g_alName[g_curAlIdx], 4000)
                ENDIF
            ENDIF
        ENDIF
        g_menuLevel = ML_NONE
        CC_FullReset(FALSE)
        EXIT
    ENDIF
ENDPROC

// ── 菜单导航 (C# HandleControllerMenuInput / OnKeyDown) ─────────────────
PROC CC_MenuNav()
    // ── 主键（开/关菜单）：**只有它用冷却** ─────────────────────────────────
    //   ★★ 2026-09-25 修用户报的"设置菜单反应不灵敏、按一次没反应、要多按几次"：
    //
    //   原实现是**整段函数**被开头那句 `IF g_keyCooldown > 0 → 减一 → EXIT` 挡住 ——
    //   冷却期内 上/下/确认 **根本没被读**（不是读了没生效，而是压根没读）。
    //   而每接受一次按键就把冷却重设成 KEY_COOLDOWN_FRAMES(20)，同时 cc_main 的主循环
    //   还会**再减一次** ⇒ 有效冷却 ≈10 帧。打开菜单那一下本身就吃掉一次 20 帧，
    //   于是"刚开菜单马上按上/下"必被吞 —— 这正是"按一次没反应、要多按几次"。
    //
    //   修法：把冷却收缩到只保护"开/关菜单"这一个动作；导航键一律即时读。
    //   同时补上僵尸菜单那套**长按连发**手感（按住 350ms 后每 120ms 一格）。
    //   ⚠ 冷却期内必须**直接 EXIT**（不能只减一继续往下跑）：
    //     开菜单是 CC_IdleMasterKey / CC_AnimatingTick 干的（它们先跑、把菜单设好、
    //     再把冷却设为 KEY_COOLDOWN_FRAMES），本函数随后同帧进来。若这里继续往下走，
    //     导航段会立刻读到"刚按下的十字键右"（它就是开启键的一半）⇒ 菜单一开就
    //     自动确认了第一项。冷却只由"开/关菜单"设置，所以 EXIT 不会影响导航手感。
    IF g_keyCooldown > 0
        g_keyCooldown = g_keyCooldown - 1
        EXIT
    ENDIF
    // ══════════════════════════════════════════════════════════════════════
    // ★★★ v0.63 重构：**主键（开/关菜单）不再由本函数处理**
    //
    //   原来这里有一段 `IN_Master()` 处理，而 CC_IdleMasterKey（闲逛）和
    //   CC_AnimatingTick（动画中）里**各有一段一模一样的**。三处都在改
    //   g_menuLevel / g_keyCooldown，只靠"冷却计数"互相错开 —— 而 cc_main 每帧
    //   还会再减一次冷却 ⇒ 三处计数一旦错位，就会看到"菜单自己开/关一下"。
    //   现在收口成两个**状态专用**处理器，本函数只管导航：
    //       · 闲逛   ⇒ CC_IdleMasterKey()
    //       · 动画中 ⇒ CC_AnimatingTick() 的结束菜单段
    // ══════════════════════════════════════════════════════════════════════
    IF g_menuLevel = ML_NONE
        EXIT
    ENDIF
    // ★★★ v0.63：一次性台词播放期间"菜单照常显示，但不接受导航输入"。
    //   原来这类地方用的是 WAIT(3200) —— WAIT 会**冻住整个脚本线程**，而 HUD 只保留
    //   一帧 ⇒ 那 3.2 秒里菜单根本没被重画，玩家看到的就是"菜单自己关闭了"；
    //   而且这段时间按键完全无响应。现在改成不阻塞：菜单每帧照画，只是这段不读导航键。
    IF GET_GAME_TIMER() < g_navLockUntil
        EXIT
    ENDIF
    INT count
    count = CC_MenuCount(g_menuLevel)
    // ── 上/下：刚按下走一格；按住则连发 ──
    BOOL navMoved = FALSE
    IF IN_UpJust()
        g_menuIndex = g_menuIndex - 1
        IF g_menuIndex < 0
            g_menuIndex = count - 1
        ENDIF
        g_menuRepeatAt = GET_GAME_TIMER() + MENU_REPEAT_FIRST_MS
        navMoved = TRUE
    ELIF IN_UpHeld() AND g_menuRepeatAt > 0 AND GET_GAME_TIMER() >= g_menuRepeatAt
        g_menuIndex = g_menuIndex - 1
        IF g_menuIndex < 0
            g_menuIndex = count - 1
        ENDIF
        g_menuRepeatAt = GET_GAME_TIMER() + MENU_REPEAT_NEXT_MS
        navMoved = TRUE
    ENDIF
    IF NOT navMoved
        IF IN_DownJust()
            g_menuIndex = g_menuIndex + 1
            IF g_menuIndex >= count
                g_menuIndex = 0
            ENDIF
            g_menuRepeatAt = GET_GAME_TIMER() + MENU_REPEAT_FIRST_MS
            navMoved = TRUE
        ELIF IN_DownHeld() AND g_menuRepeatAt > 0 AND GET_GAME_TIMER() >= g_menuRepeatAt
            g_menuIndex = g_menuIndex + 1
            IF g_menuIndex >= count
                g_menuIndex = 0
            ENDIF
            g_menuRepeatAt = GET_GAME_TIMER() + MENU_REPEAT_NEXT_MS
            navMoved = TRUE
        ENDIF
    ENDIF
    IF NOT navMoved
        // ── 确认：A，或 **十字键左/右** ────────────────────────────────────
        //   ★ 2026-09-25 用户要求："十字键左右键也可以切换，而不仅仅是 A 键切换"。
        //   作用域**只在本菜单导航里**：搭讪现场的"接受/拒绝"提示（cc_part2a 那两处
        //   IN_Select/IN_Back）不受影响，那里左右键仍然是移动方向。
        //
        //   ⚠ 动画期速度控制（cc_part2c CC_AnimCommonTick 里 IN_SpeedUp/Down 用的
        //     也是左右键）已加"菜单开着时不生效"的闸，两者不再抢同一个键。
        IF IN_Select() OR IN_LeftJust() OR IN_RightJust()
            g_menuRepeatAt = 0
            INT lvl = g_menuLevel
            INT idx = g_menuIndex
            CC_MenuSelect(lvl, idx)
        ELIF IN_Back()
            g_menuRepeatAt = 0
            // 返回上级
            IF g_menuLevel = ML_SANDBOXCAR OR g_menuLevel = ML_SERVICES OR g_menuLevel = ML_SETTINGS OR g_menuLevel = ML_COMPAT OR g_menuLevel = ML_ACTIONS OR g_menuLevel = ML_APPROACH
                g_menuLevel = ML_MAIN
                g_menuIndex = 0
            ELIF g_menuLevel = ML_STANDINGSVC OR g_menuLevel = ML_SITTINGSVC OR g_menuLevel = ML_LAYINGSVC OR g_menuLevel = ML_SITDOWNSUB
                g_menuLevel = ML_SERVICES
                g_menuIndex = 0
            ELIF g_menuLevel = ML_FINISH
                g_menuLevel = ML_NONE
            ELIF g_menuLevel = ML_LEAVELINE
                g_menuLevel = ML_LEAVE
                g_menuIndex = g_leaveCluster
            ELIF g_menuLevel = ML_LEAVE
                g_menuLevel = ML_MAIN
                g_menuIndex = 1
            ELIF g_menuLevel = ML_INTIMACYSUB
                // ★★ v0.77 修：v0.76 漏了这一层 —— 在亲密子菜单里按 B 会**把整个菜单关掉**
                //   （掉到最后的 ELSE ⇒ `g_menuLevel = ML_NONE`）。现在按 B 回到「亲密」层。
                g_menuLevel = ML_INTIMACY
                g_menuIndex = g_intSubBranch
            ELIF g_menuLevel = ML_CONVERSATION OR g_menuLevel = ML_INTIMACY OR g_menuLevel = ML_ESCALATE OR g_menuLevel = ML_INVITEVEH OR g_menuLevel = ML_GIVEMONEY
                g_menuLevel = ML_ACTIONS
                g_menuIndex = 0
            ELIF g_menuLevel = ML_APPROACHLINE
                g_menuLevel = ML_APPROACH
                g_menuIndex = 0
            ELSE
                g_menuLevel = ML_NONE
            ENDIF
        ENDIF
    ENDIF
ENDPROC

// ── Idle tick: 跟随 + 进出车联动 (C# HandleIdleState 跟随段) ────────────
PROC CC_IdleFollowTick()
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    IF NOT CC_ALIVE(g_girl)
        g_girlLastVit = -1.0
        CC_FullReset(FALSE)
        EXIT
    ENDIF
    // 距离遗弃
    FLOAT girlDist = VDIST(GET_ENTITY_COORDS(g_girl), GET_ENTITY_COORDS(g_player))
    IF NOT g_sandboxMode
        IF girlDist > 20.0
            IF g_aLifeMode AND g_curAlIdx >= 0
                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 3
                IF g_alRep[g_curAlIdx] < -1
                    g_alRep[g_curAlIdx] = -1
                ENDIF
                AL_UpdateRel(g_curAlIdx)
            ENDIF
            CC_FullReset(FALSE)
            EXIT
        ENDIF
    ENDIF
    BOOL playerInVeh = IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
    BOOL girlInVeh = IS_PED_IN_ANY_VEHICLE(g_girl, FALSE)
    // 玩家刚上车 → 她跟进
    IF playerInVeh AND NOT g_playerWasInVeh AND NOT g_backseatEntryPending
        IF g_menuLevel = ML_SERVICES OR g_menuLevel = ML_SITTINGSVC OR g_menuLevel = ML_LAYINGSVC OR g_menuLevel = ML_STANDINGSVC OR g_menuLevel = ML_SITDOWNSUB
            g_menuLevel = ML_MAIN
            g_menuIndex = 0
        ENDIF
        g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
        INT seat = 0
        IF CC_InBackSeat(g_car)
            seat = 1
        ENDIF
        // ★★★ v0.78：**门控**（C# 9958）。C# 被门控时做的事就是这两句：
        //     `girl.Task.ClearAll()` + `BlockPermanentEvents = true` ——
        //     必须清任务，否则她会顺着寻路自己摸进车里（C# 注释原话：
        //     "cancel the active follow task so she doesn't keep walking toward
        //      the car and slip in via pathfinding"）。
        //   并且**不开重试窗口**（下面那段靠 `g_vehInviteUntil`）⇒ 她就一直在原地等，
        //   直到你从菜单请她（`g_vehInvited = TRUE`）。
        //   这一段只在"你刚上车那一帧"跑，所以清任务是**一次性**的，不会每帧刷。
        IF CC_VehEntryGated()
            CLEAR_PED_TASKS(g_girl)
            SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
            // 端口侧提示（C# 没有这行，但"看不见的效果必须给可见指示"）
            CC_HudStatus("~y~她还没同意上车 —— 菜单 → 互动 → 邀请上车", 4000)
        ELIF DOES_ENTITY_EXIST(g_car) AND CC_SuitableVeh(g_car)
            IF NOT IS_VEHICLE_SEAT_FREE(g_car, INT_TO_ENUM(VEHICLE_SEAT, seat))
                seat = 2
            ENDIF
            TASK_ENTER_VEHICLE(g_girl, g_car, -1, INT_TO_ENUM(VEHICLE_SEAT, seat), 1.0, INT_TO_ENUM(ENTER_EXIT_VEHICLE_FLAGS, 0))
            // ★ v0.61：开重试窗口（见下面那段）
            g_vehInviteUntil = GET_GAME_TIMER() + 20000
            g_vehInviteAt = GET_GAME_TIMER() + 2000
        ENDIF
    ENDIF
    // ★★★ v0.61：她还没上来就再催一次。
    //   原来是"只在玩家刚上车那一帧发一次 TASK_ENTER_VEHICLE" —— 那一次要是没走成
    //   （门口被挡 / 距离太远 / 被路人顶了一下），**再也没人管她**，
    //   表现就是用户说的"要多邀请一两次才会上车"。
    //   判据用"她站着没动"（同一套 IS_PED_WALKING/RUNNING，与徒步跟随段一致）：
    //   走着 = 正在靠近，别打断；站着 = 发一次。
    //   用时间窗（g_vehInviteUntil）+ 间隔（g_vehInviteAt）双重限制：
    //   既不刷屏，也不会在她被原版系统踹下车之后陷入"上车→被踹→再请"的死循环。
    // ★★★ v0.78：门控生效时**不进这段**（否则她会绕过"请"这一步自己摸上车，
    //   那这次的"拒绝有意义"就白做了）。被门控时 `g_vehInviteUntil` 本来也不会被开，
    //   这里再加一道显式守卫，免得残留的旧窗口把她带上车。
    IF playerInVeh AND NOT girlInVeh AND NOT g_backseatEntryPending AND g_menuLevel = ML_NONE AND NOT CC_VehEntryGated()
        IF g_vehInviteUntil > 0 AND GET_GAME_TIMER() < g_vehInviteUntil
            IF GET_GAME_TIMER() >= g_vehInviteAt
                g_vehInviteAt = GET_GAME_TIMER() + 2000
                IF NOT IS_PED_WALKING(g_girl) AND NOT IS_PED_RUNNING(g_girl)
                    g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
                    IF DOES_ENTITY_EXIST(g_car) AND CC_SuitableVeh(g_car)
                        INT seatR = 0
                        IF CC_InBackSeat(g_car)
                            seatR = 1
                        ENDIF
                        IF NOT IS_VEHICLE_SEAT_FREE(g_car, INT_TO_ENUM(VEHICLE_SEAT, seatR))
                            seatR = 2
                        ENDIF
                        IF IS_VEHICLE_SEAT_FREE(g_car, INT_TO_ENUM(VEHICLE_SEAT, seatR))
                            TASK_ENTER_VEHICLE(g_girl, g_car, -1, INT_TO_ENUM(VEHICLE_SEAT, seatR), 1.0, INT_TO_ENUM(ENTER_EXIT_VEHICLE_FLAGS, 0))
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
    ENDIF
    IF girlInVeh
        // 她已经上来了 → 关掉重试窗口
        g_vehInviteUntil = 0
        // ★ v0.62：她在车里时也要**挡住"非临时事件"**。
        //   徒步分支（下面 IF NOT playerInVeh ...）一直有这句，车内分支原来没有 ⇒
        //   她在副驾上遇到引擎的惊慌事件（撞车 / 枪声 / 路人尖叫）会当场
        //   开门逃走 —— 正是用户看到的"有概率上车后又跑走"。
        //   与 CC_ClaimGirl / CC_EndAnimation 的车内分支一致。
        SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
    ENDIF
    IF g_backseatEntryPending AND playerInVeh AND girlInVeh
        g_backseatEntryPending = FALSE
    ENDIF
    // 玩家下车 → 她也下
    IF NOT playerInVeh AND g_playerWasInVeh
        IF g_menuLevel = ML_SERVICES OR g_menuLevel = ML_SITTINGSVC OR g_menuLevel = ML_LAYINGSVC OR g_menuLevel = ML_STANDINGSVC OR g_menuLevel = ML_SITDOWNSUB
            g_menuLevel = ML_MAIN
            g_menuIndex = 0
        ENDIF
        IF girlInVeh
            TASK_LEAVE_ANY_VEHICLE(g_girl, 0, INT_TO_ENUM(ENTER_EXIT_VEHICLE_FLAGS, 0))
        ENDIF
    ENDIF
    // 徒步跟随
    IF NOT playerInVeh AND NOT girlInVeh AND NOT g_backseatEntryPending
        SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
        FLOAT dist = VDIST(GET_ENTITY_COORDS(g_girl), GET_ENTITY_COORDS(g_player))
        IF dist > 50.0
            CC_FullReset(FALSE)
            EXIT
        ELIF dist > 2.0
            BOOL idleFlag = FALSE
            IF NOT IS_PED_WALKING(g_girl) AND NOT IS_PED_RUNNING(g_girl)
                idleFlag = TRUE
            ENDIF
            IF idleFlag
                FLOAT spd = 1.0
                IF dist > 7.0
                    spd = 3.0
                ENDIF
                TASK_FOLLOW_TO_OFFSET_OF_ENTITY(g_girl, g_player, <<0.0, -1.0, 0.0>>, spd, -1, 1.5, TRUE)
            ENDIF
        ELSE
            IF IS_PED_WALKING(g_girl) OR IS_PED_RUNNING(g_girl)
                TASK_TURN_PED_TO_FACE_ENTITY(g_girl, g_player, -1)
            ENDIF
        ENDIF
    ENDIF
    g_playerWasInVeh = playerInVeh
    // proposition 管理
    IF g_carPropActive
        CC_CarPropTick()
    ENDIF
    IF g_sitPropActive
        CC_StreetSitPropTick()
    ENDIF
ENDPROC

// ── 主键开菜单 (Idle) ──────────────────────────────────────────────────
PROC CC_IdleMasterKey()
    // A-Life approach 菜单入口 (无女孩时按住主键 → 开 approach)
    IF g_keyCooldown > 0
        EXIT
    ENDIF
    IF IN_Master()
        g_keyCooldown = KEY_COOLDOWN_FRAMES
        // ★ v0.63：主键处理现在只在这里（和 CC_AnimatingTick）——CC_MenuNav 里那份
        //   已删除（三处重复处理会互相打架）。顺手把长按连发计时清零，
        //   免得刚开菜单就吃到上一次残留的连发时刻。
        g_menuRepeatAt = 0
        IF g_menuLevel != ML_NONE
            IF g_sitPropActive AND g_sitPropPhase <= 2 AND g_state != ST_ANIMATING
                CC_ExitStreetSitProp()
            ENDIF
            g_menuLevel = ML_NONE
        ELIF g_sitPropActive AND (g_sitPropPhase < 2 OR g_sitPropPhase >= 3)
        ELIF g_postSexPhase > 0
        ELSE
            IF g_hasGirl
                g_menuLevel = ML_MAIN
                g_menuIndex = 0
            ELSE
                // ★ B+右 = 专用开启键: 先开菜单, 再尝试搭讪附近女性
                //   （2026-09-25 由 RB+左 改来；IN_Master 的实现在 cc_part1.sc）
                g_menuLevel = ML_MAIN
                g_menuIndex = 0
                // 拾取流程 (C# TryPickup)
                PED_INDEX foundPed
                // ★ v0.61：第二个参数 TRUE = 跳过原版站街女（s_f_y_hooker_*）。
                //   理由见 cc_part1.sc 的 g_hookerHash 长注释：那类 ped 身上挂着
                //   原版 pb_prostitute 脚本，玩家一上车它就来抢（锁视角/尖叫跑走/
                //   上车被踹）。跳过它们 = 从源头消掉那四个现象。
                foundPed = CC_FindNearestFemale(7.0, TRUE)
                IF DOES_ENTITY_EXIST(foundPed)
                    IF IS_PED_IN_COMBAT(foundPed, g_player)
                        CC_HudStatus("~r~附近没有女性！", 2000)
                    ELSE
                        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
                            g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
                            IF NOT CC_SuitableVeh(g_car)
                                CC_HudStatus("~r~先找一辆合适的车！", 2000)
                                EXIT
                            ENDIF
                            IF NOT IS_VEHICLE_SEAT_FREE(g_car, INT_TO_ENUM(VEHICLE_SEAT, 0))
                                CC_HudStatus("~r~副驾有人了！", 2000)
                                EXIT
                            ENDIF
                        ENDIF
                        // ★ v0.74 去妓女化：末尾 AND NOT g_alNoHooker（见 cc_part1 的说明）
                        g_curAlIdx = AL_GetOrCreate(foundPed, (NOT g_sandboxMode AND g_aLifeMode) AND NOT g_alNoHooker)
                        g_girl = foundPed
                        IF g_sandboxMode
                            CC_TryPickup(foundPed)
                        ELSE
                            // Prostitution: A-Life approach 菜单 (C# OpenApproachMenu)
                            g_approachCand = foundPed
                            // ★ 2026-09-25: 对应 C# 的 firstEncounter = (d == null || !d.HasMet)。
                            //   初次见面给 4 条台词、回头客给 3 条（表在 cc_applines.sc）。
                            g_appFirstEnc = TRUE
                            IF g_curAlIdx >= 0
                                IF g_alMet[g_curAlIdx]
                                    g_appFirstEnc = FALSE
                                ENDIF
                            ENDIF
                            g_appCluster = 0
                            g_menuLevel = ML_APPROACH
                            g_menuIndex = 0
                        ENDIF
                    ENDIF
                ELSE
                    // ★ v0.61：全跳过时给出解释 —— 否则玩家只知道"附近没有女性"，
                    //   会以为模组坏了，而其实是他站在红灯区、附近全是原版街女。
                    IF g_lastScanSkippedHooker
                        CC_HudStatus("~y~附近只有原版站街女，无法搭讪（会被游戏系统接管）。走开一点再试。", 3500)
                    ELSE
                        CC_HudStatus("~r~附近没有女性！", 2000)
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
    ENDIF
ENDPROC

// ── 击打反应 (C# OnTick 9779-9828) ─────────────────────────────────────
PROC CC_HitReactionTick()
    IF NOT g_hasGirl
        EXIT
    ENDIF
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    IF NOT CC_ALIVE(g_girl)
        EXIT
    ENDIF
    FLOAT vitality = TO_FLOAT(GET_ENTITY_HEALTH(g_girl) + GET_PED_ARMOUR(g_girl))
    IF g_girlLastVit < 0.0
        g_girlLastVit = vitality
        EXIT
    ENDIF
    BOOL hit = HAS_ENTITY_BEEN_DAMAGED_BY_ENTITY(g_girl, g_player, TRUE)
    IF NOT hit
        IF vitality < g_girlLastVit - 1.0
            hit = TRUE
        ENDIF
    ENDIF
    IF hit
        CLEAR_ENTITY_LAST_DAMAGE_ENTITY(g_girl)
        CLEAR_FACIAL_IDLE_ANIM_OVERRIDE(g_girl)
        g_girlLastVit = -1.0
        IF g_aLifeMode AND g_curAlIdx >= 0
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 10
            IF g_alRep[g_curAlIdx] < CC_REP_MIN
                g_alRep[g_curAlIdx] = CC_REP_MIN
            ENDIF
            AL_UpdateRel(g_curAlIdx)
            // 现金掉落排队
            IF NOT DOES_ENTITY_EXIST(g_pendingCashPed)
                PED_INDEX tmpCashPed = g_girl
                g_pendingCashPed = tmpCashPed
                g_pendingCashAmt = g_alCash[g_curAlIdx]
                g_pendingCashKey = g_curAlIdx
                g_alCash[g_curAlIdx] = 0
            ENDIF
        ENDIF
        g_menuLevel = ML_NONE
        IF g_state = ST_ANIMATING
            CC_EndAnimation()
        ENDIF
        g_pendingCashWeapon = ENUM_TO_INT(GET_SELECTED_PED_WEAPON(g_player))
        CC_TriggerHostileReaction(g_girl)
        CC_FullReset(TRUE)
    ELSE
        g_girlLastVit = vitality
    ENDIF
ENDPROC

// ── 语音/表情重应用 (C# 9830-9877) ──────────────────────────────────────
PROC CC_ReapplyTick()
    IF g_hasGirl AND CC_EXISTS(g_girl)
        IF NOT IS_STRING_NULL(g_girlVoice)
            IF GET_GAME_TIMER() >= g_voiceReapplyAt
                SET_AMBIENT_VOICE_NAME(g_girl, g_girlVoice)
                g_voiceReapplyAt = GET_GAME_TIMER() + 3000
            ENDIF
        ENDIF
    ENDIF
    IF g_aLifeMode AND GET_GAME_TIMER() >= g_faceReapplyAt
        IF CC_EXISTS(g_approachCand) AND NOT IS_ENTITY_DEAD(g_approachCand)
            INT fpIdx = AL_Find(AL_Fingerprint(g_approachCand))
            IF fpIdx >= 0
                CC_ApplyMoodFace(g_approachCand, g_alMood[fpIdx])
            ENDIF
        ENDIF
        IF g_hasGirl AND CC_EXISTS(g_girl) AND NOT IS_ENTITY_DEAD(g_girl) AND g_curAlIdx >= 0
            CC_ApplyMoodFace(g_girl, g_alMood[g_curAlIdx])
        ENDIF
        g_faceReapplyAt = GET_GAME_TIMER() + 100
    ENDIF
ENDPROC

// ── 现金掉落 (C# _pendingCashDropPed 监控) ─────────────────────────────
PROC CC_CashDropTick()
    IF g_pendingCashPed = NULL
        EXIT
    ENDIF
    IF NOT DOES_ENTITY_EXIST(g_pendingCashPed)
        g_pendingCashPed = NULL
        g_pendingCashAmt = 0
        g_pendingCashKey = -1
        EXIT
    ENDIF
    IF IS_ENTITY_DEAD(g_pendingCashPed)
        IF g_pendingCashAmt > 0
            VECTOR dp = GET_ENTITY_COORDS(g_pendingCashPed)
            CREATE_AMBIENT_PICKUP(PICKUP_MONEY_VARIABLE, <<dp.x, dp.y, dp.z + 0.1>>, 0, g_pendingCashAmt, INT_TO_ENUM(MODEL_NAMES, GET_HASH_KEY("prop_cash_pile_01")), FALSE, TRUE)
        ENDIF
        g_pendingCashPed = NULL
        g_pendingCashAmt = 0
        g_pendingCashKey = -1
    ENDIF
ENDPROC

// ── post-sex 语音序列 (C# TickPostSexVoice) ────────────────────────────
PROC CC_PostSexVoiceTick()
    IF g_postSexPhase = 0
        EXIT
    ENDIF
    IF g_postSexPhase = 1
        // ★ 2026-09-25晚：加**时间兜底**。
        //   本相位唯一的推进条件是 `IS_AMBIENT_SPEECH_PLAYING`，而它在
        //   "本端口未证明"名单里（55 个之一）。若它恒返回 TRUE（引擎垃圾值），
        //   相位永远停在 1 ⇒ `g_postSexPhase > 0` ⇒ CC_MenuNav / CC_IdleMasterKey
        //   里的"语音序列中禁止开菜单"永远生效 ⇒ **主键永久失效，只能重启游戏**。
        //   8 秒兜底：正常 SEX_FINISHED 语音最长几秒，碰不到；真卡住也能自己出来。
        IF g_postSexDeadline = 0
            g_postSexDeadline = GET_GAME_TIMER() + 8000
        ENDIF
        BOOL speaking = FALSE
        IF CC_EXISTS(g_girl)
            IF IS_AMBIENT_SPEECH_PLAYING(g_girl)
                speaking = TRUE
            ENDIF
        ENDIF
        IF NOT speaking OR GET_GAME_TIMER() > g_postSexDeadline
            g_postSexDeadline = 0
            g_postSexWait = GET_GAME_TIMER() + 500
            g_postSexPhase = 2
        ENDIF
        EXIT
    ENDIF
    IF g_postSexPhase = 2
        IF GET_GAME_TIMER() < g_postSexWait
            EXIT
        ENDIF
        g_postSexPhase = 0
        IF CC_EXISTS(g_girl) AND g_voiceEnd
            CC_AssignVoice()
            CC_PlaySpeech("HOOKER_OFFER_AGAIN")
        ENDIF
        g_menuLevel = g_postSexMenu
        g_postSexMenu = ML_NONE
    ENDIF
ENDPROC
