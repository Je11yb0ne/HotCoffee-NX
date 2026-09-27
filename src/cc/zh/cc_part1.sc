// ============================================================================
// cc_part1.sc — CinnamonCoffee → Switch 端口 主脚本 第1部分
// USING / 常量 / 文件级状态 / 基础辅助 / UI / 语音 / A-Life 注册表 / 定价
// ============================================================================
USING "rage_builtins.sch"
USING "commands_pad.sch"
USING "commands_graphics.sch"
USING "commands_hud.sch"
USING "commands_player.sch"
USING "commands_entity.sch"
USING "commands_ped.sch"
USING "commands_task.sch"
USING "commands_misc.sch"
USING "commands_streaming.sch"
USING "commands_audio.sch"
USING "commands_weapon.sch"
USING "commands_clock.sch"
USING "commands_camera.sch"
USING "commands_vehicle.sch"
USING "commands_stats.sch"
USING "pickup_enums.sch"
USING "commands_object.sch"
USING "commands_script.sch"
USING "cc_dialogue_gen.sc"
USING "cc_vehlists_gen.sc"


CONST_INT ST_IDLE 0
CONST_INT ST_APPROACHING 1
CONST_INT ST_ANIMATING 2
CONST_INT CCM_NONE 0
CONST_INT CCM_CAR 1
CONST_INT CCM_STREET 2
CONST_INT VAT_NORMAL 0
CONST_INT VAT_LOW 1

CONST_INT ML_NONE 0
CONST_INT ML_MAIN 1
CONST_INT ML_SANDBOXCAR 2
CONST_INT ML_SERVICES 3
CONST_INT ML_STANDINGSVC 4
CONST_INT ML_SITTINGSVC 5
CONST_INT ML_LAYINGSVC 6
CONST_INT ML_SITDOWNSUB 7
CONST_INT ML_COMPAT 8
CONST_INT ML_SETTINGS 9
CONST_INT ML_APPROACH 10
CONST_INT ML_APPROACHLINE 11
CONST_INT ML_FINISH 12
CONST_INT ML_ACTIONS 13
CONST_INT ML_LEAVE 14
CONST_INT ML_LEAVELINE 15
CONST_INT ML_CONVERSATION 16
CONST_INT ML_CONVSUB 17
CONST_INT ML_INTIMACY 18
CONST_INT ML_INTIMACYSUB 19
CONST_INT ML_ESCALATE 20
CONST_INT ML_INVITEVEH 21
CONST_INT ML_GIVEMONEY 22

CONST_INT VOICE_INTERVAL 7000
CONST_INT KEY_COOLDOWN_FRAMES 20
// ★ 2026-09-25：菜单上下长按连发（与僵尸菜单同款手感：按住 350ms 后每 120ms 一格）
CONST_INT MENU_REPEAT_FIRST_MS 350
CONST_INT MENU_REPEAT_NEXT_MS 120
CONST_INT HEAL_INTERVAL 3000
CONST_INT HEAL_AMOUNT 2
CONST_INT FINISH_HEAL 1
CONST_INT CC_REP_MAX 100
CONST_INT CC_REP_MIN -2

CONST_INT SLAP_CAR_SEX_SLOW 710
CONST_INT SLAP_CAR_SEX_MID 380
CONST_INT SLAP_CAR_SEX_FAST 250
CONST_INT SLAP_LOW_SEX_SLOW 935
CONST_INT SLAP_LOW_SEX_MID 480
CONST_INT SLAP_LOW_SEX_FAST 350
CONST_INT SLAP_COW_SEX_SLOW 710
CONST_INT SLAP_COW_SEX_MID 380
CONST_INT SLAP_COW_SEX_FAST 250
CONST_INT SLAP_WALL_SEX_SLOW 770
CONST_INT SLAP_WALL_SEX_MID 360
CONST_INT SLAP_WALL_SEX_FAST 250
CONST_INT SLAP_LAY_COW_SLOW 700
CONST_INT SLAP_LAY_COW_MID 280
CONST_INT SLAP_LAY_COW_FAST 190
CONST_INT SLAP_SINKRA_LAY_COW_SLOW 710
CONST_INT SLAP_SINKRA_LAY_COW_MID 380
CONST_INT SLAP_SINKRA_LAY_COW_FAST 250
CONST_INT SLAP_DOGGY_SLOW 620
CONST_INT SLAP_DOGGY_MID 430
CONST_INT SLAP_DOGGY_FAST 275
CONST_INT SLAP_RDOGGY_SLOW 385
CONST_INT SLAP_RDOGGY_MID 250
CONST_INT SLAP_RDOGGY_FAST 180
CONST_INT KISS_SOUND_SLOW 3500
CONST_INT KISS_SOUND_MID 2200
CONST_INT KISS_SOUND_FAST 1200

CONST_INT PERS_SHY 0
CONST_INT PERS_FLIRTY 1
CONST_INT PERS_DOMINANT 2
CONST_INT PERS_COLD 3
CONST_INT PERS_SWEET 4
CONST_INT PERS_GOLDDIGGER 5
CONST_INT PERS_STREETSMART 6
CONST_INT PERS_PARTYGIRL 7
CONST_INT PERS_ROMANTIC 8
CONST_INT PERS_SARCASTIC 9
CONST_INT PERS_NEEDY 10
CONST_INT PERS_INDEPENDENT 11
CONST_INT PERS_JEALOUS 12
CONST_INT PERS_CHAOTIC 13
CONST_INT PERS_MANIPULATIVE 14
CONST_INT PERS_AGGRESSIVE 15
CONST_INT PERS_PLAYFUL 16
CONST_INT PERS_MYSTERY 17
CONST_INT PERS_CLASSY 18
CONST_INT PERS_UNSTABLE 19

INT FRIENDLINESS[20]
INT CONFIDENCE[20]
INT AGGRESSIVENESS[20]
INT PATIENCE[20]
// ★★★ v0.72（D 类接线）：**补齐 C# PersonalityProfile 剩下的三项**
//   C# 20 个性格的档案是 7 个属性（C# 162-181）：
//     Friendliness / Confidence / Greed / Patience / Riskiness / Attachment / Aggressiveness
//   本端口原来只移植了 4 个（前四项 + Aggressiveness）⇒ 用到
//   `Greed` / `Riskiness` / `Attachment` 的功能**全都没法接**（D 类 26 个家族里的一大半卡在这）。
//   这里按 C# 原值 ×100 补进来（和已有四个数组**同一约定**，例如 Shy 的
//   Friendliness 0.55 → FRIENDLINESS[0] = 55，一一对应，可直接比对 C# 第 162-181 行）。
INT GREED[20]
INT RISKINESS[20]
INT ATTACHMENT[20]

PED_INDEX g_player
PED_INDEX g_girl
PED_INDEX g_approachCand
VEHICLE_INDEX g_car
BLIP_INDEX g_girlBlip
BOOL g_girlBlipValid = FALSE
OBJECT_INDEX g_dildo

INT g_state = ST_IDLE
INT g_mode = CCM_NONE
INT g_vehicleAnimType = VAT_NORMAL
BOOL g_hasGirl = FALSE
BOOL g_playerWasInVeh = FALSE
BOOL g_backseatEntryPending = FALSE
BOOL g_backseatCarSex = FALSE
// ★★★ v0.78：「她已同意上我的车」—— C# 的 `_invitedToVehicle`（431，说明见 7709）。
//   会话级：一旦请成功就一路免请（之后你上车她自动跟）；
//   清零只在 `CC_FullReset`（= 放生/收尾，对齐 C# 的 17980）。
BOOL g_vehInvited = FALSE

INT g_appPhase = 0
INT g_jHoldStart = 0
INT g_appAnimWait = 0
INT g_appSexySwap = 0
INT g_appDecision = 0
INT g_appDict = 0
VECTOR g_appTarget
FLOAT g_appHeading = 0.0
BOOL g_prostAutoApproachActive = FALSE
INT g_prostAutoApproachCd = 0

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.61：原版站街女模型识别 —— 用来**避开**原版 GTA 的嫖娼脚本
//
//   【为什么要这个东西】（根因，2026-09-26 查官方脚本源码确认）
//     卖淫(A-Life)模式选女伴时走的是 CC_FindNearestFemale —— 附近**任何**
//     站立女性都会被选中，于是经常会选中**原版街边的站街女**
//     （模型 `s_f_y_hooker_01..03`，走的是游戏的 PEDTYPE_PROSTITUTE）。
//
//     这类 ped 身上**一直挂着一个原版脚本线程** `pb_prostitute`
//     （源码：singleplayer/scripts/Ambient/Peds/pb_prostitute.sc，
//       状态机/相机在 include/private/Ambient/Prostitutes/ 下）。
//     只要玩家**坐在车里**且离她够近，那个脚本就会自动接管（不需要按喇叭）：
//       · pb_prostitute_helpers.sch  UPDATE_PLAYER_CALL_HOOKER()
//           → TIMER_DO_ONCE_WHEN_READY(tProstituteApproach, CONST_fHookerComeOverDelay)
//       · 然后它自己发 `TASK_ENTER_VEHICLE(prostitute_ped, iPlayersVehicle, VS_FRONT_RIGHT)`
//       · 进了车 → PROSTITUTE_IN_VEHICLE → DRIVING_TO_SECLUDED_SPOT
//       · 选完服务 → MANAGE_SEX_ANIMS_IN_CAR() → INITIALIZE_SEX_CAMS_WITH_BONE_CHECKS()
//           → pb_prostitute_cams.sch 里 CREATE_CAM("DEFAULT_SCRIPTED_CAMERA")
//             + SET_CAM_ACTIVE + RENDER_SCRIPT_CAMS(TRUE,FALSE)
//       ⇒ **视角被剧本相机接管并锁死**（就是"变成原版找站街女服务的样子"）。
//
//     这一个根因同时解释用户报的四个现象：
//       ① 选服务时视角变成原版样子且锁死   ← 原版 swivel 相机
//       ② 车还没上就莫名其妙又走开         ← 两个脚本对同一个 ped 反复发任务，互相打断
//       ③ 要请好几次才上车                 ← 同上
//       ④ 刚一上车就尖叫下车跑走           ← 原版 PROSTITUTE_YELL_BEFORE_LEAVING /
//                                            MANAGE_PLAYER_ATTACKING_PROSTITUTE 的"受惊"分支
//
//   【修法（用户 2026-09-26 选定：零风险方案）】
//     认领目标时**直接跳过原版站街女**，只找普通女性。
//     这样原版脚本从头到尾就没管过我们的女伴 ⇒ 四个现象一次性消失。
//     代价：搭讪模式下不能搭讪"站街女"，这是刻意的取舍。
//
//   【为什么不用 SET_ENTITY_AS_MISSION_ENTITY 解决】
//     查过原版脚本：主状态机（pb_prostitute.sc 的 WHILE/SWITCH）**根本不检查**
//     IS_ENTITY_A_MISSION_ENTITY —— 它只在 CLEANUP/特殊 Michael 分支里查。
//     而且它自己也会调 SET_ENTITY_AS_MISSION_ENTITY(prostitute_ped,TRUE,TRUE)。
//     所以"标成剧本实体"挡不住它。（本端口 cc_part2a.sc 认领时那一条保留，
//     对**普通 NPC** 仍有意义：防止她们被别的环境系统收回。）
//
//   【hash 表怎么来的】joaat("s_f_y_hooker_%02d")，i = 1..99，与 C# 原版
//     `_hookerModelHashes`（CinnamonCoffee.cs 647-649）完全一致（它也 01..99）。
//     真正存在的模型只有 01/02/03，其余 hash 落在空处、不会有 ped 命中 ——
//     照抄 C# 是为了"对得上就别改"，也顺手防住以后换模型名。
// ════════════════════════════════════════════════════════════════════════════
CONST_INT CC_HOOKER_HASH_N 99
INT g_hookerHash[99]
BOOL g_hookerHashReady = FALSE
// 最近一次 CC_FindNearestFemale 扫描里，是否**只**因为"她是原版站街女"而跳过了目标。
// 用途：全跳过时给玩家一句解释，而不是干巴巴的"附近没有女性"。
BOOL g_lastScanSkippedHooker = FALSE

INT g_menuLevel = ML_NONE
INT g_menuIndex = 0
INT g_leaveCluster = 0
BOOL g_negotiate = FALSE
INT g_svcSubParent = ML_NONE
INT g_streetSitCat = ML_NONE

// ★★★ v0.72：**默认改成「搭讪模式」**（用户要求）。
//   TRUE = 沙盒（走近即上场、不搭讪）/ FALSE = 搭讪（**先主动搭讪**，走完整 A-Life 流程）。
//   搭配下面 `g_aLifeMode = TRUE` ⇒ 开机就是"完整体验"：
//   个性 / 声望 / 话题 / 关系推进 / 搭讪成功率 全部生效，主菜单也从「服务」变成「互动」。
//   ★ 注意：搭讪模式下**不能搭讪原版站街女**（`s_f_y_hooker_01..03` 身上常驻
//     `pb_prostitute` 会抢控制权，见 `CC_FindNearestFemale` 的跳过逻辑）—— 刻意的取舍。
BOOL g_sandboxMode = FALSE

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ 2026-09-26 v0.58 修复：`g_aLifeMode` 原来**恒为 FALSE** ⇒ 整层 A-Life 从没启用
//
//  【怎么发现的】用户问"这模组有什么玩法"，回答时顺手 grep 了一下它的写入侧：
//      grep -rnE "g_aLifeMode\s*=" src/     ⇒  只有这一行声明，全工程**从没赋过值**。
//      而它被读 38 次 ⇒ 所有 `IF g_aLifeMode …` 恒假。
//
//  【根因】C# 原版的主菜单有**两个独立设置项**（CinnamonCoffee.cs 7013-7017）：
//      case 3: AI Mode   →  aLifeMode    = !aLifeMode    守卫: if (!hasGirl)
//      case 4: Game Mode →  sandboxMode  = !sandboxMode  守卫: if (!(hasGirl && aLifeMode))
//  分别对应标签 "AI Mode: A-Life/Sandbox" 与 "Game Mode: Casual/Prostitution"。
//  本端口当初只移植了 Game Mode（设置菜单的「模式: 沙盒/卖淫」那一项），
//  把 **AI Mode 整条丢掉了** ⇒ 默认值停留在 FALSE，再没人翻过它。
//  （这正是工程记忆第一条"总开关全局量没被赋值"的又一实例。）
//
//  【恒假掉的东西 —— 也就是用户觉得"没什么玩的"的真因】
//    · 主菜单「互动」项**永不出现** ⇒ 聊天/亲密/升温/邀请上车/给钱/离开 整层够不到
//    · `CC_Pay` 里 `IF g_sandboxMode OR NOT g_aLifeMode → RETURN TRUE` ⇒ 卖淫模式也**免费**
//    · 声望 `g_alRep` 永远 0 ⇒ 8 档关系永不推进，搭讪/升温的成功率恒定不变
//    · `AL_GetOrCreate(ped, prost = NOT g_sandboxMode AND g_aLifeMode)` 恒 prost=FALSE
//      ⇒ **所有女性都被当成良家**，hooker（街女）分支永不进入
//    · 情绪表情 / 死亡掉落现金 / 回头客记账 等一整串后果分支全部不执行
//
//  【为什么改成默认开】A-Life = 完整个性 + 声望 + 话题 + 收费，是这个模组**最厚**的一层；
//    在默认的「沙盒」游戏模式下它**纯属增量**（沙盒模式收费仍被 sandboxMode 短路成免费，
//    见 CC_Pay 第一句），没有任何副作用。用户可随时在设置菜单里关回去。
//    （C# 原版默认是 false，这里为照顾"上来就有得玩"改成 TRUE —— 是**有意的偏离**，不是漏移植。）
BOOL g_aLifeMode = TRUE
BOOL g_showTut = TRUE

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ 2026-09-25晚 v0.57 新增：热咖啡**总开关**
//
//  用户原话："再做个热咖啡模组的整体开关，不然的话想让 npc 不跟着我只能把 npc 杀了，
//            关闭模组后原本的伴侣变成普通 npc。"
//
//  【语义】g_modOn = FALSE 时，整个模组**休眠**：
//     · CC_MainTick 早早 EXIT ⇒ 不做任何事（不搭讪、不跟随、不巡逻、不占输入）
//     · 关闭的**那一刻**调 CC_FullReset(FALSE) 把当前女伴"放生"成普通 NPC
//       （清任务、交还所有权、移标点 —— 就是 C# 的 ReleaseGirl 语义）
//     · 僵尸侧的按键照常可用（不再被本模组屏蔽）
//
//  【★ 为什么必须留一条"再打开"的路】模组休眠后菜单是打不开的，如果不留入口，
//    用户关掉之后**再也开不回来**，只能重启游戏。所以休眠期间
//    **B + 十字键右 仍然有效 —— 按一下就把模组重新打开**（并在屏幕上给一句提示）。
//
//  【位置】设置菜单**最后一项**「热咖啡模组: 开/关」。
//    不在文件 / 不在机器上持久化 —— 本端口没有文件 IO，重启游戏后回到"开"。
BOOL g_modOn = TRUE

