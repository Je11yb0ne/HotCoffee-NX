// ============================================================================
// cc_applines.sc — "搭讪台词"表（卖淫模式 / 沙盒模式）
// ----------------------------------------------------------------------------
// 来源: C# CinnamonCoffee.cs → DrawApproachLineMenu()
//   · prostLines  (卖淫 A-Life，NOT sandboxMode)  5 簇 × (初次 4 条 / 回头 3 条)
//   · allLines    (休闲 A-Life，    sandboxMode)  5 簇 × (初次 4 条 / 回头 3 条)
//
// 簇: 0=温柔(Gentle) 1=自信(Confident) 2=高冷(Cool) 3=奉承(Flattering) 4=俏皮(Playful)
// 状态: g_appFirstEnc = TRUE → 初次见面(4条)；FALSE → 回头客(3条)
//
// ★ 为什么单独手写一个文件、不放进 gen_dialogue2.py 的自动抽取:
//   C# 那两张表是 string[][][]（簇 → 初次/回头 → 台词），而 gen_dialogue2.py
//   只会处理"按性格(PERS)索引"的 string[] 表 —— 三维表它抽不出来，会静默丢掉。
//
// ★★ 这一步修的是什么（用户反馈"五个点进去都是一样的选项"）:
//   原版流程 = 先选**搭讪风格**(5 个簇) → 再显示**该风格专属**的台词。
//   移植版一度把 5 个簇写成 5 句人话(「你好呀美人」等)，然后无论点哪个，
//   都进同一张 6 行通用表(「今晚过得怎么样？」等) ⇒ 5 个选项完全等价。
//   这里按原版恢复"一簇一套台词"，并把那张通用表整个删掉。
//
// ★ 行索引约定: 0..3 = 初次见面，4..6 = 回头客。
//   CC_AppLine() 传进来的 idx 只有 0..3 或 0..2（由 CC_AppLineCount() 决定），
//   进表前按 g_appFirstEnc 平移到 4..6。
// ============================================================================

// 当前状态下的台词条数（决定菜单行数，必须与 CC_AppLine() 的表一致）
FUNC INT CC_AppLineCount()
    IF g_appFirstEnc
        RETURN 4
    ENDIF
    RETURN 3
ENDFUNC

// ── 卖淫模式 (C# prostLines)：直给、讲价、不绕弯 ──────────────────────────
FUNC STRING CC_AppLineProst(INT idx)
    IF NOT g_appFirstEnc
        idx = idx + 4                       // 4..6 = 回头客那组
    ENDIF
    IF g_appCluster = 0                     // 温柔 Gentle
        SWITCH idx
            CASE 0 RETURN "Hey... you working?"    CASE 1 RETURN "Sorry - I don't really know how to ask this. You available?"    CASE 2 RETURN "I'm not trying to be weird. How much for your time?"    CASE 3 RETURN "Excuse me. Looking for company. Is that something you do?"
            CASE 4 RETURN "Hey, it's me again. You remember me?"    CASE 5 RETURN "Good to see you. You free right now?"    CASE 6 RETURN "I was hoping I'd find you here."
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 1                     // 自信 Confident
        SWITCH idx
            CASE 0 RETURN "You working? How much?"    CASE 1 RETURN "I've got cash. What are you offering?"    CASE 2 RETURN "Name your rate. I'm not here to haggle."    CASE 3 RETURN "Let's skip the small talk. What do you charge?"
            CASE 4 RETURN "Back again. Same deal as last time?"    CASE 5 RETURN "You free right now? I've got money."    CASE 6 RETURN "Let's not waste time. What's it gonna be?"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 2                     // 高冷 Cool
        SWITCH idx
            CASE 0 RETURN "You available?"    CASE 1 RETURN "What's your rate?"    CASE 2 RETURN "Looking for a date. You working?"    CASE 3 RETURN "You got time for me?"
            CASE 4 RETURN "You again. Good."    CASE 5 RETURN "Still working this spot?"    CASE 6 RETURN "Figured I'd swing by."
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 3                     // 奉承 Flattering
        SWITCH idx
            CASE 0 RETURN "Best-looking one out here. What do you charge?"    CASE 1 RETURN "Whatever your rate is, you're worth every penny."    CASE 2 RETURN "Seriously - how much for a girl like you?"    CASE 3 RETURN "You look incredible. You working?"
            CASE 4 RETURN "You look even better than last time. You free?"    CASE 5 RETURN "I've been thinking about coming back. You available?"    CASE 6 RETURN "Still the best out here. What do you say?"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 4                     // 俏皮 Playful
        SWITCH idx
            CASE 0 RETURN "Alright, I'll just ask - how much?"    CASE 1 RETURN "Tell me you're free. Please."    CASE 2 RETURN "I've got money and I know what I want. You interested?"    CASE 3 RETURN "You look like exactly the kind of trouble I'm paying for."
            CASE 4 RETURN "Miss me?"    CASE 5 RETURN "Back again. I know, I know. You free?"    CASE 6 RETURN "Ready for round two?"
        ENDSWITCH
        RETURN ""
    ENDIF
    RETURN ""
