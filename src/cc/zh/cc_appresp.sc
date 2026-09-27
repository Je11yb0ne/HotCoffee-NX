// ============================================================================
// cc_appresp.sc — 搭讪「回应」台词表（按你用的那句台词给回应）
// ----------------------------------------------------------------------------
// 【为什么单独一个文件】用户反馈："搭讪女 NPC，被拒绝的话好像每个角色只有一个
//   语句"。查完 C# 得到答案 —— **搭讪被拒用的表根本不对**：
//
//   C# CinnamonCoffee.cs 7200~7305 的搭讪流程是：
//     7208  ShowCivilianNotHookerRejectLine(d, cluster)  ← 平民（非性工作者）被搭讪
//     7222  ShowApproachLineAcceptResponse(d, cluster, menuIndex, firstEnc)
//     7278  ShowApproachLineRejectResponse(d, cluster, menuIndex, firstEnc)
//     7303  if (d.Reputation == -1) ShowAvoidingReactionSubtitle(d)   ← 只有"声望 -1"时才用
//   而这三者各自走不同的表：
//     · 接受/拒绝 → GetApproachLineResponseOptions(cluster, lineIndex, firstEnc, warm, accepted)
//       —— **按"你用了哪个风格(cluster)的哪一句(lineIndex)"给回应**，还分 暖/冷 与 初次/回头。
//     · 平民拒绝 → GetCivilianNotHookerRejectLines(cluster, personality)
//     · 躲着你   → GetAvoidingReactionLines
//
//   本端口原来把 **AVOID**（"她在躲你"那张表，只有 2 条）拿去当普通搭讪失败用
//   ⇒ 所以"每个角色就那么一两句，而且跟你说的什么都对不上"。
//   （AVOID 现在只在声望 ≤ -1 时才用，与 C# 一致。）
//
// 【本表怎么定】C# 是
//     cluster(5) × lineIndex(0..3) × 初次/回头(2) × 暖/冷(2) × 接受/拒绝(2) × 2 条
//   ——共 320 组、640 条。本端口**没有** 暖/冷 与 初次/回头 的细分，
//   所以按 **风格(5) × 台词号(0..3) × 接受/拒绝** 出，每组 **2 条**随机。
//   ⇒ 20 个"风格×台词"组合、每个组合接受/拒绝各 2 条 = **80 条**，
//     比原来的 2 条多 40 倍，而且**回应会跟着你刚才说的那句话变**。
//
// 【返回格式】必须是**整行**（自带 ":~s~ "）—— 调用方用
//     head("~g~"/"~r~") + 名字 + 这句 三段拼，见 cc_part2d 的 ML_APPROACHLINE。
//
// 【组装位置】merge_mods.py 放在 cc_dlgnav.sc 之后（要用它的 CC_DlgPick）、
//   cc_part2d.sc 之前（调用点在 p2d）。满足"先定义后调用"。
// ============================================================================


// ── 拒绝 ────────────────────────────────────────────────────────────────────
FUNC STRING CC_AppRespNo(INT cluster, INT lineIdx)
    STRING a = ":~s~ 不行。你找错人了。"
    STRING b = ":~s~ 不是这个。别问了。"
    INT v = CC_DlgPick(2)

    IF cluster = 0                          // 温柔 Gentle：你问得客气
        IF lineIdx = 0                      // "呃……你接活吗？"
            a = ":~s~ 呃……不。你搞错了。"
            b = ":~s~ 问得也太直接了。不行。"
        ELIF lineIdx = 1                    // "抱歉——我实在不知道怎么开口。你现在有空吗？"
            a = ":~s~ 抱歉。我现在没空。"
            b = ":~s~ 你这么说我更不好拒绝了……但还是不行。"
        ELIF lineIdx = 2                    // "我不是想耍流氓。买你的时间，多少钱？"
            a = ":~s~ 我的时间不卖。"
            b = ":~s~ 想得挺周到。但我不做这个。"
        ELSE                                // "打扰一下，想找个人陪。你接这种吗？"
            a = ":~s~ 不接。不好意思。"
            b = ":~s~ 找别人吧。我不是干这个的。"
        ENDIF
    ELIF cluster = 1                        // 自信 Confident：你直给
        IF lineIdx = 0                      // "接活吗？多少钱？"
            a = ":~s~ 不接。别浪费我时间。"
            b = ":~s~ 没有价。你走吧。"
        ELIF lineIdx = 1                    // "我带了现金。你能给什么？"
            a = ":~s~ 什么都给不了。收起来。"
            b = ":~s~ 现金？留着自己花吧。"
        ELIF lineIdx = 2                    // "报个价。我不是来砍价的。"
            a = ":~s~ 没有价可报。"
            b = ":~s~ 你砍不砍都一样——不做。"
        ELSE                                // "省掉客套——你收多少？"
            a = ":~s~ 省掉客套：不做。"
            b = ":~s~ 收多少都是零，因为我根本不接。"
        ENDIF
    ELIF cluster = 2                        // 高冷 Cool：你话少
        IF lineIdx = 0                      // "你有空吗？"
            a = ":~s~ 没空。"
            b = ":~s~ 有。但不会给你。"
        ELIF lineIdx = 1                    // "你什么价？"
            a = ":~s~ 什么价都不是。"
            b = ":~s~ 我不回答这种问题。"
        ELIF lineIdx = 2                    // "想找个人陪。你接活吗？"
            a = ":~s~ 不接。"
            b = ":~s~ 自己陪自己去。"
        ELSE                                // "有时间给我吗？"
            a = ":~s~ 没有。"
            b = ":~s~ 一分钟都没有。"
        ENDIF
    ELIF cluster = 3                        // 奉承 Flattering：你夸她
        IF lineIdx = 0                      // "这条街上最好看的一个。你收多少？"
            a = ":~s~ 夸得不错。但我不是卖的。"
            b = ":~s~ 嘴挺甜。答案还是不行。"
        ELIF lineIdx = 1                    // "不管你开什么价，你都值每一分钱。"
            a = ":~s~ 那你就把每一分钱留着自己花吧。"
            b = ":~s~ 这话我听过一百遍了。不。"
        ELIF lineIdx = 2                    // "说真的——你这样的女孩，多少钱？"
            a = ":~s~ 我这样的女孩不报价。"
            b = ":~s~ 真话是：你找错人了。"
        ELSE                                // "你太漂亮了。你接活吗？"
            a = ":~s~ 漂亮跟接不接活没关系。不接。"
            b = ":~s~ 谢谢。但不行。"
        ENDIF
    ELSE                                    // 俏皮 Playful：你逗她
        IF lineIdx = 0                      // "行，我直接问了——多少钱？"
            a = ":~s~ 哈，直接得让人反感。不。"
            b = ":~s~ 直接答你：不干。"
        ELIF lineIdx = 1                    // "告诉我你有空。拜托。"
            a = ":~s~ 没空。别拜托了。"
            b = ":~s~ 哈。真会撒娇。不行。"
        ELIF lineIdx = 2                    // "我有钱，也知道自己要什么。你有兴趣吗？"
            a = ":~s~ 有钱挺好。我没兴趣。"
            b = ":~s~ 那你再找找。我不在名单上。"
        ELSE                                // "你看起来正好是我花钱买的那种麻烦。"
            a = ":~s~ 麻烦是真的。买不到。"
            b = ":~s~ 哈！这说法不错。但还是不。"
        ENDIF
    ENDIF

    IF v = 2
        RETURN b
    ENDIF
    RETURN a