// ── 诊断行总闸（v0.57 起默认**关**）──────────────────────────────────────────
//  用户要求"删除测试显示的各种提示"。这里不是把十几处诊断调用点删掉，而是
//  在**唯一出口** CC_HudDiag 上装一个闸：默认 FALSE ⇒ 屏幕上一条诊断都不出现，
//  而全部调用点原样保留。以后要排查问题，把这里改成 TRUE 即可，不用改十几处代码。
BOOL g_showDiag = FALSE
// ★★ 2026-09-25晚 关于「坐姿/躺姿遁地」的最终结论 —— 别再把它当 bug 单独修
//
//   「沉到地面以下」本身是**原版设计**：坐姿/躺姿动作故意给负 zOffset
//   （-0.15 ~ -0.95）并关掉碰撞，靠动画自身的根位移把人的视觉高度拉回地面
//   （C# 注释原话：car-seat animation lifts them back up to ground level
//    visually；同一技巧见 GTA IV HotCoffee 的 zOffset = -2.12f）。逐条核对过，
//   本端口的 zOffset 数值与 C# 完全一致 —— 所以**沉不是缺陷**。
//
//   真正让用户看到「沉到地下 ≈2 米 + 完全不动 + 退出时自由落体」的是另一件事：
//   ★ g_mode 从来没有被赋过 CCM_CAR / CCM_STREET（移植时把 C# OpenServicesMenu
//     里的 `mode = Mode.Car / Mode.Street` 整段弄丢了，全工程只剩 `= CCM_NONE`）。
//   连锁后果（一次解释全部现象）：
//     · CC_AnimatingTick 里 IF g_mode = CCM_CAR / ELIF g_mode = CCM_STREET 两个
//       分支**都进不去** ⇒ 没有任何 tick 在跑 ⇒ 相位机永远停在 phase 0；
//       而 intro 用的是 flag 2 = HOLD_LAST_FRAME ⇒ 视觉上"播一下就定住不动"。
//     · CC_RunCarAnimation 只在 g_mode = CCM_CAR 时才被调用 ⇒ 车内的诊断行
//       从来没有被打印过（用户："没有车内的诊断行"）。
//     · CC_AnimCommonTick 的呻吟/速度/治疗全部以 g_animPhase = 2 为前提 ⇒
//       相位到不了 2 ⇒ **全程静音**。
//     · 中止分支 IF g_mode = CCM_CAR ... ELSE g_menuLevel = ML_STANDINGSVC
//       只会走 ELSE ⇒ 车里退出动作回退到"亲吻/口交/靠墙式"5 项街面页，
//       在车里再选它们就报"车内做不了"（用户原话）。
//     · CC_EndAnimation 的 CCM_STREET 分支（恢复碰撞 → 传送回 g_animOrigin
//       → 清任务 → 最后解冻）进不去 ⇒ 人还沉在地下就被解冻 ⇒ 自由落体。
//
//   ⇒ 修法 = 在动作入口把 g_mode 补上（CC_StartCarAction / CC_StartStreetAction），
//     **不需要**"贴地开关"去绕开设计。
//   （这里原来放过一个 `BOOL g_groundFix = TRUE`，零读点、纯死开关，已删 ——
//     留着只会误导：好像"遁地"是靠它解决的。删了才能逼着看真因。）

// ★ 动画相位诊断的"上一次值"：只在相位/模式**变化**的那一帧打印一次。
//   每帧打印会把 CC_AssignVoice 的 "VOICE ok" 这类一次性诊断当场顶掉 ——
//   这正是用户反馈"看不到音频诊断、只有动作诊断"的真因。
INT g_diagPhase = -1
INT g_diagMode = -1
// ★ 2026-09-26 v0.59：音频诊断的"状态记忆"。只在其**变化**时写 g_diagText，
//   免得每帧顶掉同帧别的诊断（本工程 g_diagText 是单通道，见 CC_HudDiag）。
//     -1 = 还没进过 loop 相位    0 = 呻吟链路正常
//      1 = 速度被拖成 0（静音）   2 = 深喉锁定期（原版故意不发声）
INT g_diagAudioState = -1

BOOL g_dildoOn = TRUE
BOOL g_bjSoundsOn = TRUE
BOOL g_slapSoundsOn = TRUE
BOOL g_streetSitAnimOn = TRUE
BOOL g_intLightOn = TRUE
BOOL g_carEngineOn = TRUE
BOOL g_driveable = FALSE
BOOL g_voiceSvc = TRUE
BOOL g_voiceMoan = TRUE
BOOL g_voiceEnd = TRUE

// ★★ 2026-09-25 新增：热咖啡设置第 13 项「警察无视」
//
//   【为什么加】用户实测："和女伴在一起的时候即使触发警星，警察也不会攻击我"。
//   真凶**不在**本模组的"通缉"逻辑里，而是 CC_AppPhase3 / CC_ApproachLine 在
//   女伴加入的那一刻调了 `SET_EVERYONE_IGNORE_PLAYER(TRUE)`（C# 12425 原版也这样）
//   —— 这个 native 让**所有 ped**（不止警察，是所有人）无视玩家。
//   C# 只在动作收尾里复位它，所以"女伴跟着你闲逛"期间它一直是 TRUE。
//
//   【用户拍板的语义】默认**开启**（= 保持原版/当前行为，女伴在旁时警察无视你）；
//   想被警察通缉/攻击就把这一项调成"关闭"。
//
//   【实现纪律】状态设定类调用**只在变化时做一次**（见 CC_ApplyPoliceIgnore）——
//   不写"每帧/每 5 秒重设一遍"，免得和别的修改器（MEGATARD）互相顶。
BOOL g_policeIgnore = TRUE
INT  g_policeApplied = -1        // -1 = 尚未应用过；0 = 已设 FALSE；1 = 已设 TRUE

// 菜单上下长按连发的下次触发时刻（僵尸菜单那套已验证的写法）
INT  g_menuRepeatAt = 0

// ★ 2026-09-25 新增：搭讪菜单状态（对应 C# _approachSelectedCluster / firstEncounter）
//   簇: 0=温柔 1=自信 2=高冷 3=奉承 4=俏皮（表在 cc_applines.sc 的 CC_AppLine*）
//   g_appFirstEnc: TRUE = 初次见面(4 条台词) / FALSE = 回头客(3 条)
INT  g_appCluster = 0
BOOL g_appFirstEnc = TRUE

BOOL g_sitPropActive = FALSE
INT g_sitPropPhase = 0
INT g_sitPropWait = 0
VECTOR g_sitOrigin
FLOAT g_sitOriginH = 0.0
INT g_sitReturnMenu = ML_NONE
INT g_sitReturnIdx = 0
INT g_streetFadeAt = 0

BOOL g_carPropActive = FALSE
INT g_carPropPhase = 0
INT g_carPropWait = 0
INT g_carPropPendingMenu = ML_NONE
INT g_carPropReturnMenu = ML_NONE
INT g_carPropReturnIdx = 0

TEXT_LABEL_63 g_mDict = ""
TEXT_LABEL_63 g_mAnim = ""
TEXT_LABEL_63 g_fDict = ""
TEXT_LABEL_63 g_fAnim = ""
TEXT_LABEL_63 g_curAction = ""
INT g_animPhase = 0
INT g_animWait = 0
FLOAT g_animSpeed = 1.0
FLOAT g_femSpeedScale = 1.0
FLOAT g_defDevIntensity = 0.20
BOOL g_devExcluded = FALSE
INT g_streetActionIdx = -1

BOOL g_finExecPending = FALSE
INT g_finExecAt = 0
BOOL g_finInside = FALSE
BOOL g_finForced = FALSE
BOOL g_finAsked = FALSE
BOOL g_finBroke = FALSE
BOOL g_finFreePass = FALSE

BOOL g_kissingRev = FALSE
BOOL g_rdoggyRev = FALSE
BOOL g_standBJRev = FALSE
BOOL g_sitCowPullRev = FALSE
BOOL g_carPullRev = FALSE
INT g_standBJSwallowAt = 0
BOOL g_throatLocked = FALSE
INT g_throatLockAt = 0
INT g_throatEndAt = 0
BOOL g_bjThroatPending = FALSE

INT g_keyCooldown = 0
INT g_speedCooldown = 0
// ★ v0.61：上车邀请的重试窗口。玩家上车那一刻起 20 秒内，如果她还站在车外没动，
//   CC_IdleFollowTick 会再发一次 TASK_ENTER_VEHICLE（原来只在"玩家刚上车"那一帧发一次，
//   她那次没走上就再也没人管 ⇒ 用户实测"要多邀请一两次才会上车"）。
//   用**时间窗**而不是无限重试，避免她被原版系统踹下车之后陷入"上车→被踹→再请"的死循环。
INT g_vehInviteUntil = 0
INT g_vehInviteAt = 0
INT g_lastVoice = 0
INT g_lastShake = 0
INT g_lastHeal = 0
INT g_lastGtaSpeech = 0
INT g_lastSlap = 0
INT g_lastKiss = 0
INT g_lastRumble = 0
BOOL g_rumblePulse = FALSE
BOOL g_finBurstFired = FALSE

BOOL g_healPlayer = FALSE
BOOL g_healGirl = FALSE
FLOAT g_healRate = 1.0

INT g_lastPaid = 0

TEXT_LABEL_63 g_girlVoice = ""
INT g_voiceReapplyAt = 0
INT g_faceReapplyAt = 0

FLOAT g_girlLastVit = -1.0
PED_INDEX g_pendingCashPed
INT g_pendingCashAmt = 0
INT g_pendingCashKey = -1
INT g_pendingCashWeapon = 0

INT g_shakeOffAt = 0

TEXT_LABEL_63 g_hudText = ""
TEXT_LABEL_63 g_appDictStr = ""
INT g_hudExpireAt = 0

// ── 诊断行（★ 独立于 g_hudText，故意不让普通状态消息覆盖它）──────────────
//   2026-09-25 教训：诊断信息和普通状态消息经常在**同一帧**先后写出去，
//   共用 g_hudText 的话后写的会把先写的顶掉 —— 当时字典探针的黄字被紧接着的
//   "FAILED TO LOAD ANIMATIONS" 瞬间覆盖，用户什么都没看到，白查一轮。
//   所以诊断走独立的一条，画在屏幕另一个位置（y≈0.845，状态行在 0.895）。
TEXT_LABEL_63 g_diagText = ""
INT g_diagUntil = 0

// ── 动作开始前玩家的原始位置 / 朝向 ─────────────────────────────────────
//   为什么需要（2026-09-25）：街面坐姿动作会**故意把人沉到地面以下**
//   （zOffset -0.15~-0.95，原版靠动画做视觉补偿）。原版退出时解冻即可，
//   因为 PC 上补偿到位；本端口补偿没生效，人还在地下，一旦解冻就往下掉。
//   所以退出时必须**主动把人拉回原地面位置**，再解冻。
VECTOR g_animOrigin = <<0.0, 0.0, -9000.0>>   // z = -9000 表示"还没记过"
FLOAT  g_animOriginH = 0.0

INT g_postSexPhase = 0
INT g_postSexWait = 0
// ★ 2026-09-25晚：post-sex 相位 1 的**时间兜底**截止时刻。
//   相位 1 的唯一推进条件是 IS_AMBIENT_SPEECH_PLAYING —— 那是**本端口未证明**的
//   native。若它恒返回 TRUE，相位永远停在 1 ⇒ `g_postSexPhase > 0` ⇒
//   "语音序列中禁止开菜单"永远生效 ⇒ **主键永久失效**。见 CC_PostSexVoiceTick。
INT g_postSexDeadline = 0
INT g_postSexMenu = ML_NONE

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.63 菜单/输入三项新增状态
// ════════════════════════════════════════════════════════════════════════════

// ① 「动作结束后把菜单还给玩家」——记下这个动作是从哪个菜单页启动的。
//
//   【为什么要有这个】用户报："现在开启动作或别的什么时候，菜单会自己关闭，
//     经常要重新打开菜单"。查 C# 原版发现：服务结束后 C# 会**主动重开服务菜单**
//     （CinnamonCoffee.cs 15309 `menuLevel = MenuLevel.Services;`，
//      配合 `_postSexPendingMenu = MenuLevel.Services`）。
//     而本端口的对应变量 `g_postSexMenu` **从来没有被赋过非 ML_NONE 的值**
//     （只有声明、CC_FullReset 复位、CC_PostSexVoiceTick 读取三处）——
//     ⇒ 动作一结束 `g_menuLevel = g_postSexMenu = ML_NONE` ⇒ **菜单关掉**，
//       玩家必须重新按 主键 打开。这正是"总开关从没被赋值"那一类移植漏项。
//
//   现在：动作启动时把当时的菜单页记在这里（见 CC_StartCarAction/StartStreetAction），
//         CC_EndAnimation 结束后用它重开菜单（C# 同款语义）。
INT g_actionReturnMenu = ML_NONE

// ② 菜单"短暂不接受输入"的截止时刻（**不阻塞脚本**）。
//
//   【为什么要有这个】原来这类"说完一句台词再切页"的地方用的是 `WAIT(3200)` ——
//     SanScript 的 WAIT 会**冻住整个脚本线程**，而 HUD 只保留一帧 ⇒
//     这 3.2 秒里菜单**根本没被重画** ⇒ 玩家看到的就是"菜单自己关闭了"
//     （其实只是停止绘制），而且这段时间按键全部不响应。
//   现在：不阻塞。菜单照常每帧绘制，只是这段时间内 `CC_MenuNav` 不读导航键
//        （避免把"刚按下的确认键"又当成一次导航）。
INT g_navLockUntil = 0

// ③ 调速键"开场免疫"截止时刻（v0.64，用户要求：改成"开始后 0.5 秒内免疫"）。
//
//   【为什么要有这个】用户报"有时候还是没有声音"。机制：
//     调速键是 d-pad ←/→ 且**长按生效**（IN_LeftHeld/IN_RightHeld），
//     而菜单的"确认"也是 ←/→。v0.62 把菜单在动作开始时关掉之后，
//     这一按就**直接落到调速上**了 ⇒ 玩家确认服务时压着的那一下
//     会把 g_animSpeed 一路拖到 0 ⇒ `IF g_animSpeed > 0.0` 那一支整个跳过
//     ⇒ **全程静音**（换一个女伴有声——因为 CC_FullReset 把速度复位成 1.0）。
//   规则：动作开始后 **500 ms 内完全忽略调速键**（g_speedImmuneUntil）。
//        v0.63 原先用的是"必须先把 ←/→ 松开过一次才上膛"，用户觉得别扭，改成时间窗。
INT g_speedImmuneUntil = 0

// ④ ★★★ v0.64 临时音频探针（音频问题定性用，确认正常后删掉）。
//
//   用户报"有的人有声音、有的人没有"，而本端口的发声逻辑与 C# 原版**逐行一致**
//   （同一个嗓音名表 CC_VOICE_NAME、同一个 native 组合）⇒ 静态查不出差别。
//   所以在**每个女伴第一次发出呻吟指令的那一刻**，把她的嗓音名打到屏幕上：
//     · 两个女伴显示**同一个名字**却一个有声一个没声 ⇒ 不是嗓子的问题，往引擎侧查
//     · 没声的那个显示的是**另外几个名字** ⇒ 就是那几个嗓子在本机取不到音频数据，
//       改成只用能用的那几个即可
BOOL g_voiceProbeShown = FALSE

// ④b v0.65：探针的"打印请求"标志。cc_part2c 里（那时 CC_MenuLabel 还没定义，
//   拿不到开关文案）只负责置位；真正那行文字在 cc_main.sc 的 CC_AudioProbeTick 里打 ——
//   那里所有函数都已定义，可以把**嗓音名 + 呻吟语音开关状态 + 当前动作**一次全打出来。
BOOL g_audioProbeWant = FALSE

// ════════════════════════════════════════════════════════════════════════════
// ⑤ ★★★ v0.66 「没声音」的**自愈**：嗓子自动轮换
//
//   用户实测（关键数据）：3 个女伴里 1 个没声音，**而且那个没声音的第一次播动画时
//   屏幕上的探针字是出来了的** ⇒ 说明：
//     · 呻吟那一支**确实跑到了**（开关是开的、phase 到 2、速度 > 0）
//     · `CC_PlaySpeech` 也确实发了 `PLAY_PED_AMBIENT_SPEECH_WITH_VOICE_NATIVE`
//   ⇒ **问题不在我们的逻辑，而在"这个嗓子名 + 这个 ped"在引擎里发不出声**。
//
//   静态查不出是哪一个（嗓音名表与 C# 原版逐字一致）。所以改成**运行期自愈**：
//     发完一次呻吟后 ~0.4 秒看 `IS_AMBIENT_SPEECH_PLAYING(ped)`；
//     没在播 ⇒ 换下一个嗓子名重设，最多轮 9 次；一旦检测到在播就**锁定**不再换。
//   这样：无论 9 个嗓音里哪几个在本机取不到音频数据，她都会在几次呻吟内自己
//   落到一个能出声的嗓子上；同时屏幕上会打出"换了哪个嗓子"，方便定位。
//
//   ⚠ `IS_AMBIENT_SPEECH_PLAYING` 在本端口属"未证明"native。若它恒 FALSE，
//     表现是"每隔一次呻吟换一个嗓子（最多换 9 次后锁定）"—— 声音会变，但不会更糟。
//     若它恒 TRUE，就等价于不轮换（与旧行为一致）。
//
//   ★★★ v0.71：**默认关闭这套自动轮换**（`g_voiceAutoRotate = FALSE`）。
//     理由（用户明确要求："不是每句都随机，就是每个人单独随机"）：
//       · 轮换是**动作进行中**改 `g_voiceIdx` ⇒ 同一个女伴在同一次互动里
//         声音会**变来变去** —— 正是用户说的"每句都随机"，是**反效果**。
//       · 它依赖 `IS_AMBIENT_SPEECH_PLAYING`。要是这个 native 在本机恒 FALSE，
//         它就会每次呻吟换一个嗓子，而且**可能换来换去最后锁在一个坏嗓子上**
//         （第 9 次轮完 `g_voiceLocked = TRUE`）⇒ 越修越糟。
//       · 现在已经有**玩家自己挑**的入口（设置第 7 项「女伴嗓音」，当场试听），
//         比"猜"可靠 ⇒ 自愈这套没必要再自动跑。
//     保留代码与开关：真要用回自动轮换，把这里改成 TRUE 即可（比如将来确认
//     `IS_AMBIENT_SPEECH_PLAYING` 在 NX 上可靠）。
INT  g_voiceIdx      = -1     // **当前这位女伴**的嗓音下标 0..8（-1 = 还没定 ⇒ 下次 CC_AssignVoice 会摇一次）
INT  g_voiceRotate   = 0      // 已轮换次数（封顶 9）
INT  g_voiceCheckAt  = 0      // 下次检查"有没有真在播"的时刻（0 = 不检查）
BOOL g_voiceLocked   = FALSE  // 已经确认能出声 ⇒ 不再轮换
BOOL g_voiceAutoRotate = FALSE // ★ v0.71：自动轮换总开关（默认关 —— 见上）