ENDFUNC

// ── 沙盒模式 (C# allLines)：正常搭讪的口吻 ────────────────────────────────
FUNC STRING CC_AppLineSandbox(INT idx)
    IF NOT g_appFirstEnc
        idx = idx + 4
    ENDIF
    IF g_appCluster = 0                     // 温柔 Gentle
        SWITCH idx
            CASE 0 RETURN "Excuse me, I hope I'm not bothering you."    CASE 1 RETURN "You have a really warm smile."    CASE 2 RETURN "I couldn't walk past without saying hi."    CASE 3 RETURN "Mind if I keep you company for a bit?"
            CASE 4 RETURN "Hey, good to see you again."    CASE 5 RETURN "I kept thinking about you. Want to hang out?"    CASE 6 RETURN "Come with me."
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 1                     // 自信 Confident
        SWITCH idx
            CASE 0 RETURN "Excuse me. You caught my attention."    CASE 1 RETURN "I'm not going to pretend I didn't notice you."    CASE 2 RETURN "You look like you know what you want."    CASE 3 RETURN "Got a minute?"
            CASE 4 RETURN "Hey. Let's not waste time."    CASE 5 RETURN "You free?"    CASE 6 RETURN "Come with me."
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 2                     // 高冷 Cool
        SWITCH idx
            CASE 0 RETURN "You look like you have better things to do. Same."    CASE 1 RETURN "Interesting. You're hard to read."    CASE 2 RETURN "I'll keep it short: you're stunning."    CASE 3 RETURN "Not many people carry themselves like that."
            CASE 4 RETURN "Good."    CASE 5 RETURN "Still as hard to read as ever."    CASE 6 RETURN "Don't make me ask twice."
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 3                     // 奉承 Flattering
        SWITCH idx
            CASE 0 RETURN "I'd be an idiot not to say something."    CASE 1 RETURN "You're exactly my type."    CASE 2 RETURN "Don't take this the wrong way, but you're gorgeous."    CASE 3 RETURN "You deserve someone paying attention."
            CASE 4 RETURN "Hey, I've been thinking about you."    CASE 5 RETURN "You look even better than I remembered."    CASE 6 RETURN "You're coming with me, right?"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 4                     // 俏皮 Playful
        SWITCH idx
            CASE 0 RETURN "Hey, I bet you get this a lot, but damn."    CASE 1 RETURN "You look like trouble. I like that."    CASE 2 RETURN "Tell me you're not as fun as you look."    CASE 3 RETURN "Can I steal five minutes of your time?"
            CASE 4 RETURN "Hey, ready to cause some trouble?"    CASE 5 RETURN "Miss me?"    CASE 6 RETURN "Come on, let's go."
        ENDSWITCH
        RETURN ""
    ENDIF
    RETURN ""
ENDFUNC

