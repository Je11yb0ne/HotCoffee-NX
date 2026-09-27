// ============================================================================
// cc_part2c.sc — 第2c部分: 坐/躺 proposition(Idle) / Finish 流程 / 菜单 / 主循环
// 对应 C# HandleIdleState 的 proposition 段 / Finish menu / HandleMenuSelect /
//       _finishExecutePending / OnTick / SCRIPT
// ============================================================================

// ════════════════════════════════════════════════════════════════════════
//  坐/躺 proposition 相位机 (C# HandleIdleState 10028-10381)
// ════════════════════════════════════════════════════════════════════════
PROC CC_PropPlayPair(INT propIdx, INT flagVal)
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
    CC_PropAnimData(propIdx, mD, mA, fD, fA, off, hd, sitFlag, zo, rx, rh)
    CC_ReqDict(mD)
    IF NOT ARE_STRINGS_EQUAL(mD, fD)
        CC_ReqDict(fD)
    ENDIF
    IF rx != 0.0 OR rh != 0.0
        VECTOR pp = GET_ENTITY_COORDS(g_player)
        TASK_PLAY_ANIM_ADVANCED(g_player, mD, mA, pp, <<rx, 0.0, GET_ENTITY_HEADING(g_player) + rh>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, flagVal), 0.0, EULER_YXZ)
    ELSE
        TASK_PLAY_ANIM(g_player, mD, mA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, flagVal))
    ENDIF
    TASK_PLAY_ANIM(g_girl, fD, fA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, flagVal))
ENDPROC

PROC CC_EnterStreetSitProp(INT category)
    IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        EXIT
    ENDIF
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    g_streetSitCat = category
    g_sitOrigin = GET_ENTITY_COORDS(g_player)
    g_sitOriginH = GET_ENTITY_HEADING(g_player)
    SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
    g_sitPropActive = TRUE
    g_sitPropPhase = -1
    IF NOT g_streetSitAnimOn
        g_sitPropPhase = 0
    ENDIF
    g_sitPropWait = 0
ENDPROC

PROC CC_ExitStreetSitProp()
    IF NOT g_sitPropActive
        EXIT
    ENDIF
    IF g_sitPropPhase <= 0
        CLEAR_PED_TASKS(g_player)
        IF CC_EXISTS(g_girl)
            CLEAR_PED_TASKS(g_girl)
        ENDIF
        g_sitPropActive = FALSE
        g_sitPropPhase = 0
        EXIT
    ENDIF
    // ★★ 2026-09-25 修 g_sitReturnMenu（此前它**只被读、从没被赋值** ⇒ 站起动画播完后
    //   菜单直接关掉、玩家还得重新开菜单回服务页）。
    //
    //   C# 的做法（CinnamonCoffee.cs 6466-6467 / 6724-6725）：从"已就坐"子菜单按返回
    //   站起来时，记下 `_streetSitExitReturnMenu = _serviceSubParent`、
    //   `_streetSitExitReturnIdx = 1`；等站起动画播完（phase 4）再把菜单恢复到那一页。
    //
    //   这里把赋值放进**唯一的站起入口** CC_ExitStreetSitProp() ⇒ 所有调用点
    //   （主键、Idle 期备份退出、看门狗兜底）都自然覆盖到，不必逐个改。
    g_sitReturnMenu = ML_SERVICES
    IF g_streetSitCat = ML_LAYINGSVC
        g_sitReturnIdx = 2      // 躺姿服务
    ELSE
        g_sitReturnIdx = 1      // 坐姿服务
    ENDIF
    CC_PropPlayPair(3, 2)
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
    CC_PropAnimData(3, mD, mA, fD, fA, off, hd, sitFlag, zo, rx, rh)
    FLOAT exitDur = CC_SafeAnimDur(fD, fA, 1.5)
    g_sitPropPhase = 3
    g_sitPropWait = GET_GAME_TIMER() + ROUND(exitDur * 1000.0)
ENDPROC

// ── 坐/躺 proposition 卡死 —— 最后一道逃生（每帧从主循环调用）────────────
//
// 与 CC_StreetSitPropTick 里那道看门狗的分工：
//   · 那道：只在 Idle 期、坐躺 tick 还在跑时生效，走"温和"的 phase 3→4 站起动画
//   · 这道：**不依赖任何 tick**，主循环每帧无条件调用。哪怕状态机被卡在别的状态、
//           坐躺 tick 根本没被调用，也能把玩家从"永久锁死"里捞出来。
//
// 判据：g_sitPropActive 为真 + 相位不是"合法静止态"(2) + 该相位的等待时间
//       已超时 8 秒以上。正常转场 1.5~3 秒（最坏被 CC_SafeAnimDur 夹到 10 秒，
//       那时是 wait+8000=18 秒才触发）⇒ 正常流程**永远碰不到**。
FUNC BOOL CC_SitPropStuckEscape()
    IF NOT g_sitPropActive
        RETURN FALSE
    ENDIF
    IF g_state = ST_ANIMATING
        // 动画期归 CC_AnimatingTick 管（那里的主键能开结束菜单）—— 别抢它的活。
        // 本逃生只管"已经回到 Idle、却因为转场没推完而主键失效"这种锁死。
        RETURN FALSE
    ENDIF
    IF g_sitPropPhase = 2
        RETURN FALSE
    ENDIF
    IF g_sitPropWait <= 0
        RETURN FALSE
    ENDIF
    IF GET_GAME_TIMER() <= g_sitPropWait + 8000
        RETURN FALSE
    ENDIF
    CC_HudStatus("~r~Recovered from a stuck transition", 3000)
    FREEZE_ENTITY_POSITION(g_player, FALSE)
    SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
    SET_ENTITY_COORDS_NO_OFFSET(g_player, <<g_sitOrigin.x, g_sitOrigin.y, g_sitOrigin.z>>, FALSE, FALSE, FALSE)
    SET_ENTITY_HEADING(g_player, g_sitOriginH)
    IF CC_EXISTS(g_girl)
        FREEZE_ENTITY_POSITION(g_girl, FALSE)
        SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
    ENDIF
    g_sitPropActive = FALSE
    g_sitPropPhase = 0
    // 如果卡死时菜单是被锁住的（ML_NONE），给回主菜单，让玩家立刻能操作
    IF g_menuLevel = ML_NONE AND g_state = ST_IDLE
        g_menuLevel = ML_MAIN
        g_menuIndex = 0
    ENDIF
    RETURN TRUE
ENDFUNC