// ★★★ v0.70：玩家在设置第 7 项「女伴嗓音」里**钉死**的序号。
//   -1（默认）= "随机（每人不同）" ⇒ 每次换人都现摇一个 0..8 —— 这就是用户要的
//   "不是每句都随机，就是每个人单独随机"。
//   ≥ 0 = 全局统一用这一个（用途：在你这台机器上试出"哪几个嗓子能出声"之后钉住它，
//         免得随机到不出声的那几个）。**换女伴不会重置它**。
INT  g_voiceFix      = -1

BOOL g_sinkraAvail = FALSE
BOOL g_sinkraLay = TRUE
BOOL g_sinkraRevLay = TRUE
BOOL g_sinkraFace = TRUE
INT g_sinkraLowModels[28]
INT g_sinkraLowN = 0
VECTOR g_sinkraOffCow = <<0.02, 0.08, 0.56>>
VECTOR g_sinkraOffRevCow = <<0.02, -0.075, 0.56>>
VECTOR g_sinkraOffFace = <<-0.05, -0.50, 0.60>>
VECTOR g_wallBangOff = <<0.02, 0.36, 0.145>>

CONST_INT AL_MAX 20
INT g_alFP[AL_MAX]
BOOL g_alUsed[AL_MAX]
TEXT_LABEL_15 g_alName[AL_MAX]
INT g_alPers[AL_MAX]
INT g_alRep[AL_MAX]
INT g_alMood[AL_MAX]
INT g_alVoice[AL_MAX]
INT g_alDict[AL_MAX]
INT g_alCash[AL_MAX]
INT g_alStored[AL_MAX]
BOOL g_alHooker[AL_MAX]
// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.74：**去妓女化**总开关（用户 2026-09-27 定的方向）
//
//  【用户原话】"不想做成妓女的那种，既然都有好感度了，做成炮友的那种比较好，不要收费的玩法。"
//    ⇒ 保留玩家选模式的权力（沙盒仍然"上来就能开干"，不改），
//      只把**搭讪模式**里"她算不算收费街女"这一层改掉。
//
//  【它到底控制什么】只控制 **`g_alHooker[]` 这个标记**（=`AL_GetOrCreate` 的 `prost` 参数）。
//    三处调用点都写成 `(... ) AND NOT g_alNoHooker`。
//    打开（TRUE）后，A-Life 女伴一律按 **Casual（休闲）规则集**处理：
//      · 告别菜单 3 句 → **4 句**（第 3 簇「奉承」下多出第 4 项「我爱你。」）
//      · 告别台词用 `leaveLines`（休闲）而不是 `prostLeaveLines`（街面）
//      · 收尾不再走"内射要价 $100 / 没钱→强行"那条**计费链路**
//      · 给钱接受率不再有"交易型"加成；人生目标走非街女池子
//
//  【它不影响什么】
//    · **沙盒模式完全不受影响** —— 沙盒本来就是 `prost=FALSE`（"上来就能开干"照旧）。
//    · 不影响"搭讪模式必须先从打招呼开始"这件事 —— 那是由 `g_sandboxMode` 决定的，
//      跟本开关无关。（用户实测确认过这个行为是对的，要保持。）
//
//  【怎么回退】改成 FALSE 即回到 v0.73b 行为（搭讪模式下她们是收费街女）。
// ════════════════════════════════════════════════════════════════════════════
BOOL g_alNoHooker = TRUE
// ★ v0.76：允不允许"声望 ≥ 95 ⇒ 痴迷"（`RST_OBSESSED`）。
//   ☠ C# 的**休闲阶梯没有这一档**（`Obsessed` 只在卖淫阶梯出现）⇒ 这是**我加的**。
//   不想要就改成 FALSE（一行回退，其余关系逻辑不受影响）。
BOOL g_alObsessOn = TRUE
BOOL g_alMet[AL_MAX]
BOOL g_alMet2[AL_MAX]
INT g_alTopics[AL_MAX]
INT g_alCim[AL_MAX]
INT g_alPrefBJ[AL_MAX]
INT g_alPrefRough[AL_MAX]
INT g_alGoal[AL_MAX]
INT g_alRel[AL_MAX]
// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.75：**关系状态**（对应 C# 的 Casual 阶梯）
//
//  【为什么另开一个数组，而不是改 g_alRel】
//    `g_alRel[]` 是**声望档（0..7）**，它唯一的用途是给"道别台词"当索引
//    （`CC_LEAVEPROST_*_H(relIdx)`、`tIdx = relIdx*3 + lnIdx`）。
//    而关系状态要表达的是**质变**（暧昧 / 女友 / 朋友区…），不是声望高低。
//    两个都留着：`g_alRel` 管台词索引，`g_alRSt` 管关系玩法。**别混用。**
//
//  【数值】负数是负向态；0..3 是按声望自动升的基础态；4..7 是进阶态
//    （**一旦置上就锁定**，声望回落不会把它们冲掉）。
//    对照 C# `UpdateCasualRelationshipState`（CinnamonCoffee.cs:16620-16651）。
CONST_INT RST_HOSTILE -2      // 敌视（声望 ≤ -2，且**不可逆**——只靠声望回不来）
CONST_INT RST_AVOIDING -1     // 回避（声望 = -1；退出时会恢复"上一次的正向态"）
CONST_INT RST_STRANGER 0      // 陌生人
CONST_INT RST_ACQUAINT 1      // 认识
CONST_INT RST_FAMILIAR 2      // 熟识
CONST_INT RST_FRIENDLY 3      // 友好
CONST_INT RST_FLIRTY 4        // 暧昧   ←「调情」成功才会到这一档
CONST_INT RST_FRIENDZONE 5    // 朋友区 ←「表白」失败 / 调情失手
CONST_INT RST_GIRLFRIEND 6    // 女友   ←「表白」成功（同时清掉"街女"标记）
CONST_INT RST_OBSESSED 7      // 痴迷   ← 声望 ≥ 95

INT g_alRSt[AL_MAX]
// "上次的正向关系态" —— C# 的 `LastPositiveRelationship`。
//   声望掉到 -1 会进"回避"，声望涨回来时要**恢复**到掉之前那一档，
//   而不是退回"陌生人"（否则辛苦刷的关系一掉就没了）。
INT g_alRStLast[AL_MAX]
// ★ v0.76：她答应「要不要保持这种随便的关系」了没有（0=没问过 / 1=答应了）。
//   对应亲密子菜单分支 0 的第 8 项。目前只用于**显示与记账**，
//   不做惩罚逻辑（C# 那边也没做）。
INT g_alCasualOK[AL_MAX]
// ★ v0.76：亲密子菜单当前在看哪个分支（0=问她的偏好 8 项 / 1=试探一下 3 项）
INT g_intSubBranch = 0
INT g_curAlIdx = -1

INT g_prPrice[100]
BOOL g_prLock[100]
BOOL g_prAvail[100]
INT g_aLifeFinPrice = 0
BOOL g_appRejCasual[AL_MAX]
BOOL g_appRejSet[AL_MAX]
BOOL g_aLifeHookerSvc = FALSE
INT g_menuDrawAt = 0

// v0.69：A-Life 条目补建的节流时间戳（见 cc_main 的"统一收口"）
INT g_alReconcileAt = 0

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.69：A-Life 索引的**唯一安全入口**（用户报"选什么对话她都不回应"）
//
// 【为什么必须有这三个】本工程到处写 `g_alPers[g_curAlIdx]` / `g_alName[g_curAlIdx]`，
//   而 `g_curAlIdx` 可以是 **-1**（没条目 / 条目被释放）。SanScript **不做越界检查**：
//     · 读 `g_alPers[-1]` ⇒ 拿到一段垃圾 ⇒ 台词语气/嗓子里乱变；
//     · 更常见的是外层套着 `IF g_curAlIdx >= 0` ⇒ 整段**什么都不做**。
//   用户看到的就是："在互动里选什么对话，女伴都不回应；但服务那条路照走"
//   —— 因为只有聊天/试探/提议这几支依赖 A-Life 条目。
//
// 【用法】凡"按性格/名字取东西"的地方一律用 CC_Pers() / CC_GirlName()，
//   不要再直接写 `g_alPers[g_curAlIdx]`。
// ════════════════════════════════════════════════════════════════════════════

// A-Life 条目当前可用吗（比 `g_curAlIdx >= 0` 更严：也防越界）
FUNC BOOL CC_AlOk()
    IF g_curAlIdx < 0
        RETURN FALSE
    ENDIF
    IF g_curAlIdx >= AL_MAX
        RETURN FALSE
    ENDIF
    RETURN TRUE
ENDFUNC

// 当前女伴的性格索引（越界/无条目时返回 0 = 中性兜底，保证**永远有一句台词可显示**）
FUNC INT CC_Pers()
    IF NOT CC_AlOk()
        RETURN 0
    ENDIF
    RETURN g_alPers[g_curAlIdx]
ENDFUNC

// 当前女伴的声望（越界/无条目时返回 0）。
// ★ v0.72 新增：原来各处直接写 `g_alRep[g_curAlIdx]`，
//   而 `g_curAlIdx = -1` 时那是 `g_alRep[-1]` —— **越界读**，不报错、只给垃圾，
//   会把"接受率"算成一个随机数。和 CC_Pers() 同一个道理，统一走安全入口。
FUNC INT CC_Rep()
    IF NOT CC_AlOk()
        RETURN 0
    ENDIF
    RETURN g_alRep[g_curAlIdx]
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.72（D 类接线）：服务动作 → "价格组"（C# PriceGroup）
//
//   C# 的 `GetServicePriceGroup(false, idx)`（C# 6027）把街面的 12 个动作归成 5 组：
//       0 = Kissing（亲吻）      1 = Facesitting（颜骑）
//       2..4 = Blowjob（口交）   5..9 = Cowgirl（女上位/性交）
//       10..11 = Doggy（后入，含粗暴）
//   本端口的 `act` 号**和 C# 的 idx 完全一致**（见 cc_part2d 的服务菜单映射），
//   所以这层映射可以直接照抄。
// ════════════════════════════════════════════════════════════════════════════
FUNC INT CC_SvcGroup(INT act)
    IF act = 0
        RETURN 0
    ELIF act = 1
        RETURN 1
    ELIF act >= 2 AND act <= 4
        RETURN 2
    ELIF act >= 5 AND act <= 9
        RETURN 3
    ELIF act >= 10 AND act <= 11
        RETURN 4
    ENDIF
    RETURN 2
ENDFUNC

// 这个动作是不是"口交"（C# 17618 IsEscalateBJItem：街面 idx 2/3/4）。
FUNC BOOL CC_IsBJAct(INT act)
    IF act >= 2 AND act <= 4
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// 这个动作是不是"粗暴后入"（C# 17632 IsEscalateRoughItem：街面 idx 11）。
FUNC BOOL CC_IsRoughAct(INT act)
    IF act = 11
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// 声望夹紧（C# 各处 `Math.Max(REP_MIN, ...)` / `Math.Min(REP_MAX, ...)` 的统一写法）。
PROC CC_ClampRep(INT idx)
    IF g_alRep[idx] > CC_REP_MAX
        g_alRep[idx] = CC_REP_MAX
    ENDIF
    IF g_alRep[idx] < CC_REP_MIN
        g_alRep[idx] = CC_REP_MIN
    ENDIF
ENDPROC

// 硬要她做"她说过不愿做的口交"的声望惩罚（C# 8629-8631，逐档照抄）。
FUNC INT CC_NoBjPenalty()
    INT p = CC_Pers()
    IF p = PERS_AGGRESSIVE OR p = PERS_DOMINANT OR p = PERS_UNSTABLE OR p = PERS_JEALOUS
        RETURN -10
    ELIF p = PERS_COLD OR p = PERS_INDEPENDENT OR p = PERS_CLASSY OR p = PERS_STREETSMART OR p = PERS_CHAOTIC
        RETURN -8
    ENDIF
    RETURN -5
ENDFUNC

// 硬要她做"她说过不愿做的粗暴"的声望惩罚（C# 8663-8665，逐档照抄）。
FUNC INT CC_NoRoughPenalty()
    INT p = CC_Pers()
    IF p = PERS_AGGRESSIVE OR p = PERS_DOMINANT OR p = PERS_UNSTABLE OR p = PERS_JEALOUS
        RETURN -10
    ELIF p = PERS_COLD OR p = PERS_INDEPENDENT OR p = PERS_CLASSY OR p = PERS_STREETSMART OR p = PERS_CHAOTIC
        RETURN -7
    ENDIF
    RETURN -5
ENDFUNC

// 当前女伴的显示名。★ 故意写成 **PROC + 出参**，不写成 FUNC：
//   `g_alName` 是 TEXT_LABEL_15，而 sc.exe **拒绝** 把它当返回值转换
//   （`FUNC STRING` + `RETURN g_alName[...]` ⇒
//     `Unable to convert RETURN expression to FUNC return type`；
//    改成 `FUNC TEXT_LABEL_15` 后，把它当**参数**往下传又会报
//     `Invalid implicit string conversion ... passed by value to VARARGS`）。
//   出参（引用）是唯一稳的形态 —— 本工程 check_refparams.py 就是专门守 `&` 的。
PROC CC_GetGirlName(TEXT_LABEL_15 &out)
    out = "她"
    IF CC_AlOk()
        out = g_alName[g_curAlIdx]
    ENDIF
ENDPROC

FUNC STRING CC_FEMALE_NAME(INT i)
    SWITCH i
        CASE 0 RETURN "Aaliyah"
        CASE 1 RETURN "Adriana"
        CASE 2 RETURN "Alexa"
        CASE 3 RETURN "Alice"
        CASE 4 RETURN "Alina"
        CASE 5 RETURN "Amber"
        CASE 6 RETURN "Amelia"
        CASE 7 RETURN "Anastasia"
        CASE 8 RETURN "Angela"
        CASE 9 RETURN "Angelina"
        CASE 10 RETURN "Anna"
        CASE 11 RETURN "Aria"
        CASE 12 RETURN "Ariana"
        CASE 13 RETURN "Ashley"
        CASE 14 RETURN "Audrey"
        CASE 15 RETURN "Aurora"
        CASE 16 RETURN "Autumn"
        CASE 17 RETURN "Ava"
        CASE 18 RETURN "Bailey"
        CASE 19 RETURN "Bella"
        CASE 20 RETURN "Bianca"
        CASE 21 RETURN "Brianna"
        CASE 22 RETURN "Brooke"
        CASE 23 RETURN "Camila"
        CASE 24 RETURN "Carmen"
        CASE 25 RETURN "Caroline"
        CASE 26 RETURN "Cassandra"
        CASE 27 RETURN "Chloe"
        CASE 28 RETURN "Claire"
        CASE 29 RETURN "Crystal"
        CASE 30 RETURN "Daisy"
        CASE 31 RETURN "Dakota"
        CASE 32 RETURN "Daniela"
        CASE 33 RETURN "Daphne"
        CASE 34 RETURN "Delilah"
        CASE 35 RETURN "Destiny"
        CASE 36 RETURN "Diana"
        CASE 37 RETURN "Elena"
        CASE 38 RETURN "Elisa"
        CASE 39 RETURN "Ember"
        CASE 40 RETURN "Emily"
        CASE 41 RETURN "Emma"
        CASE 42 RETURN "Eva"
        CASE 43 RETURN "Faith"
        CASE 44 RETURN "Fiona"
        CASE 45 RETURN "Francesca"
        CASE 46 RETURN "Gabrielle"
        CASE 47 RETURN "Gemma"
    ENDSWITCH
    RETURN "Anna"
ENDFUNC

FUNC STRING CC_VOICE_NAME(INT i)
    // ★★★ v0.79：**删掉 `S_F_Y_HOOKER_03_BLACK_FULL_02`**（用户要求）。
    //   它在本机没有音频数据（v0.72 起只是"随机时跳过 + 标签标出"），
    //   用户实测确认无声后要求直接删条目 ⇒ 现在表是 **8 个嗓子（0..7）**，
    //   原来的 8 号（BLACK_FULL_03）上移到 7。
    //   ☠ 改这里必须同步：`CC_VoiceOk` / `CC_VoiceRandOk` 的范围、
    //     `CC_VoiceIdxClamp`、设置菜单的 "/9" 标签（cc_part2d）、
    //     cc_main 的 `CC_VoiceProbeTick` 回绕值。
    SWITCH i
        CASE 0 RETURN "S_F_Y_HOOKER_01_WHITE_FULL_01"
        CASE 1 RETURN "S_F_Y_HOOKER_01_WHITE_FULL_02"
        CASE 2 RETURN "S_F_Y_HOOKER_01_WHITE_FULL_03"
        CASE 3 RETURN "S_F_Y_HOOKER_02_WHITE_FULL_01"
        CASE 4 RETURN "S_F_Y_HOOKER_02_WHITE_FULL_02"
        CASE 5 RETURN "S_F_Y_HOOKER_02_WHITE_FULL_03"
        CASE 6 RETURN "S_F_Y_HOOKER_03_BLACK_FULL_01"
        CASE 7 RETURN "S_F_Y_HOOKER_03_BLACK_FULL_03"
    ENDSWITCH
    RETURN "S_F_Y_HOOKER_01_WHITE_FULL_01"
ENDFUNC

FUNC STRING CC_APP_DICT_NAME(INT i)
    SWITCH i
        CASE 0 RETURN "mini@hookers_spvanilla"
        CASE 1 RETURN "mini@hookers_spcokehead"
        CASE 2 RETURN "mini@hookers_spcrackhead"
        CASE 3 RETURN "mini@hookers_spfrench"
    ENDSWITCH
    RETURN "mini@hookers_spvanilla"
ENDFUNC

// approach 字典名同步 (STRING → native STRING 参数中转)
PROC CC_SyncAppDict(INT idx)
    g_appDictStr = CC_APP_DICT_NAME(idx)
ENDPROC


