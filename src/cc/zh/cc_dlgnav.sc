// ============================================================================
// cc_dlgnav.sc — 台词"变体选择层"（dialogue variant navigation）
// ----------------------------------------------------------------------------
// 【为什么需要这个文件】用户问："代码为什么只调用这么点的台词表？说来说去
//   就那几句话。" 查完 gen_dialogue2.py 得到答案 —— 不是表少，是**每个表里的
//   候选句只用了第 1 条**。
//
//   C# 原版里很多台词函数长这样：
//
//       private static string[] GetPropositionAcceptLines(string name, string p)
//       {
//           switch (p) {
//               case "Shy": return new string[] { 第1句, 第2句, 第3句, 第4句 };
//               case "Cold": ...
//           }
//       }
//       // 调用处：
//       propAccept = GetPropositionAcceptLines(...);
//       ShowSubtitle(propAccept[rng.Next(propAccept.Length)], 4500);   // ← 随机挑
//
//   gen_dialogue2.py 把「数组的第 N 个元素」生成成 `CC_<家族>_<N>_T(pIdx)`：
//   N = **同一场景下可随机挑的第几条**，pIdx = 性格。
//
//   ★★ 移植版的调用点**一律写死 `_1`** ⇒ 同一个场景无论见谁、来几次，
//      永远那第 1 句。这就是"说来说去就那几句话"的真正原因。
//
// 【★ 别名里的"真实条数"——别直接照变体号范围用】
//   gen_dialogue2.py 是按「所有性格里**最长的那个数组**」定变体总数的；短的那些
//   用 `rets[-1]`（最后一条）**补位**。所以我们看到 `_5 _6 _7 _8` 存在，
//   但它们很可能只是 `_4` 的副本。核对结果（tools/audit_dialogue.py 可复现）：
//
//       家族        函数里的变体号   真实互不相同的条数
//       PROPACC          1..8              4
//       PROPDECL         1..8              4
//       SBXACC           1..6              3
//       AVOID            1..4              2
//       SVCUNAV          1..4              2   （★ 该场景目前是死代码）
//       VEHINVACC        1..2              1   （没有第二句可用）
//       GMACC            1..18             9   （= 3 个金额档 × 每档 3 条）
//       ILYREJ           1..6              3
//
//   ⇒ 本文件**只暴露真实互不相同的那几条**，多的不接（接了也只是重复同一句）。
//
// 【为什么用 IF/ELIF 而不是 SWITCH】
//   本工程的 sc.exe 只吃得下单行 `CASE n RETURN x` 的 SWITCH（见 cc_applines.sc
//   里 CC_ConvResp 上面那段踩坑注释：多行函数体/ELSE 一律 `parse error`）。
//   这里分支多，统一用 IF/ELIF，不吃编译器脾气的赌。
//
// 【头（_H）为什么不用按变体取】
//   已逐条核对：本文件涉及的所有家族的 `_1_H … _N_H` **内容完全相同**
//   （都只是颜色码 `~g~` / `~r~`）。所以调用方直接用 `_1_H(pIdx)` 即可；
//   只有正文 `_T`（以及 GMACC 的金额后缀 `_S`）需要跟着变体号走。
//   ★ 以后若重新生成台词表，先用 tools/audit_dialogue.py 复核这一点再改。
//
// 【调用约定】调用方先 `INT v = CC_DlgPick(N)`，再用同一个 v 去取 `_T` / `_S`，
//   保证「正文」和「金额后缀」来自**同一条**台词（不然会拼出杂交句）。
//
// 【组装位置】merge_mods.py 把本文件放在 cc_applines.sc 之后、cc_part2a.sc 之前 ——
//   也就是说它在 cc_dialogue_gen.sc（定义各表）之后、cc_part2d.sc（调用各表）之前，
//   满足 SanScript「先定义后调用」。
// ============================================================================


// 摇一个 1..n 的变体号。n ≤ 1 时恒返回 1（方便"先接上、以后补内容"）。
FUNC INT CC_DlgPick(INT n)
    IF n <= 1
        RETURN 1
    ENDIF
    RETURN CC_RAND(1, n + 1)
ENDFUNC


// ── 提议性关系：接受（真实 4 条）────────────────────────────────────────────
FUNC STRING CC_PROPACC_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_PROPACC_2_T(pIdx)
    ELIF v = 3
        RETURN CC_PROPACC_3_T(pIdx)
    ELIF v = 4
        RETURN CC_PROPACC_4_T(pIdx)
    ENDIF
    RETURN CC_PROPACC_1_T(pIdx)
ENDFUNC


// ── 提议性关系：拒绝（真实 4 条）────────────────────────────────────────────
FUNC STRING CC_PROPDECL_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_PROPDECL_2_T(pIdx)
    ELIF v = 3
        RETURN CC_PROPDECL_3_T(pIdx)
    ELIF v = 4
        RETURN CC_PROPDECL_4_T(pIdx)
    ENDIF
    RETURN CC_PROPDECL_1_T(pIdx)
ENDFUNC


// ── 搭讪成功（沙盒模式，真实 3 条）──────────────────────────────────────────
FUNC STRING CC_SBXACC_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_SBXACC_2_T(pIdx)
    ELIF v = 3
        RETURN CC_SBXACC_3_T(pIdx)
    ENDIF
    RETURN CC_SBXACC_1_T(pIdx)
ENDFUNC


// ── 她在躲你（真实 2 条）────────────────────────────────────────────────────
FUNC STRING CC_AVOID_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_AVOID_2_T(pIdx)
    ENDIF
    RETURN CC_AVOID_1_T(pIdx)