// 统一入口：按模式分派
FUNC STRING CC_AppLine(INT idx)
    // ★★★ v0.79：**去妓女化之后，搭讪模式也不该再"像找妓女"**（用户反馈原话）。
    //   C# 里 `!sandboxMode` 用的是 `prostLines`（"你接活吗？多少钱？"那一套），
    //   因为**那个模式下她本来就是收费街女**，掏钱是正当流程。
    //   端口 v0.74 已去妓女化（`g_alNoHooker = TRUE` ⇒ 她不再是街女、免费），
    //   再念那套词就自相矛盾（"买你的时间，多少钱？" 而后面根本不收钱）。
    //   ⇒ 两种模式**都用"正常口吻"那套**（C# 的 `allLines` = CC_AppLineSandbox）。
    //   ⚠ 这是**有意的偏离**。想恢复原样：把下面条件里的 `OR g_alNoHooker` 删掉。
    IF g_sandboxMode OR g_alNoHooker
        RETURN CC_AppLineSandbox(idx)
    ENDIF
    RETURN CC_AppLineProst(idx)
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.79：**她期待你用哪种风格搭讪** —— C# `GetApproachCluster(personality)`
//
//   【为什么要它】C# 的搭讪成功率里有一项 `clusterBonus`（7195-7197）：
//     选中的风格 == 她偏好的风格 ⇒ **+25%**；不对味 ⇒ **−20%**。
//     端口原来完全没有这一项 ⇒ 5 个风格只是 5 套词，选哪个成功率一样。
//     有了它，"看人下菜"才有意义（高冷的人别用俏皮腔）。
//
//   返回值＝风格簇下标：0 温柔 / 1 自信 / 2 高冷 / 3 奉承 / 4 俏皮
//   （与 `g_appCluster`、`ML_APPROACH` 的标签顺序一一对应）
//
//   ★ 性格号与 C# 名的对应关系：见 cc_part1 的 CC_PERS_NAME_ZH（同序）。
// ════════════════════════════════════════════════════════════════════════════
FUNC INT CC_AppClusterFor(INT pIdx)
    IF pIdx < 0 OR pIdx > 19
        RETURN 4                     // C# 的 default = playful
    ENDIF
    // ☠ 用 IF/OR 而不是 SWITCH：这里的 CASE 是**稀疏**的（0/4/8/10…），
    //   SanScript 的 SWITCH 会编成跳转表，稀疏分布容易出问题 —— IF 链无害。
    // gentle：Shy(0) / Sweet(4) / Romantic(8) / Needy(10)
    IF pIdx = 0 OR pIdx = 4 OR pIdx = 8 OR pIdx = 10
        RETURN 0
    ENDIF
    // confident：Dominant(2) / StreetSmart(6) / Independent(11) / Aggressive(15)
    IF pIdx = 2 OR pIdx = 6 OR pIdx = 11 OR pIdx = 15
        RETURN 1
    ENDIF
    // cool：Cold(3) / Sarcastic(9) / Mysterious(17) / Classy(18)
    IF pIdx = 3 OR pIdx = 9 OR pIdx = 17 OR pIdx = 18
        RETURN 2
    ENDIF
    // flattering：GoldDigger(5) / Jealous(12) / Manipulative(14) / Unstable(19)
    IF pIdx = 5 OR pIdx = 12 OR pIdx = 14 OR pIdx = 19
        RETURN 3
    ENDIF
    // playful（C# 的 default）：Flirty(1) / PartyGirl(7) / Chaotic(13) / Playful(16)
    RETURN 4
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  v0.61 新增 —— 互动「聊天」的话题回应表
// ────────────────────────────────────────────────────────────────────────────
//  【为什么加】用户实测：互动 → 聊天 → **7 个话题点哪个都是同一句**
//    「……谢谢你还来问这个。」。
//    那一句是移植时写死的**占位台词**（cc_part2d.sc 里原来的注释自己也写了
//    "真正按分支/话题分化的版本待办"）—— 这就是那笔待办。
//
//  【C# 原版形状】GetConvResponse(branch, item, d)：
//    输入 = 话题分支 + 性格 Profile(attachment/confidence/greed/riskiness)
//           + 声望 + 心情 + 天气 + 昼夜。
//    输出 = 该话题专属的一句回应。
//  【本端口怎么接近】没有昼夜/天气判定，所以按
//      **话题(7 种) × 性格倾向(冷/中/暖 三档) × 声望** 出回应。
//    语气分档就是按 FRIENDLINESS / AGGRESSIVENESS 两张表算出来的 ——
//    这样"同一个话题对不同性格的女伴说出来的话不一样"。
//
//  【返回格式】必须以 ":~s~ " 开头 —— 调用方（cc_part2d.sc ML_CONVERSATION）
//    是 `~g~ + 名字 + 这句` 三段拼起来的，所以这句要自带冒号。
// ════════════════════════════════════════════════════════════════════════════

// 语气分档：0 = 冷/高冷，1 = 中性，2 = 暖
FUNC INT CC_ToneBucket(INT pIdx)
    IF pIdx < 0
        RETURN 1
    ENDIF
    IF FRIENDLINESS[pIdx] >= 70
        RETURN 2
    ENDIF
    IF AGGRESSIVENESS[pIdx] >= 80 OR FRIENDLINESS[pIdx] <= 45
        RETURN 0
    ENDIF
    RETURN 1