PROC CC_StreetSitPropTick()
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    // ══════════════════════════════════════════════════════════════════════
    // ★★ 看门狗（2026-09-25晚 新增）—— 保证玩家**永远不会**被永久锁死在这个流程里
    //
    //   背景：坐/躺 proposition 的"等动画播完"相位（-1/0/1/3）都靠 g_sitPropWait
    //   定时推进。而推进它的 CC_StreetSitPropTick 只在 Idle 期被调用；一旦某个
    //   相位因为任何原因没推过去（动画字典没到位、g_sitPropWait 被算成巨大值、
    //   中途状态被别的流程改坏……），玩家就会卡在
    //     g_sitPropActive = TRUE 且 g_sitPropPhase ∈ {3,4}
    //   而 CC_MenuNav / CC_IdleMasterKey 对这种情况是**明确禁止开菜单**的
    //   （"转场中禁止开菜单"）⇒ 主键失效 ⇒ 整个游戏只能重启。
    //
    //   所以这里加一道兜底：任何"等动画"相位超时 6 秒以上 ⇒ 强制收尾
    //   （恢复碰撞/解冻/传送回地面 → phase 4 → 下一帧走正常清理）。
    //   正常转场只有 1.5~3 秒，**碰不到**这个阈值；只有在真卡死时才兜底。
    //   phase 2 是"等玩家选服务"的合法静止态，phase 4 是收尾态，都不设阈值。
    // ══════════════════════════════════════════════════════════════════════
    IF g_sitPropPhase != 2 AND g_sitPropPhase != 4 AND g_sitPropWait > 0
        IF GET_GAME_TIMER() > g_sitPropWait + 6000
            CC_HudStatus("~r~Sit/lie got stuck: forced recovery", 3000)
            FREEZE_ENTITY_POSITION(g_player, FALSE)
            FREEZE_ENTITY_POSITION(g_girl, FALSE)
            SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
            SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
            SET_ENTITY_COORDS_NO_OFFSET(g_player, <<g_sitOrigin.x, g_sitOrigin.y, g_sitOrigin.z>>, FALSE, FALSE, FALSE)
            SET_ENTITY_HEADING(g_player, g_sitOriginH)
            g_sitPropPhase = 4
            g_sitPropWait = 0
        ENDIF
    ENDIF
    // -1: 野餐坐地
    IF g_sitPropPhase = -1
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
        CC_PropAnimData(0, mD, mA, fD, fA, off, hd, sitFlag, zo, rx, rh)
        CC_ReqDict(mD)
        CC_ReqDict(fD)
        VECTOR girlPos = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, off)
        VECTOR girlFinal = <<girlPos.x, girlPos.y, girlPos.z + zo>>
        CLEAR_PED_TASKS(g_girl)
        SET_PED_KEEP_TASK(g_girl, TRUE)
        SET_ENTITY_VELOCITY(g_girl, <<0.0, 0.0, 0.0>>)
        FREEZE_ENTITY_POSITION(g_girl, TRUE)
        SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<girlFinal.x, girlFinal.y, girlFinal.z>>, FALSE, FALSE, FALSE)
        SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + hd)
        FREEZE_ENTITY_POSITION(g_girl, FALSE)
        CC_PropPlayPair(0, 2)
        FLOAT sitDur = CC_SafeAnimDur(fD, fA, 1.5)
        g_sitPropPhase = 0
        g_sitPropWait = GET_GAME_TIMER() + ROUND(sitDur * 1000.0)
        IF g_sitPropWait - GET_GAME_TIMER() < 1500
            g_sitPropWait = GET_GAME_TIMER() + 1500
        ENDIF
    ELIF g_sitPropPhase = 0
        IF GET_GAME_TIMER() >= g_sitPropWait
            // 进入 proposition (下沉+关碰撞)
            TEXT_LABEL_63 mD2
            TEXT_LABEL_63 mA2
            TEXT_LABEL_63 fD2
            TEXT_LABEL_63 fA2
            VECTOR off2
            FLOAT hd2
            FLOAT zo2
            FLOAT rx2
            FLOAT rh2
            BOOL sitFlag2
            CC_PropAnimData(1, mD2, mA2, fD2, fA2, off2, hd2, sitFlag2, zo2, rx2, rh2)
            CC_ReqDict(mD2)
            CC_ReqDict(fD2)
            SET_ENTITY_COLLISION(g_player, FALSE, FALSE)
            SET_ENTITY_COLLISION(g_girl, FALSE, FALSE)
            VECTOR girlPos2 = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, off2)
            VECTOR pp2 = GET_ENTITY_COORDS(g_player)
            SET_ENTITY_COORDS_NO_OFFSET(g_player, <<pp2.x, pp2.y, pp2.z + zo2>>, FALSE, FALSE, FALSE)
            SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<girlPos2.x, girlPos2.y, girlPos2.z + zo2>>, FALSE, FALSE, FALSE)
            SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + hd2)
            FREEZE_ENTITY_POSITION(g_player, TRUE)
            FREEZE_ENTITY_POSITION(g_girl, TRUE)
            CC_PropPlayPair(1, 2)
            CC_PlayServiceVoice()
            FLOAT spDur = CC_SafeAnimDur(fD2, fA2, 1.5)
            g_sitPropPhase = 1
            g_sitPropWait = GET_GAME_TIMER() + ROUND(spDur * 1000.0)
            IF g_sitPropWait - GET_GAME_TIMER() < 1500
                g_sitPropWait = GET_GAME_TIMER() + 1500
            ENDIF
        ENDIF
    ELIF g_sitPropPhase = 1
        IF GET_GAME_TIMER() >= g_sitPropWait
            CC_PropPlayPair(2, 1)
            g_sitPropPhase = 2
            IF g_svcSubParent = ML_SERVICES OR g_svcSubParent = ML_ESCALATE
                g_menuLevel = ML_SITDOWNSUB
            ELSE
                g_menuLevel = g_streetSitCat
            ENDIF
            g_menuIndex = 0
        ENDIF
    ELIF g_sitPropPhase = 3
        IF GET_GAME_TIMER() >= g_sitPropWait
            IF NOT g_streetSitAnimOn
                CLEAR_PED_TASKS(g_player)
                IF CC_EXISTS(g_girl)
                    CLEAR_PED_TASKS(g_girl)
                ENDIF
                FREEZE_ENTITY_POSITION(g_player, FALSE)
                FREEZE_ENTITY_POSITION(g_girl, FALSE)
                SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
                SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
                SET_ENTITY_COORDS_NO_OFFSET(g_player, <<g_sitOrigin.x, g_sitOrigin.y, g_sitOrigin.z>>, FALSE, FALSE, FALSE)
                SET_ENTITY_HEADING(g_player, g_sitOriginH)
                g_sitPropPhase = 4
                g_sitPropWait = 0
            ELSE
                // 站起 (恢复位置+碰撞)
                FREEZE_ENTITY_POSITION(g_player, FALSE)
                FREEZE_ENTITY_POSITION(g_girl, FALSE)
                SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
                SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
                SET_ENTITY_COORDS_NO_OFFSET(g_player, <<g_sitOrigin.x, g_sitOrigin.y, g_sitOrigin.z>>, FALSE, FALSE, FALSE)
                SET_ENTITY_HEADING(g_player, g_sitOriginH)
                CC_PropPlayPair(4, 0)
                TEXT_LABEL_63 mD4
                TEXT_LABEL_63 mA4
                TEXT_LABEL_63 fD4
                TEXT_LABEL_63 fA4
                VECTOR off4
                FLOAT hd4
                FLOAT zo4
                FLOAT rx4
                FLOAT rh4
                BOOL sitFlag4
                CC_PropAnimData(4, mD4, mA4, fD4, fA4, off4, hd4, sitFlag4, zo4, rx4, rh4)
                FLOAT exitDur4 = CC_SafeAnimDur(fD4, fA4, 1.5)
                g_sitPropPhase = 4
                g_sitPropWait = GET_GAME_TIMER() + ROUND(exitDur4 * 1000.0)
            ENDIF
        ENDIF
    ELIF g_sitPropPhase = 4
        IF GET_GAME_TIMER() >= g_sitPropWait
            FREEZE_ENTITY_POSITION(g_player, FALSE)
            FREEZE_ENTITY_POSITION(g_girl, FALSE)
            SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
            SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
            g_sitPropActive = FALSE
            g_sitPropPhase = 0
            IF g_sitReturnMenu != ML_NONE
                g_menuLevel = g_sitReturnMenu
                g_menuIndex = g_sitReturnIdx
                g_sitReturnMenu = ML_NONE
                g_sitReturnIdx = 0
            ENDIF
        ENDIF
    ENDIF
    // loop 期备份退出
    IF g_sitPropActive AND g_sitPropPhase = 2 AND g_state != ST_ANIMATING
        IF g_postSexPhase = 0
            IF g_menuLevel != ML_SITTINGSVC AND g_menuLevel != ML_LAYINGSVC AND g_menuLevel != ML_SITDOWNSUB
                CC_ExitStreetSitProp()
                g_menuLevel = ML_NONE
            ENDIF
        ENDIF
    ENDIF
ENDPROC

// 车内 proposition loop 管理 (Idle 期, C# carPropActive 段)
PROC CC_ExitCarProp()
    IF NOT g_carPropActive
        EXIT
    ENDIF
    BOOL fpVal
    fpVal = CC_IsFirstPerson()

    TEXT_LABEL_63 dict
    dict = CC_CarDict(fpVal)
    TEXT_LABEL_63 fExit = "proposition_exit_prostitute"
    TEXT_LABEL_63 mExit = "proposition_exit_male"
    IF g_vehicleAnimType = VAT_LOW
        fExit = "low_car_prop_exit_female"
        mExit = "low_car_prop_exit_player"
    ENDIF
    IF CC_ReqDict(dict)
        INT mf = 2
        IF g_driveable
            mf = 34
        ENDIF
        TASK_PLAY_ANIM(g_girl, dict, fExit, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
        TASK_PLAY_ANIM(g_player, dict, mExit, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf))
    ENDIF
    FLOAT durMs = CC_SafeAnimDur(dict, fExit, 1.5)
    g_carPropPhase = 2
    g_carPropWait = GET_GAME_TIMER() + ROUND(durMs * 1000.0)