ENDFUNC


// ── 服务不可用（真实 4 条）──────────────────────────────────────────────────
//   ⚠ **仍然是"有调用点、但走不到"** —— 说清楚为什么，免得下次又当成 bug：
//     v0.72 补上了 C# 的性格闸（户外风险 / 亲吻情感 / 口交·粗暴偏好），
//     但那几条各自有**更具体的表**（RISKY / NOKISS / NOBJN / NOBJK / NORON / NOROK）。
//     SVCUNAV 在 C# 里是那个 **catch-all**（"其它任何不可用服务"），
//     而 C# 走到它的条件是**她自己的价目表没这一项**（`HookerPrices[gi] <= 0`，C# 5921）——
//     本端口 `PR_GenerateALifeHooker` / `PR_GenerateProstHooker` 把 5 组**全部**置成可用
//     ⇒ 没有"她不做的那一项" ⇒ 这个 catch-all 自然永远不该触发。**这是对的行为。**
//   ★ 顺手修一个隐性 bug：旧调用点写死 `CC_SVCUNAV_1_T(...)`（变体恒为 1）。
//     现在调用方走 `INT v = CC_DlgPick(CC_SVCUNAV_N)`，万一将来真触发，四个变体都能听到。
CONST_INT CC_SVCUNAV_N 4

FUNC STRING CC_SVCUNAV_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_SVCUNAV_2_T(pIdx)
    ELIF v = 3
        RETURN CC_SVCUNAV_3_T(pIdx)
    ELIF v = 4
        RETURN CC_SVCUNAV_4_T(pIdx)
    ENDIF
    RETURN CC_SVCUNAV_1_T(pIdx)
ENDFUNC


// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.72（D 类接线）：**服务内的性格拒绝表**（C# 侧的"服务细化"分支）
//
//   这些在 C# 里都挂在「选服务 → 开演之前」那一段（C# 8622-8696）：
//     · 口交 / 粗暴 先看**她自己的偏好**（`PrefBJ` / `PrefRough`，三态）：
//         已知不愿意        → NOBJK / NOROK（记一笔负声望，别硬上）
//         还不知道          → 按 `Riskiness` 掷一次风险闸 → 不过就 NOBJN / NORON
//                              （并且**当场把偏好定成"不愿意"**，和 C# 一样）
//     · 户外（街面三个菜单）且 `Riskiness < 40` 且不是亲吻 → RISKY
//     · 亲吻且 `Attachment >= 65` 且声望 < 25 且 `Greed < 55` → NOKISS
//
//   ★ 变体条数是**真实条数**（audit_dialogue.py 去掉补位副本后的值），
//     调用方一律 `CC_DlgPick(<下面这个常量>)`，别写死 `_1`。
// ════════════════════════════════════════════════════════════════════════════
CONST_INT CC_RISKY_N 4
CONST_INT CC_NOKISS_N 4
CONST_INT CC_NOBJK_N 6
CONST_INT CC_NOBJN_N 6
CONST_INT CC_NOROK_N 6
CONST_INT CC_NORON_N 6
CONST_INT CC_WALKAWAY_N 4

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.75：空串兜底 + 补上被生成器丢掉的 C# `default:` 文案
//
//  【发现了一个**已经在线的 bug**】
//    `gen_dialogue2.py` 的 `parse_cases()` 会把 C# 的 `default:` 读进 `cases[20]`，
//    但**从来不把它 emit 成 CASE** ⇒ 凡是 C# 靠 `default:` 兜底的性格，
//    在端口里一律返回**空串** ⇒ 界面只显示她的名字、**没有台词**。
//
//  【40 张表全扫，受影响的（只列会显示给人的）】
//    · `NOKISS` / `FINFORCED` / `FINASKNO` / `NOAFFORD` / `GREEDKISS` —— 缺性格
//      **12（善妒）** 和 **17（神秘）**（C# 只写了 18 个 case + default）
//    · `GFHARD` / `GFSOFT` —— 只有 **6/20** 个性格。原因更隐蔽：C# 这两张表用的是一套
//      **旧性格名**（Anxious / Cheerful / Confident / Fierce / Maternal / Spiritual），
//      和端口这 20 个只重叠 6 个 ⇒ 生成器把另外 6 个 case 直接跳过了
//    · `ILYREJ` / `ILYRESP` 的**变体 4~6** —— 缺 11 个性格
//      ⇒ ★ 所以本端口**只用变体 1~3**（那三套是满的）。**别把 ILYREJ 扩到 6。**
//
//  【修法】把 C# `default:` 的文案译成中文放这里，分发器里做空串兜底。
//    ☠ **绝不去改生成器再重跑** —— 重跑会冲掉 6000 行中文（见 src/cc_esc.sc 的教训）。
//    以后新接表，先跑 `python audit_dialogue.py` 看"真实条数"和覆盖，
//    再跑 `python check_cs_multiswitch.py` 看多 switch。
// ════════════════════════════════════════════════════════════════════════════
FUNC STRING CC_NZ(STRING s, STRING fb)
    IF ARE_STRINGS_EQUAL(s, "")
        RETURN fb
    ENDIF
    RETURN s
ENDFUNC

// NOKISS 的 C# default（GetNoKissLines，2 句）
FUNC STRING CC_NOKISS_DEF_T(INT v)
    IF v = 2 OR v = 3 OR v = 4
        RETURN ":~s~ 我不接吻。这是我的规矩。"
    ENDIF
    RETURN ":~s~ 不亲吻。那是我的底线。"
