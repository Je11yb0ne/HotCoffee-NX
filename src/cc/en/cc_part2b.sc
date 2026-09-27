// ============================================================================
// cc_part2b.sc — 第2b部分: 动作引擎(车/街) / Finish 流程 / 菜单系统 / 主循环
// 对应 C# StartCarAction / RunCarAnimation / StartStreetAction /
//       RunStreetSitAnimation / _finishExecutePending 执行段 / FinishXxxAndReopen /
//       Draw*Menu / HandleMenuSelect / OnTick
// ============================================================================

// ════════════════════════════════════════════════════════════════════════
//  车内动作 (C# StartCarAction)
// ════════════════════════════════════════════════════════════════════════
PROC CC_SetHealFor(BOOL hp, BOOL hg, FLOAT rate)
    g_healPlayer = hp
    g_healGirl = hg
    g_healRate = rate
ENDPROC

PROC CC_StartCarAction(INT idx)
    // ★ v0.63：① 记下"从哪个菜单页启动的"，动作结束后 CC_EndAnimation 用它重开菜单
    //          （C# 同款：`_postSexPendingMenu = MenuLevel.Services`）。
    //          此刻 g_menuLevel 还没被菜单侧改成 ML_NONE（改它的是 CC_MenuSelect 里
    //          "启动成功"之后那一段）⇒ 读到的是真正的来源页。
    //        ② 动作开场 500 ms 内**忽略调速键**：必须等 ←/→ 都松开过才允许改速度，
    //          免得"确认服务时压着的那一下"把 g_animSpeed 拖到 0 ⇒ 全程静音。
    g_actionReturnMenu = g_menuLevel
    g_speedImmuneUntil = GET_GAME_TIMER() + 500
    IF NOT CC_EXISTS(g_girl)
        CC_FullReset(FALSE)
        EXIT
    ENDIF
    IF NOT IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        EXIT
    ENDIF
    g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
    IF NOT DOES_ENTITY_EXIST(g_car)
        EXIT
    ENDIF
    IF NOT IS_PED_IN_ANY_VEHICLE(g_girl, FALSE)
        EXIT
    ENDIF
    IF GET_VEHICLE_PED_IS_IN(g_girl, FALSE) != g_car
        EXIT
    ENDIF
    // ★★★ 2026-09-25晚 关键修复：g_mode 必须在这里落到 CCM_CAR。
    //
    //   移植时把 C# `OpenServicesMenu()` 里的 `mode = Mode.Car;` 整段弄丢了
    //   （全工程只剩 `g_mode = CCM_NONE`），于是下列 IF 全部恒假：
    //     · CC_AnimatingTick: IF g_mode = CCM_CAR → CC_RunCarAnimation() 不被调用
    //       ⇒ 车相位机永不推进 ⇒ 卡在 phase 0（intro 停末帧）⇒ "播一下就不动"
    //       ⇒ 车内的诊断行也永远不会被打印
    //     · CC_AnimCommonTick（呻吟/速度/治疗）以 phase = 2 为前提 ⇒ 全程静音
    //     · CC_ExecuteFinish 的车内收尾分支进不去 ⇒ 拔枪/内射动画不播
    //     · CC_DildoOffsets 的车内偏移进不去 ⇒ 假体位置错
    //   放在这里（而不是菜单里）是刻意的：只要"开始一个车内动作"，
    //   g_mode 就一定是 CCM_CAR —— 不管从哪个菜单路径进来都对。
    g_mode = CCM_CAR
    // ★ v0.61 保险丝：收回"别人家的剧本相机"。
    //   原版 pb_prostitute 在车震时会用 CREATE_CAM + RENDER_SCRIPT_CAMS(TRUE) 接管视角
    //   （这就是"视角变成原版找站街女的样子、还锁死"的成因）。主修法是**选目标时
    //   跳过站街女**（见 cc_part1.sc 的 g_hookerHash），这里只是兜底：
    //   万一她身上那个原版脚本已经先把相机打开了，我们一进车服务就把它收掉。
    //   本模组自己**不用**任何剧本相机，所以这句对我们零副作用；
    //   僵尸模组全源码也没有用剧本相机（grep 过），不会误伤。
    RENDER_SCRIPT_CAMS(FALSE, FALSE)
    // 车内动作不记录落地点（车里没有负 zOffset 的沉地）；
    // 把哨兵值坐实，防止上一次街面动作的旧坐标被中止分支当成有效坐标。
    g_animOrigin = <<0.0, 0.0, -9000.0>>
    g_animOriginH = 0.0
    g_diagPhase = -1
    // 车内动作开始 → 想无视玩家（C# 13011/12935/13339 同）。
    // 走统一写点：开关=关闭时会被自动否决（警察恢复原版、能打你）。
    CC_PoliceIgnoreSet(TRUE)

    // ── 后座 (C# backseat: random@drunk_driver_2) ──
    IF CC_InBackSeat(g_car)
        // ★ 诊断探针（非致命 · 2026-09-25 改；v0.57 起并入**默认关闭**的诊断通道）
        //   DOES_ANIM_DICT_EXIST 在本端口**从未被证明**。若它自身是哑的（恒 FALSE），
        //   原来那句 EXIT 会把**所有**动画直接打死 —— 诊断反而变成故障源。
        //   现在改成「只报不拦」，且走 CC_HudDiag（g_showDiag 默认 FALSE ⇒ 平常不显示）：
        //     把 g_showDiag 改成 TRUE 后：看到这条黄字 = 该字典在本端口确实读不到
        //                                看不到       = 字典在，继续往下走
        IF NOT DOES_ANIM_DICT_EXIST("random@drunk_driver_2")
            CC_HudDiag("~y~Anim dict missing: random@drunk_driver_2", 2500)
        ENDIF
        IF NOT CC_ReqDict("random@drunk_driver_2")
            CC_HudStatus("~r~Animation failed to load!", 2000)
            EXIT
        ENDIF
        g_fAnim = "cardrunksex_loop_f"
        g_mAnim = "cardrunksex_loop_m"
        g_fDict = "random@drunk_driver_2"
        g_mDict = "random@drunk_driver_2"
        g_curAction = "sex"
        g_backseatCarSex = TRUE
        g_defDevIntensity = 0.20
        g_devExcluded = FALSE
        CC_SetHealFor(TRUE, TRUE, 0.5)
        TASK_PLAY_ANIM(g_girl, "random@drunk_driver_2", "cardrunksex_intro_f", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
        INT pflag = 2
        IF g_driveable
            pflag = 34
        ENDIF
        TASK_PLAY_ANIM(g_player, "random@drunk_driver_2", "cardrunksex_intro_m", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, pflag))
        SET_PED_KEEP_TASK(g_girl, TRUE)
        CC_AttachDildo()
        g_state = ST_ANIMATING
        g_animPhase = 0
        // ★ 2026-09-25晚：裸读 → 夹紧读。GET_ANIM_DURATION 不在已证明 native 列表里，
        //   返回 0/垃圾值会让相位机永远停在 phase 0（= 动画播一下就不动）。
        FLOAT durMs = CC_SafeAnimDur("random@drunk_driver_2", "cardrunksex_intro_f", 1.5)
        g_animWait = GET_GAME_TIMER() + ROUND(durMs * 1000.0)
        g_lastVoice = GET_GAME_TIMER()
        CC_HudStatus("~g~Here we go... ~s~(D-pad L/R: speed, X: reset, ~y~B + D-pad Right: abort~s~)", 3000)
        // ★ 音频诊断（v0.59，g_showDiag 默认关 ⇒ 平常不显示）：
        //   "坐姿/躺姿选了体位却没声音"最常见的原因就是**等得不够久** ——
        //   呻吟走的是 CC_AnimCommonTick 里 `VOICE_INTERVAL = 7000`（原版同值），
        //   而动作要先播 P1（就位）→ P2（爬上身）才进 loop，头几秒本来就静音。
        //   （v0.61 顺手删掉一行重复的 CC_HudDiag —— 同帧同文案写两遍。）
        CC_HudDiag("~y~AUDIO: first moan comes ~7s in (as in the original)", 2500)
        EXIT
    ENDIF

    // ── 前座: 车型分类 (C# 覆盖表 → GET_VEHICLE_CLASS 3/5/6/7=Low) ──
    INT mh
    mh = CC_ModelHashV(g_car)
    BOOL foundPed = FALSE
    INT i = 0
    g_vehicleAnimType = VAT_NORMAL
    WHILE i < CC_LOWVEHNAME_N
        TEXT_LABEL_63 vnv
        vnv = CC_LowVehName(i)
        IF mh = GET_HASH_KEY(vnv)
            g_vehicleAnimType = VAT_LOW
            foundPed = TRUE
        ENDIF
        i = i + 1
    ENDWHILE
    IF NOT foundPed
        i = 0
        WHILE i < CC_NORMVEHNAME_N
            TEXT_LABEL_63 vnv2
            vnv2 = CC_NormVehName(i)
            IF mh = GET_HASH_KEY(vnv2)
                g_vehicleAnimType = VAT_NORMAL
                foundPed = TRUE
            ENDIF
            i = i + 1
        ENDWHILE
    ENDIF
    IF NOT foundPed
        INT cls = ENUM_TO_INT(GET_VEHICLE_CLASS(g_car))
        IF cls = 3 OR cls = 5 OR cls = 6 OR cls = 7
            g_vehicleAnimType = VAT_LOW
        ELSE
            g_vehicleAnimType = VAT_NORMAL
        ENDIF
    ENDIF

    BOOL fpVal
    fpVal = CC_IsFirstPerson()

    TEXT_LABEL_63 dict
    dict = CC_CarDict(fpVal)
    TEXT_LABEL_63 other
    other = CC_CarDict(NOT fpVal)
    REQUEST_ANIM_DICT(other)
    IF NOT CC_ReqDict(dict)
        dict = CC_CarDict(NOT fpVal)
        IF NOT CC_ReqDict(dict)
            CC_HudStatus("~r~Animation failed to load!", 2000)
            EXIT
        ENDIF
    ENDIF

    TEXT_LABEL_63 fIntro = "proposition_to_bj_p1_prostitute"
    TEXT_LABEL_63 mIntro = "proposition_to_bj_p1_male"
    IF idx = 0
        IF g_vehicleAnimType = VAT_LOW
            g_fAnim = "low_car_bj_loop_female"
            g_mAnim = "low_car_bj_loop_player"
            fIntro = "low_car_prop_to_bj_p1_female"
            mIntro = "low_car_prop_to_bj_p1_player"
        ELSE
            g_fAnim = "bj_loop_prostitute"
            g_mAnim = "bj_loop_male"
        ENDIF
        g_curAction = "blowjob"
        g_devExcluded = FALSE
        CC_SetHealFor(TRUE, FALSE, 0.7)
    ELSE
        IF g_vehicleAnimType = VAT_LOW
            g_fAnim = "low_car_sex_loop_female"
            g_mAnim = "low_car_sex_loop_player"
            fIntro = "low_car_prop_to_sex_p1_female"
            mIntro = "low_car_prop_to_sex_p1_player"
        ELSE
            g_fAnim = "sex_loop_prostitute"
            g_mAnim = "sex_loop_male"
            fIntro = "proposition_to_sex_p1_prostitute"
            mIntro = "proposition_to_sex_p1_male"
        ENDIF
        g_curAction = "sex"
        g_devExcluded = FALSE
        CC_SetHealFor(TRUE, TRUE, 0.5)
    ENDIF
    g_fDict = dict
    g_mDict = dict
    INT pflag2 = 2
    IF g_driveable
        pflag2 = 34
    ENDIF
    TASK_PLAY_ANIM(g_girl, dict, fIntro, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
    TASK_PLAY_ANIM(g_player, dict, mIntro, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, pflag2))
    SET_PED_KEEP_TASK(g_girl, TRUE)
    IF NOT ARE_STRINGS_EQUAL(g_curAction, "blowjob")
        CC_AttachDildo()
    ENDIF
    g_state = ST_ANIMATING
    g_animPhase = 0
    FLOAT dur2 = CC_SafeAnimDur(dict, fIntro, 1.5)
    g_animWait = GET_GAME_TIMER() + ROUND(dur2 * 1000.0)
    g_lastVoice = GET_GAME_TIMER()
    CC_HudStatus("~g~Here we go... ~s~(D-pad L/R: speed, X: reset, ~y~B + D-pad Right: abort~s~)", 3000)
    // ★ 音频诊断（v0.59，g_showDiag 默认关）——理由见上面同一条注释。
    CC_HudDiag("~y~AUDIO: first moan comes ~7s in (as in the original)", 2500)
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  车内动画 tick (C# RunCarAnimation 1:1)
// ════════════════════════════════════════════════════════════════════════
PROC CC_PlayCarPair(STRING dict, STRING fA, STRING mA, INT mflag)
    TASK_PLAY_ANIM(g_girl, dict, fA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1))
    TASK_PLAY_ANIM(g_player, dict, mA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mflag))