ENDPROC
PROC CC_CarPropTick()
    IF NOT g_carEngineOn AND IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
        g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
        IF DOES_ENTITY_EXIST(g_car)
            SET_VEHICLE_ENGINE_ON(g_car, FALSE, TRUE, TRUE)
        ENDIF
    ENDIF
    IF g_carPropPhase = 0 AND GET_GAME_TIMER() >= g_carPropWait
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE) AND CC_InBackSeat(GET_VEHICLE_PED_IS_IN(g_player, FALSE))
            CC_ReqDict("random@drunk_driver_2")
            TASK_PLAY_ANIM(g_girl, "random@drunk_driver_2", "cardrunkflirt_loop_f", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1))
            INT mf = 1
            IF g_driveable
                mf = 33
            ENDIF
            TASK_PLAY_ANIM(g_player, "random@drunk_driver_2", "cardrunkflirt_loop_m", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf))
        ELSE
            BOOL fpVal
            fpVal = CC_IsFirstPerson()

            TEXT_LABEL_63 pd
            pd = CC_CarDict(fpVal)
            CC_ReqDict(pd)
            TEXT_LABEL_63 fLoop = "proposition_loop_prostitute"
            TEXT_LABEL_63 mLoop = "proposition_loop_male"
            IF g_vehicleAnimType = VAT_LOW
                fLoop = "low_car_prop_loop_female"
                mLoop = "low_car_prop_loop_player"
            ENDIF
            INT mf2 = 1
            IF g_driveable
                mf2 = 33
            ENDIF
            TASK_PLAY_ANIM(g_girl, pd, fLoop, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 1))
            TASK_PLAY_ANIM(g_player, pd, mLoop, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf2))
        ENDIF
        g_carPropPhase = 1
        IF g_carPropPendingMenu != ML_NONE
            g_menuLevel = g_carPropPendingMenu
            g_menuIndex = 0
            g_carPropPendingMenu = ML_NONE
        ENDIF
    ELIF g_carPropPhase = 2
        IF GET_GAME_TIMER() >= g_carPropWait
            g_carPropActive = FALSE
            g_carPropPhase = 0
            IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
                g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
                IF DOES_ENTITY_EXIST(g_car)
                    SET_VEHICLE_INTERIORLIGHT(g_car, FALSE)
                ENDIF
            ENDIF
            IF g_carPropReturnMenu != ML_NONE
                g_menuLevel = g_carPropReturnMenu
                g_menuIndex = g_carPropReturnIdx
                g_carPropReturnMenu = ML_NONE
                g_carPropReturnIdx = 0
            ENDIF
        ENDIF
    ENDIF
    IF g_carPropActive AND (g_carPropPhase = 0 OR g_carPropPhase = 1)
        IF g_menuLevel != ML_SERVICES AND g_menuLevel != ML_ESCALATE AND g_postSexPhase = 0
            IF g_carPropPendingMenu = ML_NONE
                CC_ExitCarProp()
            ENDIF
        ENDIF
    ENDIF
ENDPROC


// ════════════════════════════════════════════════════════════════════════
//  Finish 执行 (C# _finishExecutePending 段 — 车/街/坐躺链路)
// ════════════════════════════════════════════════════════════════════════
PROC CC_RequestFinish(BOOL inside)
    g_finInside = inside
    g_finExecPending = TRUE
    g_finExecAt = GET_GAME_TIMER() + 300
    g_menuLevel = ML_NONE
ENDPROC

PROC CC_ExecuteFinish()
    g_finExecPending = FALSE
    IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
        STOP_CURRENT_PLAYING_AMBIENT_SPEECH(g_girl)
    ENDIF
    CC_Pulse("fin")
    SET_CONTROL_SHAKE(PLAYER_CONTROL, 400, 255)
    g_shakeOffAt = GET_GAME_TIMER() + 400
    g_finBurstFired = TRUE
    g_lastRumble = GET_GAME_TIMER()

    // ── 车: loop → 拉出(P2rev→P1rev) / 内射(outro→prop) ──
    IF g_mode = CCM_CAR AND g_animPhase = 2
        IF NOT g_finInside
            // 拉出: 反向 P2
            g_carPullRev = TRUE
            g_animPhase = 3
            BOOL fpVal0
            fpVal0 = CC_IsFirstPerson()

            TEXT_LABEL_63 dict0
            dict0 = CC_CarDict(fpVal0)
            TEXT_LABEL_63 fP2r = "proposition_to_sex_p2_prostitute"
            TEXT_LABEL_63 mP2r = "proposition_to_sex_p2_male"
            IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
                fP2r = "proposition_to_bj_p2_prostitute"
                mP2r = "proposition_to_bj_p2_male"
                IF g_vehicleAnimType = VAT_LOW
                    fP2r = "low_car_prop_to_bj_p2_female"
                    mP2r = "low_car_prop_to_bj_p2_player"
                ENDIF
            ELSE
                IF g_vehicleAnimType = VAT_LOW
                    fP2r = "low_car_prop_to_sex_p2_female"
                    mP2r = "low_car_prop_to_sex_p2_player"
                ENDIF
            ENDIF
            IF CC_ReqDict(dict0)
                INT mf0 = 2
                IF g_driveable
                    mf0 = 34
                ENDIF
                TASK_PLAY_ANIM(g_girl, dict0, fP2r, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), 1.0)
                TASK_PLAY_ANIM(g_player, dict0, mP2r, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf0), 1.0)
                SET_ENTITY_ANIM_SPEED(g_girl, dict0, fP2r, -1.0)
                SET_ENTITY_ANIM_SPEED(g_player, dict0, mP2r, -1.0)
            ENDIF
            FLOAT p2rDur = CC_SafeAnimDur(dict0, fP2r, 1.5)
            g_animWait = GET_GAME_TIMER() + ROUND(p2rDur * 1000.0)
            // ★ v0.60：删掉"收尾中…"提示（用户要求：动作本身看得见，提示太吵）
            EXIT
        ENDIF
        // 内射: outro
        BOOL fpVal1
        fpVal1 = CC_IsFirstPerson()

        TEXT_LABEL_63 dict1
        dict1 = CC_CarDict(fpVal1)
        TEXT_LABEL_63 fEx = "sex_to_proposition_p1_prostitute"
        TEXT_LABEL_63 mEx = "sex_to_proposition_p1_male"
        IF ARE_STRINGS_EQUAL(g_curAction, "blowjob")
            fEx = "bj_to_proposition_p1_prostitute"
            mEx = "bj_to_proposition_p1_male"
            IF g_vehicleAnimType = VAT_LOW
                fEx = "low_car_bj_to_prop_p1_female"
                mEx = "low_car_bj_to_prop_p1_player"
            ENDIF
        ELSE
            IF g_vehicleAnimType = VAT_LOW
                fEx = "low_car_sex_to_prop_p1_female"
                mEx = "low_car_sex_to_prop_p1_player"
            ENDIF
        ENDIF
        IF CC_ReqDict(dict1)
            INT mf1 = 2
            IF g_driveable
                mf1 = 34
            ENDIF
            TASK_PLAY_ANIM(g_girl, dict1, fEx, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
            TASK_PLAY_ANIM(g_player, dict1, mEx, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, mf1))
        ENDIF
        g_animPhase = 3
        FLOAT exDur = CC_SafeAnimDur(dict1, fEx, 1.5)
        g_animWait = GET_GAME_TIMER() + ROUND(exDur * 1000.0)
        // ★ v0.60：删掉"内射收尾中…／收尾中…"提示（用户要求）
        EXIT
    ENDIF

    // ── 街 站立类 (2/5/10/11): 3s 延迟 → 黑屏相位 ──
    IF g_mode = CCM_STREET AND (g_streetActionIdx = 2 OR g_streetActionIdx = 5 OR g_streetActionIdx = 10 OR g_streetActionIdx = 11) AND g_animPhase = 2
        IF NOT g_finInside
            STOP_ANIM_TASK(g_player, g_mDict, g_mAnim, -4.0)
            IF g_streetActionIdx = 5 OR g_streetActionIdx = 10 OR g_streetActionIdx = 11
                SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, 0.0)
                VECTOR pull = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<0.0, -0.70, 0.0>>)
                SET_ENTITY_COORDS_NO_OFFSET(g_player, <<pull.x, pull.y, pull.z>>, FALSE, FALSE, FALSE)
                SET_ENTITY_HEADING(g_player, GET_ENTITY_HEADING(g_player) - 90.0)
            ENDIF
            IF CC_ReqDict("switch@trevor@jerking_off")
                TASK_PLAY_ANIM(g_player, "switch@trevor@jerking_off", "trev_jerking_off_exit", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 50))
            ENDIF
        ENDIF
        g_animWait = GET_GAME_TIMER() + 3000
        g_animPhase = 3
        // ★ v0.60：删掉"内射收尾中…／收尾中…"提示（用户要求）
        EXIT
    ENDIF

    // ── 街 坐/躺类: 反向 P2 → (fade) → FinishStreetSit ──
    IF g_mode = CCM_STREET AND g_animPhase = 2
        IF NOT g_finInside
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
            IF CC_ReqDict(fD)
                TASK_PLAY_ANIM(g_girl, fD, fA, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), 1.0)
                SET_ENTITY_ANIM_SPEED(g_girl, fD, fA, -1.0)
            ENDIF
            FLOAT p2Dur = CC_SafeAnimDur(fD, fA, 1.5)
            g_animWait = GET_GAME_TIMER() + ROUND(p2Dur * 1000.0)
            g_sitCowPullRev = TRUE
            g_animPhase = 3
            // ★ v0.60：删掉"收尾中…"提示（用户要求）
            EXIT
        ENDIF
        // 内射: 动作→prop outro
        TEXT_LABEL_63 mD2
        TEXT_LABEL_63 mA2
        TEXT_LABEL_63 fD2
        TEXT_LABEL_63 fA2
        VECTOR off2
        FLOAT hd2
        FLOAT zo2
        FLOAT rx2
        FLOAT rh2
        BOOL sitFlag2
        CC_PropIntroExitData(2, g_streetActionIdx, mD2, mA2, fD2, fA2, off2, hd2, sitFlag2, zo2, rx2, rh2)
        IF CC_ReqDict(mD2)
            IF NOT ARE_STRINGS_EQUAL(mD2, fD2)
                CC_ReqDict(fD2)
            ENDIF
            IF rx2 != 0.0
                VECTOR pp = GET_ENTITY_COORDS(g_player)
                TASK_PLAY_ANIM_ADVANCED(g_player, mD2, mA2, pp, <<rx2, 0.0, GET_ENTITY_HEADING(g_player)>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2), 0.0, EULER_YXZ)
            ELSE
                TASK_PLAY_ANIM(g_player, mD2, mA2, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
            ENDIF
            TASK_PLAY_ANIM(g_girl, fD2, fA2, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 2))
        ENDIF
        g_animPhase = 3
        FLOAT exDur2 = CC_SafeAnimDur(fD2, fA2, 1.5)
        g_animWait = GET_GAME_TIMER() + ROUND(exDur2 * 1000.0)
        // ★ v0.60：删掉"内射收尾中…"提示（用户要求）
        EXIT
    ENDIF
    // 其他: 直接收尾
    CC_PlayFinishVoice()
    CC_EndAnimation()