ENDFUNC

// FINFORCED 的 C# default（GetFinishForcedLines，2 句）
FUNC STRING CC_FINFORCED_DEF_T(INT v)
    IF v = 2 OR v = 3 OR v = 4
        RETURN ":~s~ 你事先该问一句的。"
    ENDIF
    RETURN ":~s~ 搞什么？！"
ENDFUNC

// GFHARD 的 C# default（GetGfHardBreakupLines，8 句）
FUNC STRING CC_GFHARD_DEF_T(INT v)
    IF v = 2
        RETURN ":~s~ 我一直在想这件事。我们完了。"
    ELIF v = 3
        RETURN ":~s~ 我撑不下去了。结束了。"
    ELIF v = 4
        RETURN ":~s~ 别再让它更难了。再见。"
    ELIF v = 5
        RETURN ":~s~ 我想要的不止这些。你给不了。我们到这儿了。"
    ELIF v = 6
        RETURN ":~s~ 这是我们最后一次这样说话。完了。"
    ELIF v = 7
        RETURN ":~s~ 我想我们都早知道会这样。就这样吧。"
    ELIF v = 8
        RETURN ":~s~ 我没生气。只是够了。再见。"
    ENDIF
    RETURN ":~s~ 我们之间再也走不下去了。对不起。"
ENDFUNC

// GFSOFT 的 C# default（GetGfSoftBreakupLines，8 句）
FUNC STRING CC_GFSOFT_DEF_T(INT v)
    IF v = 2
        RETURN ":~s~ 我喜欢你，但我想我们还是做朋友吧。"
    ELIF v = 3
        RETURN ":~s~ 我们能不能……就做朋友？这样更好。"
    ELIF v = 4
        RETURN ":~s~ 我在乎你。但我们还是做朋友吧。"
    ELIF v = 5
        RETURN ":~s~ 我想回到朋友关系。对不起。"
    ELIF v = 6
        RETURN ":~s~ 不是不喜欢你。我只是只能到朋友这一步。"
    ELIF v = 7
        RETURN ":~s~ 我宁愿留着你这个朋友，也不想彻底失去你。"
    ELIF v = 8
        RETURN ":~s~ 我们先踩一脚刹车吧。做朋友挺好。"
    ENDIF
    RETURN ":~s~ 我觉得我们做朋友更合适。"
ENDFUNC

// ── "我爱你"（她的正面回应）真实 3 条 ──────────────────────────────────────
//   ★ 只用变体 1~3：变体 4~6 在数据里缺 11 个性格（见上面说明）。
CONST_INT CC_ILYRESP_N 3

FUNC STRING CC_ILYRESP_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_ILYRESP_2_T(pIdx)
    ELIF v = 3
        RETURN CC_ILYRESP_3_T(pIdx)
    ENDIF
    RETURN CC_ILYRESP_1_T(pIdx)
ENDFUNC

// ── 分手（硬 / 软）真实各 4 条 ────────────────────────────────────────────
//   ★ 表里只有 6/20 个性格有 case ⇒ 其余走 CC_GF*_DEF_T（C# 的 default 译文）。
CONST_INT CC_GFHARD_N 4
CONST_INT CC_GFSOFT_N 4

// ★ v0.76：`CC_GFHARD_T` / `CC_GFSOFT_T` 这两个分发器**移到了下面**（补丁表 `CC_GF*_P_T` 之后）——
//   SanScript 要求先定义后使用，而它们要调补丁表。CONST_INT 留在原处没问题。

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.76：分手台词的**逐性格补丁表**（14 个性格）
//
//  【为什么需要】C# 的 `GetGfHardBreakupLines` / `GetGfSoftBreakupLines` 用的是**另一套
//    旧性格名**（Anxious / Cheerful / Confident / Fierce / Maternal / Spiritual），
//    和端口这 20 个只重叠 6 个 ⇒ 生成出来的表只有 6/20 个性格，剩下 14 个是空串。
//    之前用 `CC_GF*_DEF_T`（C# 的 `default:` 译文）兜底，等于这 14 个性格听的是**通用句**。
//
//  【本表】给这 14 个性格各写**一句专属**的分手台词（硬/软各一句）。
//    ★ 这 14 句是**我写的**，不是 C# 原文 —— C# 里没有它们的专属文案。
//      要撤掉的话：把分发器里的 `CC_GFHARD_P_T(pIdx)` 换回 `CC_GFHARD_DEF_T(v)` 即可。
//    ★ 只有 1 个变体（不随 v 变），因为再乘 8 个变体就是 112 句，性价比太低。
// ════════════════════════════════════════════════════════════════════════════
FUNC STRING CC_GFHARD_P_T(INT pIdx)
    IF pIdx = 1
        RETURN ":~s~ 行了，到此为止。别想着还能回头。"
    ELIF pIdx = 2
        RETURN ":~s~ 我说结束就结束。别让我重复。"
    ELIF pIdx = 4
        RETURN ":~s~ 我一直很温柔地对你。这次不行了。"
    ELIF pIdx = 5
        RETURN ":~s~ 从你身上我拿不到想要的了。散了吧。"
    ELIF pIdx = 6
        RETURN ":~s~ 我早看出来了。别装了。"
    ELIF pIdx = 7
        RETURN ":~s~ 没意思了。我去找点好玩的。"
    ELIF pIdx = 11
        RETURN ":~s~ 我不需要你。现在也一样。"
    ELIF pIdx = 12
        RETURN ":~s~ 你早就有别人了吧？我不玩了。"
    ELIF pIdx = 13
        RETURN ":~s~ 就这样吧——我说真的。……算了，是真的。"
    ELIF pIdx = 14
        RETURN ":~s~ 你会后悔的。而且你会回来。"
    ELIF pIdx = 15
        RETURN ":~s~ 滚。别让我说第三遍。"
    ELIF pIdx = 17
        RETURN ":~s~ 有些事你不必知道。到此为止。"
    ELIF pIdx = 18
        RETURN ":~s~ 这段关系已经没有体面可言了。再见。"
    ELIF pIdx = 19
        RETURN ":~s~ 我不想——不，我想。结束了。别打了。"
    ENDIF
    RETURN ""
