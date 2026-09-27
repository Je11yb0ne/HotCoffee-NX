// ============================================================================
// cc_main.sc — CinnamonCoffee 主循环（★ 唯一真源，别再让它只活在合并产物里）
//
// 【为什么单独成文件 (2026-09-25)】
//   原来主循环是从 cc_part2d.sc 的 SCRIPT 块里"手术抽取"的 —— merge_mods.py 用
//   find("ELSE") / rfind("ENDIF") / rfind("ENDWHILE") 去猜边界。
//   cc_part2d.sc 在 09:50 被改成纯函数文件、SCRIPT 块被删掉之后，抽取彻底失败：
//         AssertionError: part2d SCRIPT block not found
//   而 build_cinnamon.py 把 merge 的返回码存进 r.merge 之后**从没检查过** ——
//   于是合并静默失败，后面照样拿**旧的** simple_zombies.sc 去编译。
//   后果：09:50 之后所有源码改动（含 10:05 的动画字典探针、12:12 的中文台词）
//   一次都没进过交付产物 —— 这就是"改了没反应/模组不可用"的真相。
//
// 【现在】主循环是普通源文件；merge_mods.py 只做"拼接"，不做任何猜边界的抽取。
//   要改主循环 -> 直接改这个文件。
//
// 【调用关系】僵尸模组主循环在 IS_PLAYER_PLAYING 的 ELSE 分支里调用：
//         MenuInput()
//         CC_MainTick()
//   开机时再调用一次 CC_ModInit()。
// ============================================================================

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.57 热咖啡模组**休眠期**的唯一心跳（g_modOn = FALSE 时由 CC_MainTick 调用）
//
//   ★★ 必须定义在 CC_MainTick **之前** —— SanScript 要求"先定义后使用"，
//      放在后面会报 `Unrecognized variable or command name`（已踩过）。
//
//   职责只有一个：**保留"重新打开"的入口**。除此之外什么都不做。
//
//   【必须清零两个互斥标志】g_ccHotkeyBlock / g_ccMenuActive 保持 FALSE
//     ⇒ 僵尸模组的菜单键、互动键完全不受本模组影响（"休眠 = 零占用"）。
//
//   【为什么休眠期也得读按键】关闭模组后 CC 菜单打不开（主循环整个 EXIT 了），
//     如果不在这里留一个入口，用户**永远无法再把模组打开**，只能重启游戏。
//     所以 B + 十字键右 在休眠期依然生效 = "重新打开"。
//
//   【重新打开的那一帧要挡住僵尸侧】把 g_ccHotkeyBlock 置 TRUE：
//     僵尸模组的互动处理跑在 CC_MainTick **之后**，会看到 TRUE
//     ⇒ 不会把这一次"十字键右"顺手当成它自己的互动键。
//     （"先按十字键右、再按 B"这种顺序仍会让僵尸侧先看到一次右键 —— 那一按
//       本来就是僵尸模组自己的互动键，不由 CC 代拦；与开菜单时是同一个残余。）
//
//   ⚠ 休眠期 CC 没有调 DISABLE_ALL_CONTROL_ACTIONS，读键照样成立：
//     IN_Cancel/IN_Right 内部同时用了 IS_CONTROL_*（读物理输入、不依赖
//     disable/enable 前置）那一组，见 cc_part1.sc。
// ════════════════════════════════════════════════════════════════════════════
PROC CC_ModDormantTick()
    g_player = GET_PLAYER_PED(PLAYER_ID())
    g_ccFrame = g_ccFrame + 1
    // 休眠 = 对僵尸模组零占用：互斥标志一律清零（每帧重写，防止上一帧的残留）。
    g_ccMenuActive = FALSE
    g_ccHotkeyBlock = FALSE
    IF IN_Master()
        g_modOn = TRUE
        g_ccMenuActive = TRUE
        g_ccHotkeyBlock = TRUE   // 本帧挡住僵尸侧，避免"开回来时顺手互动一下"
        CC_HudNote("~g~CinnamonCoffee mod: ON (menu hotkey restored: B + D-pad Right)", 3500)
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.65 临时：音频现场读数（一次性，给"有的人有声音有的人没有"定性）
//
//   为什么放这里（不放 cc_part2c）：合并顺序里 **cc_part2c 排在 cc_part2d 前面**，
//   那里调不到 `CC_MenuLabel`（先定义后使用 ⇒ 会编译报 Unrecognized）。
//   这里在最末尾，什么都定义好了，于是能把
//   **「嗓音名 + 呻吟语音开关状态 + 当前动作」一次打全** ——
//   一行字就同时排除掉"开关被关了"和"动作名不对"这两类原因。
//
//   触发点：每个女伴**第一次该发出呻吟指令的那一刻**（由 cc_part2c 置 g_audioProbeWant）。
//   ⚠ **临时**诊断：音频确认正常后，删掉本 PROC + 调用点 + `g_audioProbeWant`/`g_voiceProbeShown`。
// ════════════════════════════════════════════════════════════════════════════
PROC CC_AudioProbeTick()
    IF NOT g_audioProbeWant
        EXIT
    ENDIF
    g_audioProbeWant = FALSE
    // 多段拼接（本工程既有写法，见 cc_part2d 聊天回复）：SanScript 没有字符串拼接运算符，
    // 只能一段段塞进 TEXT_LABEL 数组再循环送出。
    TEXT_LABEL_63 ap[6]
    ap[0] = "~g~[AUDIO probe] voice="
    ap[1] = g_girlVoice
    ap[2] = " | "
    ap[3] = CC_MenuLabel(ML_SETTINGS, 4)
    ap[4] = " | action="
    ap[5] = g_curAction
    BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
    INT api = 0
    WHILE api < 6
        ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(ap[api])
        api = api + 1
    ENDWHILE
    END_TEXT_COMMAND_PRINT(5000, TRUE)