ENDPROC

// 坐/躺完成收尾 (C# FinishStreetSitServiceAndReopen 简链)
PROC CC_FinishStreetSit()
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
    BOOL wasForced = g_finForced
    g_animPhase = 0
    g_animSpeed = 1.0
    g_femSpeedScale = 1.0
    g_streetActionIdx = -1
    g_curAction = ""
    g_sitCowPullRev = FALSE
    g_standBJRev = FALSE
    g_rdoggyRev = FALSE
    g_lastPaid = 0
    g_finExecPending = FALSE
    g_finInside = FALSE
    g_finForced = FALSE
    g_finBroke = FALSE
    g_finAsked = FALSE
    g_state = ST_IDLE
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
    // 回 proposition loop
    IF g_sitPropActive
        CC_PropPlayPair(2, 1)
        g_sitPropPhase = 2
        g_menuLevel = g_streetSitCat
        g_menuIndex = 0
    ELSE
        g_menuLevel = ML_NONE
    ENDIF
ENDPROC

// 站立完成收尾 (C# FinishStandingServiceAndReopen)
PROC CC_FinishStanding()
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
    FREEZE_ENTITY_POSITION(g_player, FALSE)
    SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
    IF CC_EXISTS(g_girl)
        FREEZE_ENTITY_POSITION(g_girl, FALSE)
        SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
        SET_ENTITY_NO_COLLISION_ENTITY(g_girl, g_player, TRUE)
        SET_PED_KEEP_TASK(g_girl, FALSE)
        CLEAR_PED_TASKS(g_girl)
        SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
        FLOAT dist = VDIST(GET_ENTITY_COORDS(g_girl), GET_ENTITY_COORDS(g_player))
        FLOAT spd = 1.0
        IF dist > 7.0
            spd = 3.0
        ENDIF
        TASK_FOLLOW_TO_OFFSET_OF_ENTITY(g_girl, g_player, <<0.0, -1.0, 0.0>>, spd, -1, 1.5, TRUE)
    ENDIF
    SET_PLAYER_CAN_DO_DRIVE_BY(PLAYER_ID(), TRUE)
    // 站姿动作收尾（C# 15355/15692 同）：改为状态推导，避免影子值脱节。
    // 此刻 g_state 仍是 ST_ANIMATING（下面第 715 行才置 ST_IDLE）、g_hasGirl 仍 TRUE ⇒
    //   开关=开启 → 保持无视（继续闲逛也跟着无视）；开关=关闭 → 当场 FALSE（警察能打你）。
    CC_ApplyPoliceIgnore()
    BOOL wasForced = g_finForced
    g_animPhase = 0
    g_animSpeed = 1.0
    g_femSpeedScale = 1.0
    g_streetActionIdx = -1
    g_curAction = ""
    g_lastPaid = 0
    g_finExecPending = FALSE
    g_finInside = FALSE
    g_finForced = FALSE
    g_finBroke = FALSE
    g_finAsked = FALSE
    g_kissingRev = FALSE
    g_standBJRev = FALSE
    g_rdoggyRev = FALSE
    g_state = ST_IDLE
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
    IF g_aLifeMode AND g_curAlIdx >= 0
        IF g_alRep[g_curAlIdx] <= CC_REP_MIN
            g_aLifeHookerSvc = FALSE
            IF CC_EXISTS(g_girl)
                SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, FALSE)
                TASK_WANDER_STANDARD(g_girl, 0.0, INT_TO_ENUM(EWDR_SCRIPT_FLAGS, 0))
            ENDIF
            g_menuLevel = ML_NONE
            EXIT
        ENDIF
    ENDIF
    // ★★ 2026-09-25 用户要求："站姿动作完全结束、进入可控制玩家的时候，才能重新回到
    //   可以选择站姿/坐姿/躺姿的页面"。
    //   C# 原版这里是 `menuLevel = MenuLevel.StandingServices`（= 本工程的 ML_STANDINGSVC，
    //   站立服务子页 5 项）。按用户的口径改成**服务选择页 ML_SERVICES**（站立/坐姿/躺姿
    //   三选一）—— 想直接接着选另一个站姿动作，按 A 进"站立服务"即可。
    //   （若想回到 C# 原版行为，把这一行改回 ML_STANDINGSVC 即可。）
    g_menuLevel = ML_SERVICES
    g_menuIndex = 0
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  Animating tick 公共段 (速度/治疗/呻吟/节拍/语音)
// ════════════════════════════════════════════════════════════════════════
PROC CC_AnimCommonTick_P1()
    INT nowMs = GET_GAME_TIMER()
ENDPROC