ENDFUNC

FUNC STRING CC_GFSOFT_P_T(INT pIdx)
    IF pIdx = 1
        RETURN ":~s~ 当朋友挺好。你还是挺有意思的。"
    ELIF pIdx = 2
        RETURN ":~s~ 我们做朋友吧。我说话算数。"
    ELIF pIdx = 4
        RETURN ":~s~ 我不想伤你。但我们还是做朋友吧。"
    ELIF pIdx = 5
        RETURN ":~s~ 朋友？也能有别的用处。先这样吧。"
    ELIF pIdx = 6
        RETURN ":~s~ 朋友就朋友。我不亏，你也不亏。"
    ELIF pIdx = 7
        RETURN ":~s~ 做朋友吧——还能一起玩。"
    ELIF pIdx = 11
        RETURN ":~s~ 朋友就够了。我本来也就这么打算。"
    ELIF pIdx = 12
        RETURN ":~s~ 我做不到只当朋友。……好吧，试试。"
    ELIF pIdx = 13
        RETURN ":~s~ 朋友？行啊。反正我也想不清。"
    ELIF pIdx = 14
        RETURN ":~s~ 先退一步吧。对我俩都好。"
    ELIF pIdx = 15
        RETURN ":~s~ 别逼我说难听的。做朋友。"
    ELIF pIdx = 17
        RETURN ":~s~ 就这样吧。你不需要知道更多。"
    ELIF pIdx = 18
        RETURN ":~s~ 退一步，体面一点。就做朋友。"
    ELIF pIdx = 19
        RETURN ":~s~ 朋友——不，不行。……好吧，朋友。"
    ENDIF
    RETURN ""
ENDFUNC

// ── 分发的两个出口（放在补丁表之后 —— 先定义后使用）────────────────────────
FUNC STRING CC_GFHARD_T(INT v, INT pIdx)
    STRING s = CC_GFHARD_1_T(pIdx)
    IF v = 2
        s = CC_GFHARD_2_T(pIdx)
    ELIF v = 3
        s = CC_GFHARD_3_T(pIdx)
    ELIF v = 4
        s = CC_GFHARD_4_T(pIdx)
    ENDIF
    // ★ v0.76：表里没这个性格 ⇒ 先用**逐性格补丁**（我写的 14 句），再退回 C# default 译文
    IF ARE_STRINGS_EQUAL(s, "")
        s = CC_GFHARD_P_T(pIdx)
    ENDIF
    RETURN CC_NZ(s, CC_GFHARD_DEF_T(v))
ENDFUNC

FUNC STRING CC_GFSOFT_T(INT v, INT pIdx)
    STRING s = CC_GFSOFT_1_T(pIdx)
    IF v = 2
        s = CC_GFSOFT_2_T(pIdx)
    ELIF v = 3
        s = CC_GFSOFT_3_T(pIdx)
    ELIF v = 4
        s = CC_GFSOFT_4_T(pIdx)
    ENDIF
    IF ARE_STRINGS_EQUAL(s, "")
        s = CC_GFSOFT_P_T(pIdx)
    ENDIF
    RETURN CC_NZ(s, CC_GFSOFT_DEF_T(v))
ENDFUNC

// ── 邀她上车被拒（真实 2 条，20 个性格都满）──────────────────────────────
CONST_INT CC_VEHINVREJ_N 2

FUNC STRING CC_VEHINVREJ_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_VEHINVREJ_2_T(pIdx)
    ENDIF
    RETURN CC_VEHINVREJ_1_T(pIdx)
ENDFUNC