FUNC STRING CC_PERS_NAME(INT i)
    SWITCH i
        CASE 0 RETURN "Shy"
        CASE 1 RETURN "Flirty"
        CASE 2 RETURN "Dominant"
        CASE 3 RETURN "Cold"
        CASE 4 RETURN "Sweet"
        CASE 5 RETURN "Gold Digger"
        CASE 6 RETURN "Street Smart"
        CASE 7 RETURN "Party Girl"
        CASE 8 RETURN "Romantic"
        CASE 9 RETURN "Sarcastic"
        CASE 10 RETURN "Needy"
        CASE 11 RETURN "Independent"
        CASE 12 RETURN "Jealous"
        CASE 13 RETURN "Chaotic"
        CASE 14 RETURN "Manipulative"
        CASE 15 RETURN "Aggressive"
        CASE 16 RETURN "Playful"
        CASE 17 RETURN "Mysterious"
        CASE 18 RETURN "Classy"
        CASE 19 RETURN "Unstable"
    ENDSWITCH
    RETURN "Shy"
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★ v0.73：性格的**中文**名（给菜单显示用）
//
//  【为什么要有它】用户反馈"女性性格是不显示的吗？"
//    当时 `CC_PERS_NAME()`（上面那个）存在，但**全工程零调用** —— 死代码，
//    而且返回的还是英文（"Mysterious"）。
//    ⇒ 游戏里完全看不到性格 ⇒ 任何"按性格分叉"的台词（RISKY/NOKISS/NOBJ*/NORO*、
//      B 类给钱接受率、D 类 WALKWAY）都**没法用肉眼验收**。
//
//  【用法】菜单头那一行：`性格 神秘   声望 3`（见 cc_part2d.sc 的 CC_DrawMenu）。
//    ★ 只显示"性格 + 声望"，不做任何判定 —— 它是**观察窗**，不是开关。
//    ★ 每项压到 4 字以内，免得撑破菜单宽度（MW 只有 0.216）。
// ════════════════════════════════════════════════════════════════════════════
FUNC STRING CC_PERS_NAME_ZH(INT i)
    SWITCH i
        CASE 0 RETURN "害羞"
        CASE 1 RETURN "风骚"
        CASE 2 RETURN "强势"
        CASE 3 RETURN "冷淡"
        CASE 4 RETURN "甜美"
        CASE 5 RETURN "拜金"
        CASE 6 RETURN "精明"
        CASE 7 RETURN "玩咖"
        CASE 8 RETURN "浪漫"
        CASE 9 RETURN "毒舌"
        CASE 10 RETURN "黏人"
        CASE 11 RETURN "独立"
        CASE 12 RETURN "善妒"
        CASE 13 RETURN "混乱"
        CASE 14 RETURN "操纵"
        CASE 15 RETURN "凶悍"
        CASE 16 RETURN "俏皮"
        CASE 17 RETURN "神秘"
        CASE 18 RETURN "高雅"
        CASE 19 RETURN "无常"
    ENDSWITCH
    RETURN "中性"
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★ v0.73：声望的**分档词**（给菜单显示用）
//
//  【为什么用"词"而不是数字】
//    本工程**没有** "DISPLAY_TEXT 画整数"的先例 —— `ADD_TEXT_COMPONENT_INTEGER`
//    只在字幕路线（`CC_ShowLine3` 的 `PRINT("CELL_EMAIL_BCON")`）里被用过。
//    而 SanScript 没有字符串拼接 ⇒ "声望 " + 数字 得靠组件拼。
//    HUD 上画整数属于**未在本端口验证过**的用法 ⇒ 先用纯字符串的分档词，
//    零渲染风险，且足够验收。
//
//  【档位边界照抄代码里的真实阈值】
//    · `rep <= -1`  ⇒ AVOID / WALKWAY 档（她已经在躲你）
//    · `rep <  25`  ⇒ NOKISS 的"声望 < 25"条件
//    · 其余按体验分档
//    ⚠ 改这里的边界前，先 grep 这两个阈值（cc_dlgnav 的 CC_SvcGate、
//      cc_part2d 的 ML_CONVERSATION）。
// ════════════════════════════════════════════════════════════════════════════
FUNC STRING CC_REP_WORD(INT rep)
    IF rep <= -1
        RETURN "躲你"
    ENDIF
    IF rep <= 9
        RETURN "冷淡"
    ENDIF
    IF rep <= 24
        RETURN "一般"
    ENDIF
    IF rep <= 49
        RETURN "交好"
    ENDIF
    RETURN "亲密"
ENDFUNC

FUNC BOOL CC_EXISTS(PED_INDEX p)
    IF DOES_ENTITY_EXIST(p)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC INT CC_ModelHash(PED_INDEX p)
    RETURN ENUM_TO_INT(GET_ENTITY_MODEL(p))
ENDFUNC

FUNC INT CC_ModelHashV(ENTITY_INDEX e)
    RETURN ENUM_TO_INT(GET_ENTITY_MODEL(e))
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  原版站街女模型 hash 表（为什么要这张表，见文件上方 g_hookerHash 的长注释）
//  joaat("s_f_y_hooker_01".."s_f_y_hooker_99")，与 C# `_hookerModelHashes` 同表。
//  真正存在的模型只有 01/02/03；其余 hash 命中不到任何 ped，纯冗余，照抄 C#。
// ════════════════════════════════════════════════════════════════════════════
PROC CC_InitHookerHashes()
    g_hookerHash[0] = 42647445
    g_hookerHash[1] = 348382215
    g_hookerHash[2] = 51789996
    g_hookerHash[3] = -1178882584
    g_hookerHash[4] = -1475671417
    g_hookerHash[5] = -573540847
    g_hookerHash[6] = -872885662
    g_hookerHash[7] = -2108539114
    g_hookerHash[8] = -1794481018
    g_hookerHash[9] = 904504562
    g_hookerHash[10] = 1250151974
    g_hookerHash[11] = 1514499497
    g_hookerHash[12] = -17451253
    g_hookerHash[13] = 282385097
    g_hookerHash[14] = 561675284
    g_hookerHash[15] = -1524137120
    g_hookerHash[16] = -1241111267
    g_hookerHash[17] = -946124729
    g_hookerHash[18] = 1820430869
    g_hookerHash[19] = -1309434856
    g_hookerHash[20] = -1023590869
    g_hookerHash[21] = -708025399
    g_hookerHash[22] = 1764657807
    g_hookerHash[23] = 2063085090
    g_hookerHash[24] = -1945087918
    g_hookerHash[25] = -1629030913
    g_hookerHash[26] = -1627589189
    g_hookerHash[27] = -1932897962
    g_hookerHash[28] = 2054565038
    g_hookerHash[29] = 1081948121
    g_hookerHash[30] = -1675301093
    g_hookerHash[31] = -1984214456
    g_hookerHash[32] = 2013079244
    g_hookerHash[33] = 1731233075
    g_hookerHash[34] = -483754715
    g_hookerHash[35] = -791226242
    g_hookerHash[36] = -1089161990
    g_hookerHash[37] = -1371925691
    g_hookerHash[38] = -478904895
    g_hookerHash[39] = -359200054
    g_hookerHash[40] = 1494280124
    g_hookerHash[41] = 1162035233
    g_hookerHash[42] = -1920249676
    g_hookerHash[43] = 1137884480
    g_hookerHash[44] = 1818725997
    g_hookerHash[45] = -1700435224
    g_hookerHash[46] = -963460414
    g_hookerHash[47] = 2094804822
    g_hookerHash[48] = -1023329377
    g_hookerHash[49] = 813082592
    g_hookerHash[50] = -2000004986
    g_hookerHash[51] = -1702102007
    g_hookerHash[52] = 1707446909
    g_hookerHash[53] = 1998992702
    g_hookerHash[54] = -1341511931
    g_hookerHash[55] = -1035908237
    g_hookerHash[56] = 412514348
    g_hookerHash[57] = -1639087220
    g_hookerHash[58] = -146164333
    g_hookerHash[59] = -1857066936
    g_hookerHash[60] = -1103314398
    g_hookerHash[61] = 1845928375
    g_hookerHash[62] = -1161086145
    g_hookerHash[63] = -796236099
    g_hookerHash[64] = 369652136
    g_hookerHash[65] = 197025044
    g_hookerHash[66] = -69354157
    g_hookerHash[67] = -375711538
    g_hookerHash[68] = 1382312543
    g_hookerHash[69] = 1857823274
    g_hookerHash[70] = -1458432299
    g_hookerHash[71] = -697634426
    g_hookerHash[72] = 344878556
    g_hookerHash[73] = 30525539
    g_hookerHash[74] = -268196665
    g_hookerHash[75] = -575537116
    g_hookerHash[76] = 1523710562
    g_hookerHash[77] = 1093125902
    g_hookerHash[78] = -99075864
    g_hookerHash[79] = -1601960863
    g_hookerHash[80] = -228349937
    g_hookerHash[81] = -989803174
    g_hookerHash[82] = 308602897
    g_hookerHash[83] = -457831244
    g_hookerHash[84] = 771432253
    g_hookerHash[85] = 944059345
    g_hookerHash[86] = 1068909235
    g_hookerHash[87] = 1365501454
    g_hookerHash[88] = 1681460152
    g_hookerHash[89] = 618269643
    g_hookerHash[90] = -1647084100
    g_hookerHash[91] = -1352425252
    g_hookerHash[92] = 2084059782
    g_hookerHash[93] = 1810078173
    g_hookerHash[94] = -1002648946
    g_hookerHash[95] = -166023607
    g_hookerHash[96] = -1594883083
    g_hookerHash[97] = -788732914
    g_hookerHash[98] = 188110976
    g_hookerHashReady = TRUE
ENDPROC

// 这个 ped 是不是"原版 GTA 嫖娼脚本会管"的站街女？
// TRUE ⇒ 调用方必须避开她：否则原版 pb_prostitute 会在玩家一上车时接管，
//        用剧本相机锁死视角、并且和本模组互相抢任务（上车被踹 / 尖叫跑走）。
FUNC BOOL CC_IsVanillaHookerModel(PED_INDEX p)
    IF NOT DOES_ENTITY_EXIST(p)
        RETURN FALSE
    ENDIF
    IF NOT g_hookerHashReady
        CC_InitHookerHashes()
    ENDIF
    INT mh = CC_ModelHash(p)
    INT i = 0
    WHILE i < CC_HOOKER_HASH_N
        IF g_hookerHash[i] = mh
            RETURN TRUE
        ENDIF
        i = i + 1
    ENDWHILE
    RETURN FALSE
ENDFUNC

FUNC BOOL CC_ALIVE(PED_INDEX p)
    IF NOT DOES_ENTITY_EXIST(p)
        RETURN FALSE
    ENDIF
    IF IS_ENTITY_DEAD(p)
        RETURN FALSE
    ENDIF
    RETURN TRUE
ENDFUNC

// 2^n (SanScript 无 << 运算符, << 是向量字面量)
FUNC INT CC_BIT(INT n)
    INT v = 1
    INT k = 0
    WHILE k < n
        v = v * 2
        k = k + 1
    ENDWHILE
    RETURN v
ENDFUNC

FUNC INT CC_RAND(INT lo, INT hi)
    IF hi <= lo
        RETURN lo
    ENDIF
    RETURN GET_RANDOM_INT_IN_RANGE(lo, hi)
ENDFUNC

FUNC INT CC_RAND100()
    RETURN GET_RANDOM_INT_IN_RANGE(0, 100)
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
// ★★★ v0.79：坏嗓子**直接从表里删掉**（原来是"跳过 + 标出"）
//
//   【历史】v0.72 用户报"第八个嗓子没声音"，名字是
//     `S_F_Y_HOOKER_03_BLACK_FULL_02`。当时的做法是：表里**保留**它、
//     随机分配时跳过、菜单标签标"（本机无音频）"。
//   【现在】用户实测确认无声，要求"把这个条目以及相关代码删了"
//     ⇒ 已经从 `CC_VOICE_NAME` 里删掉，表变成 **8 个（0..7）**，
//       `CC_VOICE_BAD` 这个常量也随之取消（只剩这一个已知坏嗓子，没有第二个）。
//
//   ★ 以后如果再发现坏嗓子：在 `CC_VoiceOk` 里加一行 `IF v = N RETURN FALSE`，
//     并把 N 标注出来（不要删表项 —— 删表项要动上面那一串范围检查）。
// ════════════════════════════════════════════════════════════════════════════
CONST_INT CC_VOICE_COUNT 8

// 某个嗓音下标在本机**是否可用**（"能不能真的发声"是引擎+音频数据的事，
// 这里只维护"我们**验过**的那几个"）。
FUNC BOOL CC_VoiceOk(INT v)
    IF v < 0 OR v >= CC_VOICE_COUNT
        RETURN FALSE
    ENDIF
    RETURN TRUE
ENDFUNC

// 随机挑一个**可用**的嗓子。现在池里没有坏嗓子 ⇒ 直接均匀取一个；
// 仍走 `CC_VoiceOk` 是为了将来加坏嗓子时不用改这里。
FUNC INT CC_VoiceRandOk()
    IF CC_VOICE_COUNT <= 0
        RETURN 0
    ENDIF
    INT r = 0
    INT guard = 0
    WHILE guard < CC_VOICE_COUNT
        r = CC_RAND(0, CC_VOICE_COUNT)
        IF CC_VoiceOk(r)
            RETURN r
        ENDIF
        guard = guard + 1
    ENDWHILE
    RETURN 0                     // 兜底：0 号（WHITE_FULL_01）
ENDFUNC

FUNC BOOL CC_ROLL(INT pctVal)
    IF GET_RANDOM_INT_IN_RANGE(0, 100) < pctVal
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC INT CC_StatHash()
    INT mh
    mh = CC_ModelHash(g_player)
    INT h0 = GET_HASH_KEY("player_zero")
    INT h1 = GET_HASH_KEY("player_one")
    INT h2 = GET_HASH_KEY("player_two")
    IF mh = h0
        RETURN GET_HASH_KEY("SP0_TOTAL_CASH")
    ENDIF
    IF mh = h1
        RETURN GET_HASH_KEY("SP1_TOTAL_CASH")
    ENDIF
    IF mh = h2
        RETURN GET_HASH_KEY("SP2_TOTAL_CASH")
    ENDIF
    RETURN GET_HASH_KEY("SP0_TOTAL_CASH")
ENDFUNC

FUNC INT CC_GetMoney()
    INT val = 0
    STAT_GET_INT(INT_TO_ENUM(STATSENUM, CC_StatHash()), val, -1)
    RETURN val
ENDFUNC

PROC CC_SetMoney(INT val)
    STAT_SET_INT(INT_TO_ENUM(STATSENUM, CC_StatHash()), val, FALSE)
ENDPROC

PROC CC_AddMoney(INT amt)
    INT cur
    cur = CC_GetMoney()
    CC_SetMoney(cur + amt)
ENDPROC

// ── 诊断行（独立通道，不受 g_showTut 开关影响，也不会被状态消息顶掉）──
//   放在 CC_ReqDict 之前：SanScript 要求先定义后调用。
//
//   ★ v0.57：加总闸 `g_showDiag`（默认 FALSE）。
//     用户要求"删除测试显示的各种提示" —— 收在**唯一出口**这一处，
//     十几处 CC_HudDiag 调用点（DICT EXISTS / CAR p0 intro / VOICE ok …）
//     全部随之静默；以后要排查，把 g_showDiag 改回 TRUE 即可。
PROC CC_HudDiag(STRING msg, INT durMs)
    IF NOT g_showDiag
        EXIT
    ENDIF
    g_diagText = msg
    g_diagUntil = GET_GAME_TIMER() + durMs
ENDPROC

// ── ★ 动画时长安全读取（2026-09-25晚 新增）──────────────────────────────
//   GET_ANIM_DURATION 是整条动画系统里**风险最高**的一个读数：
//     · 它不在"已实机跑通脚本"的 native 白名单里（基线僵尸模组从没用过它）；
//     · 整条相位机（0 intro → 1 过渡 → 2 loop → 3 收尾）的每一级都以
//       `g_animWait = now + 时长` 作为唯一推进条件。
//   它只要返回 0 / 负数 / 一个离谱的大数，相位机就永远离不开 phase 0 ——
//   而 intro/过渡用的都是 flag 2 = HOLD_LAST_FRAME，视觉表现就是
//   **"动画播一下就不动了"**（用户实测现象）。
//
//   这里统一夹紧，绝不把裸读数直接当定时器用：
//     d <= 0.05s  → 读不出来（字典没加载 / native 哑了）⇒ 用调用方给的兜底值
//     d >  60.0s  → 离谱（单位不对 / 垃圾值）      ⇒ 夹到 10s
//   正常动画 0.3 ~ 10s，两个夹子都碰不到它，所以对正常动作零影响。
FUNC FLOAT CC_SafeAnimDur(STRING dict, STRING anim, FLOAT fallbackSec)
    FLOAT d = GET_ANIM_DURATION(dict, anim)
    IF d <= 0.05
        RETURN fallbackSec
    ENDIF
    IF d > 60.0
        RETURN 10.0
    ENDIF
    RETURN d
ENDFUNC

// ── 动画字典加载（★ 2026-09-25 修：请求必须放在循环里每帧重发）────────────
//
// 【原来的写法（坏）】
//     REQUEST_ANIM_DICT(dict)          // 只发一次
//     WHILE NOT HAS_ANIM_DICT_LOADED(dict)
//         WAIT(0)                      // 循环里不再重发
//         ... 1 秒超时 -> RETURN FALSE
//     ENDWHILE
//
// 【为什么不行】REQUEST_ANIM_DICT 是**流式请求**，不是"发一次就一定会到"。
//   它需要在资产就绪之前**持续重发**（游戏自带的写法就是每帧发一次）。
//   发一次然后干等，请求得不到维持 —— 字典永远加载不上，1 秒后超时返回
//   FALSE，调用方打一句 "FAILED TO LOAD ANIMATIONS" 就 EXIT。
//   而**每一个**动画入口都经过这里 ⇒ 所有动作全灭。
//
// 【基线对照】实机跑通的僵尸模组是这么写的（非阻塞、持续重发）：
//     REQUEST_ANIM_DICT("rcmbarry")            // 主循环里每帧发
//     IF HAS_ANIM_DICT_LOADED("rcmbarry") ...  // 只用标志位判断
//
// 【现在】把 REQUEST_ANIM_DICT 挪进循环里每帧重发。超时放宽到 2.5 秒
//   （Switch 从 SD 卡冷读一个动画字典本来就慢），并且超时时把
//   "这个字典在不在本端口" 一起报出来，方便一眼定性。
CONST_INT CC_DICT_TIMEOUT_MS 2500

