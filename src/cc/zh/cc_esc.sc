// ============================================================
// cc_esc.sc — 「升级 / 进一步」菜单（C# MenuLevel.Escalate）的接受/拒绝台词。
// ★ 手维护文件 —— 由 gen_esc.py 生成，改台词请改 gen_esc.py 再重跑。
// ★ 不要从 cc_dialogue_gen.sc 找这两张表：那里是坏的，已删除（见下）。
// ============================================================
//
// 【为什么单独一个文件】
//   C#: GetEscalateAcceptLine(name, personality, lineIdx) / ...RejectLine(...)
//   是**多维**表：函数体里是 **3 个 switch (personality) 块**，由 if (lineIdx == N) 分段。
//   而 gen_dialogue2.py 的 parse_cases() 是"按 case 名全量扫、后写覆盖先写" ⇒
//     · 只留下最后一块（lineIdx == 0），还被复制成 _1/_2
//       ⇒ 「找点乐子 / 靠近点 / 想做吗」三个菜单项说出**同一句话**；
//     · 另有 17 处 C# 用了**时钟三元式**（昼夜两句），被抽成**空串**。
//   ⇒ 这里用 extract_esc.py 重新抽（按 switch 块切、取最长字面量）+ 手工翻译。
//
// 【接口】
//   CC_ESCACC_H()                 头（"~g~"）
//   CC_ESCACC_T(lineIdx, pIdx)    正文；lineIdx = 菜单第几项 0/1/2
//   CC_ESCREJ_H() / CC_ESCREJ_T(...)   同上，头是 "~r~"
//   ★ pIdx = **性格**索引 0..19（不是 A-Life 槽位号）
//   ★ 内部 _L0 / _L1 / _L2 对应 lineIdx = 0 / 1 / 2
//
// 【翻译口径】沿用 cc_dialogue_gen.sc 的中文风格（口语/短句/省略号/不用引号）。
//   lineIdx == 0 一块与旧生成物同源 ⇒ 沿用旧译法，避免"同一句换说法"。
//
// 【已知简化】C# 里 lineIdx == 0 有 6 个性格按 **昼夜**（21:00-03:00）分两句；
//   这里统一取其中一句（不加 GET_CLOCK_HOURS 依赖 —— 少一个 native 风险）。
// ============================================================


FUNC STRING CC_ESCACC_H()
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_ESCREJ_H()
    RETURN "~r~"
ENDFUNC