ENDPROC

PROC CC_CarShake()
    IF NOT ARE_STRINGS_EQUAL(g_curAction, "sex")
        EXIT
    ENDIF
    IF g_animSpeed < 0.75
        EXIT
    ENDIF
    IF NOT IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        EXIT
    ENDIF
    VEHICLE_INDEX car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
    IF NOT DOES_ENTITY_EXIST(car)
        EXIT
    ENDIF
    INT now = GET_GAME_TIMER()
    INT interval = ROUND(670.0 / g_animSpeed)
    IF now - g_lastShake >= interval
        FLOAT force = 0.65 * g_animSpeed
        IF force > 1.3
            force = 1.3
        ENDIF
        // C#: car.ApplyForce(-force * UpVector) — UpVector 用 offV 差替代
        VECTOR cup = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(car, <<0.0, 0.0, 1.0>>)
        VECTOR cpos = GET_ENTITY_COORDS(car)
        VECTOR up = cup - cpos
        APPLY_FORCE_TO_ENTITY(car, APPLY_TYPE_EXTERNAL_IMPULSE, <<up.x * -force, up.y * -force, up.z * -force>>, <<0.0, 0.0, 0.0>>, 0, FALSE, FALSE, TRUE, TRUE, FALSE)
        g_lastShake = now
    ENDIF
ENDPROC