ENDPROC

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.66 「没声音」自愈：嗓子自动轮换
//
//   【为什么】用户实测：3 个女伴里 1 个没声音，而且**那个没声音的第一次播动画时
//     探针字是出来了的** ⇒ 我们的逻辑全跑到了（开关是开的、phase 到 2、速度 > 0、
//     语音命令也确实发了），问题只在"这个嗓子名 + 这个 ped"在引擎里发不出声。
//     嗓音名表与 C# 原版**逐字一致** ⇒ 静态查不出是哪一个，只能**运行期自愈**。
//
//   【做法】发完一次呻吟后 ~0.4 秒看 `IS_AMBIENT_SPEECH_PLAYING(ped)`：
//     · 在播  ⇒ `g_voiceLocked = TRUE`：这个嗓子是好的，不再轮换
//     · 没在播 ⇒ 换下一个嗓子名（0..8 环形），最多轮 9 次
//   于是"那 9 个嗓子里哪几个在本机取不到音频数据"不再是死局 ——
//   她会在几次呻吟之内自己落到一个能出声的嗓子上，并把新嗓子打在屏幕上。
//
//   ⚠ `IS_AMBIENT_SPEECH_PLAYING` 属本端口"未证明"native（CC_PostSexVoiceTick 也在用）：
//     · 恒 FALSE ⇒ 最坏是"每次呻吟换一个嗓子"，最多 9 次后锁定（不会比现在更糟）
//     · 恒 TRUE  ⇒ 等价于不轮换（与旧行为一致）
//   ⚠ 这是**临时自愈 + 探针**：确认够用后，再把嗓子写死成一个稳定的即可。
// ════════════════════════════════════════════════════════════════════════════
PROC CC_VoiceProbeTick()
    // ★★★ v0.71：**默认关闭**（总开关见 cc_part1.sc 的 `g_voiceAutoRotate`）。
    //   这套轮换会在**动作进行中**改 `g_voiceIdx` ⇒ 同一个女伴在同一次互动里
    //   嗓子会变来变去 —— 正是用户说的"不是每句都随机"里**不想要**的那种；
    //   而且若 `IS_AMBIENT_SPEECH_PLAYING` 在本机恒 FALSE，它还会"越修越糟"
    //   （换来换去、最后锁在一个坏嗓子上）。
    //   现在"没声音"由**玩家手动挑**（设置第 7 项「女伴嗓音」，当场试听）解决。
    IF NOT g_voiceAutoRotate
        EXIT
    ENDIF
    IF g_voiceCheckAt = 0 OR GET_GAME_TIMER() < g_voiceCheckAt
        EXIT
    ENDIF
    g_voiceCheckAt = 0
    IF g_voiceLocked OR NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    IF IS_AMBIENT_SPEECH_PLAYING(g_girl)
        g_voiceLocked = TRUE      // 这个嗓子真能出声 ⇒ 锁定，不再换
        EXIT
    ENDIF
    IF g_voiceRotate >= 8         // 八种都轮过了，别再折腾（v0.79：删掉坏嗓子后是 8 个）
        g_voiceLocked = TRUE
        EXIT
    ENDIF
    g_voiceRotate = g_voiceRotate + 1
    g_voiceIdx = g_voiceIdx + 1
    IF g_voiceIdx > 7
        g_voiceIdx = 0
    ENDIF
    g_girlVoice = CC_VOICE_NAME(g_voiceIdx)
    SET_AMBIENT_VOICE_NAME(g_girl, g_girlVoice)
    g_voiceReapplyAt = GET_GAME_TIMER() + 3000
    // ★ v0.79：**删掉了"把换成了哪个嗓子打到屏幕上"那行探针**（用户要求）。
    //   原来这里有一句 `CC_HudNote(g_girlVoice, 2500)`（不受门控 ⇒ 会跟字幕叠在下方）。
    //   这套轮换本身默认关闭（`g_voiceAutoRotate = FALSE`），删掉只是去掉残余的屏幕噪音。
    //   要回来看嗓子名：走 `CC_HudDiag`（受 `g_showDiag` 门控，不会污染正常画面）。