FUNC STRING CC_ESCACC_L0_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN ":~s~ ……好。就是……离我近点。"    CASE 1 RETURN ":~s~ 还以为你永远不问呢。"    CASE 2 RETURN ":~s~ 你带路。但我说了算。"    CASE 3 RETURN ":~s~ 行。别搞得煞有介事。"
        CASE 4 RETURN ":~s~ 我很乐意。"    CASE 5 RETURN ":~s~ 行。让我觉得这晚上花得值。"    CASE 6 RETURN ":~s~ 好吧。低调点。"    CASE 7 RETURN ":~s~ 太好了，我们走吧。"
        CASE 8 RETURN ":~s~ 这话我等好久了。"    CASE 9 RETURN ":~s~ 行，总比干站着强。"    CASE 10 RETURN ":~s~ 好……别放手。"    CASE 11 RETURN ":~s~ 按我的条件。明白？"
        CASE 12 RETURN ":~s~ 只有我们俩才行。"    CASE 13 RETURN ":~s~ 好！现在就走！"    CASE 14 RETURN ":~s~ 我准了。暂时。"    CASE 15 RETURN ":~s~ 总算。走。"
        CASE 16 RETURN ":~s~ 哦，好。走。"    CASE 17 RETURN ":~s~ ……那就来吧。"    CASE 18 RETURN ":~s~ 好吧。但我可是有要求的。"    CASE 19 RETURN ":~s~ 好！好！别反悔！"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCACC_L1_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN ":~s~ ……我不介意。"    CASE 1 RETURN ":~s~ 我还想你什么时候才肯靠过来。"    CASE 2 RETURN ":~s~ 你还在问？很好。过来。"    CASE 3 RETURN ":~s~ ……行。别在我边上晃。"
        CASE 4 RETURN ":~s~ 当然。过来吧。"    CASE 5 RETURN ":~s~ 嗯。让我觉得有意思点。"    CASE 6 RETURN ":~s~ 来吧。但先看清场合。"    CASE 7 RETURN ":~s~ 嗯，过来点。"
        CASE 8 RETURN ":~s~ 我就盼着你这样。"    CASE 9 RETURN ":~s~ 哦，看看谁终于动了。"    CASE 10 RETURN ":~s~ 拜托。我想你近一点。"    CASE 11 RETURN ":~s~ 行。但别挤着我。"
        CASE 12 RETURN ":~s~ 只有你。别人不行。"    CASE 13 RETURN ":~s~ 好！快过来！"    CASE 14 RETURN ":~s~ 我允许。别浪费了。"    CASE 15 RETURN ":~s~ 别问了，做就是了。"
        CASE 16 RETURN ":~s~ 你可算开口了。"    CASE 17 RETURN ":~s~ ……嗯。"    CASE 18 RETURN ":~s~ 可以。"    CASE 19 RETURN ":~s~ 好。现在就来。"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCACC_L2_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN ":~s~ 这也……太直白了。不过……行吧。"    CASE 1 RETURN ":~s~ 天呐，总算有个直说的了。好。"    CASE 2 RETURN ":~s~ 直奔主题。我喜欢。来。"    CASE 3 RETURN ":~s~ 利落。行。"
        CASE 4 RETURN ":~s~ 你本可以问得客气点，不过……好。"    CASE 5 RETURN ":~s~ 够大胆。让我觉得这时间花得值。"    CASE 6 RETURN ":~s~ 真直接。行。但按我的规矩来。"    CASE 7 RETURN ":~s~ 好！这才对嘛！"
        CASE 8 RETURN ":~s~ 和我设想的完全不一样，但是……好。"    CASE 9 RETURN ":~s~ 真会说话。行吧，好。"    CASE 10 RETURN ":~s~ ……好。别让我等。"    CASE 11 RETURN ":~s~ 至少你够诚实。行。"
        CASE 12 RETURN ":~s~ 你最好只对我这么问。"    CASE 13 RETURN ":~s~ 好！走走走！"    CASE 14 RETURN ":~s~ 够直接。这我可以接受。"    CASE 15 RETURN ":~s~ 这才像话。"
        CASE 16 RETURN ":~s~ 哈！好。当然好。"    CASE 17 RETURN ":~s~ ……嗯。"    CASE 18 RETURN ":~s~ 一点含蓄都没有。……行吧。"    CASE 19 RETURN ":~s~ 好。就现在。走。"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCREJ_L0_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN ":~s~ 我……我还没准备好。"    CASE 1 RETURN ":~s~ 也许待会。还没到时候。"    CASE 2 RETURN ":~s~ 你还没资格拥有我一晚。"    CASE 3 RETURN ":~s~ 今晚不可能。"
        CASE 4 RETURN ":~s~ 我对你有好感，但还没到那步。"    CASE 5 RETURN ":~s~ 想拥有我一晚，光靠开口可不够。"    CASE 6 RETURN ":~s~ 跟你还没熟到那一步。"    CASE 7 RETURN ":~s~ 现在不行。再玩一轮再来问我。"
        CASE 8 RETURN ":~s~ 在那之前，我们之间还缺点什么。"    CASE 9 RETURN ":~s~ 提议很迷人。坚决拒绝。"    CASE 10 RETURN ":~s~ 我想……但你得让我知道你是认真的。"    CASE 11 RETURN ":~s~ 这种事不由你的时间表。"
        CASE 12 RETURN ":~s~ 你最好没对别人也这么问。"    CASE 13 RETURN ":~s~ 现在气氛不对。"    CASE 14 RETURN ":~s~ 想拥有我一晚，先拿出诚意。"    CASE 15 RETURN ":~s~ 想都别想。"
        CASE 16 RETURN ":~s~ 嗯……还差一点。"    CASE 17 RETURN ":~s~ 还没到时候。"    CASE 18 RETURN ":~s~ 不是这么玩的。"    CASE 19 RETURN ":~s~ 不！别逼我！"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCREJ_L1_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN ":~s~ 我现在还是想留点距离。"    CASE 1 RETURN ":~s~ 现在不行。"    CASE 2 RETURN ":~s~ 你可以想。不表示你能得到。"    CASE 3 RETURN ":~s~ 介意。离远点。"
        CASE 4 RETURN ":~s~ 那样我还不太自在。"    CASE 5 RETURN ":~s~ 靠那么近，是有代价的。"    CASE 6 RETURN ":~s~ 保持距离。"    CASE 7 RETURN ":~s~ 也许待会吧。"
        CASE 8 RETURN ":~s~ 还不行。我们还没到那步。"    CASE 9 RETURN ":~s~ 介意，我介意。问得倒是不错。"    CASE 10 RETURN ":~s~ 我想，但是……我得先信得过你。"    CASE 11 RETURN ":~s~ 我喜欢自己的空间，谢了。"
        CASE 12 RETURN ":~s~ 我现在不想要那个。"    CASE 13 RETURN ":~s~ 不行！时机不对！"    CASE 14 RETURN ":~s~ 靠近不是权利，是挣来的。"    CASE 15 RETURN ":~s~ 退后。"
        CASE 16 RETURN ":~s~ 唔……还不行。"    CASE 17 RETURN ":~s~ 不行。"    CASE 18 RETURN ":~s~ 这有点唐突了。"    CASE 19 RETURN ":~s~ 现在别靠近我！"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCREJ_L2_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN ":~s~ 你不能就这么……说出来。不行。"    CASE 1 RETURN ":~s~ 你本可以说得好听点。再试试。"    CASE 2 RETURN ":~s~ 你还没资格。"    CASE 3 RETURN ":~s~ 不行。"
        CASE 4 RETURN ":~s~ 这……太过了。不用了，谢谢。"    CASE 5 RETURN ":~s~ 可不是白给的。"    CASE 6 RETURN ":~s~ 你就这么跟女人说话的？"    CASE 7 RETURN ":~s~ 我也有底线的。不行。"
        CASE 8 RETURN ":~s~ 我不想这样开始。"    CASE 9 RETURN ":~s~ 哇哦。不行。"    CASE 10 RETURN ":~s~ 不是这样。好好问。"    CASE 11 RETURN ":~s~ 不是这么回事。"
        CASE 12 RETURN ":~s~ 别这么跟我说话。"    CASE 13 RETURN ":~s~ 不行！糟糕！错了！"    CASE 14 RETURN ":~s~ 这样可得不到你想要的。"    CASE 15 RETURN ":~s~ 嘴巴放干净点。"
        CASE 16 RETURN ":~s~ 大胆加分。但还是不行。"    CASE 17 RETURN ":~s~ 不行。"    CASE 18 RETURN ":~s~ 绝对不行。"    CASE 19 RETURN ":~s~ 你竟敢。……也许待会。但不行。"
    ENDSWITCH
    RETURN ""
ENDFUNC

// lineIdx 分发：0 / 1 / 2 → _L0 / _L1 / _L2 的正文。
FUNC STRING CC_ESCACC_T(INT lineIdx, INT pIdx)
    IF lineIdx = 0
        RETURN CC_ESCACC_L0_T(pIdx)
    ELIF lineIdx = 1
        RETURN CC_ESCACC_L1_T(pIdx)
    ENDIF
    RETURN CC_ESCACC_L2_T(pIdx)
ENDFUNC

// lineIdx 分发：0 / 1 / 2 → _L0 / _L1 / _L2 的正文。
FUNC STRING CC_ESCREJ_T(INT lineIdx, INT pIdx)
    IF lineIdx = 0
        RETURN CC_ESCREJ_L0_T(pIdx)
    ELIF lineIdx = 1
        RETURN CC_ESCREJ_L1_T(pIdx)
    ENDIF
    RETURN CC_ESCREJ_L2_T(pIdx)
ENDFUNC