ENDFUNC


// ── 接受 ────────────────────────────────────────────────────────────────────
//   和拒绝同一套坐标：回应跟着**你用的那句台词**走。
//   （C# 接受也是走 GetApproachLineResponseOptions(..., accepted: true)。）
FUNC STRING CC_AppRespYes(INT cluster, INT lineIdx)
    STRING a = ":~s~ 行吧。"
    STRING b = ":~s~ 好吧。"
    INT v = CC_DlgPick(2)

    IF cluster = 0                          // 温柔 Gentle
        IF lineIdx = 0
            a = ":~s~ 呃……行。你至少问得客气。"
            b = ":~s~ 好。就冲你这么客气。"
        ELIF lineIdx = 1
            a = ":~s~ 你别紧张啊。行，走吧。"
            b = ":~s~ 看你这么笨拙，我竟然心软了。行。"
        ELIF lineIdx = 2
            a = ":~s~ 不耍流氓就好。行。"
            b = ":~s~ 时间是啊……那就看你怎么买了。"
        ELSE
            a = ":~s~ 陪就陪会儿吧。"
            b = ":~s~ 行。反正我也没事。"
        ENDIF
    ELIF cluster = 1                        // 自信 Confident
        IF lineIdx = 0
            a = ":~s~ 干脆。我喜欢。行。"
            b = ":~s~ 那就别废话了。走吧。"
        ELIF lineIdx = 1
            a = ":~s~ 现金在手，态度就好。行。"
            b = ":~s~ 知道带钱，说明你懂事。"
        ELIF lineIdx = 2
            a = ":~s~ 不砍价？那我们能谈。"
            b = ":~s~ 成交。我就喜欢干脆人。"
        ELSE
            a = ":~s~ 行，省事。走。"
            b = ":~s~ 就喜欢你这种不绕弯的。"
        ENDIF
    ELIF cluster = 2                        // 高冷 Cool
        IF lineIdx = 0
            a = ":~s~ 有。"
            b = ":~s~ 行。别废话。"
        ELIF lineIdx = 1
            a = ":~s~ 问价就直接谈。行。"
            b = ":~s~ 你问对了。走吧。"
        ELIF lineIdx = 2
            a = ":~s~ 可以。"
            b = ":~s~ 行。我不讨厌直接的人。"
        ELSE
            a = ":~s~ 给你一点时间。"
            b = ":~s~ 行。快点。"
        ENDIF
    ELIF cluster = 3                        // 奉承 Flattering
        IF lineIdx = 0
            a = ":~s~ 眼睛不错。行。"
            b = ":~s~ 就冲你这句，今天我心情好。"
        ELIF lineIdx = 1
            a = ":~s~ 值每一分钱？……那我就收下这句。"
            b = ":~s~ 行啊，会说话的我不讨厌。"
        ELIF lineIdx = 2
            a = ":~s~ 说真的，你嘴挺甜。行。"
            b = ":~s~ 好吧，看在你诚心。"
        ELSE
            a = ":~s~ 会说话。那就试试看。"
            b = ":~s~ 夸我的人不少，你算说得好听的。"
        ENDIF
    ELSE                                    // 俏皮 Playful
        IF lineIdx = 0
            a = ":~s~ 哈！这么直接。行吧。"
            b = ":~s~ 直接得可爱。走。"
        ELIF lineIdx = 1
            a = ":~s~ 哈，撒娇有用哦。行。"
            b = ":~s~ 拜托就算了，笑一个给你个面子。"
        ELIF lineIdx = 2
            a = ":~s~ 知道自己要什么，这点我喜欢。走。"
            b = ":~s~ 有钱又有主意？那有点意思。"
        ELSE
            a = ":~s~ 哈！这说法我认。行。"
            b = ":~s~ 麻烦是真麻烦，但你敢说。走。"
        ENDIF
    ENDIF

    IF v = 2
        RETURN b
    ENDIF
    RETURN a
ENDFUNC