FUNC BOOL CC_ReqDict(STRING dict)
    IF HAS_ANIM_DICT_LOADED(dict)
        RETURN TRUE
    ENDIF
    INT start = GET_GAME_TIMER()
    WHILE NOT HAS_ANIM_DICT_LOADED(dict)
        REQUEST_ANIM_DICT(dict)                 // ★ 必须每帧重发（原来漏了这句）
        WAIT(0)
        IF GET_GAME_TIMER() - start > CC_DICT_TIMEOUT_MS
            // 超时：分两种情况报，一条就能定性
            IF DOES_ANIM_DICT_EXIST(dict)
                CC_HudDiag("~y~DICT EXISTS / LOAD TIMEOUT - streaming too slow", 8000)
            ELSE
                CC_HudDiag("~r~DICT NOT FOUND IN PORT (does_exist = FALSE)", 8000)
            ENDIF
            RETURN FALSE
        ENDIF
    ENDWHILE
    RETURN TRUE
ENDFUNC

PROC CC_DrawRect(FLOAT x, FLOAT y, FLOAT w, FLOAT h, INT r, INT gc, INT b, INT a)
    DRAW_RECT(x + w * 0.5, y + h * 0.5, w, h, r, gc, b, a)
ENDPROC

PROC CC_DrawText(STRING msg, FLOAT x, FLOAT y, FLOAT w, FLOAT scale, BOOL cen, INT r, INT gc, INT b, INT a, INT fnt)
    SET_TEXT_FONT(INT_TO_ENUM(TEXT_FONTS, fnt))
    SET_TEXT_SCALE(scale, scale)
    SET_TEXT_COLOUR(r, gc, b, a)
    SET_TEXT_WRAP(x, x + w)
    SET_TEXT_CENTRE(cen)
    SET_TEXT_DROPSHADOW(2, 2, 0, 0, 0)
    BEGIN_TEXT_COMMAND_DISPLAY_TEXT("STRING")
    ADD_TEXT_COMPONENT_SUBSTRING_KEYBOARD_DISPLAY(msg)
    FLOAT drawX = x
    // ★ 2026-09-25 说明（别再往这里加偏移）：cen = TRUE 时，传出去的 x 就是
    //   **字符串中心**，不是左边缘 —— 这是本工程所有调用点一致的约定：
    //     · HUD 状态行 / 诊断行  传 x=0.5, w=1.0  → 居中在屏幕正中 ✓
    //   所以"居中文本跑到框外"不要在这里统一加 w*0.5（那会把上面两行
    //   推到 x=1.0 屏幕右外），而要**在调用点把中心算对**。
    //   本次"热咖啡"的修复就是在 cc_part2d.sc 的调用点改成 MPX + MW*0.5。
    END_TEXT_COMMAND_DISPLAY_TEXT(drawX, y)
ENDPROC

// 台词显示 v2: head + 名字 + tail (packed 台词表统一出口)
PROC CC_ShowLine2(STRING head, STRING tlTxt, STRING speaker, INT durMs)
    BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
    IF NOT ARE_STRINGS_EQUAL(head, "")
        ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(head)
    ENDIF
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(speaker)
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(tlTxt)
    END_TEXT_COMMAND_PRINT(durMs, TRUE)
ENDPROC

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.79：**和她的话之间那个"："** —— 给"正文表里没带分隔符"的那几张用
//
//  【约定】本工程大多数台词表的正文（`_T`）自己带分隔符，例如
//     `CC_SBXACC_1_T` = `":~s~ 哦……呃。行吧。好。"` ⇒ 显示成「名字：哦……呃。」。
//  但有几张表的正文**只有句子**（数据里没有冒号），而显示时又走 `CC_ShowLine2`
//  （= head + 名字 + 正文）⇒ 屏幕上变成「名字哦……好。嗯。」——**少一个冒号**。
//  实测受影响且**在用**的：`FINACC`（内射答应）、`FINUNASK`（她自己也享受/没问就内射）、
//  `ILYRESP`（女友回"我也爱你"）、`ILYREJ`（拒绝"我爱你"）。
//  （`LEAVEPROST` 同样没有冒号，但那是**街女**那条道别分支 —— 端口去妓女化后走不到。）
//
//  【为什么不去改那 600 多条数据】它们是生成物的唯一副本（生成器绝不能重跑），
//  按条改风险大；而"补一个分隔符"本质是**显示层的事** ⇒ 在这一层加一个组件就够。
//  顺带把 `head`（颜色码，例如 `~r~`）的语义也保住了：颜色仍然在名字之前生效。
//
//  ★ 以后新接一张表：**先看它的 `_T` 有没有自带 `:` / `：`** ——
//    有 ⇒ 用 `CC_ShowLine2`；没有 ⇒ 用这个（否则就是"少个标点"）。
// ════════════════════════════════════════════════════════════════════════════
PROC CC_ShowLine2C(STRING head, STRING tlTxt, STRING speaker, INT durMs)
    BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
    IF NOT ARE_STRINGS_EQUAL(head, "")
        ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(head)
    ENDIF
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(speaker)
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME("：")
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(tlTxt)
    END_TEXT_COMMAND_PRINT(durMs, TRUE)
ENDPROC

// 金额行显示: head + 名字 + pre + $amt + suf
PROC CC_ShowLine3(STRING head, STRING pre, INT amt, STRING suf, STRING speaker, INT durMs)
    BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
    IF NOT ARE_STRINGS_EQUAL(head, "")
        ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(head)
    ENDIF
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(speaker)
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(pre)
    ADD_TEXT_COMPONENT_INTEGER(amt)
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(suf)
    END_TEXT_COMMAND_PRINT(durMs, TRUE)
ENDPROC

PROC CC_HudStatus(STRING msg, INT durMs)
    IF NOT g_showTut
        EXIT
    ENDIF
    g_hudText = msg
    g_hudExpireAt = GET_GAME_TIMER() + durMs
ENDPROC

// ★★★ v0.57：**不受门控**的 HUD 状态行。
//   与 CC_HudStatus 的唯一区别是**不吃 g_showTut 这一闸** —— 用户把「教学提示」
//   关掉之后，"总开关已关闭/已开启"这类**关键状态反馈**仍然必须出现，
//   否则他关掉模组却看不到任何确认、以为没生效（甚至连"怎么再打开"都不知道）。
//   ⚠ 只用来反馈"模组总开关"这种级别的状态，别拿它发日常提示（那属于 CC_HudStatus）。
PROC CC_HudNote(STRING msg, INT durMs)
    g_hudText = msg
    g_hudExpireAt = GET_GAME_TIMER() + durMs
ENDPROC

PROC CC_Subtitle(STRING msg, INT durMs)
    BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(msg)
    END_TEXT_COMMAND_PRINT(durMs, TRUE)
ENDPROC

PROC CC_HelpText(STRING msg)
    BEGIN_TEXT_COMMAND_DISPLAY_HELP("CELL_EMAIL_BCON")
    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(msg)
    END_TEXT_COMMAND_DISPLAY_HELP(INT_TO_ENUM(eHelpMessageId, 0), FALSE, TRUE, -1)
ENDPROC


// 哨兵槽: "[[NAME]]"=插名字, "[[AMT]]"=插金额整数
PROC CC_ShowLine(STRING parts[8], INT intVal, STRING speaker, INT durMs)
    BEGIN_TEXT_COMMAND_PRINT("CELL_EMAIL_BCON")
    INT i = 0
    WHILE i < 8
        IF ARE_STRINGS_EQUAL(parts[i], "[[NAME]]")
            ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(speaker)
        ELSE
            IF ARE_STRINGS_EQUAL(parts[i], "[[AMT]]")
                ADD_TEXT_COMPONENT_INTEGER(intVal)
            ELSE
                IF NOT ARE_STRINGS_EQUAL(parts[i], "")
                    ADD_TEXT_COMPONENT_SUBSTRING_PLAYER_NAME(parts[i])
                ENDIF
            ENDIF
        ENDIF
        i = i + 1
    ENDWHILE
    END_TEXT_COMMAND_PRINT(durMs, TRUE)
ENDPROC

PROC CC_InitTraits()
    FRIENDLINESS[0] = 55
    CONFIDENCE[0] = 10
    AGGRESSIVENESS[0] = 10
    PATIENCE[0] = 40
    FRIENDLINESS[1] = 85
    CONFIDENCE[1] = 65
    AGGRESSIVENESS[1] = 30
    PATIENCE[1] = 60
    FRIENDLINESS[2] = 45
    CONFIDENCE[2] = 95
    AGGRESSIVENESS[2] = 85
    PATIENCE[2] = 50
    FRIENDLINESS[3] = 15
    CONFIDENCE[3] = 70
    AGGRESSIVENESS[3] = 60
    PATIENCE[3] = 25
    FRIENDLINESS[4] = 90
    CONFIDENCE[4] = 35
    AGGRESSIVENESS[4] = 10
    PATIENCE[4] = 75
    FRIENDLINESS[5] = 60
    CONFIDENCE[5] = 75
    AGGRESSIVENESS[5] = 45
    PATIENCE[5] = 45
    FRIENDLINESS[6] = 40
    CONFIDENCE[6] = 80
    AGGRESSIVENESS[6] = 65
    PATIENCE[6] = 55
    FRIENDLINESS[7] = 80
    CONFIDENCE[7] = 70
    AGGRESSIVENESS[7] = 35
    PATIENCE[7] = 50
    FRIENDLINESS[8] = 85
    CONFIDENCE[8] = 40
    AGGRESSIVENESS[8] = 10
    PATIENCE[8] = 70
    FRIENDLINESS[9] = 35
    CONFIDENCE[9] = 75
    AGGRESSIVENESS[9] = 50
    PATIENCE[9] = 35
    FRIENDLINESS[10] = 70
    CONFIDENCE[10] = 20
    AGGRESSIVENESS[10] = 20
    PATIENCE[10] = 80
    FRIENDLINESS[11] = 45
    CONFIDENCE[11] = 85
    AGGRESSIVENESS[11] = 65
    PATIENCE[11] = 40
    FRIENDLINESS[12] = 50
    CONFIDENCE[12] = 55
    AGGRESSIVENESS[12] = 75
    PATIENCE[12] = 30
    FRIENDLINESS[13] = 55
    CONFIDENCE[13] = 60
    AGGRESSIVENESS[13] = 90
    PATIENCE[13] = 20
    FRIENDLINESS[14] = 65
    CONFIDENCE[14] = 90
    AGGRESSIVENESS[14] = 40
    PATIENCE[14] = 60
    FRIENDLINESS[15] = 10
    CONFIDENCE[15] = 90
    AGGRESSIVENESS[15] = 95
    PATIENCE[15] = 15
    FRIENDLINESS[16] = 80
    CONFIDENCE[16] = 55
    AGGRESSIVENESS[16] = 25
    PATIENCE[16] = 65
    FRIENDLINESS[17] = 30
    CONFIDENCE[17] = 70
    AGGRESSIVENESS[17] = 45
    PATIENCE[17] = 50
    FRIENDLINESS[18] = 65
    CONFIDENCE[18] = 80
    AGGRESSIVENESS[18] = 25
    PATIENCE[18] = 55
    FRIENDLINESS[19] = 45
    CONFIDENCE[19] = 50
    AGGRESSIVENESS[19] = 80
    PATIENCE[19] = 10
    // ★★★ v0.72（D 类接线）：Greed / Riskiness / Attachment —— 逐值照抄 C# 162-181（×100）。
    //   顺序与 PERS_* 完全一致：Shy Flirty Dominant Cold Sweet GoldDigger StreetSmart
    //   PartyGirl Romantic Sarcastic Needy Independent Jealous Chaotic Manipulative
    //   Aggressive Playful Mysterious Classy Unstable
    //   · Greed      —— 给钱档位门槛 / 亲吻"为钱破例" / 关系重提接受率
    //   · Riskiness  —— 户外愿不愿意做（<40 拒绝）/ 口交·粗暴"第一次"的风险闸
    //   · Attachment —— 亲吻的情感闸（>=65 且声望<25 且不贪 ⇒ 不给亲）
    GREED[0] = 20     GREED[1] = 30     GREED[2] = 40     GREED[3] = 55
    GREED[4] = 10     GREED[5] = 95     GREED[6] = 65     GREED[7] = 35
    GREED[8] = 15     GREED[9] = 45     GREED[10] = 30    GREED[11] = 25
    GREED[12] = 50    GREED[13] = 50    GREED[14] = 85    GREED[15] = 60
    GREED[16] = 20    GREED[17] = 40    GREED[18] = 70    GREED[19] = 55
    RISKINESS[0] = 15     RISKINESS[1] = 60     RISKINESS[2] = 55     RISKINESS[3] = 25
    RISKINESS[4] = 40     RISKINESS[5] = 70     RISKINESS[6] = 50     RISKINESS[7] = 80
    RISKINESS[8] = 35     RISKINESS[9] = 45     RISKINESS[10] = 30    RISKINESS[11] = 50
    RISKINESS[12] = 40    RISKINESS[13] = 90    RISKINESS[14] = 65    RISKINESS[15] = 70
    RISKINESS[16] = 75    RISKINESS[17] = 55    RISKINESS[18] = 30    RISKINESS[19] = 95
    ATTACHMENT[0] = 70     ATTACHMENT[1] = 50     ATTACHMENT[2] = 35     ATTACHMENT[3] = 20
    ATTACHMENT[4] = 85     ATTACHMENT[5] = 25     ATTACHMENT[6] = 30     ATTACHMENT[7] = 40
    ATTACHMENT[8] = 90     ATTACHMENT[9] = 30     ATTACHMENT[10] = 95    ATTACHMENT[11] = 15
    ATTACHMENT[12] = 80    ATTACHMENT[13] = 45    ATTACHMENT[14] = 20    ATTACHMENT[15] = 25
    ATTACHMENT[16] = 55    ATTACHMENT[17] = 40    ATTACHMENT[18] = 45    ATTACHMENT[19] = 60
ENDPROC

FUNC INT AL_Fingerprint(PED_INDEX p)
    IF NOT CC_EXISTS(p)
        RETURN 0
    ENDIF
    INT fpVal
    fpVal = CC_ModelHash(p)
    INT comp = 0
    WHILE comp < 12
        INT d = GET_PED_DRAWABLE_VARIATION(p, INT_TO_ENUM(PED_COMPONENT, comp))
        INT tVal = GET_PED_TEXTURE_VARIATION(p, INT_TO_ENUM(PED_COMPONENT, comp))
        fpVal = fpVal + ((d * 31 + tVal) * (comp * 7 + 13))
        comp = comp + 1
    ENDWHILE
    RETURN fpVal
ENDFUNC

FUNC INT AL_Find(INT fpVal)
    INT i = 0
    WHILE i < AL_MAX
        IF g_alUsed[i]
            IF g_alFP[i] = fpVal
                RETURN i
            ENDIF
        ENDIF
        i = i + 1
    ENDWHILE
    RETURN -1
ENDFUNC

PROC AL_RollMood(INT idx)
    INT fri = FRIENDLINESS[g_alPers[idx]]
    INT roll
    roll = CC_RAND(0, 100)
    IF fri >= 70
        IF roll < 30
            g_alMood[idx] = 0
        ELIF roll < 52
            g_alMood[idx] = 1
        ELIF roll < 70
            g_alMood[idx] = 3
        ELIF roll < 82
            g_alMood[idx] = 2
        ELIF roll < 92
            g_alMood[idx] = 5
        ELSE
            g_alMood[idx] = 4
        ENDIF
    ELIF fri >= 40
        IF roll < 25
            g_alMood[idx] = 0
        ELIF roll < 33
            g_alMood[idx] = 1
        ELIF roll < 55
            g_alMood[idx] = 2
        ELIF roll < 75
            g_alMood[idx] = 4
        ELIF roll < 88
            g_alMood[idx] = 3
        ELIF roll < 96
            g_alMood[idx] = 5
        ELSE
            g_alMood[idx] = 6
        ENDIF
    ELSE
        IF roll < 42
            g_alMood[idx] = 2
        ELIF roll < 69
            g_alMood[idx] = 4
        ELIF roll < 82
            g_alMood[idx] = 0
        ELIF roll < 90
            g_alMood[idx] = 6
        ELIF roll < 97
            g_alMood[idx] = 3
        ELSE
            g_alMood[idx] = 1
        ENDIF
    ENDIF
ENDPROC

PROC AL_AssignGoal(INT idx)
    INT p = g_alPers[idx]
    INT pool = 0
    IF g_alHooker[idx]
        IF p = PERS_SWEET OR p = PERS_ROMANTIC OR p = PERS_NEEDY OR p = PERS_PLAYFUL OR p = PERS_FLIRTY
            pool = CC_RAND(0, 2)
            IF pool = 0
                g_alGoal[idx] = 0
            ELSE
                g_alGoal[idx] = 6
            ENDIF
        ELSE
            g_alGoal[idx] = CC_RAND(4, 6)
        ENDIF
    ELSE
        g_alGoal[idx] = CC_RAND(0, 6)
    ENDIF
ENDPROC

PROC AL_RollCim(INT idx)
    INT p = g_alPers[idx]
    INT chance = 40
    IF p = PERS_DOMINANT OR p = PERS_PARTYGIRL OR p = PERS_CHAOTIC OR p = PERS_GOLDDIGGER
        chance = 60
    ENDIF
    IF p = PERS_SHY OR p = PERS_ROMANTIC OR p = PERS_CLASSY
        chance = 20
    ENDIF
    IF CC_ROLL(chance)
        g_alCim[idx] = 1
    ELSE
        g_alCim[idx] = 0
    ENDIF
ENDPROC