PROC CC_FinishCarService()
    // C# FinishCarServiceAndReopen 核心路径(简节拍): 回 proposition loop, 开 Services
    CC_DetachDildo()
    IF g_healPlayer
        INT hp = GET_ENTITY_HEALTH(g_player) + FINISH_HEAL
        INT mx = GET_PED_MAX_HEALTH(g_player)
        IF hp > mx
            hp = mx
        ENDIF
        SET_ENTITY_HEALTH(g_player, hp)
    ENDIF
    IF g_healGirl AND CC_EXISTS(g_girl)
        INT hp2 = GET_ENTITY_HEALTH(g_girl) + FINISH_HEAL
        INT mx2 = GET_PED_MAX_HEALTH(g_girl)
        IF hp2 > mx2
            hp2 = mx2
        ENDIF
        SET_ENTITY_HEALTH(g_girl, hp2)
    ENDIF
    SET_PLAYER_CAN_DO_DRIVE_BY(PLAYER_ID(), TRUE)
    BOOL wasForced = g_finForced
    g_animPhase = 0
    g_animSpeed = 1.0
    g_femSpeedScale = 1.0
    g_speedCooldown = 0
    g_kissingRev = FALSE
    g_standBJRev = FALSE
    g_rdoggyRev = FALSE
    g_sitCowPullRev = FALSE
    g_carPullRev = FALSE
    g_standBJSwallowAt = 0
    g_backseatCarSex = FALSE
    g_throatLocked = FALSE
    g_throatLockAt = 0
    g_throatEndAt = 0
    g_finExecPending = FALSE
    g_finInside = FALSE
    g_finForced = FALSE
    g_finBroke = FALSE
    g_finAsked = FALSE
    g_state = ST_IDLE
    g_negotiate = FALSE
    g_keyCooldown = KEY_COOLDOWN_FRAMES
    IF NOT wasForced AND g_aLifeMode AND g_curAlIdx >= 0
        IF g_alRep[g_curAlIdx] >= 0
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
            IF g_alRep[g_curAlIdx] > CC_REP_MAX
                g_alRep[g_curAlIdx] = CC_REP_MAX
            ENDIF
            AL_UpdateRel(g_curAlIdx)
        ENDIF
    ENDIF
    // ★ v0.67：删掉"完成！"提示（用户要求，与 cc_part2a 同）。
    // 敌对 → 释放
    IF g_aLifeMode AND g_curAlIdx >= 0
        IF g_alRep[g_curAlIdx] <= CC_REP_MIN
            g_aLifeHookerSvc = FALSE
            g_carPropActive = FALSE
            IF CC_EXISTS(g_girl)
                SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, FALSE)
                TASK_WANDER_STANDARD(g_girl, 0.0, INT_TO_ENUM(EWDR_SCRIPT_FLAGS, 0))
            ENDIF
            g_menuLevel = ML_NONE
            EXIT
        ENDIF
    ENDIF
    IF NOT g_hasGirl
        EXIT
    ENDIF
    // 回 proposition loop
    IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
        IF CC_InBackSeat(g_car)
            CC_ReqDict("random@drunk_driver_2")
            CC_PlayCarPair("random@drunk_driver_2", "cardrunkflirt_loop_f", "cardrunkflirt_loop_m", 33)
            IF g_driveable
                CC_PlayCarPair("random@drunk_driver_2", "cardrunkflirt_loop_f", "cardrunkflirt_loop_m", 1)
            ENDIF
        ELSE
            BOOL fpVal
            fpVal = CC_IsFirstPerson()

            TEXT_LABEL_63 dict
            dict = CC_CarDict(fpVal)
            TEXT_LABEL_63 fLoop = "proposition_loop_prostitute"
            TEXT_LABEL_63 mLoop = "proposition_loop_male"
            IF g_vehicleAnimType = VAT_LOW
                fLoop = "low_car_prop_loop_female"
                mLoop = "low_car_prop_loop_player"
            ENDIF
            CC_ReqDict(dict)
            INT mf = 1
            IF g_driveable
                mf = 33
            ENDIF
            TASK_PLAY_ANIM(g_girl, dict, fLoop, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1))
            TASK_PLAY_ANIM(g_player, dict, mLoop, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf))
        ENDIF
    ENDIF
    g_carPropActive = TRUE
    g_carPropPhase = 1
    g_menuLevel = ML_SERVICES
    g_menuIndex = 0
ENDPROC