ENDPROC

PROC CC_MainTick()
    // ════════════════════════════════════════════════════════════════════════
    // ★★★ v0.57 热咖啡模组**总开关**：g_modOn = FALSE 时整个模组休眠。
    //
    //   放在**最顶部**、早于一切逻辑 —— 休眠期连"控制屏蔽 / 警察无视 / 状态机 /
    //   菜单绘制 / 诊断"统统不做，对僵尸模组与游戏本体的占用降到**零**。
    //   于是玩家可以"让女伴不再跟随"而**不必杀掉她**（关闭时女伴被放生为普通 NPC）。
    //
    //   CC_ModDormantTick 只保留一件事：**重新打开的入口**（B + 十字键右）。
    //   若不保留，模组休眠后菜单打不开 ⇒ 用户再也开不回来，只能重启游戏。
    // ════════════════════════════════════════════════════════════════════════
    IF NOT g_modOn
        CC_ModDormantTick()
        EXIT
    ENDIF

    // ★ 记录"本帧开始时菜单是否已经开着"。
    //   用途见**函数末尾**的「输入互斥标志」：关闭菜单的那一帧，
    //   开头是 ML_MAIN、末尾已被 CC_MenuNav 改成 ML_NONE —— 若只看末尾就会
    //   漏掉那一帧的互斥，而那一帧的"十字键右"会被僵尸侧当成互动键。
    BOOL menuWasOpen = FALSE
    IF g_menuLevel != ML_NONE
        menuWasOpen = TRUE
    ENDIF

    // 僵尸菜单点进来 -> 打开 CC 菜单
    // ★ v0.65 修：原来这里写的是 `g_ccMenuIndex = 0` —— 那是**僵尸模组自己的**菜单光标
    //   变量（本工程的光标是 `g_menuIndex`）⇒ 既没重置到 CC 菜单的光标，
    //   又顺手把僵尸菜单的光标清了。改成正确的变量。
    //   （这条路径目前是死的：全工程没有任何地方把 `g_ccOpenFromMenu` 置 TRUE ——
    //     "从僵尸菜单开 CC 菜单"那个入口已经废弃，但顺手修掉，免得以后复用又踩。）
    IF g_ccOpenFromMenu
        g_ccOpenFromMenu = FALSE
        IF g_menuLevel = ML_NONE
            g_menuLevel = ML_MAIN
            g_menuIndex = 0
        ENDIF
    ENDIF

            g_player = GET_PLAYER_PED(PLAYER_ID())
            g_ccFrame = g_ccFrame + 1
            CC_ShakeOff()

            // ── 控制屏蔽 (菜单/动画时; C# 9639-9650) ──
            // ════════════════════════════════════════════════════════════════════
            // ★★★ v0.64：**只压"引擎自动切电影视角"，绝不碰玩家的镜头操作**。
            //
            //   【认锅】v0.63 我在这一块里顺手把 `INPUT_NEXT_CAMERA` 和整个
            //   `CAMERA_CONTROL` 组（含 `INPUT_LOOK_LR/UD`）也禁了，
            //   结果用户实测"动作播放的时候视角完全无法移动和切换了" ——
            //   **右摇杆转视角、切换视角键全废**。这是"禁过头"：
            //   用户只要求"按 A 别切电影视角"，我却把镜头控制整组拿走了。
            //   ⇒ 全部撤回。镜头完全交给玩家。
            //
            //   【真正要压的是"自动切"】GTA 有个**车内怠速电影视角**：
            //   玩家在车里一段时间不操作，引擎**自动**把镜头切成电影机位轮播。
            //   我们的动画期把玩家输入关掉 ⇒ 引擎判"闲置" ⇒ 自动触发；
            //   **菜单开着时同样算闲置**，所以菜单开着时也要压。
            //   原版依据（直接抄）：`pb_prostitute_player.sch` 的
            //     PROC MANAGE_CAMERAS_DURING_SEX()
            //         DISABLE_CINEMATIC_BONNET_CAMERA_THIS_UPDATE()
            //         DISABLE_CINEMATIC_VEHICLE_IDLE_MODE_THIS_UPDATE()
            //   名字里的 THIS_UPDATE ⇒ 必须**每帧**发。
            //   ★ 这两句只压"自动切"，**不影响玩家手动切视角/转视角**。
            // ════════════════════════════════════════════════════════════════════
            IF g_menuLevel != ML_NONE OR g_state = ST_ANIMATING OR g_state = ST_APPROACHING
                DISABLE_ALL_CONTROL_ACTIONS(PLAYER_CONTROL)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_MOVE_LEFT_ONLY, TRUE)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_MOVE_UP_ONLY, TRUE)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_LOOK_LR, TRUE)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_LOOK_UD, TRUE)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_MOVE_LR, TRUE)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_ACCELERATE, TRUE)
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_BRAKE, TRUE)
                // ★ 显式把"切换视角键"再放行一次（DISABLE_ALL 会连它一起关掉）——
                //   玩家要在动画里换视角/换机位，这是明确需求。
                ENABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_NEXT_CAMERA, TRUE)
                // ══════════════════════════════════════════════════════════
                // ★★★ v0.79：**车震时放开车内"切视角"**（用户报"切不到第一视角"）
                //
                //   【为什么原来切不到】下面那三行 `DISABLE_CONTROL_ACTION(…, INPUT_VEH_CIN_CAM, …)`
                //   是为了满足你早先那条要求 ——「动作播放时按 A 别跳到电影视角」。
                //   但在**车内**，这个动作就是那个"切视角"键（本端口上 A 触发它）：
                //   按住它 ⇒ 按 A **什么都不发生** ⇒ 车里的机位**永远停在当前视角**，
                //   第一视角自然进不去。而徒步时切视角走的是 `INPUT_NEXT_CAMERA`
                //   （第 229 行，一直是放行的）⇒ 所以**只有"车内"这个场景出问题**，
                //   和你观察到的现象对得上。
                //
                //   【现在的取法】只有「**动画中 + 玩家在车里**」（= 车震）才**不压**它，
                //   其余情况（菜单开着 / 走近中 / 徒步动画）保持原样。
                //   代价说清楚：车震时按 A 会把机位**整个循环**走一遍（含电影视角），
                //   多按几下就能停在第一视角。这是你那两个要求本身的冲突 ——
                //   "同一个键既要能切到第一视角、又要保证它永远切不到电影视角"做不到。
                //   想恢复旧行为：把下面 `IF NOT ccCamCycle` 改成 `IF TRUE`。
                //   ★ "引擎自动切电影视角"那两句（IDLE_MODE / BONNET）**仍然每帧压制** ——
                //     本次只放开**玩家手按**的那一下，自动轮播照旧被压住。
                //   ⚠ 试出来仍然是"没反应"：说明这个端口上那个键读的是**别的**动作
                //     （2013 版枚举里没有 `INPUT_VEH_CAM_SELECT` 这类名字）⇒ 告诉我，
                //     我改用"每帧强制 `SET_FOLLOW_VEHICLE_CAM_VIEW_MODE` 到第一视角"那条路。
                // ══════════════════════════════════════════════════════════
                BOOL ccCamCycle = FALSE
                IF g_state = ST_ANIMATING AND IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
                    ccCamCycle = TRUE
                ENDIF
                // ══════════════════════════════════════════════════════════
                // ★★★ v0.66：**只禁"电影视角"这一个动作**；右摇杆与切视角键一律不动。
                //
                //   【怎么定位到它的】v0.64 把所有镜头键的 disable 都撤了，用户报
                //   "A 键还是可以切换电影视角"。反过来推：
                //     · 动画期 `DISABLE_ALL_CONTROL_ACTIONS(PLAYER_CONTROL)` 一直开着，
                //       A 却照样切电影视角 ⇒ **触发它的不是 PLAYER 组的动作**；
                //     · v0.63 那把 `CAMERA_CONTROL` 组也禁掉时它就不出现了
                //       （代价是右摇杆/切视角一起废 —— 那是我禁过头）⇒
                //       说明它读的是 **CAMERA 组**（或 FRONTEND 组）。
                //   所以在 **PLAYER + CAMERA + FRONTEND 三组**都按住 `INPUT_VEH_CIN_CAM`
                //   （就是"车内电影视角"这个动作），但**绝不动 `INPUT_LOOK_*`（右摇杆）
                //   与 `INPUT_NEXT_CAMERA`（切换视角键）** —— 那两个是用户明确要留的。
                //   ⚠ `INPUT_VEH_CIN_CAM` **不是**菜单导航键 ⇒ 禁它不影响菜单读键
                //     （本工程 IN_* 只读 FRONTEND 的 UP/DOWN/LEFT/RIGHT/ACCEPT/CANCEL/X/Y/LB/RB）。
                // ══════════════════════════════════════════════════════════
                IF NOT ccCamCycle
                    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_CIN_CAM, TRUE)
                    DISABLE_CONTROL_ACTION(CAMERA_CONTROL, INPUT_VEH_CIN_CAM, TRUE)
                    DISABLE_CONTROL_ACTION(FRONTEND_CONTROL, INPUT_VEH_CIN_CAM, TRUE)
                ENDIF
                // 再压住"引擎自动切"（每帧）：
                DISABLE_CINEMATIC_VEHICLE_IDLE_MODE_THIS_UPDATE()
                DISABLE_CINEMATIC_BONNET_CAMERA_THIS_UPDATE()
            ENDIF

            // ════════════════════════════════════════════════════════════════════
            // ★★★ v0.63：女伴死亡的**统一收口**（与状态机无关）
            //
            //   【用户实测】把女伴打死后，**地图标点一直挂着**，直到人走远、尸体被
            //     回收才消失。
            //   【根因】原来只有两个地方检测死亡，而且都是"某个状态里"的：
            //       · CC_IdleFollowTick（只在 ST_IDLE）
            //       · CC_ApproachTick （只在 ST_APPROACHING）
            //     而真正放生她 / 移标点的是 CC_FullReset → CC_RemoveGirlBlip。
            //     一旦她**在动作中（ST_ANIMATING）或菜单开着的时候**被打死，
            //     上面两个 tick 都不会跑 ⇒ 没有任何人执行 CC_FullReset ⇒
            //     标点继续挂在尸体上（实体标点，尸体被引擎回收才消失 = "走远才消失"）。
            //
            //   【修法】提到这里、摆在状态机之前：只要"有女伴 + 她确实不活了"就
            //     完整放生一次（CC_FullReset 会顺手移标点、复位状态、收掉玩家姿势）。
            //     `CC_EXISTS AND NOT CC_ALIVE` 才是"存在且已死"——避免 g_girl 为空时
            //     每帧误触发。
            // ════════════════════════════════════════════════════════════════════
            IF g_hasGirl AND CC_EXISTS(g_girl) AND NOT CC_ALIVE(g_girl)
                g_menuLevel = ML_NONE
                CC_FullReset(FALSE)
            ENDIF

            // ════════════════════════════════════════════════════════════════════
            // ★★★ v0.69：A-Life 条目的**统一收口**（每帧 reconcile）
            //
            //   【用户实测】"有时候返回后，在互动里选什么对话，女伴都不回应；
            //     但服务那条路照走。"
            //   【根因】聊天 / 试探 / 提议这几支都要读 A-Life 条目
            //     （`g_alPers[g_curAlIdx]`、`g_alRep[g_curAlIdx]`…），
            //     而它们原来整段套在 `IF g_curAlIdx >= 0` 里。
            //     一旦 `g_curAlIdx = -1`（条目没建起来，或建的时候 ped 还没 stream
            //     进来导致 AL_Fingerprint 返回 0），这些分支就**一声不吭地整段跳过**
            //     —— 而"服务"不依赖条目，所以照样能推进。现象完全吻合。
            //     `g_curAlIdx` 有 **5 个赋值点**（拾取/放生/敌对释放…），
            //     靠"每条路径都记得建条目"是防不住的 ⇒ 必须收敛成**唯一写点 + 每帧纠正**。
            //
            //   【修法】只要"有女伴 + 她存在 + 条目不可用"，就补建一条。
            //     成功一次后 `g_curAlIdx >= 0`，本支不再进入 —— 不是每帧重设的轮询。
            //     `AL_GetOrCreate` 只在 ped 不存在时才返回 -1，所以正常情况下一次就成。
            //     但 ped 万一还没 stream 进来会返回 -1 ⇒ 加 250 ms 节流，避免每帧空转。
            // ════════════════════════════════════════════════════════════════════
            IF g_hasGirl AND CC_EXISTS(g_girl) AND NOT CC_AlOk()
                IF GET_GAME_TIMER() >= g_alReconcileAt
                    g_alReconcileAt = GET_GAME_TIMER() + 250
                    // ★ v0.74 去妓女化：末尾 AND NOT g_alNoHooker（见 cc_part1 的说明）
                    g_curAlIdx = AL_GetOrCreate(g_girl, (NOT g_sandboxMode AND g_aLifeMode) AND NOT g_alNoHooker)
                ENDIF
            ENDIF

            IF g_keyCooldown > 0
                g_keyCooldown = g_keyCooldown - 1
            ENDIF

            // ── 警察无视开关（设置第 13 项）──
            //   变化驱动：只有"想要的值的上一次实际设过的值"不同才发 native。
            //   放在状态机**之前**：状态机本身（CC_AppPhase3 等）会调用它把
            //   影子值同步过去，这样不会出现"外部设了 TRUE、影子还以为没设"的脱节。
            CC_ApplyPoliceIgnore()

            // ── post-sex 语音 ──
            CC_PostSexVoiceTick()

            // ── v0.65 临时音频探针（一次性读数；见 CC_AudioProbeTick 说明）──
            CC_AudioProbeTick()

            // ── v0.66 嗓子自愈：查"到底有没有真在播"，没播就换下一个嗓子 ──
            CC_VoiceProbeTick()

            // ── 现金掉落 ──
            CC_CashDropTick()

            // ── 死亡扫描 (C# 两阶段 → 单段简版: 徒手杀 80% 存活 / 掉落) ──
            IF g_aLifeMode AND GET_GAME_TIMER() >= g_ccDeadScanAt
                g_ccDeadScanAt = GET_GAME_TIMER() + 1000
                IF g_curAlIdx >= 0
                    IF CC_EXISTS(g_girl)
                        IF IS_ENTITY_DEAD(g_girl)
                            BOOL byPlayer = HAS_ENTITY_BEEN_DAMAGED_BY_ENTITY(g_girl, g_player, TRUE)
                            INT wTag = ENUM_TO_INT(GET_PED_CAUSE_OF_DEATH(g_girl))
                            BOOL unarmed = FALSE
                            IF byPlayer AND wTag = GET_HASH_KEY("WEAPON_UNARMED")
                                unarmed = TRUE
                            ENDIF
                            CLEAR_ENTITY_LAST_DAMAGE_ENTITY(g_girl)
                            IF byPlayer AND g_alCash[g_curAlIdx] > 0
                                VECTOR dp = GET_ENTITY_COORDS(g_girl)
                                CREATE_AMBIENT_PICKUP(PICKUP_MONEY_VARIABLE, <<dp.x, dp.y, dp.z + 0.1>>, 0, g_alCash[g_curAlIdx], INT_TO_ENUM(MODEL_NAMES, GET_HASH_KEY("prop_cash_pile_01")), FALSE, TRUE)
                                g_alCash[g_curAlIdx] = 0
                            ENDIF
                            IF NOT (byPlayer AND unarmed AND CC_ROLL(80))
                                // 未存活 → 记忆清除
                                g_alUsed[g_curAlIdx] = FALSE
                            ELSE
                                // 徒手 80% 存活 → 声望惩罚
                                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 15
                                IF g_alRep[g_curAlIdx] < CC_REP_MIN
                                    g_alRep[g_curAlIdx] = CC_REP_MIN
                                ENDIF
                                AL_UpdateRel(g_curAlIdx)
                            ENDIF
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF

            // ── 击打反应 ──
            CC_HitReactionTick()

            // ── 语音/表情重应用 ──
            CC_ReapplyTick()

            // ── 坐/躺 proposition 卡死兜底 ──
            //   ★ 2026-09-25晚：用户实测"坐姿女上位中止后回到坐姿、按键全无反应、
            //     只能重启游戏"。根因已修（中止分支改回坐/躺类目，见 cc_part2c.sc），
            //     这里再放一道**与状态机无关**的兜底：万一还有别的路径把坐/躺
            //     转场卡住，主键不会被"转场中禁止开菜单"永久挡住。
            CC_SitPropStuckEscape()

            // ── 状态机 ──
            SWITCH g_state
                CASE ST_IDLE
                    IF g_hasGirl
                        CC_IdleFollowTick()
                    ENDIF
                    CC_IdleMasterKey()
                    CC_MenuNav()
                    BREAK
                CASE ST_APPROACHING
                    CC_ApproachTick()
                    // ★ 见下：菜单导航与 g_state 解耦
                    IF g_menuLevel != ML_NONE
                        CC_MenuNav()
                    ENDIF
                    BREAK
                CASE ST_ANIMATING
                    CC_AnimatingTick()
                    // ★★★ 2026-09-25晚 修"菜单卡死，只能重启游戏"：
                    //
                    //   在 loop 里按 主键(B+右) 会打开 ML_FINISH（结束：拔出 / 内射），
                    //   但**此时 g_state 仍然是 ST_ANIMATING**（动画没停，只是开了个菜单）。
                    //   而 CC_MenuNav() 原来只写在 CASE ST_IDLE 分支里 ⇒
                    //   菜单被画出来了、僵尸侧的输入也已经被屏蔽（g_menuLevel != ML_NONE
                    //   ⇒ g_ccMenuActive = TRUE），可是**没有任何代码在处理这个菜单的按键**
                    //   ⇒ 不能上下选、不能确认、不能退出，只能重启游戏（用户实测）。
                    //
                    //   修法：只要菜单开着，无论 g_state 是什么都要跑导航。
                    //   放在 SWITCH 之后、用 g_menuLevel 做判据，就不会和 ST_IDLE 里的
                    //   那次调用重复（ST_IDLE 分支里那次是"无条件"调用，负责用主键开菜单）。
                    IF g_menuLevel != ML_NONE
                        CC_MenuNav()
                    ENDIF
                    BREAK
            ENDSWITCH

            // ── 菜单绘制 ──
            // ★ SanScript 的 HUD 只保留一帧, 必须每帧重画; 降频会让菜单
            //   10 帧只出现 1 帧 (疯狂闪烁)。僵尸菜单就是每帧画的。
            IF g_menuLevel != ML_NONE
                CC_DrawMenu()
            ENDIF

            // ── HUD 状态 ──
            // ★★★ v0.79：**位置上移**（0.895 → 0.76）。
            //   根因：这一行画在 x=0.5 / y=0.895 = **屏幕下方居中**，
            //   而 `CC_ShowLine2` 走的是 GTA 自带的字幕（`END_TEXT_COMMAND_PRINT`），
            //   位置也在下方居中 ⇒ **两行字叠在一起**（用户报"提示文字和女伴说的话重叠"）。
            //   上移到 0.76：字幕区让开，且不会碰到菜单（最长的设置页到 y≈0.65 就结束）。
            IF NOT IS_STRING_NULL(g_hudText)
                IF GET_GAME_TIMER() < g_hudExpireAt
                    CC_DrawText(g_hudText, 0.5, 0.76, 1.0, 0.45, TRUE, 255, 255, 255, 255, 0)
                ELSE
                    g_hudText = ""
                ENDIF
            ENDIF

            // ── 诊断行（独立位置，故意不让上面的状态消息覆盖它）──
            //   2026-09-25 教训：诊断和状态消息经常在**同一帧**先后写出去，
            //   共用一个变量就是"后写的顶掉先写的" —— 字典探针的黄字当时瞬间
            //   被 "FAILED TO LOAD ANIMATIONS" 覆盖，装机后什么都没看到。
            //   这一行只由 CC_HudDiag 写，普通状态消息碰不到。
            IF NOT IS_STRING_NULL(g_diagText)
                IF GET_GAME_TIMER() < g_diagUntil
                    CC_DrawText(g_diagText, 0.5, 0.845, 1.0, 0.38, TRUE, 255, 215, 90, 255, 0)
                ELSE
                    g_diagText = ""
                ENDIF
            ENDIF

            // ════════════════════════════════════════════════════════════════════
            // ★★ 2026-09-25晚：与僵尸模组的输入互斥标志 —— **必须在本函数末尾算**
            //
            // 【为什么从开头搬到这里】僵尸模组主循环里的顺序是：
            //        MenuInput()          ← 用**上一帧末**的值
            //        CC_MainTick()        ← 本函数（CC_MenuNav 在内部跑、菜单在这里才开/关）
            //        <互动键处理>          ← 用**本帧末**的值（跑在本函数之后！）
            //
            //   原来标志在本函数**开头**算，那时本帧的菜单还没开 ⇒ "开菜单的那一帧"
            //   g_ccHotkeyBlock 仍是 FALSE ⇒ 僵尸侧的互动键当场看到 FALSE ⇒
            //   按 B+十字键右 开菜单时会**顺手触发一次僵尸互动**（招募/开箱/用药…）。
            //
            //   判据取 `menuWasOpen OR g_menuLevel != ML_NONE`：
            //     · 开菜单那一帧 → 末尾 ML_MAIN            ⇒ TRUE（靠后半句）
            //     · 关菜单那一帧 → 开头 ML_MAIN(menuWasOpen) ⇒ TRUE（靠 menuWasOpen）
            //     · 平常闲逛     → 两者皆否                  ⇒ FALSE（僵尸互动照常可用）
            //   **完全在 CC 侧解决，不动僵尸模组一行代码。**
            //
            //   （"先按十字键右、再按 B"这种顺序仍会让僵尸侧先看到一次右键 ——
            //     但那一按**本来就是**僵尸模组自己的互动键，无法也不该由 CC 代它拦。）
            // ════════════════════════════════════════════════════════════════════
            g_ccMenuActive = FALSE
            g_ccHotkeyBlock = FALSE
            IF menuWasOpen OR g_menuLevel != ML_NONE
                g_ccMenuActive = TRUE
                g_ccHotkeyBlock = TRUE
            ENDIF
            IF g_state != ST_IDLE
                g_ccHotkeyBlock = TRUE
            ENDIF
        ENDPROC

// ── CC 初始化（开机一次性） ───────────────────────────────────────────────
PROC CC_ModInit()
    CC_InitTraits()
    g_sinkraAvail = DOES_ANIM_DICT_EXIST("cowgirl@sinkra")
    INT smi = 0
    WHILE smi < 27
        TEXT_LABEL_63 nmv
        nmv = CC_SinkraModelName(smi)
        g_sinkraLowModels[smi] = GET_HASH_KEY(nmv)
        smi = smi + 1
    ENDWHILE
    g_sinkraLowN = 27
ENDPROC