FUNC INT AL_GetOrCreate(PED_INDEX p, BOOL prost)
    INT fpVal = AL_Fingerprint(p)
    IF fpVal = 0
        RETURN -1
    ENDIF
    INT idx = AL_Find(fpVal)
    IF idx >= 0
        IF prost
            IF NOT g_alHooker[idx]
                g_alHooker[idx] = TRUE
            ENDIF
        ENDIF
        IF g_alMood[idx] = 0
            AL_RollMood(idx)
        ENDIF
        RETURN idx
    ENDIF
    INT slot = -1
    INT i = 0
    WHILE i < AL_MAX
        IF NOT g_alUsed[i]
            slot = i
            i = AL_MAX
        ENDIF
        i = i + 1
    ENDWHILE
    IF slot < 0
        INT worst = 0
        INT wi = 1
        WHILE wi < AL_MAX
            IF g_alRep[wi] < g_alRep[worst]
                worst = wi
            ENDIF
            wi = wi + 1
        ENDWHILE
        slot = worst
    ENDIF
    g_alUsed[slot] = TRUE
    g_alFP[slot] = fpVal
    g_alName[slot] = CC_FEMALE_NAME(CC_RAND(0, 48))
    g_alPers[slot] = CC_RAND(0, 20)
    g_alRep[slot] = 0
    g_alRel[slot] = 2
    g_alMood[slot] = 0
    // ★ v0.72：用 CC_VoiceRandOk()（跳过已知无音频的嗓子）。
    g_alVoice[slot] = CC_VoiceRandOk()
    g_alDict[slot] = CC_RAND(0, 4)
    g_alHooker[slot] = prost
    // ★ v0.75：关系状态初值 = 陌生人
    g_alRSt[slot] = RST_STRANGER
    g_alRStLast[slot] = RST_STRANGER
    g_alCasualOK[slot] = 0
    g_alMet[slot] = FALSE
    g_alMet2[slot] = FALSE
    g_alTopics[slot] = 0
    g_alPrefBJ[slot] = 0
    g_alPrefRough[slot] = 0
    g_alCim[slot] = 0
    IF prost
        g_alCash[slot] = CC_RAND(5, 41)
        g_alStored[slot] = CC_RAND(50, 501)
    ELSE
        g_alCash[slot] = CC_RAND(0, 81)
        g_alStored[slot] = CC_RAND(0, 301)
    ENDIF
    AL_RollCim(slot)
    AL_AssignGoal(slot)
    AL_RollMood(slot)
    RETURN slot
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.75：关系状态推进（C# `UpdateCasualRelationshipState` 的忠实移植）
//
//  【C# 原逻辑（CinnamonCoffee.cs:16620-16651）】
//    ① 声望 ≤ -2 → 敌视，**直接返回**（连"上次正向态"都不记）
//    ② 声望 = -1 → 把**当前**的正向态存进 LastPositiveRelationship，然后 → 回避
//    ③ 当前是"回避" → 恢复 LastPositiveRelationship（没有就回陌生人）
//    ④ 已经是进阶态（暧昧/朋友区/女友/痴迷）→ **锁定，直接返回**
//    ⑤ 否则按声望铺基础态：<10 陌生人 / <30 认识 / <60 熟识 / ≥60 友好
//
//  【端口的实现口径】第 ④ 步的"进阶态锁定"和 C# 一致；
//    第 ① 步的"敌视"在 C# 里也不可逆（声望回到 -1 以上时会先走第 ③ 步恢复上次正向态，
//    但 `Hostile` 本身没被恢复过）——这里照抄。
// ════════════════════════════════════════════════════════════════════════════
PROC AL_UpdateRSt(INT idx)
    // ★ 审查补上 `>= AL_MAX`：AL_UpdateRel 只管下界，上界一直没人挡。
    IF idx < 0 OR idx >= AL_MAX
        EXIT
    ENDIF
    INT rep = g_alRep[idx]
    // ① 敌视
    IF rep <= -2
        g_alRSt[idx] = RST_HOSTILE
        EXIT
    ENDIF
    // ② 回避（先把当前正向态存起来）
    IF rep = -1
        INT curW = g_alRSt[idx]
        IF curW >= RST_STRANGER
            g_alRStLast[idx] = curW
        ENDIF
        g_alRSt[idx] = RST_AVOIDING
        EXIT
    ENDIF
    // ③ 从"回避"恢复
    IF g_alRSt[idx] = RST_AVOIDING
        g_alRSt[idx] = g_alRStLast[idx]
    ENDIF
    // ④ 进阶态锁定（C#：`isAdvanced` 一旦置上就 `return`，声望回落也冲不掉）
    //    ★ 注意：C# 的**休闲阶梯里并没有"痴迷"** —— `Relationship = "Obsessed"`
    //      全工程只出现在**卖淫**阶梯（CinnamonCoffee.cs:16614）。
    //      ⇒ 这里也**不赋** `RST_OBSESSED`；休闲模式的最高态就是「女友」。
    //      （常量与中文名保留，是为了显示函数完整、以及将来若要给个"满好感奖励"时能用。
    //        要加的话说一声——那属于"C# 没做的"，得你点头。）
    IF g_alRSt[idx] >= RST_FLIRTY
        EXIT
    ENDIF
    // ⑤ 基础态阶梯
    IF rep < 10
        g_alRSt[idx] = RST_STRANGER
    ELIF rep < 30
        g_alRSt[idx] = RST_ACQUAINT
    ELIF rep < 60
        g_alRSt[idx] = RST_FAMILIAR
    ELSE
        g_alRSt[idx] = RST_FRIENDLY
    ENDIF
    // ⑥ ★ v0.76：声望 ≥ 95 ⇒ **痴迷**（用户要求"全部做上"时加的）
    //   ☠ **C# 的休闲阶梯里没有这一档**（`Obsessed` 只在卖淫阶梯出现）⇒ 这一条是**我加的**。
    //    不想要就把 `g_alObsessOn` 改成 FALSE（一行）。
    //    一旦置上就锁定（下次进 ④ 会直接 EXIT）。
    IF g_alObsessOn AND rep >= 95
        g_alRSt[idx] = RST_OBSESSED
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════════
//  ★ v0.75：**声望档 + 关系状态一起更新**（统一收口）
//    `AL_UpdateRel` 本来就是"改完声望就调一次"的唯一入口（全工程 20 处调用点）
//    ⇒ 把 `AL_UpdateRSt` 挂在它末尾，所有既有调用点**自动**同步，
//      一个都不用改，也不会漏 —— 这就是"清理/检测类逻辑别只挂某个状态 tick"那条纪律。
//  ☠ 定义顺序：SanScript 要求**先定义后使用**，所以本 PROC 必须排在 `AL_UpdateRSt` **之后**
//    （第一版写反了，sc.exe 报 `Unrecognized variable or command name 'AL_UPDATERST'`，
//      而且报的是**调用点那一行**，看着像别处的问题）。
// ════════════════════════════════════════════════════════════════════════════
PROC AL_UpdateRel(INT idx)
    INT rep = g_alRep[idx]
    IF rep <= CC_REP_MIN
        g_alRel[idx] = 0
    ELIF rep < 0
        g_alRel[idx] = 1
    ELIF rep < 10
        g_alRel[idx] = 2
    ELIF rep < 25
        g_alRel[idx] = 3
    ELIF rep < 50
        g_alRel[idx] = 4
    ELIF rep < 75
        g_alRel[idx] = 5
    ELIF rep < 90
        g_alRel[idx] = 6
    ELSE
        g_alRel[idx] = 7
    ENDIF
    AL_UpdateRSt(idx)
ENDPROC

// 关系状态的中文名（菜单里显示用）
FUNC STRING CC_RST_NAME_ZH(INT s)
    IF s <= RST_HOSTILE
        RETURN "敌视"
    ENDIF
    IF s = RST_AVOIDING
        RETURN "躲着你"
    ENDIF
    SWITCH s
        CASE 0 RETURN "陌生人"
        CASE 1 RETURN "认识"
        CASE 2 RETURN "熟识"
        CASE 3 RETURN "友好"
        CASE 4 RETURN "暧昧"
        CASE 5 RETURN "朋友区"
        CASE 6 RETURN "女友"
        CASE 7 RETURN "痴迷"
    ENDSWITCH
    RETURN "陌生人"
ENDFUNC

// 她的回应算不算"正面"？—— C# 的 `IsPositive(s)` 是"回应不以 ~r~ 开头"，
//   而端口的回应文本由 `CC_ToneBucket(性格)`（0 冷 / 1 中 / 2 暖）决定，
//   所以这里用语气桶 + 声望 + 心情凑出等价判据。
// ★ 用在两处：①「调情」的关系跳档 roll ②「表白」的成功判定。
FUNC BOOL CC_RespPositive(INT pIdx)
    IF pIdx < 0 OR pIdx > 19
        RETURN FALSE
    ENDIF
    // ☠☠ 审查补上的一句：下面要读 `g_alMood[g_curAlIdx]`，
    //   而 `g_curAlIdx = -1` 时那是**越界读** —— 不报错、只给垃圾（工程纪律第一条）。
    //   ★ 只靠 `CC_Rep()` 挡不住：它在条目不可用时返回 0，会**放过**这个守卫。
    IF NOT CC_AlOk()
        RETURN FALSE
    ENDIF
    IF CC_Rep() <= RST_AVOIDING          // 声望 ≤ -1 ⇒ 一定负面
        RETURN FALSE
    ENDIF
    // 语气桶：与 `cc_applines.CC_ToneBucket` **同一口径**。
    // ☠ 这里内联而不调它，是因为 SanScript 要求**先定义后使用**，
    //   而 `cc_applines.sc` 的合并顺序在 `cc_part1.sc` **之后**
    //   （第一版直接调 CC_ToneBucket，sc.exe 报 `Unrecognized variable or command name`）。
    //   改动两份时记得同步。
    INT b = 1
    IF FRIENDLINESS[pIdx] >= 70
        b = 2
    ELIF AGGRESSIVENESS[pIdx] >= 80 OR FRIENDLINESS[pIdx] <= 45
        b = 0
    ENDIF
    IF b = 0                             // 冷淡语气 ⇒ 负面
        RETURN FALSE
    ENDIF
    IF b = 2                             // 暖语气 ⇒ 正面
        RETURN TRUE
    ENDIF
    // 中性语气：看声望和心情（心情 4=生气 / 5=烦躁 时算负面）
    IF g_alMood[g_curAlIdx] = 4 OR g_alMood[g_curAlIdx] = 5
        RETURN FALSE
    ENDIF
    RETURN CC_Rep() >= 10
ENDFUNC

// ★★★ v0.75：「调情」的关系跳档
//   （C# `_convSelectedBranch == 3` 分支，CinnamonCoffee.cs:8171-8208）
//
//   【口径】
//     ① 已经是「朋友区」⇒ 只扣 1 点声望（C#：她自己说过"这样很尴尬"），不跳档
//     ② 已经是「暧昧」或更高 ⇒ 什么都不做（C# 的 `isAlreadyFlirty`）
//     ③ 否则：**先按原状态记下跳档概率**（C# 是先 `flirtRel = …` 再加声望），
//        加 10 点声望（C# 的 `flirtBonus`），然后掷：
//          认识 65% ｜ 熟识 35% ｜ 友好 10% ｜ 陌生人 0%（跳不了）
//        掷中 ⇒ 关系变「暧昧」
//        没掷中且当前是熟识/友好 ⇒ 再掷 1% 掉「朋友区」
//
//   ★ C# 里还有一条"菜单第 8 项是「我爱你」，女友 + 声望>80 时 flirtBonus=15"的规则；
//     端口没有那个菜单项（聊天菜单是压平的），所以这里固定 10。
PROC CC_FlirtPivot()
    IF NOT CC_AlOk()
        EXIT
    ENDIF
    INT st = g_alRSt[g_curAlIdx]        // ★ 先抓状态（C# 也是先抓再加声望）
    INT pIdx = g_alPers[g_curAlIdx]
    // ── 正面回应（C# 的 `if (isPositive)` 分支）──────────────────────────────
    IF CC_RespPositive(pIdx)
        IF st = RST_FRIENDZONE
            // 她把你当朋友 —— 调情只是尴尬（C#：声望 -1）
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 1
            CC_ClampRep(g_curAlIdx)
            AL_UpdateRel(g_curAlIdx)
            EXIT
        ENDIF
        IF st >= RST_FLIRTY
            EXIT                             // 已经是暧昧/女友 ⇒ 不再跳档（C# isAlreadyFlirty）
        ENDIF
        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 10      // C# 的 flirtBonus
        CC_ClampRep(g_curAlIdx)
        AL_UpdateRel(g_curAlIdx)
        INT ch = 0
        IF st = RST_ACQUAINT
            ch = 65
        ELIF st = RST_FAMILIAR
            ch = 35
        ELIF st = RST_FRIENDLY
            ch = 10
        ENDIF
        IF ch > 0
            IF CC_ROLL(ch)
                g_alRSt[g_curAlIdx] = RST_FLIRTY
            ELIF st = RST_FAMILIAR OR st = RST_FRIENDLY
                IF CC_ROLL(1)
                    g_alRSt[g_curAlIdx] = RST_FRIENDZONE
                ENDIF
            ENDIF
        ENDIF
        EXIT
    ENDIF
    // ── 负面回应（C# 的 else 分支：声望 -10 + 朋友区风险）──────────────────────
    //   ★ C# 里还有一条"菜单第 8 项 = 「我爱你」，被拒时按是否女友扣 5/10"的规则；
    //     端口的聊天菜单是压平的、没有那一项 ⇒ 这里只走"普通调情被拒"。
    IF st = RST_FRIENDZONE
        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 1
    ELSE
        g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 10
        IF st = RST_FAMILIAR AND CC_ROLL(8)
            g_alRSt[g_curAlIdx] = RST_FRIENDZONE
        ELIF st = RST_FRIENDLY AND CC_ROLL(15)
            g_alRSt[g_curAlIdx] = RST_FRIENDZONE
        ENDIF
    ENDIF
    CC_ClampRep(g_curAlIdx)
    AL_UpdateRel(g_curAlIdx)
ENDPROC

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.76：三个"按关系档算接受率"的 helper —— 全部照抄 C#，一律返回**整数百分比**
//
//  【为什么重写】端口原来的升温接受率是 `15 + 声望/2 + 性格修正`，
//    完全**没有用到关系档** ⇒ 女友和陌生人差别很小，跟"炮友向"的体感不符。
//    C# 是明确按关系档给的（女友 0.85、痴迷 0.92…）。这里逐条照抄。
//
//  【口径】`RISKINESS[]` / `FRIENDLINESS[]` / `ATTACHMENT[]` 都是 0..100（C# 的 0..1 ×100），
//    所以 `risk * 28 / 100` 就等于 C# 的 `risk * 0.28`。
//    `声望 * 0.003` ⇒ 整数 `声望 * 3 / 10`。
//  ☠ 三个函数都在 `g_alRSt` / 那些属性数组都定义之后，且被 cc_part2d 调用（合并顺序在后）✓
// ════════════════════════════════════════════════════════════════════════════

// 升温（Escalate）的接受率 —— C# CinnamonCoffee.cs:8559-8569
FUNC INT CC_EscAccPct(INT line, INT pIdx)
    // ══════════════════════════════════════════════════════════════════════════
    //  ★★ 审查修正：**沙盒模式必须保持原样**
    //    用户的原话是"上来就能开干的沙盒不能删"。而沙盒里的女伴 A-Life 条目
    //    声望恒为 0 ⇒ 关系档永远是「陌生人」 ⇒ 走下面那套新公式会变成 **1~8%**
    //    ⇒ **等于把沙盒的升温功能废掉了**。所以这里原样返回 v0.76 之前的旧公式。
    //  ☠ 教训：**换公式时要先想"每个模式在新公式下会落到哪一档"** ——
    //    新公式依赖一个在某个模式里恒为初始值的量（这里是声望/关系档），
    //    就会悄悄改变那个模式的整体手感。
    // ══════════════════════════════════════════════════════════════════════════
    IF g_sandboxMode
        INT a0 = 15 + CC_Rep() / 2
        IF FRIENDLINESS[pIdx] >= 70
            a0 = a0 + 20
        ENDIF
        IF AGGRESSIVENESS[pIdx] >= 80
            a0 = a0 - 20
        ENDIF
        IF line = 2
            a0 = a0 + (AGGRESSIVENESS[pIdx] - 50) * 22 / 100
        ELIF line = 0
            a0 = a0 + AGGRESSIVENESS[pIdx] * 5 / 100
        ENDIF
        IF a0 < 5
            a0 = 5
        ENDIF
        IF a0 > 95
            a0 = 95
        ENDIF
        RETURN a0
    ENDIF
    INT r = RISKINESS[pIdx]
    INT rel = RST_STRANGER
    IF CC_AlOk()
        rel = g_alRSt[g_curAlIdx]
    ENDIF
    INT a = 0
    IF rel = RST_OBSESSED
        a = 92
    ELIF rel = RST_GIRLFRIEND
        a = 85
    ELIF rel = RST_FLIRTY
        a = 52 + r * 28 / 100
    ELIF rel = RST_FRIENDLY
        a = 22 + r * 25 / 100
    ELIF rel = RST_FAMILIAR
        a = 8 + r * 20 / 100
    ELIF rel = RST_ACQUAINT
        a = 3 + r * 12 / 100
    ELSE
        a = 1 + r * 7 / 100
    ENDIF
    // 说了哪一句：直白那句（2）对冒险型加分；含蓄那句（0）小幅加分
    IF line = 2
        a = a + (r - 50) * 22 / 100
    ELIF line = 0
        a = a + r * 5 / 100
    ENDIF
    a = a + CC_Rep() * 3 / 10
    IF a < 2
        a = 2
    ENDIF
    IF a > 98
        a = 98
    ENDIF
    RETURN a
ENDFUNC