FUNC STRING CC_VEHINVREJ_H(INT v, INT pIdx)
    IF v = 2
        RETURN CC_VEHINVREJ_2_H(pIdx)
    ENDIF
    RETURN CC_VEHINVREJ_1_H(pIdx)
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.76：亲密子菜单的**回答台词**
//
//  【C# 结构】`DrawIntimacySubMenu`（CinnamonCoffee.cs:18434）两个分支：
//    · 分支 0「问她的偏好」**8 项**   · 分支 1「试探一下」**3 项**
//  【本表口径】按 `(item, tone)` 出答案，tone 用 `CC_ToneBucket` 的 3 档（0 冷 / 1 中 / 2 暖）。
//    ☠ **不是** C# 原文的逐性格对应（那要 8×20 + 3×20 = 220 句）。
//      用"语气档 × 条目"= 33 句，既有性格区分度、又不至于失控。
//      C# 的逐性格版本在 `GetIntimacySubResponse`，要铺满的话是**另一个大工程**。
// ════════════════════════════════════════════════════════════════════════════
FUNC STRING CC_INTASK_T(INT item, INT tone)
    IF item = 0
        IF tone = 2
            RETURN ":~s~ 亲啊。不过得看我心情。"
        ELIF tone = 0
            RETURN ":~s~ 亲不亲我说了算。"
        ENDIF
        RETURN ":~s~ 看情况。别一上来就贴。"
    ELIF item = 1
        IF tone = 2
            RETURN ":~s~ 私下吧。人多我不自在。"
        ELIF tone = 0
            RETURN ":~s~ 私下。别问为什么。"
        ENDIF
        RETURN ":~s~ 私下。我不想被人看着。"
    ELIF item = 2
        IF tone = 2
            RETURN ":~s~ 喜欢被认真对待。别的……你自己琢磨。"
        ELIF tone = 0
            RETURN ":~s~ 别打听。到时候你自然会知道。"
        ENDIF
        RETURN ":~s~ 慢慢来就知道。我不写清单。"
    ELIF item = 3
        IF tone = 2
            RETURN ":~s~ 可以啊。不过你得先让我舒服。"
        ELIF tone = 0
            RETURN ":~s~ 那得看你怎么问。"
        ENDIF
        RETURN ":~s~ 看情况。别当成理所当然。"
    ELIF item = 4
        IF tone = 2
            RETURN ":~s~ 别太狠就行。"
        ELIF tone = 0
            RETURN ":~s~ 想都别想。"
        ENDIF
        RETURN ":~s~ 轻点。我不喜欢被当东西。"
    ELIF item = 5
        IF tone = 2
            RETURN ":~s~ 会啊。有时候我喜欢带节奏。"
        ELIF tone = 0
            RETURN ":~s~ 不。别指望。"
        ENDIF
        RETURN ":~s~ 看心情。别指望每次。"
    ELIF item = 6
        IF tone = 2
            RETURN ":~s~ 别骗我。别的都能商量。"
        ELIF tone = 0
            RETURN ":~s~ 别越界。我说不行就是不行。"
        ENDIF
        RETURN ":~s~ 别勉强我。我说停就停。"
    ENDIF
    // item = 7：「要不要保持这种随便的关系」——这句就是**炮友关系的确认**
    IF tone = 2
        RETURN ":~s~ 行啊。我不喜欢被绑着。"
    ELIF tone = 0
        RETURN ":~s~ 随便？我只是不想谈以后。"
    ENDIF
    RETURN ":~s~ 可以。但别把我当随便的人。"
ENDFUNC

FUNC STRING CC_INTTEST_T(INT item, INT tone)
    IF item = 0
        IF tone = 2
            RETURN ":~s~ 这话我爱听。……再说一遍。"
        ELIF tone = 0
            RETURN ":~s~ 少来这套。"
        ENDIF
        RETURN ":~s~ 嘴挺甜。然后呢？"
    ELIF item = 1
        IF tone = 2
            RETURN ":~s~ 我也有点想了。"
        ELIF tone = 0
            RETURN ":~s~ 慢慢来。"
        ENDIF
        RETURN ":~s~ 近到哪一步？"
    ENDIF
    IF tone = 2
        RETURN ":~s~ 想。别让我说第二遍。"
    ELIF tone = 0
        RETURN ":~s~ 现在不想。"
    ENDIF
    RETURN ":~s~ ……看你怎么做。"
ENDFUNC




FUNC STRING CC_RISKY_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_RISKY_2_T(pIdx)
    ELIF v = 3
        RETURN CC_RISKY_3_T(pIdx)
    ELIF v = 4
        RETURN CC_RISKY_4_T(pIdx)
    ENDIF
    RETURN CC_RISKY_1_T(pIdx)
ENDFUNC

FUNC STRING CC_NOKISS_T(INT v, INT pIdx)
    STRING s = CC_NOKISS_1_T(pIdx)
    IF v = 2
        s = CC_NOKISS_2_T(pIdx)
    ELIF v = 3
        s = CC_NOKISS_3_T(pIdx)
    ELIF v = 4
        s = CC_NOKISS_4_T(pIdx)
    ENDIF
    // ★ 性格 12(善妒)/17(神秘) 在 C# 里走 default ⇒ 表里是空串 ⇒ 兜底
    RETURN CC_NZ(s, CC_NOKISS_DEF_T(v))
ENDFUNC

FUNC STRING CC_NOBJK_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_NOBJK_2_T(pIdx)
    ELIF v = 3
        RETURN CC_NOBJK_3_T(pIdx)
    ELIF v = 4
        RETURN CC_NOBJK_4_T(pIdx)
    ELIF v = 5
        RETURN CC_NOBJK_5_T(pIdx)
    ELIF v = 6
        RETURN CC_NOBJK_6_T(pIdx)
    ENDIF
    RETURN CC_NOBJK_1_T(pIdx)
ENDFUNC

FUNC STRING CC_NOBJN_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_NOBJN_2_T(pIdx)
    ELIF v = 3
        RETURN CC_NOBJN_3_T(pIdx)
    ELIF v = 4
        RETURN CC_NOBJN_4_T(pIdx)
    ELIF v = 5
        RETURN CC_NOBJN_5_T(pIdx)
    ELIF v = 6
        RETURN CC_NOBJN_6_T(pIdx)
    ENDIF
    RETURN CC_NOBJN_1_T(pIdx)
ENDFUNC

FUNC STRING CC_NOROK_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_NOROK_2_T(pIdx)
    ELIF v = 3
        RETURN CC_NOROK_3_T(pIdx)
    ELIF v = 4
        RETURN CC_NOROK_4_T(pIdx)
    ELIF v = 5
        RETURN CC_NOROK_5_T(pIdx)
    ELIF v = 6
        RETURN CC_NOROK_6_T(pIdx)
    ENDIF
    RETURN CC_NOROK_1_T(pIdx)
ENDFUNC