PROC CC_RunCarAnimation()
    IF NOT CC_EXISTS(g_girl)
        CC_EndAnimation()
        EXIT
    ENDIF
    // ★ 2026-09-25晚：原来这里有一段**每帧**打印的车内相位诊断。
    //   已删除，原因有二：
    //     1) 每帧写 g_diagText 会把同帧其它一次性诊断（如 CC_AssignVoice 的
    //        "VOICE ok"）当场顶掉 —— 单通道冲突，用户因此看不到音频诊断；
    //     2) 功能上和 CC_AnimatingTick 里的统一相位诊断完全重复。
    //   现在由 CC_AnimatingTick → CC_DiagAnimPhase() 统一打印，
    //   而且只在"相位/模式变化"的那一帧写一次（车/街都能看到）。
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_PHONE, TRUE)
    DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_EXIT, TRUE)
    IF NOT g_carEngineOn AND IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
        IF DOES_ENTITY_EXIST(g_car)
            SET_VEHICLE_ENGINE_ON(g_car, FALSE, TRUE, TRUE)
        ENDIF
    ENDIF
    IF NOT g_driveable
        DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_ACCELERATE, TRUE)
        DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_BRAKE, TRUE)
        DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_MOVE_LR, TRUE)
        DISABLE_CONTROL_ACTION(PLAYER_CONTROL, INPUT_VEH_HANDBRAKE, TRUE)
    ENDIF

    // ── 后座系统 (简化 3 相: 0 intro 2 loop 3 outro) ──
    IF g_backseatCarSex
        IF g_carPullRev AND g_animPhase = 3
            SET_ENTITY_ANIM_SPEED(g_player, "random@drunk_driver_2", "cardrunksex_intro_m", -1.0)
            IF CC_EXISTS(g_girl)
                SET_ENTITY_ANIM_SPEED(g_girl, "random@drunk_driver_2", "cardrunksex_intro_f", -1.0)
            ENDIF
        ENDIF
        IF g_animPhase = 0
            IF g_animWait <= GET_GAME_TIMER()
                CC_ReqDict("random@drunk_driver_2")
                CC_PlayCarPair("random@drunk_driver_2", g_fAnim, g_mAnim, 1)
                g_animPhase = 2
                g_lastShake = GET_GAME_TIMER() + 500
            ENDIF
        ELIF g_animPhase = 2
            CC_CarShake()
        ELIF g_animPhase = 3
            IF g_animWait <= GET_GAME_TIMER()
                CC_PlayFinishVoice()
                CC_FinishCarService()
            ENDIF
        ENDIF
        EXIT
    ENDIF

    BOOL fpVal
    fpVal = CC_IsFirstPerson()

    TEXT_LABEL_63 dict
    dict = CC_CarDict(fpVal)

    // 拉出反向: 每帧强制 -1 速度 (C# 注释: 同帧发会被忽略)
    IF g_carPullRev AND (g_animPhase = 3 OR g_animPhase = 4)
        BOOL lowFlag = FALSE
        IF g_vehicleAnimType = VAT_LOW
            lowFlag = TRUE
        ENDIF
        BOOL bjFlag = FALSE
        IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
            bjFlag = TRUE
        ENDIF
        TEXT_LABEL_63 revFA = "proposition_to_sex_p2_prostitute"
        TEXT_LABEL_63 revMA = "proposition_to_sex_p2_male"
        IF g_animPhase = 3
            IF bjFlag
                IF lowFlag
                    revFA = "low_car_prop_to_bj_p2_female"
                    revMA = "low_car_prop_to_bj_p2_player"
                ELSE
                    revFA = "proposition_to_bj_p2_prostitute"
                    revMA = "proposition_to_bj_p2_male"
                ENDIF
            ELSE
                IF lowFlag
                    revFA = "low_car_prop_to_sex_p2_female"
                    revMA = "low_car_prop_to_sex_p2_player"
                ELSE
                    revFA = "proposition_to_sex_p2_prostitute"
                    revMA = "proposition_to_sex_p2_male"
                ENDIF
            ENDIF
        ELSE
            IF bjFlag
                IF lowFlag
                    revFA = "low_car_prop_to_bj_p1_female"
                    revMA = "low_car_prop_to_bj_p1_player"
                ELSE
                    revFA = "proposition_to_bj_p1_prostitute"
                    revMA = "proposition_to_bj_p1_male"
                ENDIF
            ELSE
                IF lowFlag
                    revFA = "low_car_prop_to_sex_p1_female"
                    revMA = "low_car_prop_to_sex_p1_player"
                ELSE
                    revFA = "proposition_to_sex_p1_prostitute"
                    revMA = "proposition_to_sex_p1_male"
                ENDIF
            ENDIF
        ENDIF
        SET_ENTITY_ANIM_SPEED(g_player, dict, revMA, -1.0)
        IF CC_EXISTS(g_girl)
            SET_ENTITY_ANIM_SPEED(g_girl, dict, revFA, -1.0)
        ENDIF
    ENDIF

    IF g_animPhase = 0
        IF g_animWait <= GET_GAME_TIMER()
            TEXT_LABEL_63 fP2 = "proposition_to_sex_p2_prostitute"
            TEXT_LABEL_63 mP2 = "proposition_to_sex_p2_male"
            IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
                fP2 = "proposition_to_bj_p2_prostitute"
                mP2 = "proposition_to_bj_p2_male"
                IF g_vehicleAnimType = VAT_LOW
                    fP2 = "low_car_prop_to_bj_p2_female"
                    mP2 = "low_car_prop_to_bj_p2_player"
                ENDIF
            ELSE
                IF g_vehicleAnimType = VAT_LOW
                    fP2 = "low_car_prop_to_sex_p2_female"
                    mP2 = "low_car_prop_to_sex_p2_player"
                ENDIF
            ENDIF
            INT mf = 2
            IF g_driveable
                mf = 34
            ENDIF
            TASK_PLAY_ANIM(g_girl, dict, fP2, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
            TASK_PLAY_ANIM(g_player, dict, mP2, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf))
            FLOAT p2Dur = CC_SafeAnimDur(dict, fP2, 1.5)
            g_animWait = GET_GAME_TIMER() + ROUND(p2Dur * 1000.0)
            IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
                CC_AttachDildo()
            ENDIF
            g_animPhase = 1
        ENDIF
    ELIF g_animPhase = 1
        IF g_animWait <= GET_GAME_TIMER()
            INT mf2 = 1
            IF g_driveable
                mf2 = 33
            ENDIF
            CC_PlayCarPair(dict, g_fAnim, g_mAnim, mf2)
            g_animPhase = 2
            g_lastShake = GET_GAME_TIMER() + 500
        ENDIF
    ELIF g_animPhase = 2
        CC_CarShake()
    ELIF g_animPhase = 3
        IF g_animWait <= GET_GAME_TIMER()
            IF g_carPullRev
                BOOL isLow3 = FALSE
                IF g_vehicleAnimType = VAT_LOW
                    isLow3 = TRUE
                ENDIF
                BOOL isBj3 = FALSE
                IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
                    isBj3 = TRUE
                ENDIF
                TEXT_LABEL_63 fP1r = "proposition_to_sex_p1_prostitute"
                TEXT_LABEL_63 mP1r = "proposition_to_sex_p1_male"
                IF isBj3
                    IF isLow3
                        fP1r = "low_car_prop_to_bj_p1_female"
                        mP1r = "low_car_prop_to_bj_p1_player"
                    ELSE
                        fP1r = "proposition_to_bj_p1_prostitute"
                        mP1r = "proposition_to_bj_p1_male"
                    ENDIF
                ELSE
                    IF isLow3
                        fP1r = "low_car_prop_to_sex_p1_female"
                        mP1r = "low_car_prop_to_sex_p1_player"
                    ENDIF
                ENDIF
                INT mf3 = 2
                IF g_driveable
                    mf3 = 34
                ENDIF
                TASK_PLAY_ANIM(g_girl, dict, fP1r, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), 1.0)
                TASK_PLAY_ANIM(g_player, dict, mP1r, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf3), 1.0)
                SET_ENTITY_ANIM_SPEED(g_player, dict, mP1r, -1.0)
                SET_ENTITY_ANIM_SPEED(g_girl, dict, fP1r, -1.0)
                FLOAT p1rDur = CC_SafeAnimDur(dict, fP1r, 1.5)
                g_animWait = GET_GAME_TIMER() + ROUND(p1rDur * 1000.0)
                g_animPhase = 4
                EXIT
            ENDIF
            TEXT_LABEL_63 fP2E = "sex_to_proposition_p2_prostitute"
            TEXT_LABEL_63 mP2E = "sex_to_proposition_p2_male"
            IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
                fP2E = "bj_to_proposition_p2_prostitute"
                mP2E = "bj_to_proposition_p2_male"
                IF g_vehicleAnimType = VAT_LOW
                    fP2E = "low_car_bj_to_prop_p2_female"
                    mP2E = "low_car_bj_to_prop_p2_player"
                ENDIF
            ELSE
                IF g_vehicleAnimType = VAT_LOW
                    fP2E = "low_car_sex_to_prop_p2_female"
                    mP2E = "low_car_sex_to_prop_p2_player"
                ENDIF
            ENDIF
            INT mf4 = 0
            IF g_driveable
                mf4 = 32
            ENDIF
            TASK_PLAY_ANIM(g_girl, dict, fP2E, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 0))
            TASK_PLAY_ANIM(g_player, dict, mP2E, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf4))
            FLOAT p2eDur = CC_SafeAnimDur(dict, fP2E, 1.5)
            g_animWait = GET_GAME_TIMER() + ROUND(p2eDur * 1000.0)
            g_animPhase = 4
        ENDIF
    ELIF g_animPhase = 4
        IF g_animWait <= GET_GAME_TIMER()
            g_carPullRev = FALSE
            CC_PlayFinishVoice()
            CC_FinishCarService()
        ENDIF
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  街头动作 (C# StartStreetAction)
// ════════════════════════════════════════════════════════════════════════
PROC CC_StartStreetAction(INT idx)
    // ★ v0.63：同 CC_StartCarAction —— 记来源菜单页 + 调速键开场免疫。
    g_actionReturnMenu = g_menuLevel
    g_speedImmuneUntil = GET_GAME_TIMER() + 500
    IF NOT CC_EXISTS(g_girl)
        CC_FullReset(FALSE)
        EXIT
    ENDIF
    // ★ 2026-09-25 修复：原来这里只是裸 EXIT —— 玩家在车里选街面动作时
    //   **屏幕上一个字都不给**，用户完全不知道发生了什么（"选哪个都没反应"）。
    //   这些动作本来就必须在车外做（它们要把人摆到地面/墙边），
    //   现在至少把原因说出来。
    IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        CC_HudStatus("~y~This one's outside-only - get out of the car", 2500)
        EXIT
    ENDIF
    // ★★★ 2026-09-25晚 关键修复：g_mode 落到 CCM_STREET（同 CC_StartCarAction）。
    //   丢了 C# OpenServicesMenu 里的 `mode = Mode.Street;` ⇒ 这些全恒假：
    //     · CC_AnimatingTick 的街面 tick 分支（站立/坐躺两套相位机）⇒ 卡 phase 0
    //     · CC_AnimCommonTick（呻吟/速度/治疗）⇒ 全程静音、无速度控制
    //     · CC_EndAnimation 的 CCM_STREET 分支 ⇒ 退出时不恢复碰撞、不传送回
    //       g_animOrigin ⇒ 人还沉在地下就被解冻 ⇒ **自由落体**（用户实测现象）
    //     · 中止分支的 ELSE ⇒ 一律回到 ML_STANDINGSVC
    g_mode = CCM_STREET
    g_diagPhase = -1
    TEXT_LABEL_63 mD
    TEXT_LABEL_63 mA
    TEXT_LABEL_63 fD
    TEXT_LABEL_63 fA
    VECTOR offV
    FLOAT headingOff
    FLOAT zOffset
    FLOAT rotX
    BOOL isSitting
    CC_StreetAnimData(idx, mD, mA, fD, fA, offV, headingOff, isSitting, zOffset, rotX)
    // ★ 诊断探针（非致命 · 同上）：只报不拦，别让探针自己把动画打死。
    //   v0.57 起走 CC_HudDiag（默认关闭），要看时把 g_showDiag 改成 TRUE。
    IF NOT DOES_ANIM_DICT_EXIST(mD)
        CC_HudDiag("~y~Anim dict missing - no assets for this street action", 2500)
    ENDIF
    IF NOT CC_ReqDict(mD)
        CC_HudStatus("~r~Animation failed to load!", 2000)
        EXIT
    ENDIF
    IF NOT ARE_STRINGS_EQUAL(mD, fD)
        IF NOT CC_ReqDict(fD)
            CC_HudStatus("~r~Animation failed to load!", 2000)
            EXIT
        ENDIF
    ENDIF
    g_mDict = mD
    g_mAnim = mA
    g_fDict = fD
    g_fAnim = fA
    g_streetActionIdx = idx
    IF idx = 0
        g_curAction = "kissing"
        g_devExcluded = TRUE
        g_defDevIntensity = 0.0
        CC_SetHealFor(FALSE, FALSE, 0.0)
    ELIF idx = 1
        g_curAction = "sex"
        g_devExcluded = TRUE
        g_defDevIntensity = 0.0
        CC_SetHealFor(FALSE, TRUE, 0.7)
    ELIF idx <= 4
        g_curAction = "blowjob"
        g_devExcluded = FALSE
        g_defDevIntensity = 0.14
        CC_SetHealFor(TRUE, FALSE, 0.7)
    ELSE
        g_curAction = "sex"
        g_devExcluded = FALSE
        g_defDevIntensity = 0.22
        CC_SetHealFor(TRUE, TRUE, 0.5)
    ENDIF

    // ══════════════════════════════════════════════════════════════════════
    // ★★★ 2026-09-25晚（第 4 次"合并掉 C# 独立分支"）：坐/躺动作的**起手过渡 P1**
    //
    //   C# 对坐/躺系动作在 StartStreetAction 里是**完全独立的一条分支**
    //   （CinnamonCoffee.cs 13204-13325）：先播 `GetStreetPropIntroExitAnimData(0, …)`
    //   —— 也就是"**爬上身 / 就位**"那一段过渡（P1，flag 2 = 停末帧），
    //   之后才由 RunStreetSitAnimation 推进到 P2 → loop。
    //
    //   移植时这条分支整个丢了 ⇒ 本函数对所有动作一律直接播 `CC_StreetAnimData(idx)`
    //   （那是 **loop**）⇒ 用户实测："一选体位就立刻在运动、没有爬上身的过渡、还没声音；
    //   过一会儿 tick 的 phase 0 才把 P2（爬上身）播出来" —— 顺序整个反了。
    //
    //   位置口径与 C# 一致：**绝对高度** `g_sitOrigin.z + iZOff`（不是累计）。
    //
    //   ⚠ 本轮**未实现** C# 的 "1/4/8/9 先黑屏再就位（animPhase = -1）" 和
    //      "idx 7 的 P1 中途延迟黑屏（_streetOutroFadeAt）" —— 这两步是**转场观感**，
    //      不影响功能；见 dist/INSTALL.md 的风险清单。
    // ══════════════════════════════════════════════════════════════════════
    BOOL sitFlow = FALSE
    IF g_sitPropActive
        IF idx = 1 OR idx = 3 OR idx = 4 OR idx = 6 OR idx = 7 OR idx = 8 OR idx = 9
            sitFlow = TRUE
        ENDIF
    ENDIF
    IF sitFlow
        TEXT_LABEL_63 iMD
        TEXT_LABEL_63 iMA
        TEXT_LABEL_63 iFD
        TEXT_LABEL_63 iFA
        VECTOR iOff
        FLOAT iHd
        FLOAT iZo
        FLOAT iRx
        FLOAT iRh
        BOOL iSit
        CC_PropIntroExitData(0, idx, iMD, iMA, iFD, iFA, iOff, iHd, iSit, iZo, iRx, iRh)
        IF NOT CC_ReqDict(iMD)
            CC_HudStatus("~r~Animation failed to load!", 2000)
            g_mode = CCM_NONE
            EXIT
        ENDIF
        IF NOT ARE_STRINGS_EQUAL(iMD, iFD)
            IF NOT CC_ReqDict(iFD)
                CC_HudStatus("~r~Animation failed to load!", 2000)
                g_mode = CCM_NONE
                EXIT
            ENDIF
        ENDIF
        g_streetActionIdx = idx
        // C#: 只有 facesitting(1) 不戴假体
        IF idx != 1
            CC_AttachDildo()
        ENDIF
        SET_ENTITY_NO_COLLISION_ENTITY(g_girl, g_player, FALSE)
        SET_ENTITY_COLLISION(g_player, FALSE, FALSE)
        SET_ENTITY_COLLISION(g_girl, FALSE, FALSE)
        // Z 就位：绝对高度（proposition 地面高度 + 动作表 zOffset）
        VECTOR sp = GET_ENTITY_COORDS(g_player)
        FLOAT actionZ = g_sitOrigin.z + iZo
        FLOAT sDz = actionZ - sp.z
        SET_ENTITY_COORDS_NO_OFFSET(g_player, <<sp.x, sp.y, actionZ>>, FALSE, FALSE, FALSE)
        IF CC_EXISTS(g_girl)
            VECTOR gp = GET_ENTITY_COORDS(g_girl)
            SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<gp.x, gp.y, gp.z + sDz>>, FALSE, FALSE, FALSE)
        ENDIF
        // 女孩按 C# 的各自偏移就位（C# 13262-13282 / 14460-14492）
        IF CC_EXISTS(g_girl)
            BOOL gMoved = FALSE
            VECTOR gg
            FLOAT gHd = GET_ENTITY_HEADING(g_player)
            IF idx = 1
                gg = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<0.73, -0.77, -0.3>>)
                gMoved = TRUE
            ELIF idx = 4
                gg = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<0.17, 0.8, -0.37>>)
                gHd = gHd + 90.0
                gMoved = TRUE
            ELIF idx = 7
                // 直接路径用"坐姿女上位"的偏移（反向偏移由 C# 的延迟黑屏再摆一次）
                gg = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<0.85, -0.01, -0.02>>)
                gMoved = TRUE
            ELIF idx = 8
                gg = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<-0.65, 0.0, -0.02>>)
                gMoved = TRUE
            ELIF idx = 9
                gg = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<-0.35, 0.15, -0.02>>)
                gHd = gHd + 180.0
                gMoved = TRUE
            ENDIF
            IF gMoved
                SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<gg.x, gg.y, gg.z>>, FALSE, FALSE, FALSE)
                SET_ENTITY_HEADING(g_girl, gHd)
            ENDIF
        ENDIF
        g_mDict = iMD
        g_mAnim = iMA
        g_fDict = iFD
        g_fAnim = iFA
        // C#: facesitting(1) / laying BJ(4) 从 2.5 秒处开始播
        FLOAT mStartPhase = 0.0
        IF idx = 1 OR idx = 4
            FLOAT totalGetin = CC_SafeAnimDur(iMD, iMA, 0.0)
            IF totalGetin > 0.01
                mStartPhase = 2.5 / totalGetin
                IF mStartPhase > 0.99
                    mStartPhase = 0.99
                ENDIF
            ENDIF
        ENDIF
        VECTOR pp0 = GET_ENTITY_COORDS(g_player)
        IF iRx != 0.0 OR iRh != 0.0
            TASK_PLAY_ANIM_ADVANCED(g_player, iMD, iMA, pp0, <<iRx, 0.0, GET_ENTITY_HEADING(g_player) + iRh>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), mStartPhase, EULER_YXZ)
        ELSE
            TASK_PLAY_ANIM(g_player, iMD, iMA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), mStartPhase)
        ENDIF
        TASK_PLAY_ANIM(g_girl, iFD, iFA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
        FREEZE_ENTITY_POSITION(g_player, TRUE)
        FREEZE_ENTITY_POSITION(g_girl, TRUE)
        // 想无视玩家 —— 走统一写点（开关=关闭时自动否决）
        CC_PoliceIgnoreSet(TRUE)
        WAIT(100)
        // P1 时长：C# 对 1/4 只取女方时长；其余取男女较大者
        FLOAT p1Dur = CC_SafeAnimDur(iMD, iMA, 2.0)
        IF mStartPhase > 0.0
            p1Dur = p1Dur * (1.0 - mStartPhase)
        ENDIF
        IF NOT ARE_STRINGS_EQUAL(iMD, iFD)
            FLOAT fP1 = CC_SafeAnimDur(iFD, iFA, 2.0)
            IF idx = 1 OR idx = 4
                p1Dur = fP1
            ELSE
                IF fP1 > p1Dur
                    p1Dur = fP1
                ENDIF
            ENDIF
        ENDIF
        g_state = ST_ANIMATING
        g_animPhase = 0
        g_animWait = GET_GAME_TIMER() + ROUND(p1Dur * 1000.0)
        g_lastVoice = GET_GAME_TIMER()
        CC_HudStatus("~g~Here we go... ~s~(D-pad L/R: speed, X: reset, ~y~B + D-pad Right: abort~s~)", 3000)
        // ★ 音频诊断（v0.59，g_showDiag 默认关 ⇒ 平常不显示）：
        //   "坐姿/躺姿选了体位却没声音"最常见的原因就是**等得不够久** ——
        //   呻吟走的是 CC_AnimCommonTick 里 `VOICE_INTERVAL = 7000`（原版同值），
        //   而动作要先播 P1（就位）→ P2（爬上身）才进 loop，头几秒本来就静音。
        //   （v0.61 顺手删掉一行重复的 CC_HudDiag —— 同帧同文案写两遍。）
        CC_HudDiag("~y~AUDIO: first moan comes ~7s in (as in the original)", 2500)
        EXIT
    ENDIF

    SET_ENTITY_NO_COLLISION_ENTITY(g_girl, g_player, FALSE)
    VECTOR girlPos = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, offV)
    VECTOR pp = GET_ENTITY_COORDS(g_player)

    // ★★★ 2026-09-25晚 修"坐姿/躺姿陷进地底 ≈2 米"。
    //
    //   坐/躺类动作（1/3/4/6/7/8/9）在**进 proposition 的那一刻**就已经被沉过一次了
    //   —— CC_StreetSitPropTick 的 phase 0→1 会 SET_ENTITY_COLLISION(ped, FALSE) 并且
    //   按 CC_PropAnimData(1, ...) 的 zOffset = **-0.855** 把人和女孩一起沉下去
    //   （C# 同样如此，见 CinnamonCoffee.cs 10203-10231）。
    //
    //   而这支函数原来对所有动作一律用 `当前位置.z + zOffset`：
    //   坐/躺时"当前位置"= 已经沉了 -0.855 的位置 ⇒ 再沉 action 表的 zOffset
    //   （idx3/idx7 = -0.87，idx1/4/8/9 = -0.59）⇒ **合计 -1.7 米左右**
    //   —— 正是用户实测的"陷在地底大概两米"。
    //
    //   C# 原版给坐/躺走的是**另一条分支**（StartStreetAction 里的
    //   `if (_streetSitPropActive && (idx==6||3||1||7||4||8||9))`），
    //   它用的是 **绝对高度** `_streetSitOriginPos.Z + iZOff`，不是累加。
    //   `_streetSitOriginPos` 就存在进 proposition 的时候（C# 13099），
    //   对应本工程的 `g_sitOrigin`（cc_part2c.sc CC_EnterStreetSitProp 里存）。
    //   这里补上同一套口径。
    BOOL sitLayFlow = FALSE
    IF g_sitPropActive
        IF idx = 1 OR idx = 3 OR idx = 4 OR idx = 6 OR idx = 7 OR idx = 8 OR idx = 9
            sitLayFlow = TRUE
        ENDIF
    ENDIF

    // 落地点必须记"还没被沉下去的那个地面位置"：
    //   · 普通街面动作：它们没沉过 ⇒ 当前位置就是地面位置
    //   · 坐/躺类：当前位置已经在地面以下 -0.855 了 ⇒ 必须用 g_sitOrigin，
    //     否则退出/中止时"把人还原到地下"（用户实测"退出后还是在地底"）
    IF sitLayFlow
        g_animOrigin = <<g_sitOrigin.x, g_sitOrigin.y, g_sitOrigin.z>>
        g_animOriginH = g_sitOriginH
    ELSE
        g_animOrigin = pp
        g_animOriginH = GET_ENTITY_HEADING(g_player)
    ENDIF

    IF isSitting
        SET_ENTITY_COLLISION(g_player, FALSE, FALSE)
        SET_ENTITY_COLLISION(g_girl, FALSE, FALSE)
    ENDIF

    // Z 目标：玩家用**绝对高度**；女孩吃**同一个位移 deltaZ**（保持两人相对关系）。
    //   非坐/躺时 targetZ = pp.z + zOffset ⇒ deltaZ = zOffset，和原来逐字等价。
    FLOAT targetZ = pp.z + zOffset
    IF sitLayFlow
        targetZ = g_sitOrigin.z + zOffset
    ENDIF
    FLOAT deltaZ = targetZ - pp.z
    SET_ENTITY_COORDS_NO_OFFSET(g_player, <<pp.x, pp.y, targetZ>>, FALSE, FALSE, FALSE)
    SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<girlPos.x, girlPos.y, girlPos.z + deltaZ>>, FALSE, FALSE, FALSE)
    SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + headingOff)
    FREEZE_ENTITY_POSITION(g_player, TRUE)
    FREEZE_ENTITY_POSITION(g_girl, TRUE)
    // 想无视玩家 —— 走统一写点（开关=关闭时自动否决）
    CC_PoliceIgnoreSet(TRUE)
    WAIT(100)
    // ★★★ 2026-09-25 修复：这里**绝对不能解冻**。
    //
    //   原来这两行写的是：
    //       // ★ C# 原版: unfreeze so the anim can move her —— 冻结的实体动画任务驱动不了
    //       FREEZE_ENTITY_POSITION(g_player, FALSE)
    //       FREEZE_ENTITY_POSITION(g_girl, FALSE)
    //   那个注释是**错的**，是从 C# 另一个流程抄错了上下文：
    //       C# 10144-10153（坐道具/野餐坐地流程）才是 freeze → 传送 → unfreeze，
    //       而且那里**只动女孩**、zOffset = 0，从不把人沉到地下。
    //       C# 13335-13341（街面动作流程，就是这一段）冻住两人之后 **没有任何解冻**，
    //       一直到 EndAnimation 才解冻。
    //
    //   为什么解冻会炸：
    //     上面 isSitting 分支已经 SET_ENTITY_COLLISION(ped, FALSE, FALSE)，
    //     而且 set 了负的 zOffset（-0.15 ~ -0.95，故意沉到地面以下）。
    //     碰撞关掉 + 位置在地面以下 + 解冻 ⇒ **物理直接把它往地心拽**；
    //     动画任务每帧又把位置拉回动画原点 ⇒ 肉眼看到的是
    //     「掉下去 → 传送上来 → 掉下去 → 传送上来」无限循环，直到手动 abort。
    //   冻结住位置就没事：FREEZE_ENTITY_POSITION 锁的是**位置**，不锁动画 ——
    //   TASK_PLAY_ANIM 照常播。原版就是这么工作的。

    INT mflag = 0
    IF idx = 0
        mflag = 0
    ELSE
        mflag = 2
    ENDIF
    // 需要身体旋转的动作 (rotX) 用 ADVANCED
    IF rotX != 0.0
        VECTOR pp2 = GET_ENTITY_COORDS(g_player)
        TASK_PLAY_ANIM_ADVANCED(g_player, mD, mA, pp2, <<rotX, 0.0, GET_ENTITY_HEADING(g_player)>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), 0.0, EULER_YXZ)
    ELSE
        TASK_PLAY_ANIM(g_player, mD, mA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mflag))
    ENDIF
    TASK_PLAY_ANIM(g_girl, fD, fA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
    IF idx != 0 AND idx != 1
        CC_AttachDildo()
    ENDIF
    g_state = ST_ANIMATING
    g_animPhase = 0
    FLOAT durMs = CC_SafeAnimDur(fD, fA, 1.5)
    g_animWait = GET_GAME_TIMER() + ROUND(durMs * 1000.0)
    g_lastVoice = GET_GAME_TIMER()
    CC_HudStatus("~g~Here we go... ~s~(D-pad L/R: speed, X: reset, ~y~B + D-pad Right: abort~s~)", 3000)
    // ★ 音频诊断（v0.59，g_showDiag 默认关）——理由见上面同一条注释。
    CC_HudDiag("~y~AUDIO: first moan comes ~7s in (as in the original)", 2500)
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  街头动画 tick — 站立类 (Kissing ping-pong / BJ·Wall·Doggy 黑屏相位)
//  C# HandleAnimatingState 的街段合并实现 (行为 1:1)
// ════════════════════════════════════════════════════════════════════════
PROC CC_StreetStandingTick()
    // Kissing ping-pong (0.417↔0.500)
    IF g_streetActionIdx = 0
        IF ARE_STRINGS_EQUAL(g_mDict, "mp_ped_interaction")
            FLOAT mProg = GET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim)
            IF g_animPhase = 0
                IF mProg >= 0.500
                    g_kissingRev = TRUE
                    SET_ENTITY_ANIM_SPEED(g_player, g_mDict, g_mAnim, -g_animSpeed)
                    SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, -g_animSpeed)
                    g_animPhase = 2
                ENDIF
            ELIF g_animPhase = 2
                IF g_kissingRev AND mProg <= 0.417
                    g_kissingRev = FALSE
                ELIF NOT g_kissingRev AND mProg >= 0.500
                    g_kissingRev = TRUE
                ENDIF
                FLOAT spd = g_animSpeed
                IF g_kissingRev
                    spd = -g_animSpeed
                ENDIF
                SET_ENTITY_ANIM_SPEED(g_player, g_mDict, g_mAnim, spd)
                SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, spd)
                IF mProg <= 0.01 OR mProg >= 0.95
                    SET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim, 0.417)
                    SET_ENTITY_ANIM_CURRENT_TIME(g_girl, g_fDict, g_fAnim, 0.417)
                    g_kissingRev = FALSE
                ENDIF
            ELIF g_animPhase = 3
                IF mProg >= 0.95
                    IF CC_EXISTS(g_girl) AND g_voiceEnd AND NOT g_finForced AND NOT g_finBroke
                        CC_AssignVoice()
                        CC_PlaySpeech("HOOKER_OFFER_AGAIN")
                    ENDIF
                    CC_EndAnimation()
                ENDIF
            ENDIF
        ENDIF
        EXIT
    ENDIF

    // Standing BJ / Wall Sex / Doggy / Rough Doggy (2/5/10/11) — 黑屏相位机
    IF g_streetActionIdx = 2 OR g_streetActionIdx = 5 OR g_streetActionIdx = 10 OR g_streetActionIdx = 11
        IF g_animPhase != 2
            IF g_animPhase = 0
                CC_DisableMovement()
                IF GET_GAME_TIMER() >= g_animWait
                    DO_SCREEN_FADE_OUT(650)
                    g_animWait = GET_GAME_TIMER() + 1000
                    g_animPhase = 1
                ENDIF
            ELIF g_animPhase = 1
                IF IS_SCREEN_FADED_OUT() AND GET_GAME_TIMER() >= g_animWait
                    STOP_ANIM_TASK(g_player, "trevor_1_int-15", "player_two_dual-15", -4.0)
                    STOP_ANIM_TASK(g_girl, "switch@michael@prostitute", "exit_hooker", -4.0)
                    VECTOR ppW = GET_ENTITY_COORDS(g_player)
                    IF g_streetActionIdx = 2
                        // 女: 跪地 loop
                        TASK_PLAY_ANIM_ADVANCED(g_player, g_mDict, g_mAnim, ppW, <<0.0, 0.0, GET_ENTITY_HEADING(g_player)>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 9), 0.0, EULER_YXZ)
                        TASK_PLAY_ANIM(g_girl, g_fDict, g_fAnim, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 9))
                    ELSE
                        // Wall/Doggy: 女方重新定位到 loop 偏移
                        TEXT_LABEL_63 mD2
                        TEXT_LABEL_63 mA2
                        TEXT_LABEL_63 fD2
                        TEXT_LABEL_63 fA2
                        VECTOR off2
                        FLOAT hd2
                        FLOAT zo2
                        FLOAT rx2
                        BOOL sitFlag2
                        CC_StreetAnimData(g_streetActionIdx, mD2, mA2, fD2, fA2, off2, hd2, sitFlag2, zo2, rx2)
                        VECTOR gl = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, off2)
                        SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<gl.x, gl.y, gl.z>>, FALSE, FALSE, FALSE)
                        SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + hd2)
                        FLOAT loopStart = 0.0
                        IF g_streetActionIdx = 11
                            loopStart = 0.205
                        ENDIF
                        TASK_PLAY_ANIM(g_player, g_mDict, g_mAnim, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 9), loopStart)
                        TASK_PLAY_ANIM(g_girl, g_fDict, g_fAnim, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 9), loopStart)
                    ENDIF
                    g_animWait = GET_GAME_TIMER() + 400
                    g_animPhase = 100
                ENDIF
            ELIF g_animPhase = 100
                IF GET_GAME_TIMER() >= g_animWait
                    DO_SCREEN_FADE_IN(500)
                    g_animPhase = 2
                ENDIF
            ELIF g_animPhase = 3
                IF GET_GAME_TIMER() >= g_animWait
                    IF NOT g_finInside
                        STOP_ANIM_TASK(g_player, "switch@trevor@jerking_off", "trev_jerking_off_exit", -4.0)
                    ENDIF
                    DO_SCREEN_FADE_OUT(500)
                    g_animWait = GET_GAME_TIMER() + 1000
                    g_animPhase = 4
                ENDIF
            ELIF g_animPhase = 4
                IF IS_SCREEN_FADED_OUT() AND GET_GAME_TIMER() >= g_animWait
                    STOP_ANIM_TASK(g_player, g_mDict, g_mAnim, -4.0)
                    STOP_ANIM_TASK(g_girl, g_fDict, g_fAnim, -4.0)
                    IF NOT g_finInside
                        STOP_ANIM_TASK(g_player, "switch@trevor@jerking_off", "trev_jerking_off_exit", -4.0)
                    ENDIF
                    IF g_streetActionIdx = 2
                        // 女: 反向跪起 (manageress_kneel_intro 从 2.10s 反放)
                        VECTOR go = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<0.0, 1.29, 0.07>>)
                        SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<go.x, go.y, go.z>>, FALSE, FALSE, FALSE)
                        SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + 270.0)
                        TASK_PLAY_ANIM(g_player, "trevor_1_int-15", "player_two_dual-15", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 50))
                        FLOAT fDur = CC_SafeAnimDur("missheist_jewel", "manageress_kneel_intro", 3.0)
                        FLOAT startP = 0.5
                        IF fDur > 0.0
                            startP = 2.10 / fDur
                            IF startP > 0.99
                                startP = 0.99
                            ENDIF
                        ENDIF
                        TASK_PLAY_ANIM(g_girl, "missheist_jewel", "manageress_kneel_intro", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 10), startP)
                        SET_ENTITY_ANIM_SPEED(g_girl, "missheist_jewel", "manageress_kneel_intro", -1.0)
                    ELSE
                        // Wall/Doggy: 女方回接吻位
                        VECTOR go2 = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<0.0, 1.29, 0.07>>)
                        SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<go2.x, go2.y, go2.z>>, FALSE, FALSE, FALSE)
                        SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + 270.0)
                        TASK_PLAY_ANIM(g_player, "trevor_1_int-15", "player_two_dual-15", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 50))
                    ENDIF
                    CC_DetachDildo()
                    DO_SCREEN_FADE_IN(500)
                    g_animWait = GET_GAME_TIMER() + 2100
                    g_standBJSwallowAt = GET_GAME_TIMER() + 1000
                    g_animPhase = 5
                ENDIF
            ELIF g_animPhase = 5
                CC_DisableMovement()
                IF g_streetActionIdx = 2 AND CC_EXISTS(g_girl)
                    SET_ENTITY_ANIM_SPEED(g_girl, "missheist_jewel", "manageress_kneel_intro", -1.0)
                ENDIF
                IF g_standBJSwallowAt > 0 AND GET_GAME_TIMER() >= g_standBJSwallowAt
                    g_standBJSwallowAt = 0
                    IF NOT g_finInside OR g_finForced OR g_finBroke
                        IF g_aLifeMode AND (g_finForced OR g_finBroke)
                            CC_PlaySpit()
                        ENDIF
                    ENDIF
                ENDIF
                IF GET_GAME_TIMER() >= g_animWait
                    FREEZE_ENTITY_POSITION(g_player, FALSE)
                    SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
                    CLEAR_PED_TASKS(g_player)
                    SET_PLAYER_CAN_DO_DRIVE_BY(PLAYER_ID(), TRUE)
                    // 车内动作收尾（C# 10983 同）：改为状态推导，避免影子值脱节。
                    // 开关=开启 → 保持无视；开关=关闭 → 当场 FALSE。
                    CC_ApplyPoliceIgnore()
                    g_animWait = GET_GAME_TIMER() + 2100
                    g_animPhase = 6
                ENDIF
            ELIF g_animPhase = 6
                IF g_streetActionIdx = 2 AND CC_EXISTS(g_girl)
                    SET_ENTITY_ANIM_SPEED(g_girl, "missheist_jewel", "manageress_kneel_intro", -1.0)
                ENDIF
                IF GET_GAME_TIMER() >= g_animWait
                    IF CC_EXISTS(g_girl) AND g_voiceEnd AND NOT g_finForced AND NOT g_finBroke
                        CC_AssignVoice()
                        CC_PlaySpeech("SEX_FINISHED")
                        g_postSexPhase = 1
                    ENDIF
                    IF (g_finForced OR g_finBroke) AND g_aLifeMode AND g_curAlIdx >= 0
                        g_alMood[g_curAlIdx] = 4
                        CC_ApplyMoodFace(g_girl, 4)
                    ENDIF
                    CC_EndAnimation()
                ENDIF
            ENDIF
            EXIT
        ENDIF
        // loop 期: ping-pong
        IF g_streetActionIdx = 2
            FLOAT sProg = GET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim)
            IF g_standBJRev AND sProg <= 0.01
                g_standBJRev = FALSE
            ELIF NOT g_standBJRev AND sProg >= 0.35
                g_standBJRev = TRUE
            ENDIF
            FLOAT sspd = g_animSpeed
            IF g_standBJRev
                sspd = -g_animSpeed
            ENDIF
            SET_ENTITY_ANIM_SPEED(g_player, g_mDict, g_mAnim, sspd)
            SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, sspd)
            IF g_standBJRev AND sProg > 0.50
                SET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim, 0.01)
                SET_ENTITY_ANIM_CURRENT_TIME(g_girl, g_fDict, g_fAnim, 0.01)
                g_standBJRev = FALSE
            ENDIF
        ENDIF
        IF g_streetActionIdx = 11
            FLOAT rProg = GET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim)
            IF g_rdoggyRev AND rProg <= 0.19
                g_rdoggyRev = FALSE
            ELIF NOT g_rdoggyRev AND rProg >= 0.70
                g_rdoggyRev = TRUE
            ENDIF
            FLOAT rspd = g_animSpeed
            IF g_rdoggyRev
                rspd = -g_animSpeed
            ENDIF
            SET_ENTITY_ANIM_SPEED(g_player, g_mDict, g_mAnim, rspd)
            SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, rspd)
            IF NOT g_rdoggyRev AND rProg <= 0.01
                SET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim, 0.19)
                SET_ENTITY_ANIM_CURRENT_TIME(g_girl, g_fDict, g_fAnim, 0.19)
            ENDIF
        ENDIF
    ENDIF