// 「邀她上车」的接受率 —— C# CinnamonCoffee.cs:7693-7702
//   ★ 女友 / 朋友区 / 痴迷 ⇒ C# 里 **根本不走拒绝**（`IsALifeVehicleEntryGated` 直接放行）
//     ⇒ 这里给 95，等于"几乎不拒"。
FUNC INT CC_VehAccPct(INT pIdx)
    // ★★ 审查修正：沙盒**原本从不拒绝**（v0.76 之前）⇒ 返回 100 ⇒ `CC_ROLL(100)` 恒真。
    //   同 CC_EscAccPct 的理由：沙盒里声望恒 0 ⇒ 关系档永远是「陌生人」⇒ 新公式只有 3~10%。
    IF g_sandboxMode
        RETURN 100
    ENDIF
    INT r = RISKINESS[pIdx]
    INT rel = RST_STRANGER
    IF CC_AlOk()
        rel = g_alRSt[g_curAlIdx]
    ENDIF
    INT a = 0
    IF rel >= RST_FRIENDZONE
        a = 95
    ELIF rel = RST_FLIRTY
        a = 65 + r * 15 / 100
    ELIF rel = RST_FRIENDLY
        a = 44 + r * 12 / 100
    ELIF rel = RST_FAMILIAR
        a = 20 + r * 20 / 100
    ELIF rel = RST_ACQUAINT
        a = 8 + r * 12 / 100
    ELSE
        a = 3 + r * 7 / 100
    ENDIF
    a = a + CC_Rep() * 3 / 10
    IF a < 3
        a = 3
    ENDIF
    IF a > 95
        a = 95
    ENDIF
    RETURN a
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★★★ v0.78：「自动上车」门控 —— C# `IsALifeVehicleEntryGated`（17425-17435）
//
//  C# 原文规则：
//      if (!aLifeMode || !sandboxMode) return false;   // 门控只在 "A-Life + 沙盒" 生效
//      if (_invitedToVehicle) return false;            // 已经请过一次 ⇒ 不再门控
//      if (dGate.IsHooker) return false;               // 街女永远跟随
//      if (rel == Friendzoned / Girlfriend / Obsessed) return false;   // 这三档自动上车
//      return true;                                    // 其余（陌生…友好）⇒ 必须请
//  被门控时 C# 做什么（9958 / 12432 / 12766 三处同一套）：
//      girl.Task.ClearAll(); girl.BlockPermanentEvents = true;
//  ⇒ 她**不上车**，原地等你请（「邀请上车」菜单项）。而 `ShowInviteVehicleItem`
//     （17442）用的就是**同一套条件** ⇒ C# 里"该请"和"有这一项"是等价的。
//
//  ★ 端口两处**有意的偏离**（下面用注释标了，方便一行改回）：
//   ① 去掉 C# 的 `!sandboxMode ⇒ 不门控`：C# 那条的理由是"搭讪模式的女伴一律算收费街女
//      （`IsHooker`），街女永远跟随"。端口 v0.74 已去妓女化（`g_alNoHooker = TRUE`）
//      ⇒ 那个类别在端口**不存在**了，留着它等于"门控永远不生效"（白装）。
//      所以端口改成**只在搭讪模式门控**。
//   ② 沙盒模式短路：沙盒的定位是零摩擦（你明确要求过不许改沙盒手感），而且 v0.77b
//      已经给三个接受率 helper 都加了同样的沙盒短路 ⇒ 这里保持一致。
//      想让沙盒也门控：删掉下面那个 `IF g_sandboxMode … ENDIF`。
// ════════════════════════════════════════════════════════════════════════════
FUNC BOOL CC_VehEntryGated()
    IF NOT g_aLifeMode
        RETURN FALSE             // 非 A-Life（普通路人跟随）不适用
    ENDIF
    IF g_sandboxMode
        RETURN FALSE             // ★ 偏离②：沙盒零摩擦（见上）
    ENDIF
    IF NOT g_hasGirl
        RETURN FALSE
    ENDIF
    IF g_vehInvited
        RETURN FALSE             // 已经请过一次 ⇒ 之后一路免请（C# 同）
    ENDIF
    // ☠ 数组读取必须套在 CC_AlOk() 里：g_curAlIdx = -1 时会越界（读到的只是垃圾）。
    IF CC_AlOk()
        IF g_alHooker[g_curAlIdx]
            RETURN FALSE         // 收费街女永远跟随（端口去妓女化后恒不成立，保留以对齐 C#）
        ENDIF
        INT vgSt = g_alRSt[g_curAlIdx]
        IF vgSt = RST_FRIENDZONE OR vgSt >= RST_GIRLFRIEND
            RETURN FALSE         // 朋友区 / 女友 / 痴迷 ⇒ 自动上车（C# 同）
        ENDIF
    ENDIF
    RETURN TRUE
ENDFUNC

// 「试探一下」第 1 项（"你让人难以抗拒"）的接受率 —— C# CinnamonCoffee.cs:8432-8440
FUNC INT CC_TwAccPct(INT pIdx)
    // ★★ 审查修正：沙盒保持老公式（`40 + 声望×3` ⇒ 声望 0 时 40%）。
    //   否则沙盒里"试探一下"会从 40% 掉到 8~13%。
    IF g_sandboxMode
        INT t0 = 40 + CC_Rep() * 3
        IF t0 > 95
            t0 = 95
        ENDIF
        RETURN t0
    ENDIF
    INT fri = FRIENDLINESS[pIdx]
    INT att = ATTACHMENT[pIdx]
    INT rel = RST_STRANGER
    IF CC_AlOk()
        rel = g_alRSt[g_curAlIdx]
    ENDIF
    INT a = 0
    IF rel >= RST_GIRLFRIEND
        a = 95
    ELIF rel = RST_FLIRTY
        a = 80 + att * 10 / 100
    ELIF rel = RST_FRIENDLY
        a = 55 + fri * 20 / 100
    ELIF rel = RST_FAMILIAR
        a = 35 + fri * 15 / 100
    ELIF rel = RST_ACQUAINT
        a = 20 + fri * 10 / 100
    ELSE
        a = 8 + fri * 5 / 100
    ENDIF
    a = a + CC_Rep() * 4 / 10
    IF a < 4
        a = 4
    ENDIF
    IF a > 95
        a = 95
    ENDIF
    RETURN a
ENDFUNC

PROC CC_ApplyMoodFace(PED_INDEX p, INT mood)
    IF NOT CC_EXISTS(p)
        EXIT
    ENDIF
    STRING animName = "mood_normal_1"
    IF mood = 1
        animName = "mood_happy_1"
    ELIF mood = 2
        animName = "mood_aiming_1"
    ELIF mood = 3
        animName = "mood_happy_1"
    ELIF mood = 4
        animName = "mood_angry_1"
    ELIF mood = 5
        animName = "mood_stressed_1"
    ELIF mood = 6
        animName = "mood_injured_1"
    ELSE
        animName = "mood_normal_1"
    ENDIF
    SET_FACIAL_IDLE_ANIM_OVERRIDE(p, animName)
ENDPROC

PROC CC_PlaySpeech(STRING speechName)
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    STOP_CURRENT_PLAYING_AMBIENT_SPEECH(g_girl)
    SET_AUDIO_FLAG("IsDirectorModeActive", TRUE)
    IF NOT IS_STRING_NULL(g_girlVoice)
        IF NOT ARE_STRINGS_EQUAL(g_girlVoice, "")
            // ★ 2026-09-25 修：末参数 SyncOverNetwork 必须**显式传 FALSE**。
            //   .sch 声明：
            //     PLAY_PED_AMBIENT_SPEECH_WITH_VOICE_NATIVE(..., BOOL SyncOverNetwork = FALSE)
            //     PLAY_PED_AMBIENT_SPEECH_NATIVE(...,           BOOL SyncOverNetwork = TRUE)
            //   —— 注意**两个默认值相反**。C# 原版两处都显式传 0(=FALSE)。
            //   不传的话 NATIVE 版会拿到 TRUE，网络同步语义下单人游戏里
            //   这句语音可能根本不播（静默）。
            PLAY_PED_AMBIENT_SPEECH_WITH_VOICE_NATIVE(g_girl, speechName, g_girlVoice, "SPEECH_PARAMS_FORCE_SHOUTED", FALSE)
            SET_AUDIO_FLAG("IsDirectorModeActive", FALSE)
            // ★ 临时诊断：绿字出现了 = 脚本确实在发语音指令（且嗓子已设）。
            //   若看得到绿字却没声音 ⇒ 问题在 native/音频资源，不在脚本逻辑。
            EXIT
        ENDIF
    ENDIF
    PLAY_PED_AMBIENT_SPEECH_NATIVE(g_girl, speechName, "SPEECH_PARAMS_FORCE_SHOUTED", FALSE)
    SET_AUDIO_FLAG("IsDirectorModeActive", FALSE)
    // ★ 临时诊断：出现红字 = 走到"没有嗓子"那条分支 ⇒ CC_AssignVoice 没起作用
    CC_HudDiag("~r~VOICE: no voice name set!", 3000)
ENDPROC

// 嗓音下标夹紧：CC_VOICE_NAME 只定义 0..7（v0.79 删掉一个坏嗓子后是 8 个），
// 超出会落到默认（能出声但不可控），而自动轮换需要严格 0..7 才好循环。
FUNC INT CC_VoiceIdxClamp(INT v)
    IF v < 0
        RETURN 0
    ENDIF
    IF v > 7
        RETURN 7
    ENDIF
    RETURN v
ENDFUNC

PROC CC_AssignVoice()
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    // ★★★ 2026-09-25 修复（第二轮）：这原来是
    //     `IF g_curAlIdx < 0 → EXIT` 和 `IF NOT g_aLifeMode 且模型是 stock hooker → EXIT`。
    //   两条 EXIT 都会**既不设 g_girlVoice、也不调 SET_AMBIENT_VOICE_NAME**。
    //   而 g_aLifeMode 默认 = FALSE、沙盒模式下 g_curAlIdx 多半 = -1，
    //   女孩又是 stock hooker 模型 ⇒ 两条都命中 ⇒ g_girlVoice 永远是空串 ⇒
    //     · CC_PlaySpeech 掉到无 voice 分支
    //     · CC_ReapplyTick 因为 g_girlVoice 为空而什么都不做
    //   ⇒ **全程一点声音都没有**（动作能播、静音）。
    //
    //   对照 C# 原版：沙盒分支里**没有**"stock 模型就跳过"这回事 ——
    //   它就是 `voice = HOOKER_VOICES[rng.Next(...)]` 然后**总是**
    //   `SET_AMBIENT_VOICE_NAME(ped, voice)`。移植时把 A-Life 分支读错了。
    //
    //   ★★★ v0.66 改：不再"已有嗓子就只重设"，而是**统一由 `g_voiceIdx` 驱动**
    //      （0..8）。这样"嗓子自动轮换"（见 cc_main 的 CC_VoiceProbeTick）才成立：
    //      轮换时改 `g_voiceIdx` 即可，重新定嗓也走同一条路，不会两套口径打架。
    //      `g_voiceIdx = -1` 表示"这个女伴还没定过嗓"。
    //
    //   ★★★ v0.70 改：定嗓口径改成 **每个女伴单独随机**（用户要求：
    //      "不是每句都随机，就是每个人单独随机"）。
    //
    //      旧写法取的是 `g_alVoice[g_curAlIdx]` —— 那是**按 A-Life 条目**存的，
    //      而条目是按"外观指纹"（模型 hash + 12 个部件的 drawable/texture）认人的。
    //      ⇒ **两个同款同打扮的路人女会撞进同一条目** ⇒ 同一个名字、
    //        同一个性格、**同一个嗓子**。这就是"每个女伴声音都一样"的来源。
    //      （同理，撞条目时名字/性格也会一样 —— 同一个根因。）
    //
    //      新写法：**每次"换人"现摇一次**（`g_voiceIdx` 在 CC_FullReset 里回 -1，
    //      每人只摇一次、句内不再变），并顺手写回条目（让"同名同性格"时口径一致）。
    //      · 用户若在设置第 7 项**钉死**了某个序号（`g_voiceFix >= 0`）⇒ 全员统一用它；
    //      · 否则（`g_voiceFix = -1`，默认"随机"）⇒ 每人一个。
    IF g_voiceIdx < 0
        IF g_voiceFix >= 0
            g_voiceIdx = g_voiceFix
        ELSE
            // ★ v0.72：随机也要**跳过已知无音频的嗓子**（见 CC_VoiceRandOk），
            //   否则会有女伴被分到一个"哑巴"嗓子，表现为"有的女伴有声、有的没声"。
            g_voiceIdx = CC_VoiceRandOk()
        ENDIF
        g_voiceLocked = FALSE
        g_voiceRotate = 0
        IF CC_AlOk()
            g_alVoice[g_curAlIdx] = g_voiceIdx
        ENDIF
    ENDIF
    g_girlVoice = CC_VOICE_NAME(g_voiceIdx)
    SET_AMBIENT_VOICE_NAME(g_girl, g_girlVoice)
    g_voiceReapplyAt = GET_GAME_TIMER() + 3000
ENDPROC

PROC CC_PlayMoaning()
    IF NOT g_voiceMoan
        EXIT
    ENDIF
    IF NOT CC_ALIVE(g_girl)
        EXIT
    ENDIF
    IF IS_STRING_NULL(g_curAction)
        EXIT
    ENDIF
    IF ARE_STRINGS_EQUAL(g_curAction, "kissing")
        EXIT
    ENDIF
    IF g_aLifeMode
        IF g_curAlIdx >= 0
            IF g_alRep[g_curAlIdx] <= -1
                IF NOT CC_ROLL(1)
                    EXIT
                ENDIF
            ENDIF
        ENDIF
    ENDIF
    CC_AssignVoice()
    STRING sp = "SEX_GENERIC"
    IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
        sp = "SEX_ORAL"
    ENDIF
    CC_PlaySpeech(sp)
    IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
        g_lastGtaSpeech = GET_GAME_TIMER()
    ENDIF
ENDPROC

PROC CC_PlayServiceVoice()
    IF NOT g_voiceSvc
        EXIT
    ENDIF
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    CC_AssignVoice()
    CC_PlaySpeech("HOOKER_OFFER_SERVICE")
ENDPROC

PROC CC_PlaySpit()
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    // ★ 原来写的是"请求 + 同一帧检查 HAS_ANIM_DICT_LOADED" —— 同一个道理，
    //   请求刚发出去那帧标志位几乎不可能就绪，冷加载时这个表情永远不播。
    //   改成：已加载就播，没加载就发请求（下帧/下次才可能就绪）。
    //   不为一个装饰性表情把脚本线程堵住。
    IF HAS_ANIM_DICT_LOADED("mp_facial")
        PLAY_FACIAL_ANIM(g_girl, "mic_chatter", "mp_facial")
    ELSE
        REQUEST_ANIM_DICT("mp_facial")
    ENDIF
    CC_PlaySpeech("GENERIC_CURSE_HIGH")
ENDPROC

PROC CC_PlayFinishVoice()
    IF IS_STRING_NULL(g_curAction)
        EXIT
    ENDIF
    IF ARE_STRINGS_EQUAL(g_curAction, "kissing")
        EXIT
    ENDIF
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    IF g_bjSoundsOn
        IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
            IF g_mode != CCM_CAR
                IF NOT g_sitPropActive
                    IF NOT g_finInside
                        IF g_aLifeMode
                            IF g_finForced OR g_finBroke
                                CC_PlaySpit()
                            ENDIF
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
    ENDIF
    IF NOT g_voiceEnd
        EXIT
    ENDIF
    CC_AssignVoice()
    IF g_finForced OR g_finBroke
        IF g_aLifeMode
            IF g_curAlIdx >= 0
                g_alMood[g_curAlIdx] = 4
                CC_ApplyMoodFace(g_girl, 4)
            ENDIF
        ENDIF
        EXIT
    ENDIF
    IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
        CC_PlaySpeech("HOOKER_OFFER_AGAIN")
        EXIT
    ENDIF
    g_postSexPhase = 1
    CC_PlaySpeech("SEX_FINISHED")
ENDPROC

FUNC INT CC_PriceLo(INT grpI)
    SWITCH grpI
        CASE 0 RETURN 20
        CASE 1 RETURN 20
        CASE 2 RETURN 20
        CASE 3 RETURN 40
    ENDSWITCH
    RETURN 40
ENDFUNC

FUNC INT CC_PriceHi(INT grpI)
    SWITCH grpI
        CASE 0 RETURN 80
        CASE 1 RETURN 60
        CASE 2 RETURN 60
        CASE 3 RETURN 80
    ENDSWITCH
    RETURN 100
ENDFUNC

FUNC INT CC_NegotiateChance(INT grpI)
    SWITCH grpI
        CASE 0 RETURN 35
        CASE 1 RETURN 40
        CASE 2 RETURN 50
        CASE 3 RETURN 30
    ENDSWITCH
    RETURN 20
ENDFUNC

FUNC INT CC_AvailChance(INT grpI)
    SWITCH grpI
        CASE 0 RETURN 15
        CASE 1 RETURN 60
        CASE 2 RETURN 20
        CASE 3 RETURN 55
    ENDSWITCH
    RETURN 40
ENDFUNC

FUNC INT CC_GroupOf(INT carFlag, INT svcIdx)
    IF carFlag = 1
        IF svcIdx = 0
            RETURN 1
        ENDIF
        RETURN 3
    ENDIF
    SWITCH svcIdx
        CASE 0 RETURN 0
        CASE 1 RETURN 2
        CASE 2 RETURN 1
        CASE 3 RETURN 1
        CASE 4 RETURN 1
        CASE 5 RETURN 3
        CASE 6 RETURN 3
        CASE 7 RETURN 3
        CASE 8 RETURN 3
        CASE 9 RETURN 3
        CASE 10 RETURN 4
    ENDSWITCH
    RETURN 4
ENDFUNC

PROC PR_Generate(INT alIdx)
    IF alIdx < 0
        EXIT
    ENDIF
    INT baseI = alIdx * 5
    INT grpI = 0
    WHILE grpI < 5
        IF CC_ROLL(CC_AvailChance(grpI))
            g_prAvail[baseI + grpI] = TRUE
            g_prPrice[baseI + grpI] = CC_RAND(CC_PriceLo(grpI), CC_PriceHi(grpI) + 1)
        ELSE
            g_prAvail[baseI + grpI] = FALSE
            g_prPrice[baseI + grpI] = 0
        ENDIF
        g_prLock[baseI + grpI] = FALSE
        grpI = grpI + 1
    ENDWHILE
ENDPROC

PROC PR_GenerateALifeHooker(INT alIdx)
    IF alIdx < 0
        EXIT
    ENDIF
    INT baseI = alIdx * 5
    INT lo5[5]
    INT hi5[5]
    lo5[0] = 60
    hi5[0] = 110
    lo5[1] = 80
    hi5[1] = 150
    lo5[2] = 80
    hi5[2] = 130
    lo5[3] = 130
    hi5[3] = 220
    lo5[4] = 140
    hi5[4] = 240
    INT grpI = 0
    WHILE grpI < 5
        g_prAvail[baseI + grpI] = TRUE
        g_prPrice[baseI + grpI] = CC_RAND(lo5[grpI], hi5[grpI] + 1)
        g_prLock[baseI + grpI] = FALSE
        grpI = grpI + 1
    ENDWHILE