ENDFUNC

// 话题回应。topic: 0问情况 1问心情 2问目标 3夸夸她 4调情 5聊城市 6问名字
//  ★ 注意：v0.61 初版这里写的是 `SWITCH topic / CASE n / … / ELSE / ENDSWITCH`，
//   sc.exe 直接报 `parse error, expecting ENDSWITCH`（编译中止）。
//   本工程里**所有能编过的 SWITCH** 都是同一形态：单行 `CASE n RETURN x`，
//   没有 BREAK、没有 ELSE/DEFAULT、没有多行函数体（见本文件 40~114 行、
//   cc_part1.sc 的 CC_PERS_NAME / CC_FEMALE_NAME）。
//   ⇒ 多分支一律改用 IF/ELIF 链，别赌 SWITCH 的写法细节。
FUNC STRING CC_ConvResp(INT topic, INT pIdx)
    INT b = CC_ToneBucket(pIdx)
    IF topic = 0
        IF b = 2
            RETURN ":~s~ You really want to hear it? ... Things are alright, just getting by. Thanks for asking."
        ELIF b = 0
            RETURN ":~s~ Nothing worth asking about. Same as always."
        ENDIF
        RETURN ":~s~ Doing okay, I guess. Just scraping by, day to day."
    ELIF topic = 1
        IF b = 2
            RETURN ":~s~ Good mood today. Even better now that I see you."
        ELIF b = 0
            RETURN ":~s~ None of your business."
        ENDIF
        RETURN ":~s~ Can't say good or bad. You?"
    ELIF topic = 2
        IF b = 2
            RETURN ":~s~ I want to save up and start over somewhere new."
        ELIF b = 0
            RETURN ":~s~ Don't pry into my plans."
        ENDIF
        RETURN ":~s~ Take it one step at a time. Just get through tonight first."
    ELIF topic = 3
        IF b = 2
            RETURN ":~s~ Sweet talker. ... But I like hearing it."
        ELIF b = 0
            RETURN ":~s~ Save it. I hear that eight times a day."
        ENDIF
        RETURN ":~s~ Thanks. You're pretty smooth with words."
    ELIF topic = 4
        IF b = 2
            RETURN ":~s~ Don't stop, keep talking."
        ELIF b = 0
            RETURN ":~s~ Cut the act. Get to the point if you've got one."
        ENDIF
        RETURN ":~s~ Bold of you. ... Go on."
    ELIF topic = 5
        IF b = 2
            RETURN ":~s~ Los Santos ... all shiny by day, but we both know what it's like at night."
        ELIF b = 0
            RETURN ":~s~ Nothing to talk about. This dump."
        ENDIF
        RETURN ":~s~ That's this city. All kinds of people."
    ENDIF
    // 6 = 问名字（其余越界值也走这里）
    IF b = 2
        RETURN ":~s~ Remember it, then. And don't call me 'hey' anymore."
    ELIF b = 0
        RETURN ":~s~ ... Fine. Don't go telling everyone."
    ENDIF
    RETURN ":~s~ You just remember that."
ENDFUNC

// ── 「试探一下」(Test Waters) 的回应 ────────────────────────────────────────
//   【为什么加】原来这一支**只改了两个数**（声望 +2、关系档刷新），
//    屏幕上**一行字都没有** ⇒ 用户看到的就是"点了没反应"。
//   现在按 性格语气(冷/中/暖) × 判定结果 给一句完整回应。
//   ⚠ 返回的是**整行**（自带 ":~s~"），调用方用 head="~g~" + 名字 + 这句拼。
FUNC STRING CC_TestWaterLine(INT pIdx, BOOL ok)
    INT b = CC_ToneBucket(pIdx)
    IF b = 2
        IF ok
            RETURN ":~s~ Mmm ... the way you talk, my heartbeat's all over the place. ... Fine, you can come a little closer."
        ENDIF
        RETURN ":~s~ Mmm ... the way you talk, my heartbeat's all over the place. ... But not today, don't rush."
    ELIF b = 0
        IF ok
            RETURN ":~s~ What are you doing? Step back. ... Tch, fine, just this once."
        ENDIF
        RETURN ":~s~ What are you doing? Step back. ... No. Don't push your luck."
    ENDIF
    IF ok
        RETURN ":~s~ ... What do you mean? ... Fine. You can come a little closer."
    ENDIF
    RETURN ":~s~ ... What do you mean? ... No. At least not right now."
ENDFUNC
