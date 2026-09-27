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
            CASE 0 RETURN "呃……你接活吗？"    CASE 1 RETURN "抱歉——我实在不知道怎么开口。你现在有空吗？"    CASE 2 RETURN "我不是想耍流氓。买你的时间，多少钱？"    CASE 3 RETURN "打扰一下，想找个人陪。你接这种吗？"
            CASE 4 RETURN "嘿，又是我。还记得我吗？"    CASE 5 RETURN "见到你真好。现在有空吗？"    CASE 6 RETURN "我就盼着能在这儿碰到你。"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 1                     // 自信 Confident
        SWITCH idx
            CASE 0 RETURN "接活吗？多少钱？"    CASE 1 RETURN "我带了现金。你能给什么？"    CASE 2 RETURN "报个价。我不是来砍价的。"    CASE 3 RETURN "省掉客套——你收多少？"
            CASE 4 RETURN "又来了。跟上回一样？"    CASE 5 RETURN "你现在有空吗？我有钱。"    CASE 6 RETURN "别浪费时间了。怎么说？"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 2                     // 高冷 Cool
        SWITCH idx
            CASE 0 RETURN "你有空吗？"    CASE 1 RETURN "你什么价？"    CASE 2 RETURN "想找个人陪。你接活吗？"    CASE 3 RETURN "有时间给我吗？"
            CASE 4 RETURN "又是你。挺好。"    CASE 5 RETURN "还在这片儿做？"    CASE 6 RETURN "想着顺路过来看看。"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 3                     // 奉承 Flattering
        SWITCH idx
            CASE 0 RETURN "这条街上最好看的一个。你收多少？"    CASE 1 RETURN "不管你开什么价，你都值每一分钱。"    CASE 2 RETURN "说真的——你这样的女孩，多少钱？"    CASE 3 RETURN "你太漂亮了。你接活吗？"
            CASE 4 RETURN "你比上次还好看。有空吗？"    CASE 5 RETURN "我一直想着再来找你。你有空吗？"    CASE 6 RETURN "还是这儿最棒的。你说呢？"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 4                     // 俏皮 Playful
        SWITCH idx
            CASE 0 RETURN "行，我直接问了——多少钱？"    CASE 1 RETURN "告诉我你有空。拜托。"    CASE 2 RETURN "我有钱，也知道自己要什么。你有兴趣吗？"    CASE 3 RETURN "你看起来正好是我花钱买的那种麻烦。"
            CASE 4 RETURN "想我了吗？"    CASE 5 RETURN "又来了。我知道，我知道。有空吗？"    CASE 6 RETURN "准备好第二轮了？"
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
            CASE 0 RETURN "打扰一下，希望没烦到你。"    CASE 1 RETURN "你的笑容很暖。"    CASE 2 RETURN "不打个招呼就走过去，我做不到。"    CASE 3 RETURN "介意我陪你待一会儿吗？"
            CASE 4 RETURN "嘿，又见到你真好。"    CASE 5 RETURN "我一直在想你。想一起走走吗？"    CASE 6 RETURN "跟我来。"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 1                     // 自信 Confident
        SWITCH idx
            CASE 0 RETURN "打扰一下。你吸引了我的注意。"    CASE 1 RETURN "我不会假装没看见你。"    CASE 2 RETURN "你看起来很清楚自己要什么。"    CASE 3 RETURN "有空吗？"
            CASE 4 RETURN "嘿。别浪费时间了。"    CASE 5 RETURN "你有空吗？"    CASE 6 RETURN "跟我来。"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 2                     // 高冷 Cool
        SWITCH idx
            CASE 0 RETURN "你看起来有更值得做的事。我也是。"    CASE 1 RETURN "有意思。你这人很难读。"    CASE 2 RETURN "长话短说：你太美了。"    CASE 3 RETURN "没几个人有你这种气场。"
            CASE 4 RETURN "又见面了。挺好。"    CASE 5 RETURN "还是跟以前一样难读。"    CASE 6 RETURN "别让我问第二遍。"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 3                     // 奉承 Flattering
        SWITCH idx
            CASE 0 RETURN "什么都不说才是个傻子。"    CASE 1 RETURN "你正好是我的类型。"    CASE 2 RETURN "别误会——但你真是美得惊人。"    CASE 3 RETURN "你值得有人把注意力放在你身上。"
            CASE 4 RETURN "嘿，我一直在想你。"    CASE 5 RETURN "你比我记忆里还要好看。"    CASE 6 RETURN "你要跟我走的，对吧？"
        ENDSWITCH
        RETURN ""
    ENDIF
    IF g_appCluster = 4                     // 俏皮 Playful
        SWITCH idx
            CASE 0 RETURN "嘿，我猜你听这话听腻了——但真绝。"    CASE 1 RETURN "你看起来像个麻烦。我喜欢。"    CASE 2 RETURN "别告诉我你没看起来那么好逗。"    CASE 3 RETURN "能偷你五分钟吗？"
            CASE 4 RETURN "嘿，准备好一起惹点事了吗？"    CASE 5 RETURN "想我了吗？"    CASE 6 RETURN "来吧，咱们走。"
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
            RETURN ":~s~ 你想听啊？……最近还行，就这么过。谢谢你问。"
        ELIF b = 0
            RETURN ":~s~ 有什么好问的。就那样。"
        ENDIF
        RETURN ":~s~ 还行吧。混口饭吃，日子照过。"
    ELIF topic = 1
        IF b = 2
            RETURN ":~s~ 今天心情不错。看见你就更好了。"
        ELIF b = 0
            RETURN ":~s~ 不关你的事。"
        ENDIF
        RETURN ":~s~ 说不上好，也说不上坏。你呢？"
    ELIF topic = 2
        IF b = 2
            RETURN ":~s~ 我想攒够钱，换个地方重新开始。"
        ELIF b = 0
            RETURN ":~s~ 别打听我以后干什么。"
        ENDIF
        RETURN ":~s~ 走一步看一步。先把今晚过完。"
    ELIF topic = 3
        IF b = 2
            RETURN ":~s~ 嘴真甜。……不过我爱听。"
        ELIF b = 0
            RETURN ":~s~ 省省吧，这话我一天听八遍。"
        ENDIF
        RETURN ":~s~ 谢了。你倒是挺会说话。"
    ELIF topic = 4
        IF b = 2
            RETURN ":~s~ 别停啊，接着说下去。"
        ELIF b = 0
            RETURN ":~s~ 少来这套。有正事说正事。"
        ENDIF
        RETURN ":~s~ 胆子不小。……继续。"
    ELIF topic = 5
        IF b = 2
            RETURN ":~s~ 洛圣都啊……白天光鲜，晚上什么样你我心里都有数。"
        ELIF b = 0
            RETURN ":~s~ 没什么好聊的。这破地方。"
        ENDIF
        RETURN ":~s~ 这城市就这样，什么人都有。"
    ENDIF
    // 6 = 问名字（其余越界值也走这里）
    IF b = 2
        RETURN ":~s~ 记住了啊，别再叫我「喂」。"
    ELIF b = 0
        RETURN ":~s~ ……行吧。别到处说。"
    ENDIF
    RETURN ":~s~ 你记着点。"
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
            RETURN ":~s~ 嗯……你这么说话，我心跳都乱了。……行吧，你可以再近一点。"
        ENDIF
        RETURN ":~s~ 嗯……你这么说话，我心跳都乱了。……不过今天不行，别急。"
    ELIF b = 0
        IF ok
            RETURN ":~s~ 你干嘛？退后点。……啧，算了，就这一次。"
        ENDIF
        RETURN ":~s~ 你干嘛？退后点。……不行，别得寸进尺。"
    ENDIF
    IF ok
        RETURN ":~s~ ……你什么意思？……行吧。你可以再近一点。"
    ENDIF
    RETURN ":~s~ ……你什么意思？……不行。至少现在不行。"
ENDFUNC