ENDPROC

PROC PR_GenerateProstHooker(INT alIdx)
    IF alIdx < 0
        EXIT
    ENDIF
    INT baseI = alIdx * 5
    INT lo5[5]
    INT hi5[5]
    lo5[0] = 20
    hi5[0] = 70
    lo5[1] = 20
    hi5[1] = 55
    lo5[2] = 20
    hi5[2] = 55
    lo5[3] = 35
    hi5[3] = 70
    lo5[4] = 35
    hi5[4] = 80
    INT grpI = 0
    WHILE grpI < 5
        g_prAvail[baseI + grpI] = TRUE
        g_prPrice[baseI + grpI] = CC_RAND(lo5[grpI], hi5[grpI] + 1)
        g_prLock[baseI + grpI] = FALSE
        grpI = grpI + 1
    ENDWHILE
ENDPROC

FUNC INT PR_GetPrice(INT alIdx, INT carFlag, INT svcIdx)
    IF alIdx < 0
        RETURN 0
    ENDIF
    INT grpI
    grpI = CC_GroupOf(carFlag, svcIdx)
    RETURN g_prPrice[alIdx * 5 + grpI]
ENDFUNC

PROC PR_Negotiate(INT alIdx, INT carFlag, INT svcIdx)
    IF alIdx < 0
        EXIT
    ENDIF
    INT grpI
    grpI = CC_GroupOf(carFlag, svcIdx)
    INT priceI = alIdx * 5 + grpI
    IF g_prLock[priceI]
        EXIT
    ENDIF
    IF NOT CC_ROLL(CC_NegotiateChance(grpI))
        g_negotiate = FALSE
        EXIT
    ENDIF
    INT newPrice = g_prPrice[priceI] - CC_RAND(5, 16)
    INT priceFloor
    priceFloor = CC_PriceLo(grpI) - 10
    IF priceFloor < 5
        priceFloor = 5
    ENDIF
    IF newPrice <= priceFloor
        newPrice = priceFloor
        g_prLock[priceI] = TRUE
    ENDIF
    g_prPrice[priceI] = newPrice
ENDPROC

PROC CC_AttachGirlBlip()
    IF g_girlBlipValid
        EXIT
    ENDIF
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    g_girlBlip = ADD_BLIP_FOR_ENTITY(g_girl)
    // ★★ 2026-09-25晚 v0.57 用户要求：伴侣的地图标点 **红 → 黄**。
    //   ADD_BLIP_FOR_ENTITY 建出来的是**默认红点**，所以必须显式设色。
    //   颜色号 = 官方 commands_hud.sch 的 BLIP_COLOUR_* 常量：
    //     1 = 红  2 = 绿  3 = 蓝  4 = 白  **5 = 黄**（BLIP_COLOUR_YELLOW）
    //   这里直接写字面量 5，和僵尸模组自己的写法一致（它写的是 SET_BLIP_COLOUR(..., 1)），
    //   避免为一个常量去多引一个 .sch。
    //   ★ 若实机看着不是黄（例如被 minimap 主题改写），改这一行即可：
    //     黄色 = 5，蓝色 = 3。
    SET_BLIP_COLOUR(g_girlBlip, 5)
    g_girlBlipValid = TRUE
ENDPROC

PROC CC_RemoveGirlBlip()
    IF g_girlBlipValid
        REMOVE_BLIP(g_girlBlip)
    ENDIF
    g_girlBlip = NULL
    g_girlBlipValid = FALSE
ENDPROC

PROC CC_DetachDildo()
    IF DOES_ENTITY_EXIST(g_dildo)
        DETACH_ENTITY(g_dildo, TRUE, TRUE)
        DELETE_OBJECT(g_dildo)
    ENDIF
    g_dildo = NULL
ENDPROC

PROC CC_DildoOffsets(FLOAT &ox, FLOAT &oy, FLOAT &oz, FLOAT &rx, FLOAT &ry, FLOAT &rz)
    ox = 0.04
    oy = 0.13
    oz = 0.0
    rx = 250.0
    ry = -35.0
    rz = 0.0
    IF g_mode = CCM_CAR
        IF g_backseatCarSex
            ox = 0.145
            oy = 0.13
            oz = -0.01
            rx = 255.0
            ry = 44.0
            rz = 0.0
            EXIT
        ENDIF
        IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
            IF g_vehicleAnimType = VAT_LOW
                ox = 0.05
                oy = 0.15
                oz = 0.0
                rx = 260.0
                ry = -15.0
                rz = 0.0
            ELSE
                ox = 0.04
                oy = 0.13
                oz = 0.0
                rx = 260.0
                ry = -35.0
                rz = 0.0
            ENDIF
        ELSE
            IF g_vehicleAnimType = VAT_LOW
                ox = 0.07
                oy = 0.14
                oz = 0.01
                rx = 270.0
                ry = -35.0
                rz = 0.0
            ELSE
                ox = 0.07
                oy = 0.14
                oz = 0.025
                rx = 280.0
                ry = -35.0
                rz = 0.0
            ENDIF
        ENDIF
        EXIT
    ENDIF
    SWITCH g_streetActionIdx
        CASE 2
            ox = 0.07
            oy = 0.15
            oz = 0.0
            rx = 240.0
            ry = -8.0
            rz = 0.0
            BREAK
        CASE 3
            ox = 0.04
            oy = 0.13
            oz = 0.0
            rx = 260.0
            ry = -35.0
            rz = 0.0
            BREAK
        CASE 4
            ox = 0.09
            oy = 0.16
            oz = 0.01
            rx = 260.0
            ry = -5.0
            rz = 0.0
            BREAK
        CASE 5
            ox = 0.085
            oy = 0.15
            oz = 0.03
            rx = 283.0
            ry = -22.0
            rz = 0.0
            BREAK
        CASE 6
            ox = 0.07
            oy = 0.14
            oz = 0.01
            rx = 270.0
            ry = -35.0
            rz = 0.0
            BREAK
        CASE 7
            ox = 0.07
            oy = 0.14
            oz = 0.01
            rx = 270.0
            ry = 2.0
            rz = 0.0
            BREAK
        CASE 8
            ox = 0.07
            oy = 0.14
            oz = 0.01
            rx = 270.0
            ry = 2.0
            rz = 0.0
            BREAK
        CASE 9
            ox = 0.01
            oy = 0.14
            oz = 0.01
            rx = 270.0
            ry = -16.0
            rz = 0.0
            BREAK
        CASE 10
            ox = 0.104
            oy = 0.14
            oz = 0.0
            rx = 265.0
            ry = -32.0
            rz = 0.0
            BREAK
        CASE 11
            ox = 0.13
            oy = 0.14
            oz = -0.0275
            rx = 230.0
            ry = -5.0
            rz = 0.0
            BREAK
    ENDSWITCH
ENDPROC

PROC CC_AttachDildo()
    IF NOT g_dildoOn
        EXIT
    ENDIF
    CC_DetachDildo()
    INT mdl = GET_HASH_KEY("prop_cs_dildo_01")
    REQUEST_MODEL(INT_TO_ENUM(MODEL_NAMES, mdl))
    INT start = GET_GAME_TIMER()
    WHILE NOT HAS_MODEL_LOADED(INT_TO_ENUM(MODEL_NAMES, mdl))
        WAIT(0)
        IF GET_GAME_TIMER() - start > 1000
            EXIT
        ENDIF
    ENDWHILE
    IF NOT HAS_MODEL_LOADED(INT_TO_ENUM(MODEL_NAMES, mdl))
        EXIT
    ENDIF
    VECTOR pp = GET_ENTITY_COORDS(g_player)
    g_dildo = CREATE_OBJECT(INT_TO_ENUM(MODEL_NAMES, mdl), <<pp.x, pp.y, pp.z>>, TRUE, TRUE, TRUE)
    SET_MODEL_AS_NO_LONGER_NEEDED(INT_TO_ENUM(MODEL_NAMES, mdl))
    IF NOT DOES_ENTITY_EXIST(g_dildo)
        EXIT
    ENDIF
    FLOAT ox = 0.0
    FLOAT oy = 0.0
    FLOAT oz = 0.0
    FLOAT rx = 0.0
    FLOAT ry = 0.0
    FLOAT rz = 0.0
    CC_DildoOffsets(ox, oy, oz, rx, ry, rz)
    INT bone = GET_PED_BONE_INDEX(g_player, INT_TO_ENUM(PED_BONETAG, 11816))
    ATTACH_ENTITY_TO_ENTITY(g_dildo, g_player, bone, <<ox, oy, oz>>, <<rx, ry, rz>>, FALSE, FALSE, FALSE, FALSE, EULER_YXZ, TRUE, FALSE)
ENDPROC

// ── 输入读取（四通道, 参考 zombies_nx simple_zombies.sc 在本端口已验证的写法）──
//   FRONTEND 键要从 FRONTEND_CONTROL 组读（未禁用时走普通 IS_CONTROL_*）;
//   菜单打开时 PLAYER 组被禁用, 该组要用 IS_DISABLED_CONTROL_* 读。
//   四通道全读保证任意禁用状态下都能读到。
//
// ★★ 2026-09-25 重排：**DISABLED 通道提到最前**。
//   僵尸模组的菜单**只**读 `IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, …)`
//   就完全灵敏（用户实测"僵尸菜单没问题"），而本模组原来把"普通通道"写在最前。
//   菜单打开时 PLAYER 组已经被 DISABLE_ALL_CONTROL_ACTIONS 禁掉，普通通道在那里
//   本来就应该读到假值 —— 顺序不改变结果（任一通道为真即为真），但先命中未禁用
//   的通道既省 native 也更贴近"已验证可用"的那套写法。
//
// ★ 命名约定：xxxJust = 刚按下（边沿，只此一帧）; xxxHeld = 按住（电平）。
FUNC BOOL IN_UpJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_UpHeld()
    IF IS_DISABLED_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_UP)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_DownJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_DownHeld()
    IF IS_DISABLED_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_DOWN)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_LeftJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_LeftHeld()
    IF IS_DISABLED_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_LEFT)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_RightJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_RightHeld()
    IF IS_DISABLED_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_RIGHT)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_AcceptJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_ACCEPT)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_ACCEPT)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_ACCEPT)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_ACCEPT)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_CancelJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_CancelHeld()
    IF IS_DISABLED_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ELIF IS_CONTROL_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_CANCEL)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_XJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_X)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_X)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_X)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_X)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// ★ v0.60：备用复位键。本端口"按键 → GTA control"的映射没在 Switch 上验证过 X
//   （INPUT_FRONTEND_X），而 Y 在整套 CC 源码里**零占用**（grep 全 src 只有这里）。
//   两个键都认，实测哪个灵就留哪个 —— 成本为零，也不会和任何现有键冲突。
FUNC BOOL IN_YJust()
    IF IS_DISABLED_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_Y)
        RETURN TRUE
    ELIF IS_DISABLED_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_Y)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(PLAYER_CONTROL, INPUT_FRONTEND_Y)
        RETURN TRUE
    ELIF IS_CONTROL_JUST_PRESSED(FRONTEND_CONTROL, INPUT_FRONTEND_Y)
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// ★★★ 2026-09-25 用户要求：开启键从「RB + 十字键左」改成「B + 十字键右」。
//   本端口上 B = INPUT_FRONTEND_CANCEL（僵尸模组菜单里"单按 B 关闭"用的就是它，
//   用户已验证），十字键右 = INPUT_FRONTEND_RIGHT。
//   两种先后顺序都认（先按 B 再按右 / 先按右再按 B），最大限度好按。
//   ⚠ 十字键右同时是僵尸模组的"互动键"，所以 merge_mods.py 里给僵尸的互动键
//     加了"B 按住时不响应"的闸（否则开菜单会顺手互动一下）。
FUNC BOOL IN_Master()
    IF IN_CancelJust() AND IN_RightHeld()
        RETURN TRUE
    ENDIF
    IF IN_RightJust() AND IN_CancelHeld()
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL IN_MasterHeld()
    IF IN_CancelHeld() AND IN_RightHeld()
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// ── 兼容旧名（保持调用点不动） ──
FUNC BOOL IN_Up()
    RETURN IN_UpJust()
ENDFUNC

FUNC BOOL IN_Down()
    RETURN IN_DownJust()
ENDFUNC

FUNC BOOL IN_Select()
    RETURN IN_AcceptJust()
ENDFUNC

FUNC BOOL IN_Back()
    RETURN IN_CancelJust()
ENDFUNC

FUNC BOOL IN_SpeedUp()
    RETURN IN_RightHeld()
ENDFUNC

FUNC BOOL IN_SpeedDown()
    RETURN IN_LeftHeld()
ENDFUNC

FUNC BOOL IN_SpeedReset()
    // ★ v0.60：X 为主，Y 为同义备用（见 IN_YJust 上方注释）。
    //   两个都要判 —— 只要其中一个在 Switch 上真的能上来，复位就能用。
    IF IN_XJust()
        RETURN TRUE
    ENDIF
    RETURN IN_YJust()
ENDFUNC

// ════════════════════════════════════════════════════════════════════════════
//  ★★ 2026-09-25 新增：警察无视开关（设置菜单第 13 项）
//
//  【为什么需要】用户实测"和女伴在一起时触发警星，警察不攻击"。
//  真凶 = 女伴加入那一刻调的 `SET_EVERYONE_IGNORE_PLAYER(TRUE)`（C# 12425 同）——
//  这个 native 让**所有 ped**（不止警察，是所有人）无视玩家。
//
//  【唯一写点 CC_PoliceIgnoreSet(on)】所有想"无视/不无视玩家"的代码位置都走它：
//     · on=TRUE 且 开关=开启   → 真的发 SET_EVERYONE_IGNORE_PLAYER(TRUE)
//     · 其余情况（含开关=关闭）→ 发 FALSE
//     ⇒ 开关=关闭时，本模组**绝不可能**把玩家设成"被无视"（警察恢复原版、能打你）。
//     ⇒ 只与"上一次真正设过的值"（g_policeApplied）比较，变了才发 native
//       （用户同时开 MEGATARD，每帧顶掉别人的状态会打架）。
//
//  【为什么必须收敛成唯一写点】先前几处"动作收尾"直接裸调 FALSE，绕过了影子值：
//     加入时设 TRUE(影子=1) → 收尾裸设 FALSE(影子仍=1) → 此后闲逛 reconcile
//     以为"已经是 1 了"→ 不再重设 ⇒ **开关开着警察也能打你**（开关形同虚设）。
//     统一写点后影子值永远与真实状态一致，开关才可靠。
// ════════════════════════════════════════════════════════════════════════════

// 统一写点：任何想"无视玩家"的位置都调这里；开关关闭时会自动否决（换成 FALSE）。
PROC CC_PoliceIgnoreSet(BOOL wantOn)
    INT want = 0
    IF wantOn AND g_policeIgnore
        want = 1
    ENDIF
    IF want = g_policeApplied
        EXIT
    ENDIF
    g_policeApplied = want
    IF want = 1
        SET_EVERYONE_IGNORE_PLAYER(PLAYER_ID(), TRUE)
    ELSE
        SET_EVERYONE_IGNORE_PLAYER(PLAYER_ID(), FALSE)
    ENDIF
ENDPROC

// 每帧 reconcile：由**当前状态**推出"想不想无视"，再交给统一写点。
//   语义：开关开启时，"女伴在旁 / 动作或搭讪进行中"都无视玩家
//         （= C# 原版行为，且把"闲逛跟随期"也一并保持——这正是用户选的
//          「默认开启 = 警察无视你」）；开关关闭时 want 恒 0 ⇒ 警察恢复原版。
PROC CC_ApplyPoliceIgnore()
    BOOL wantOn = FALSE
    IF g_state = ST_ANIMATING OR g_state = ST_APPROACHING
        wantOn = TRUE
    ENDIF
    IF g_hasGirl
        wantOn = TRUE
    ENDIF
    CC_PoliceIgnoreSet(wantOn)
ENDPROC

// 坐/躺类动作号 (1/3/4/6/7/8/9)？—— 站姿(0/2/5/10/11) 是它的补集。
FUNC BOOL CC_IsSitLayIdx(INT idx)
    IF idx = 1 OR idx = 3 OR idx = 4 OR idx = 6 OR idx = 7 OR idx = 8 OR idx = 9
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// 当前是否正在播「站姿」街面动作（用户要求：站姿进行中不许切到坐/躺）。
FUNC BOOL CC_StandingActionActive()
    IF g_state != ST_ANIMATING
        RETURN FALSE
    ENDIF
    IF g_mode != CCM_STREET
        RETURN FALSE
    ENDIF
    IF g_streetActionIdx < 0
        RETURN FALSE
    ENDIF
    IF CC_IsSitLayIdx(g_streetActionIdx)
        RETURN FALSE
    ENDIF
    RETURN TRUE
ENDFUNC

PROC CC_DisableMovement()
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_MOVE_UP_ONLY, TRUE)
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_MOVE_DOWN_ONLY, TRUE)
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_MOVE_LEFT_ONLY, TRUE)
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_MOVE_RIGHT_ONLY, TRUE)
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_SPRINT, TRUE)
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_JUMP, TRUE)
ENDPROC

PROC CC_Pulse(STRING which)
    IF ARE_STRINGS_EQUAL(which, "fin")
        SET_CONTROL_SHAKE(PLAYER_CONTROL, 400, 255)
        g_shakeOffAt = GET_GAME_TIMER() + 400
    ELIF ARE_STRINGS_EQUAL(which, "slap")
        SET_CONTROL_TRIGGER_SHAKE(PLAYER_CONTROL, 40, 255, 40, 255)
    ELIF ARE_STRINGS_EQUAL(which, "kiss")
        SET_CONTROL_TRIGGER_SHAKE(PLAYER_CONTROL, 60, 120, 60, 120)
    ENDIF
ENDPROC

PROC CC_ShakeOff()
    IF g_shakeOffAt > 0
        IF GET_GAME_TIMER() >= g_shakeOffAt
            STOP_CONTROL_SHAKE(PLAYER_CONTROL)
            g_shakeOffAt = 0
        ENDIF
    ENDIF
ENDPROC