PROC CC_AnimCommonTick()
    INT nowMs = GET_GAME_TIMER()
    IF NOT IS_STRING_NULL(g_curAction)
        IF g_animPhase = 2
            // ★★★ v0.64：先查「呻吟语音」总开关 —— 这是"动作在跑、却完全没有女声"
            //   最容易踩到、也最难自己发现的原因（设置菜单里的「呻吟语音: 开/关」，
            //   一次 A / ← / → 就翻过去了，而且它是**全局**的、换女伴也不会自己恢复）。
            //   看到屏幕上这句就知道去哪儿开回来，不用再猜"是不是模组坏了"。
            IF NOT g_voiceMoan
                // ★★★ v0.79：**屏幕上那句提示删掉了**（用户要求："把播放女伴呻吟音频
                //   时候的提示删了"）。原来这里会打
                //     「呻吟语音开关是关的（设置菜单 → 呻吟语音）⇒ 本场没有女声」。
                //   现在只留状态记账（`g_diagAudioState`），屏幕上一个字都不出。
                //   ⚠ 代价说清楚：如果哪天又"全程没女声"，**先自己看一眼
                //     设置 → 呻吟语音 是不是被关掉了**（那只开关是全局的，换女伴不会自己恢复）。
                g_diagAudioState = 3
            ELIF g_animSpeed > 0.0
                // ★★★ 2026-09-26 v0.59：补上 C# 漏掉的三个前置条件
                //   （CinnamonCoffee.cs 10531-10532）：
                //       && !_bjThroatPending && _throatLockAt == 0 && !_throatSpeedLocked
                //   端口原来一条都没搬 ⇒ 口交系（idx 2/3/4）在"深喉锁定期"里照样呻吟，
                //   而 C# 在那一段时间是**故意**让她出不了声的（深喉 WAV 在放）。
                //   变量对应：_bjThroatPending→g_bjThroatPending、
                //             _throatLockAt→g_throatLockAt、_throatSpeedLocked→g_throatLocked。
                IF NOT g_bjThroatPending AND g_throatLockAt = 0 AND NOT g_throatLocked
                    g_diagAudioState = 0
                    INT voiceInterval = ROUND(VOICE_INTERVAL / (1.0 + (g_animSpeed - 1.0) * 0.55))
                    IF nowMs - g_lastVoice > voiceInterval
                        CC_PlayMoaning()
                        g_lastVoice = nowMs
                        // ★ v0.65 临时音频探针：每个女伴**第一次出声的那一刻**请求打一行现场读数
                        //   （真正的文字在 cc_main.sc 的 CC_AudioProbeTick 里打 ——
                        //    那里 CC_MenuLabel 已定义，能一并显示"呻吟语音"开关状态）。
                        //   定性用，音频确认正常后删掉这 4 行。见 cc_part1.sc g_voiceProbeShown。
                        IF NOT g_voiceProbeShown
                            g_voiceProbeShown = TRUE
                            g_audioProbeWant = TRUE
                        ENDIF
                        // ★★★ v0.66：排一次"到底有没有真在播"的检查（见 CC_VoiceProbeTick）。
                        //   没锁定的女伴每次发完呻吟都查 —— 查不到就自动换下一个嗓子。
                        IF NOT g_voiceLocked
                            g_voiceCheckAt = GET_GAME_TIMER() + 400
                        ENDIF
                    ENDIF
                ELSE
                    // ★ 音频诊断（g_showDiag 默认关 ⇒ 平常不显示；变化驱动，不顶同帧别的诊断）
                    //   看到这条 = 正在深喉锁定期 ⇒ 这一段**原版也不发声**，不是 bug。
                    IF g_diagAudioState != 2
                        g_diagAudioState = 2
                        CC_HudDiag("~y~AUDIO: deep-throat lock - no moans this stretch (as in the original)", 1500)
                    ENDIF
                ENDIF
            ELSE
                // ★ 这里是"动作在跑、速度却被拖到 0"的情况（呻吟那一支被
                //   `g_animSpeed > 0.0` 整个跳过）。成因是调速键（d-pad 左/右、长按生效）
                //   与菜单"确认"共键，选完服务那一下容易把速度一路拖到 0。
                // ★★★ v0.79：**屏幕上那句 AUDIO 提示删掉了**（用户要求："把播放女伴呻吟
                //   音频时候的提示、探针文字删了"）。只留状态记账，不再打屏幕。
                //   ⚠ 代价：以后"动作在跑但没声音"时，**先看左下角有没有速度提示**
                //     （按调速键时本来就会回显"速度：偏慢/正常/1.0x…"），
                //     显示 `速度 0（静音！）` ⇒ 按 **X** 复位速度即可。
                g_diagAudioState = 1
            ENDIF
            // 节拍震动 (替代 slap WAV)
            IF g_slapSoundsOn AND ARE_STRINGS_EQUAL(g_curAction, "sex") AND g_animSpeed >= 1.4 AND g_streetActionIdx != 1
                INT slowV = SLAP_CAR_SEX_SLOW
                INT midV = SLAP_CAR_SEX_MID
                INT fastV = SLAP_CAR_SEX_FAST
                IF g_mode = CCM_STREET
                    SWITCH g_streetActionIdx
                        CASE 5
                            slowV = SLAP_WALL_SEX_SLOW
                            midV = SLAP_WALL_SEX_MID
                            fastV = SLAP_WALL_SEX_FAST
                            BREAK
                        CASE 6
                        CASE 7
                            slowV = SLAP_COW_SEX_SLOW
                            midV = SLAP_COW_SEX_MID
                            fastV = SLAP_COW_SEX_FAST
                            BREAK
                        CASE 8
                        CASE 9
                            IF g_sinkraAvail AND (g_sinkraLay OR g_sinkraRevLay)
                                slowV = SLAP_SINKRA_LAY_COW_SLOW
                                midV = SLAP_SINKRA_LAY_COW_MID
                                fastV = SLAP_SINKRA_LAY_COW_FAST
                            ELSE
                                slowV = SLAP_LAY_COW_SLOW
                                midV = SLAP_LAY_COW_MID
                                fastV = SLAP_LAY_COW_FAST
                            ENDIF
                            BREAK
                        CASE 10
                            slowV = SLAP_DOGGY_SLOW
                            midV = SLAP_DOGGY_MID
                            fastV = SLAP_DOGGY_FAST
                            BREAK
                        CASE 11
                            slowV = SLAP_RDOGGY_SLOW
                            midV = SLAP_RDOGGY_MID
                            fastV = SLAP_RDOGGY_FAST
                            BREAK
                    ENDSWITCH
                ENDIF
                INT slapInterval = slowV
                IF g_animSpeed <= 2.0
                    FLOAT t = g_animSpeed - 1.0
                    slapInterval = ROUND(TO_FLOAT(slowV) + TO_FLOAT(midV - slowV) * t)
                ELSE
                    FLOAT t2 = g_animSpeed - 2.0
                    slapInterval = ROUND(TO_FLOAT(midV) + TO_FLOAT(fastV - midV) * t2)
                ENDIF
                IF nowMs - g_lastSlap > slapInterval
                    CC_Pulse("slap")
                    g_lastSlap = nowMs
                ENDIF
            ENDIF
            // 接吻节拍 (替代 kiss WAV)
            IF ARE_STRINGS_EQUAL(g_curAction, "kissing")
                INT kissInterval = KISS_SOUND_SLOW
                IF g_animSpeed <= 2.0
                    FLOAT kt = g_animSpeed - 1.0
                    kissInterval = ROUND(TO_FLOAT(KISS_SOUND_SLOW) + TO_FLOAT(KISS_SOUND_MID - KISS_SOUND_SLOW) * kt)
                ELSE
                    FLOAT kt2 = g_animSpeed - 2.0
                    kissInterval = ROUND(TO_FLOAT(KISS_SOUND_MID) + TO_FLOAT(KISS_SOUND_FAST - KISS_SOUND_MID) * kt2)
                ENDIF
                IF nowMs - g_lastKiss > kissInterval
                    CC_Pulse("kiss")
                    g_lastKiss = nowMs
                ENDIF
            ENDIF
            // 治疗
            IF nowMs - g_lastHeal > HEAL_INTERVAL
                INT hp = HEAL_AMOUNT
                IF g_animSpeed > 1.0
                    hp = ROUND(HEAL_AMOUNT * g_healRate * g_animSpeed)
                ELSE
                    hp = ROUND(HEAL_AMOUNT * g_healRate)
                ENDIF
                IF g_healPlayer
                    INT cur = GET_ENTITY_HEALTH(g_player) + hp
                    INT mx = GET_PED_MAX_HEALTH(g_player)
                    IF cur > mx
                        cur = mx
                    ENDIF
                    SET_ENTITY_HEALTH(g_player, cur)
                ENDIF
                IF g_healGirl AND CC_EXISTS(g_girl)
                    INT cur2 = GET_ENTITY_HEALTH(g_girl) + hp
                    INT mx2 = GET_PED_MAX_HEALTH(g_girl)
                    IF cur2 > mx2
                        cur2 = mx2
                    ENDIF
                    SET_ENTITY_HEALTH(g_girl, cur2)
                ENDIF
                g_lastHeal = nowMs
            ENDIF
            // 速度控制 (loop 期)
            // ★ 2026-09-25：菜单开着时**不读**左右键 —— 十字键左右现在的语义是
            //   "菜单确认/切换"（见 CC_MenuNav），必须让位，否则在结束菜单里按左右
            //   会同时把动作速度改掉。
            // ★★★ v0.64 调速键"开场免疫"（修「有时候还是没有声音」）：
            //   调速键 = d-pad ←/→ 且**长按生效**，而菜单"确认"也是 ←/→。
            //   v0.62 把菜单在动作开始时关掉后，这一按就直接落到调速上了 ⇒
            //   确认服务时压着的那一下会把 g_animSpeed 拖到 0 ⇒ 呻吟那一支被
            //   `IF g_animSpeed > 0.0` 整个跳过 ⇒ **全程静音**
            //   （换一个女伴有声，因为 CC_FullReset 把速度复位成 1.0）。
            //   规则（用户 2026-09-26 明确要求）：动作开场 **500 ms 内忽略调速键**，
            //   之后正常调速。（v0.63 用过的"必须松开过一次才上膛"手感别扭，已换掉。）
            IF g_speedCooldown = 0 AND NOT g_finExecPending AND g_menuLevel = ML_NONE AND GET_GAME_TIMER() >= g_speedImmuneUntil
                BOOL changed = FALSE
                BOOL spdReset = FALSE
                // ★★★ v0.60 修复"按 X 复位速度没效果"：
                //   原因一（真 bug）：复位原来是 ELIF 链的**最后一支**。
                //     十字键左/右既是调速键，也是菜单里的"确认"键 —— 用户选完服务
                //     那一下经常还压着左右键，于是前两支先命中本帧，复位永远轮不到。
                //     复位是离散单次动作、跟长按调速不打架 ⇒ 提到**最前**。
                //   原因二（可观测性）：同时加了屏幕读数（见下面 CC_HudNote），
                //     这样"键到底有没有被读到"屏幕上直接看得出来，不用猜。
                IF IN_SpeedReset()
                    g_animSpeed = 1.0
                    spdReset = TRUE
                    changed = TRUE
                ELIF IN_SpeedUp()
                    g_animSpeed = g_animSpeed + 0.05
                    IF g_animSpeed > 3.0
                        g_animSpeed = 3.0
                    ENDIF
                    changed = TRUE
                ELIF IN_SpeedDown()
                    g_animSpeed = g_animSpeed - 0.05
                    IF g_animSpeed < 0.0
                        g_animSpeed = 0.0
                    ENDIF
                    changed = TRUE
                ENDIF
                IF changed
                    g_speedCooldown = 10
                    IF g_mode = CCM_CAR AND NOT g_backseatCarSex
                        BOOL fpS
                        fpS = CC_IsFirstPerson()

                        TEXT_LABEL_63 dS
                        dS = CC_CarDict(fpS)
                        SET_ENTITY_ANIM_SPEED(g_player, dS, g_mAnim, g_animSpeed)
                        IF CC_EXISTS(g_girl)
                            SET_ENTITY_ANIM_SPEED(g_girl, dS, g_fAnim, g_animSpeed * g_femSpeedScale)
                        ENDIF
                    ELSE
                        SET_ENTITY_ANIM_SPEED(g_player, g_mDict, g_mAnim, g_animSpeed)
                        IF CC_EXISTS(g_girl)
                            SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, g_animSpeed * g_femSpeedScale)
                        ENDIF
                    ENDIF
                    // ★★★ v0.60：速度一变 → 屏幕上留读数。
                    //
                    //   【为什么要打这个】
                    //     g_animSpeed 这一条变量同时决定三件事：
                    //       ① 呻吟能不能出声（<= 0 就整段静音）
                    //       ② 节拍震动 / 接吻震动的强弱（阈值 1.4 / 2.0）
                    //       ③ 治疗速率
                    //     而"动画本身快慢"走的是 SET_ENTITY_ANIM_SPEED —— 这条 native
                    //     在本端口的白名单里**没被证明过**（官方 dev_ng 头文件在用，
                    //     大概率存在，但没人实机确认）。也就是说：调速按下去，**画面
                    //     可能完全不变**，用户就会以为"按键没反应"。
                    //     所以改用变量自己出读数：只要打出来 ⇒ 键被读到了；
                    //     读到了而画面不动 ⇒ 是 SET_ENTITY_ANIM_SPEED 的事，与按键无关。
                    //     一眼定性，不用猜。
                    //   【为什么用不受门控的 CC_HudNote】
                    //     这是"我刚按的键到底生效了没"的直接反馈，属于关键状态反馈，
                    //     不该被「教学提示」开关吃掉（否则关了教学又变成"没反应"）。
                    //   【为什么分级而不是显示数字】
                    //     本端口的 dev_ng 里**没有** INT_TO_STRING / CONVERT_INT_TO_STRING
                    //     可用（grep 全 dev_ng 无此命令），不想为一个 HUD 读数新引入
                    //     未证明的 native ⇒ 用固定文案分级。
                    //   区间用 1.0±0.05 包住 1.00，因为 0.05 累加/累减有浮点误差，
                    //   不能写 `g_animSpeed = 1.0` 这种精确比较。
                    STRING spdMsg
                    IF spdReset
                        spdMsg = "~g~Speed reset to 1.0x"
                    ELIF g_animSpeed < 0.005
                        spdMsg = "~r~Speed 0 (muted!)"
                    ELIF g_animSpeed < 0.95
                        spdMsg = "~o~Speed: slow"
                    ELIF g_animSpeed < 1.05
                        spdMsg = "~g~Speed: normal 1.0x"
                    ELIF g_animSpeed < 2.0
                        spdMsg = "~g~Speed: fast"
                    ELIF g_animSpeed < 2.95
                        spdMsg = "~g~Speed: very fast"
                    ELSE
                        spdMsg = "~g~Speed: 3.0x max"
                    ENDIF
                    CC_HudNote(spdMsg, 1600)
                ENDIF
            ENDIF
        ENDIF
    ENDIF
    // speedCooldown 递减
    IF g_speedCooldown > 0
        g_speedCooldown = g_speedCooldown - 1
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  动画相位诊断（★ 只在相位/模式**变化**的那一帧写一次）
//
//  【为什么必须"变化才写"】
//    诊断与 "VOICE ok" 这类一次性消息共用 g_diagText 这一个通道。
//    原来这里**每帧**写一次 ⇒ 同帧内先写的一次性消息被后写的相位文本顶掉
//    ⇒ 用户实测"看不到音频诊断、只看到动作诊断"。（单通道冲突）
//    改成变化驱动后，两者互不干扰：一次性消息能正常显示满自己的时长。
//
//  【怎么读这条诊断】
//    ~r~ g_mode NONE  → ★移植缺陷复现：动作入口没设 g_mode ⇒ 相位机根本不会跑。
//                       （2026-09-25 的真凶就是它；见到它先查 CC_Start*Action）
//    ~y~ p0           → intro 中。intro 用 flag 2 = 停在末帧，所以"人不动"是正常的；
//                       只要这句能变成 p2 就说明相位机在跑。
//    ~g~ p2           → loop，正常应该停在这里。呻吟/速度控制/治疗都从这一级开始生效。
//    ~y~ p3+          → 收尾阶段。
// ════════════════════════════════════════════════════════════════════════
PROC CC_DiagAnimPhase()
    IF g_animPhase = g_diagPhase AND g_mode = g_diagMode
        EXIT
    ENDIF
    g_diagPhase = g_animPhase
    g_diagMode = g_mode
    IF g_mode = CCM_CAR
        IF g_backseatCarSex
            IF g_animPhase = 2
                CC_HudDiag("~g~CAR backseat p2 LOOP OK", 1500)
            ELSE
                CC_HudDiag("~y~CAR backseat intro/finish", 1500)
            ENDIF
        ELIF g_animPhase = 0
            CC_HudDiag("~y~CAR p0 intro (hold last frame)", 1500)
        ELIF g_animPhase = 1
            CC_HudDiag("~y~CAR p1 -> p2 transition", 1500)
        ELIF g_animPhase = 2
            CC_HudDiag("~g~CAR p2 LOOP OK", 1500)
        ELSE
            CC_HudDiag("~y~CAR p3+ finish", 1500)
        ENDIF
    ELIF g_mode = CCM_STREET
        IF g_animPhase = 0
            CC_HudDiag("~y~ST p0 intro", 1500)
        ELIF g_animPhase = 1
            CC_HudDiag("~y~ST p1 fade/transition", 1500)
        ELIF g_animPhase = 2
            CC_HudDiag("~g~ST p2 LOOP OK", 1500)
        ELIF g_animPhase = 100
            CC_HudDiag("~y~ST p100 fade-in", 1500)
        ELSE
            CC_HudDiag("~y~ST p3+ finish", 1500)
        ENDIF
    ELSE
        // ★ 这条红字 = 上面那类移植缺陷复现：g_mode 没被设过
        //   ⇒ CC_AnimatingTick 两个 tick 分支都进不去 ⇒ 相位机死。
        //   看到它**别去查相位机**，去查 CC_StartCarAction / CC_StartStreetAction。
        CC_HudDiag("~r~g_mode NONE! phase machine dead (port bug)", 4000)
    ENDIF