FUNC STRING CC_NORON_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_NORON_2_T(pIdx)
    ELIF v = 3
        RETURN CC_NORON_3_T(pIdx)
    ELIF v = 4
        RETURN CC_NORON_4_T(pIdx)
    ELIF v = 5
        RETURN CC_NORON_5_T(pIdx)
    ELIF v = 6
        RETURN CC_NORON_6_T(pIdx)
    ENDIF
    RETURN CC_NORON_1_T(pIdx)
ENDFUNC

// ── 走开（二次机会）── C# 7915-7928 ─────────────────────────────────────────
//   触发条件**很窄**：她已经在躲你（声望 ≤ -1）**并且**你明知故问、又问了一遍
//   同一个话题。第一次问（哪怕她在躲你）只是冷冷回一句，不会当场走人。
FUNC STRING CC_WALKAWAY_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_WALKAWAY_2_T(pIdx)
    ELIF v = 3
        RETURN CC_WALKAWAY_3_T(pIdx)
    ELIF v = 4
        RETURN CC_WALKAWAY_4_T(pIdx)
    ENDIF
    RETURN CC_WALKAWAY_1_T(pIdx)
ENDFUNC


// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.72（D 类接线）：**收尾（内射）那一段的台词**
//
//   本端口的 `g_finInside` / `g_finForced` / `g_finBroke` / `g_finFreePass` /
//   `g_alCim` 这套状态**早就在跑了**（cc_part1 / cc_part2b / cc_part2c），
//   缺的只是"开口说话" ⇒ 这四张表接上属于**只加台词、不改玩法**。
//
//   对应 C#（7516-7560）：
//     · `g_alCim = 1`（她自己享受）→ **FINUNASK**（C# 7540 ShowCimPreferenceUnaskedSubtitle）
//     · 她答应/顺势接受          → **FINACC**  （C# 7373/7381/7398/7411 ShowFinishInsideAcceptSubtitle）
//     · 强行内射（没钱/她没答应）→ **FINFORCED**（沙盒，C# 7550）
//                                 / **FCFHOOK**（A-Life 妓女且翻脸，C# 7592）
// ════════════════════════════════════════════════════════════════════════════
CONST_INT CC_FINACC_N 6
CONST_INT CC_FINUNASK_N 6
CONST_INT CC_FINFORCED_N 4
CONST_INT CC_FCFHOOK_N 4

FUNC STRING CC_FINACC_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_FINACC_2_T(pIdx)
    ELIF v = 3
        RETURN CC_FINACC_3_T(pIdx)
    ELIF v = 4
        RETURN CC_FINACC_4_T(pIdx)
    ELIF v = 5
        RETURN CC_FINACC_5_T(pIdx)
    ELIF v = 6
        RETURN CC_FINACC_6_T(pIdx)
    ENDIF
    RETURN CC_FINACC_1_T(pIdx)
ENDFUNC

FUNC STRING CC_FINUNASK_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_FINUNASK_2_T(pIdx)
    ELIF v = 3
        RETURN CC_FINUNASK_3_T(pIdx)
    ELIF v = 4
        RETURN CC_FINUNASK_4_T(pIdx)
    ELIF v = 5
        RETURN CC_FINUNASK_5_T(pIdx)
    ELIF v = 6
        RETURN CC_FINUNASK_6_T(pIdx)
    ENDIF
    RETURN CC_FINUNASK_1_T(pIdx)
ENDFUNC

FUNC STRING CC_FINFORCED_T(INT v, INT pIdx)
    STRING s = CC_FINFORCED_1_T(pIdx)
    IF v = 2
        s = CC_FINFORCED_2_T(pIdx)
    ELIF v = 3
        s = CC_FINFORCED_3_T(pIdx)
    ELIF v = 4
        s = CC_FINFORCED_4_T(pIdx)
    ENDIF
    // ★ 性格 12(善妒)/17(神秘) 在 C# 里走 default ⇒ 表里是空串 ⇒ 兜底
    RETURN CC_NZ(s, CC_FINFORCED_DEF_T(v))
ENDFUNC

FUNC STRING CC_FCFHOOK_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_FCFHOOK_2_T(pIdx)
    ELIF v = 3
        RETURN CC_FCFHOOK_3_T(pIdx)
    ELIF v = 4
        RETURN CC_FCFHOOK_4_T(pIdx)
    ENDIF
    RETURN CC_FCFHOOK_1_T(pIdx)
ENDFUNC


// ── 给钱：接受 ──────────────────────────────────────────────────────────────
//   变体号约定（★ 重要）：**档位 × 3 + (1..3)**
//       v = 1..3  → 小额档 (tier 0) 的三条
//       v = 4..6  → 中额档 (tier 1) 的三条
//       v = 7..9  → 大额档 (tier 2) 的三条
//
//   ★★★ v0.68 修的一个真 bug：
//     旧代码把**档位号直接当变体号**用（tier 1 → `_2_T`，tier 2 → `_3_T`），
//     而 `_1 _2 _3` 其实是**小额档的三条**！
//     ⇒ 给十万块时她说的是"这……谢了"这种小费语气，
//       三档金额听到的永远是**同一档（小额）**的三句话。
//     （v0.61 以前还额外抱怨过"三档反应一样"，根因就在这里，当时只调了阈值没查表。）
FUNC STRING CC_GMACC_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_GMACC_2_T(pIdx)
    ELIF v = 3
        RETURN CC_GMACC_3_T(pIdx)
    ELIF v = 4
        RETURN CC_GMACC_4_T(pIdx)
    ELIF v = 5
        RETURN CC_GMACC_5_T(pIdx)
    ELIF v = 6
        RETURN CC_GMACC_6_T(pIdx)
    ELIF v = 7
        RETURN CC_GMACC_7_T(pIdx)
    ELIF v = 8
        RETURN CC_GMACC_8_T(pIdx)
    ELIF v = 9
        RETURN CC_GMACC_9_T(pIdx)
    ENDIF
    RETURN CC_GMACC_1_T(pIdx)