ENDPROC

// 街头坐/躺动作 (1/3/4/6/7/8/9) — Animating 期间相位推进 (C# RunStreetSitAnimation)
PROC CC_StreetSitActionTick()
    IF g_animPhase = 0
        // P1 (prop→动作) 播放完 → P2 或 loop
        IF GET_GAME_TIMER() >= g_animWait
            TEXT_LABEL_63 mD
            TEXT_LABEL_63 mA
            TEXT_LABEL_63 fD
            TEXT_LABEL_63 fA
            VECTOR off
            FLOAT hd
            FLOAT zo
            FLOAT rx
            FLOAT rh
            BOOL sitFlag
            CC_PropIntroExitData(1, g_streetActionIdx, mD, mA, fD, fA, off, hd, sitFlag, zo, rx, rh)
            CC_ReqDict(mD)
            IF NOT ARE_STRINGS_EQUAL(mD, fD)
                CC_ReqDict(fD)
            ENDIF
            IF rx != 0.0
                VECTOR pp = GET_ENTITY_COORDS(g_player)
                TASK_PLAY_ANIM_ADVANCED(g_player, mD, mA, pp, <<rx, 0.0, GET_ENTITY_HEADING(g_player)>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), 0.0, EULER_YXZ)
            ELSE
                TASK_PLAY_ANIM(g_player, mD, mA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
            ENDIF
            TASK_PLAY_ANIM(g_girl, fD, fA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
            FLOAT p2Dur = CC_SafeAnimDur(fD, fA, 2.0)
            g_animWait = GET_GAME_TIMER() + ROUND(p2Dur * 1000.0)
            g_animPhase = 1
        ENDIF
    ELIF g_animPhase = 1
        IF GET_GAME_TIMER() >= g_animWait
            // → loop
            TEXT_LABEL_63 mD3
            TEXT_LABEL_63 mA3
            TEXT_LABEL_63 fD3
            TEXT_LABEL_63 fA3
            VECTOR off3
            FLOAT hd3
            FLOAT zo3
            FLOAT rx3
            BOOL sitFlag3
            CC_StreetAnimData(g_streetActionIdx, mD3, mA3, fD3, fA3, off3, hd3, sitFlag3, zo3, rx3)
            g_mDict = mD3
            g_mAnim = mA3
            g_fDict = fD3
            g_fAnim = fA3
            VECTOR gl3 = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, off3)
            // ★★★ 2026-09-25晚：这里**不要再加 zo3**。
            //   女孩的高度 = 玩家 Z + 表里的 offV.z（例如 idx3 的 -0.02）就够了 ——
            //   这些是"车内座位动画"，男女双方本来就是相对同一把椅子建模的，
            //   所以两人必须在同一个基准高度上。
            //   原来写的是 `gl3.z + zo3`：玩家此时已经被沉到 g_sitOrigin.z + zOffset
            //   （≈ 地面 -0.87），女孩再叠一次 -0.87 ⇒ 她单独被埋掉近 0.9 米。
            //   C# 对应位置用的是 `player.GetOffsetPosition(sOff)`（只有 offV，没有 zOffset），
            //   见 CinnamonCoffee.cs 14618-14626。这里与之一致。
            SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<gl3.x, gl3.y, gl3.z>>, FALSE, FALSE, FALSE)
            SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + hd3)
            IF rx3 != 0.0
                VECTOR pp3 = GET_ENTITY_COORDS(g_player)
                TASK_PLAY_ANIM_ADVANCED(g_player, mD3, mA3, pp3, <<rx3, 0.0, GET_ENTITY_HEADING(g_player)>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1), 0.0, EULER_YXZ)
            ELSE
                TASK_PLAY_ANIM(g_player, mD3, mA3, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1))
            ENDIF
            TASK_PLAY_ANIM(g_girl, fD3, fA3, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1))
            g_animPhase = 2
        ENDIF
    ENDIF
ENDPROC