ENDPROC

// Animating 主 tick
PROC CC_AnimatingTick()
    IF NOT CC_EXISTS(g_girl)
        CC_EndAnimation()
        EXIT
    ENDIF
    // ★ 相位诊断（2026-09-25晚 重写）：只在相位/模式变化的那一帧写一次。
    //   原来这里每帧写 g_diagText，把同帧的 "VOICE ok" / 车内相位文本全顶掉了
    //   —— 那就是用户报的"只有动作诊断、看不到音频诊断"。
    CC_DiagAnimPhase()
    CC_AnimCommonTick()
    IF g_mode = CCM_CAR
        CC_RunCarAnimation()
    ELIF g_mode = CCM_STREET
        IF g_streetActionIdx = 1 OR g_streetActionIdx = 3 OR g_streetActionIdx = 4 OR g_streetActionIdx = 6 OR g_streetActionIdx = 7 OR g_streetActionIdx = 8 OR g_streetActionIdx = 9
            CC_StreetSitActionTick()
        ELSE
            CC_StreetStandingTick()
        ENDIF
    ENDIF
    // 完成执行 (C# _finishExecutePending)
    IF g_finExecPending AND g_menuLevel = ML_NONE AND GET_GAME_TIMER() >= g_finExecAt
        CC_ExecuteFinish()
    ENDIF
    // ── 街面坐/躺 「收尾相位(3) 完成」检测 ─────────────────────────────────
    //   ★★★ 2026-09-25晚 修"选内射 → 收尾动画播完 → 按键全死，只能重启游戏"：
    //
    //     原来这里只判 `g_sitCowPullRev`，而那个标记**只在"拔出"路径**里被设
    //     （CC_ExecuteFinish 的 `NOT g_finInside` 分支，cc_part2c.sc 那一支）。
    //     "**内射**"路径不设它 ⇒ 这段永远不成立 ⇒ `g_animPhase` 永远停在 3
    //     ⇒ **没有任何人**调用 CC_FinishStreetSit() ⇒ 两人定格在收尾动画里；
    //        而主键处理对 phase 3 既不满足 `= 2` 也不满足 `< 2` ⇒ 两个分支都不进
    //     ⇒ **按任何键都没反应**。
    //
    //   现在改成：只要是「街面 + 坐/躺 proposition 仍在 + phase = 3」，
    //   收尾动画一播完就收工 —— **拔出 / 内射两条路都覆盖**。
    //   `g_sitPropActive` 这个条件同时把站立黑屏相位机(2/5/10/11)排除掉：
    //   它们也用 phase 3，但那类动作没有 proposition。
    IF g_mode = CCM_STREET AND g_sitPropActive AND g_animPhase = 3
        IF GET_GAME_TIMER() >= g_animWait
            BOOL wasPull = g_sitCowPullRev
            g_sitCowPullRev = FALSE
            IF wasPull AND NOT g_finInside
                CC_PlaySpit()
            ENDIF
            CC_FinishStreetSit()
        ENDIF
    ENDIF
    // 主键: 完成/中止
    IF IN_Master() AND g_keyCooldown = 0
        g_keyCooldown = KEY_COOLDOWN_FRAMES
        IF g_animPhase = 2
            IF ARE_STRINGS_EQUAL(g_curAction, "kissing") OR ARE_STRINGS_EQUAL(g_curAction, "facesitting")
                // 吻/facesit: 直接完成
                IF g_mode = CCM_STREET AND g_streetActionIdx = 0
                    g_kissingRev = FALSE
                    SET_ENTITY_ANIM_CURRENT_TIME(g_player, g_mDict, g_mAnim, 0.500)
                    SET_ENTITY_ANIM_CURRENT_TIME(g_girl, g_fDict, g_fAnim, 0.500)
                    SET_ENTITY_ANIM_SPEED(g_player, g_mDict, g_mAnim, g_animSpeed)
                    SET_ENTITY_ANIM_SPEED(g_girl, g_fDict, g_fAnim, g_animSpeed)
                    g_animPhase = 3
                    // ★ v0.60：删掉"收尾中…"提示（用户要求）
                ELSE
                    CC_PlayFinishVoice()
                    CC_EndAnimation()
                ENDIF
            ELIF g_menuLevel = ML_NONE
                // 打开 Finish 菜单 (选择 拉出/内射 后由 CC_RequestFinish 执行)
                g_menuLevel = ML_FINISH
                g_menuIndex = 0
            ELSE
                // ★ 2026-09-25晚：Finish 菜单已经开着时再按 主键(B+右) = 撤销（等价于按 B 返回），
                //   回到动画循环继续。
                //   原来这里没有这个分支：主键会**重新**执行上面两行 ⇒ 用户把光标移到
                //   "内射"之后手一抖再按一次主键，光标就被弹回第 0 项 —— 看着像"按键失灵"。
                //   （真正的"按键完全没反应"是另一回事，见 cc_main.sc CASE ST_ANIMATING
                //     里补上的 CC_MenuNav 调用。）
                g_menuLevel = ML_NONE
            ENDIF
        ELIF g_animPhase < 2
            // intro 中止 → 退款 + 回 proposition
            IF g_lastPaid > 0
                CC_AddMoney(g_lastPaid)
                CC_HudStatus("~r~Aborted! ~s~Refund: ~g~$", 3000)
                g_lastPaid = 0
            ELSE
                CC_HudStatus("~r~Aborted!", 3000)
            ENDIF
            CC_DetachDildo()

            // ══════════════════════════════════════════════════════════════
            // ★★★ 2026-09-25晚（第二次修）：**坐/躺系动作的中止是独立分支**
            //
            //   C# 原版把"中止"分成两条完全不同的路径（CinnamonCoffee.cs 11584-11730）：
            //     · 街面**站立**类 (0/2/5/10/11)：收拾干净 → **站起来** → 回 StandingServices
            //     · 街面**坐/躺**类 (1/3/4/6/7/8/9)：**退回 proposition 循环** →
            //                                     回 Sitting/LayingServices
            //   移植时两条被合并成了一条（用的是"站立"那条的语义）——
            //   **和"坐/躺双重下沉"是同一类错误**：C# 的独立分支被合并 ⇒ 丢了语义。
            //
            //   错误合并导致的连锁反应（用户实测"坐姿女上位中止后回到坐姿，
            //   然后按键全没反应，只能重启"）：
            //     ① 菜单被设成 ML_STANDINGSVC（站立服务）——但人还在坐姿 proposition 里
            //     ② Idle 期的"备份退出"判据是
            //          g_menuLevel != ML_SITTINGSVC/ML_LAYINGSVC/ML_SITDOWNSUB → 立刻终止
            //        回 ML_STANDINGSVC **正好命中** ⇒ 玩家还没反应过来就被拖进
            //        "站起"流程（CC_ExitStreetSitProp → phase 3→4）
            //     ③ 那段转场期间主键被"转场中禁止开菜单"挡住
            //        （CC_MenuNav / CC_IdleMasterKey 里
            //         g_sitPropActive AND (g_sitPropPhase < 2 OR >= 3) ⇒ 什么都不做）
            //        ⇒ 表现就是"按键全无反应"。
            //
            //   正确做法（逐条对齐 C# 11584-11663）：
            //     · 位置修回 proposition 的下沉 Z（**绝对**高度 g_sitOrigin.z - 0.855）
            //     · 重播 proposition loop 动画（CC_PropPlayPair(2,1)）
            //     · **保持** FREEZE + 关闭的碰撞（坐姿就是这么待着的，别解冻）
            //     · g_sitPropActive = TRUE、g_sitPropPhase = 2（= loop 就位）
            //     · 菜单回 **g_streetSitCat**（坐/躺类目），不是 ML_STANDINGSVC
            // ══════════════════════════════════════════════════════════════
            BOOL ccSitAbort = FALSE
            IF g_mode = CCM_STREET AND g_sitPropActive
                IF g_streetActionIdx = 1 OR g_streetActionIdx = 3 OR g_streetActionIdx = 4 OR g_streetActionIdx = 6 OR g_streetActionIdx = 7 OR g_streetActionIdx = 8 OR g_streetActionIdx = 9
                    ccSitAbort = TRUE
                ENDIF
            ENDIF

            IF ccSitAbort
                INT ccSitIdx = g_streetActionIdx
                // ① 屏幕恢复可见。
                //    C# 对 faceseat/反向女上位/躺系(1/7/4/8/9) 中止时会检查
                //    (IS_SCREEN_FADED_OUT || IS_SCREEN_FADING_OUT) 再淡入。
                //    后一个 native 本工程没用过、未证明 ⇒ 这里改用
                //    「按动作号限定 + 无条件淡入」：只对**可能已经黑屏**的那 5 个动作做，
                //    且不判断当前屏幕状态（屏幕已可见时 DO_SCREEN_FADE_IN 等同空操作）。
                IF ccSitIdx = 1 OR ccSitIdx = 7 OR ccSitIdx = 4 OR ccSitIdx = 8 OR ccSitIdx = 9
                    DO_SCREEN_FADE_IN(500)
                ENDIF
                // ② 位置修回 proposition 的**绝对** Z。
                //    C#: propZ = _streetSitOriginPos.Z + (-0.855f)
                //    ⚠ 这里**不能**用 g_animOrigin：那个是"地面位置"（给退出/中止用），
                //      坐姿 proposition 要的是**沉下去 0.855** 的那个位置。
                FLOAT ccPropZ = g_sitOrigin.z + (-0.855)
                VECTOR ccRp = GET_ENTITY_COORDS(g_player)
                FLOAT ccDz = ccPropZ - ccRp.z
                IF ccDz > 0.01 OR ccDz < -0.01
                    SET_ENTITY_COORDS_NO_OFFSET(g_player, <<ccRp.x, ccRp.y, ccPropZ>>, FALSE, FALSE, FALSE)
                    IF CC_EXISTS(g_girl)
                        VECTOR ccRg = GET_ENTITY_COORDS(g_girl)
                        SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<ccRg.x, ccRg.y, ccRg.z + ccDz>>, FALSE, FALSE, FALSE)
                    ENDIF
                ENDIF
                // ③ 重播 proposition loop（男女各一段）—— 和正常流程 phase1→2 同一句
                //    先按 C# 把"会被动作 intro 挪走"的几个动作的女孩摆回 proposition 偏移
                //    （C# 11593-11600：只有 faceseat/反向女上位/躺系 1/7/4/8/9 需要）
                IF ccSitIdx = 1 OR ccSitIdx = 7 OR ccSitIdx = 4 OR ccSitIdx = 8 OR ccSitIdx = 9
                    TEXT_LABEL_63 ccM1
                    TEXT_LABEL_63 ccM2
                    TEXT_LABEL_63 ccF1
                    TEXT_LABEL_63 ccF2
                    VECTOR ccOff
                    FLOAT ccHd
                    FLOAT ccZo
                    FLOAT ccRx
                    FLOAT ccRh
                    BOOL ccSit
                    CC_PropAnimData(2, ccM1, ccM2, ccF1, ccF2, ccOff, ccHd, ccSit, ccZo, ccRx, ccRh)
                    IF CC_EXISTS(g_girl)
                        VECTOR ccGp = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, ccOff)
                        SET_ENTITY_COORDS_NO_OFFSET(g_girl, <<ccGp.x, ccGp.y, ccGp.z>>, FALSE, FALSE, FALSE)
                        SET_ENTITY_HEADING(g_girl, GET_ENTITY_HEADING(g_player) + ccHd)
                    ENDIF
                ENDIF
                CC_PropPlayPair(2, 1)
                // ④ 状态：回 Idle，但**保留** proposition（phase 2 = loop 就位）
                g_state = ST_IDLE
                g_animPhase = 0
                g_animSpeed = 1.0
                g_curAction = ""
                g_streetActionIdx = -1
                g_lastPaid = 0
                g_sitPropActive = TRUE
                g_sitPropPhase = 2
                g_menuLevel = g_streetSitCat      // ★ 坐/躺类目。改成 ML_STANDINGSVC 会触发备份退出
                g_menuIndex = 0
                g_keyCooldown = KEY_COOLDOWN_FRAMES * 3
            ELSE
                // ── 街面站立类 / 车内：原来的"完整收拾 + 站起来"路径 ──
                //
                // ★ 2026-09-25晚 补：**黑屏相位机里中止，必须把屏幕淡回来**。
                //   站立口交 / 靠墙式 / 后入 / 粗暴后入 (2/5/10/11) 的 intro 会
                //   DO_SCREEN_FADE_OUT(650) 再换动作（见 CC_StreetStandingTick）。
                //   如果中止正好落在那段黑屏里，而这个分支只清任务、不淡入，
                //   玩家就会被留在**纯黑屏幕**，看着完全像死机（用户怀疑"站立里面
                //   可能有问题"，这是其中一条）。
                //   C# 对应处（11701-11706）会在这 4 个动作 + 屏幕已黑/正在黑时
                //   DO_SCREEN_FADE_IN(500)。这里不判断屏幕状态（IS_SCREEN_FADING_OUT
                //   未证明），改成**按动作号限定 + 无条件淡入** —— 只影响这 4 个动作，
                //   屏幕已可见时等同空操作。
                IF g_streetActionIdx = 2 OR g_streetActionIdx = 5 OR g_streetActionIdx = 10 OR g_streetActionIdx = 11
                    DO_SCREEN_FADE_IN(500)
                ENDIF
                // ★★★ 2026-09-25晚 修复"取消动作后自由落体"：
                //   原顺序是 CLEAR_TASKS → FREEZE(FALSE) → SET_COLLISION(TRUE)。
                //   街面站立类虽然没沉地，但**车内有负 zOffset**、也曾在这一步炸过：
                //   先解冻 ⇒ 物理把人往下拽 ⇒ "取消之后自由落体"。
                //   正确顺序与 CC_EndAnimation 保持一致：
                //     ① 先把碰撞恢复  ② 传送回动作开始前记下的地面位置
                //     ③ 再清任务      ④ 最后才解冻
                SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
                //   只对街面动作还原坐标：车内动作**不记录** g_animOrigin（车里没有
                //   负 zOffset 的沉地），若无条件还原，一次车内中止就会拿"上一次街面
                //   动作留下的陈旧坐标"把玩家瞬移过去。
                IF g_mode = CCM_STREET AND g_animOrigin.z > -8999.0
                    SET_ENTITY_COORDS(g_player, g_animOrigin)
                    SET_ENTITY_HEADING(g_player, g_animOriginH)
                ENDIF
                CLEAR_PED_TASKS(g_player)
                FREEZE_ENTITY_POSITION(g_player, FALSE)
                IF CC_EXISTS(g_girl)
                    SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
                    CLEAR_PED_TASKS(g_girl)
                    FREEZE_ENTITY_POSITION(g_girl, FALSE)
                ELSE
                    FREEZE_ENTITY_POSITION(g_girl, FALSE)
                ENDIF
                g_state = ST_IDLE
                g_animPhase = 0
                g_animSpeed = 1.0
                g_curAction = ""
                g_streetActionIdx = -1
                g_lastPaid = 0
                IF g_mode = CCM_CAR
                    g_carPropActive = TRUE
                    g_carPropPhase = 1
                    g_menuLevel = ML_SERVICES
                ELSE
                    g_menuLevel = ML_STANDINGSVC
                ENDIF
                g_menuIndex = 0
                g_keyCooldown = KEY_COOLDOWN_FRAMES * 3
            ENDIF
        ENDIF
    ENDIF
ENDPROC