ENDFUNC

// 给钱：金额**后缀**。必须和 CC_GMACC_T 用同一个 v（一句话被生成器切成了
// 「正文(含'$')」+「后缀」两半，取错就拼成杂交句）。
FUNC STRING CC_GMACC_S(INT v, INT pIdx)
    IF v = 2
        RETURN CC_GMACC_2_S(pIdx)
    ELIF v = 3
        RETURN CC_GMACC_3_S(pIdx)
    ELIF v = 4
        RETURN CC_GMACC_4_S(pIdx)
    ELIF v = 5
        RETURN CC_GMACC_5_S(pIdx)
    ELIF v = 6
        RETURN CC_GMACC_6_S(pIdx)
    ELIF v = 7
        RETURN CC_GMACC_7_S(pIdx)
    ELIF v = 8
        RETURN CC_GMACC_8_S(pIdx)
    ELIF v = 9
        RETURN CC_GMACC_9_S(pIdx)
    ENDIF
    RETURN CC_GMACC_1_S(pIdx)
ENDFUNC


// ── 给钱：拒绝（B 类修复，v0.72）────────────────────────────────────────────
//   与 GMACC 完全对称：**档位 × 3 + (1..3)**（v=1..3 小额 / 4..6 中额 / 7..9 大额）。
//   ★ 先说清楚"这表原来是好的"：
//     审计里把 `_T == ": $"` 误判成"空行"，其实**不是** ——
//     C# 的拒绝台词有一半是**金额打头**（`"$X? Please. Don't insult me."`），
//     生成器把它切成 正文(": $") + 金额 + 后缀("？拜托。别侮辱我。")，
//     所以 `_T` 只有 ": $"、真正的字在 `_S` 里，配合 CC_ShowLine3 是**正确**的。
//     真要修的只有两处**英文没翻译**（tier0 的 Cold `No.` / Mysterious `...No.`），已修。
//   ★ 之所以要新加调度器：旧代码**根本没有**任何调用方（ML_GIVEMONEY 无条件接受），
//     表是"死的"。这里把 v 展开成 9 个分支，供 cc_part2d 的给钱判定调用。
FUNC STRING CC_GMREJ_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_GMREJ_2_T(pIdx)
    ELIF v = 3
        RETURN CC_GMREJ_3_T(pIdx)
    ELIF v = 4
        RETURN CC_GMREJ_4_T(pIdx)
    ELIF v = 5
        RETURN CC_GMREJ_5_T(pIdx)
    ELIF v = 6
        RETURN CC_GMREJ_6_T(pIdx)
    ELIF v = 7
        RETURN CC_GMREJ_7_T(pIdx)
    ELIF v = 8
        RETURN CC_GMREJ_8_T(pIdx)
    ELIF v = 9
        RETURN CC_GMREJ_9_T(pIdx)
    ENDIF
    RETURN CC_GMREJ_1_T(pIdx)
ENDFUNC

// 给钱拒绝：金额**后缀**（必须与 CC_GMREJ_T 用同一个 v）。
FUNC STRING CC_GMREJ_S(INT v, INT pIdx)
    IF v = 2
        RETURN CC_GMREJ_2_S(pIdx)
    ELIF v = 3
        RETURN CC_GMREJ_3_S(pIdx)
    ELIF v = 4
        RETURN CC_GMREJ_4_S(pIdx)
    ELIF v = 5
        RETURN CC_GMREJ_5_S(pIdx)
    ELIF v = 6
        RETURN CC_GMREJ_6_S(pIdx)
    ELIF v = 7
        RETURN CC_GMREJ_7_S(pIdx)
    ELIF v = 8
        RETURN CC_GMREJ_8_S(pIdx)
    ELIF v = 9
        RETURN CC_GMREJ_9_S(pIdx)
    ENDIF
    RETURN CC_GMREJ_1_S(pIdx)
ENDFUNC

// 给钱拒绝：这条台词里**有没有金额**（1=有 ⇒ 用 CC_ShowLine3，0=没有 ⇒ 用 CC_ShowLine2）。
//   C# 里同一档的三条台词混着"带金额"和"不带金额"两种，
//   不带金额的（如 Cold 的 `"No."`）如果硬塞一个 CC_ShowLine3 会把金额插进正文中间，
//   变成 "…$X…" 的杂交句，所以必须按这条台词自己的 I 标志走。
FUNC INT CC_GMREJ_I(INT v, INT pIdx)
    IF v = 2
        RETURN CC_GMREJ_2_I(pIdx)
    ELIF v = 3
        RETURN CC_GMREJ_3_I(pIdx)
    ELIF v = 4
        RETURN CC_GMREJ_4_I(pIdx)
    ELIF v = 5
        RETURN CC_GMREJ_5_I(pIdx)
    ELIF v = 6
        RETURN CC_GMREJ_6_I(pIdx)
    ELIF v = 7
        RETURN CC_GMREJ_7_I(pIdx)
    ELIF v = 8
        RETURN CC_GMREJ_8_I(pIdx)
    ELIF v = 9
        RETURN CC_GMREJ_9_I(pIdx)
    ENDIF
    RETURN CC_GMREJ_1_I(pIdx)
ENDFUNC


// ── "我爱你"被拒（真实 3 条）────────────────────────────────────────────────
//   ★ 表的索引是**性格**（pIdx 0..19），变体 v 是 1..3。
//     C# 侧是 `ilyOpts[rng.Next(ilyOpts.Length)]` ⇒ **随机**挑，不是按菜单行号。
FUNC STRING CC_ILYREJ_T(INT v, INT pIdx)
    IF v = 2
        RETURN CC_ILYREJ_2_T(pIdx)
    ELIF v = 3
        RETURN CC_ILYREJ_3_T(pIdx)
    ENDIF
    RETURN CC_ILYREJ_1_T(pIdx)
ENDFUNC


// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.72（D 类接线）：**开演前的性格闸**（C# HandleMenuSelect 8622-8696）
//
//   调用方式：`IF CC_SvcGate(act) → 已经说过话/记过账，调用方直接 EXIT`。
//   返回 TRUE = 被拦下（**不要**开演）。
//
//   判定顺序**逐条照抄 C#**（顺序会影响"她说哪句"）：
//     ① 口交（act 2/3/4）先看 `g_alPrefBJ`：
//          2 = 她说过不愿    → NOBJK + 记惩罚（C# 8625-8637）
//          0 = 还不知道      → Riskiness < 50 ⇒ NOBJN，**并把偏好定成"不愿"**；否则定成"愿意"
//                              （C# 8638-8652，逐字：bjRisk < 0.50）
//          1 = 愿意          → 放行
//     ② 粗暴后入（act 11）同理看 `g_alPrefRough`，风险阈值 65（C# 8657-8686，roughRisk < 0.65）
//     ③ 户外风险：街面三个菜单本来就是户外 ⇒ Riskiness < 40 且不是亲吻 → RISKY（C# 8688-8696）
//     ④ 亲吻情感闸：亲吻且 Attachment ≥ 65 且声望 < 25 且 Greed < 55 → NOKISS（C# 5895-5899）
//
//   ★ 只在 **A-Life 模式 + 有条目** 时生效，和 C# 一样（C# 的闸都在
//     `aLifeMode && _currentGirlFp != null` 里）。沙盒模式行为**完全不变**。
//   ★ 一切都走安全入口（CC_Pers/CC_Rep/CC_AlOk），条目缺失时**直接放行**
//     —— 不能因为"读不到条目"就让她拒绝（那会变成"点了没反应"，是本工程反复栽的坑）。
// ════════════════════════════════════════════════════════════════════════════
FUNC BOOL CC_SvcGate(INT act)
    IF NOT g_aLifeMode
        RETURN FALSE
    ENDIF
    IF NOT CC_AlOk()
        RETURN FALSE
    ENDIF
    INT p = CC_Pers()
    INT grp = CC_SvcGroup(act)
    TEXT_LABEL_15 gn
    CC_GetGirlName(gn)
    // ① 口交偏好
    IF CC_IsBJAct(act)
        IF g_alPrefBJ[g_curAlIdx] = 2
            CC_ShowLine2(CC_NOBJK_1_H(p), CC_NOBJK_T(CC_DlgPick(CC_NOBJK_N), p), gn, 3000)
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + CC_NoBjPenalty()
            CC_ClampRep(g_curAlIdx)
            AL_UpdateRel(g_curAlIdx)
            RETURN TRUE
        ENDIF
        IF g_alPrefBJ[g_curAlIdx] = 0
            IF RISKINESS[p] < 50
                CC_ShowLine2(CC_NOBJN_1_H(p), CC_NOBJN_T(CC_DlgPick(CC_NOBJN_N), p), gn, 3000)
                g_alPrefBJ[g_curAlIdx] = 2
                RETURN TRUE
            ENDIF
            g_alPrefBJ[g_curAlIdx] = 1
        ENDIF
    ENDIF
    // ② 粗暴偏好
    IF CC_IsRoughAct(act)
        IF g_alPrefRough[g_curAlIdx] = 2
            CC_ShowLine2(CC_NOROK_1_H(p), CC_NOROK_T(CC_DlgPick(CC_NOROK_N), p), gn, 3000)
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + CC_NoRoughPenalty()
            CC_ClampRep(g_curAlIdx)
            AL_UpdateRel(g_curAlIdx)
            RETURN TRUE
        ENDIF
        IF g_alPrefRough[g_curAlIdx] = 0
            IF RISKINESS[p] < 65
                CC_ShowLine2(CC_NORON_1_H(p), CC_NORON_T(CC_DlgPick(CC_NORON_N), p), gn, 3000)
                g_alPrefRough[g_curAlIdx] = 2
                RETURN TRUE
            ENDIF
            g_alPrefRough[g_curAlIdx] = 1
        ENDIF
    ENDIF
    // ③ 户外风险（街面菜单 = 户外；亲吻永远豁免）
    IF RISKINESS[p] < 40 AND grp != 0
        CC_ShowLine2(CC_RISKY_1_H(p), CC_RISKY_T(CC_DlgPick(CC_RISKY_N), p), gn, 3000)
        RETURN TRUE
    ENDIF
    // ④ 亲吻情感闸
    IF grp = 0 AND ATTACHMENT[p] >= 65 AND CC_Rep() < 25 AND GREED[p] < 55
        CC_ShowLine2(CC_NOKISS_1_H(p), CC_NOKISS_T(CC_DlgPick(CC_NOKISS_N), p), gn, 3000)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC
