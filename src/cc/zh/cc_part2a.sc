// ============================================================================
// cc_part2a.sc — 第2a部分: 车辆工具 / 动画数据表 / 动作引擎 / 搭讪状态机
// 对应 C# StartCarAction / RunCarAnimation / StartStreetAction /
//       GetStreetAnimData / GetStreetPropAnimData / GetStreetPropIntroExitAnimData /
//       HandleApproachingState / HandleIdleState跟随段 / TriggerHostileReaction / FullReset
// ============================================================================

// ── 车辆工具 ────────────────────────────────────────────────────────────
FUNC BOOL CC_InBackSeat(VEHICLE_INDEX car)
    IF NOT DOES_ENTITY_EXIST(car)
        RETURN FALSE
    ENDIF
    PED_INDEX seatPed1 = GET_PED_IN_VEHICLE_SEAT(car, VS_BACK_LEFT)
    IF seatPed1 = g_player
        RETURN TRUE
    ENDIF
    PED_INDEX seatPed2 = GET_PED_IN_VEHICLE_SEAT(car, VS_BACK_RIGHT)
    IF seatPed2 = g_player
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

FUNC BOOL CC_SuitableVeh(VEHICLE_INDEX car)
    IF NOT DOES_ENTITY_EXIST(car)
        RETURN FALSE
    ENDIF
    INT cls = ENUM_TO_INT(GET_VEHICLE_CLASS(car))
    IF cls = 8 OR cls = 13 OR cls = 14 OR cls = 15 OR cls = 16
        RETURN FALSE
    ENDIF
    RETURN TRUE
ENDFUNC

// C# IsSinkraLowerModel
FUNC BOOL CC_IsSinkraLowerModel()
    IF g_sinkraLowN = 0
        RETURN FALSE
    ENDIF
    IF NOT CC_EXISTS(g_girl)
        RETURN FALSE
    ENDIF
    INT mh
    mh = CC_ModelHash(g_girl)
    INT i = 0
    WHILE i < g_sinkraLowN
        IF g_sinkraLowModels[i] = mh
            RETURN TRUE
        ENDIF
        i = i + 1
    ENDWHILE
    RETURN FALSE
ENDFUNC

// cfg sinkraLowerModels 名单
FUNC STRING CC_SinkraModelName(INT i)
    SWITCH i
        CASE 0 RETURN "csb_abigail"          CASE 1 RETURN "g_f_y_ballas_01"
        CASE 2 RETURN "g_f_y_families_01"    CASE 3 RETURN "a_f_y_importexport_01"
        CASE 4 RETURN "g_f_y_vagos_01"       CASE 5 RETURN "ig_amandatownley"
        CASE 6 RETURN "ig_denise"            CASE 7 RETURN "ig_djblakmad"
        CASE 8 RETURN "ig_janet"             CASE 9 RETURN "ig_karen_daniels"
        CASE 10 RETURN "csb_magenta"         CASE 11 RETURN "ig_marnie"
        CASE 12 RETURN "ig_mary_ann"         CASE 13 RETURN "ig_maude"
        CASE 14 RETURN "ig_michelle"         CASE 15 RETURN "ig_molly"
        CASE 16 RETURN "ig_mrs_thornhill"    CASE 17 RETURN "ig_patricia"
        CASE 18 RETURN "ig_patricia_02"      CASE 19 RETURN "csb_patricia"
        CASE 20 RETURN "csb_patricia_02"     CASE 21 RETURN "csb_screenwriter"
        CASE 22 RETURN "ig_sessanta"         CASE 23 RETURN "ig_traceydesanta"
        CASE 24 RETURN "csb_wendy"           CASE 25 RETURN "csb_djblamadon"
        CASE 26 RETURN "ig_djblamadon"
    ENDSWITCH
    RETURN "ig_denise"
ENDFUNC

// ── animName dict 选择 (车) ─────────────────────────────────────────────────
FUNC STRING CC_CarDict(BOOL firstPerson)
    IF g_vehicleAnimType = VAT_LOW
        IF firstPerson
            RETURN "mini@prostitutes@sexlow_veh_first_person"
        ENDIF
        RETURN "mini@prostitutes@sexlow_veh"
    ENDIF
    IF firstPerson
        RETURN "mini@prostitutes@sexnorm_veh_first_person"
    ENDIF
    RETURN "mini@prostitutes@sexnorm_veh"
ENDFUNC

FUNC BOOL CC_IsFirstPerson()
    IF GET_FOLLOW_PED_CAM_VIEW_MODE() = CAM_VIEW_MODE_FIRST_PERSON
        RETURN TRUE
    ENDIF
    RETURN FALSE
ENDFUNC

// ── 街头 12 动作数据 (C# GetStreetAnimData 1:1) ─────────────────────────
// 输出: mDict/mAnim/fDict/fAnim/offV/hdg/isSitting/zOffset/rotX
PROC CC_StreetAnimData(INT idx, TEXT_LABEL_63 &mDict, TEXT_LABEL_63 &mAnim, TEXT_LABEL_63 &fDict, TEXT_LABEL_63 &fAnim, VECTOR &offV, FLOAT &hdg, BOOL &isSitting, FLOAT &zOffset, FLOAT &rotX)
    SWITCH idx
        CASE 0 // Kissing
            mDict = "mp_ped_interaction"
            mAnim = "kisses_guy_a"
            fDict = "mp_ped_interaction"
            fAnim = "kisses_guy_a"
            offV = <<0.0, 1.29, 0.07>>
            hdg = 180.0
            isSitting = FALSE
            zOffset = 0.0
            rotX = 0.0
            BREAK
        CASE 1 // Facesitting
            IF g_sinkraAvail AND g_sinkraFace
                mDict = "saveveniceb@"
                mAnim = "t_sleep_loop_veniceb"
                fDict = "cowgirl@sinkra"
                fAnim = "cowgirl_clip"
                IF CC_IsSinkraLowerModel()
                    offV = g_sinkraOffFace
                ELSE
                    offV = <<-0.05, -0.50, 0.48>>
                ENDIF
                hdg = 169.0
                isSitting = TRUE
                zOffset = -0.59
                rotX = 0.0
            ELSE
                mDict = "saveveniceb@"
                mAnim = "t_sleep_loop_veniceb"
                fDict = "mini@prostitutes@sexnorm_veh"
                fAnim = "sex_loop_prostitute"
                offV = <<0.73, -0.77, -0.3>>
                hdg = 0.0
                isSitting = TRUE
                zOffset = -0.59
                rotX = 0.0
            ENDIF
            BREAK
        CASE 2 // Standing Blowjob
            mDict = "misscarsteal2pimpsex"
            mAnim = "pimpsex_punter"
            fDict = "misscarsteal2pimpsex"
            fAnim = "pimpsex_hooker"
            offV = <<0.0, 0.6, 0.0>>
            hdg = 180.0
            isSitting = FALSE
            zOffset = 0.0
            rotX = 0.0
            BREAK
        CASE 3 // Sitting Blowjob
            mDict = "mini@prostitutes@sexnorm_veh"
            mAnim = "bj_loop_male"
            fDict = "mini@prostitutes@sexnorm_veh"
            fAnim = "bj_loop_prostitute"
            offV = <<0.78, 0.0, -0.02>>
            hdg = 0.0
            isSitting = TRUE
            zOffset = -0.87
            rotX = 5.0
            BREAK
        CASE 4 // Laying Blowjob
            mDict = "saveveniceb@"
            mAnim = "t_sleep_loop_veniceb"
            fDict = "mini@prostitutes@sexnorm_veh"
            fAnim = "bj_loop_prostitute"
            offV = <<0.17, 0.8, -0.37>>
            hdg = 90.0
            isSitting = TRUE
            zOffset = -0.59
            rotX = 0.0
            BREAK
        CASE 5 // Wall Banger
            mDict = "misscarsteal2pimpsex"
            mAnim = "shagloop_pimp"
            fDict = "misscarsteal2pimpsex"
            fAnim = "shagloop_hooker"
            IF CC_IsSinkraLowerModel()
                offV = g_wallBangOff
            ELSE
                offV = <<0.02, 0.36, 0.048>>
            ENDIF
            hdg = 190.0
            isSitting = TRUE
            zOffset = -0.15
            rotX = 0.0
            BREAK
        CASE 6 // Sitting Cowgirl
            mDict = "mini@prostitutes@sexnorm_veh"
            mAnim = "sex_loop_male"
            fDict = "mini@prostitutes@sexnorm_veh"
            fAnim = "sex_loop_prostitute"
            offV = <<0.85, -0.01, -0.02>>
            hdg = 0.0
            isSitting = TRUE
            zOffset = 0.0
            rotX = 5.0
            BREAK
        CASE 7 // Reverse Cowgirl
            mDict = "mini@prostitutes@sexnorm_veh"
            mAnim = "sex_loop_male"
            fDict = "mini@prostitutes@sexnorm_veh"
            fAnim = "sex_loop_prostitute"
            offV = <<-0.83, 0.4, 0.02>>
            hdg = 180.0
            isSitting = TRUE
            zOffset = -0.87
            rotX = 5.0
            BREAK
        CASE 8 // Laying Cowgirl
            IF g_sinkraAvail AND g_sinkraLay
                mDict = "saveveniceb@"
                mAnim = "t_sleep_loop_veniceb"
                fDict = "cowgirl@sinkra"
                fAnim = "cowgirl_clip"
                IF CC_IsSinkraLowerModel()
                    offV = g_sinkraOffCow
                ELSE
                    offV = <<0.02, 0.08, 0.44>>
                ENDIF
                hdg = 169.0
                isSitting = TRUE
                zOffset = -0.59
                rotX = 0.0
            ELSE
                mDict = "random@drunk_driver_2"
                mAnim = "cardrunksex_loop_m"
                fDict = "random@drunk_driver_2"
                fAnim = "cardrunksex_loop_f"
                offV = <<-0.65, 0.0, -0.02>>
                hdg = 0.0
                isSitting = TRUE
                zOffset = -0.95
                rotX = 0.0
            ENDIF
            BREAK
        CASE 9 // Laying Reversed Cowgirl
            IF g_sinkraAvail AND g_sinkraRevLay
                mDict = "saveveniceb@"
                mAnim = "t_sleep_loop_veniceb"
                fDict = "cowgirl@sinkra"
                fAnim = "cowgirl_clip"
                IF CC_IsSinkraLowerModel()
                    offV = g_sinkraOffRevCow
                ELSE
                    offV = <<0.02, -0.075, 0.44>>
                ENDIF
                hdg = 0.0
                isSitting = TRUE
                zOffset = -0.59
                rotX = 0.0
            ELSE
                mDict = "random@drunk_driver_2"
                mAnim = "cardrunksex_loop_m"
                fDict = "random@drunk_driver_2"
                fAnim = "cardrunksex_loop_f"
                offV = <<-0.35, 0.15, -0.02>>
                hdg = 180.0
                isSitting = TRUE
                zOffset = -0.95
                rotX = 0.0
            ENDIF
            BREAK
        CASE 10 // Doggy Style
            mDict = "rcmpaparazzo_2"
            mAnim = "shag_loop_a"
            fDict = "rcmpaparazzo_2"
            fAnim = "shag_loop_poppy"
            offV = <<-0.03, 0.3, -0.1>>
            hdg = 0.0
            isSitting = FALSE
            zOffset = 0.0
            rotX = 0.0
            BREAK
        CASE 11 // Rough Doggy Style
            mDict = "trevor_1_int-13"
            mAnim = "player_two_dual-13"
            fDict = "trevor_1_int-13"
            fAnim = "cs_ashley_dual-13"
            offV = <<-0.11, 0.33, -0.11>>
            hdg = 13.0
            isSitting = FALSE
            zOffset = 0.0
            rotX = 0.0
            BREAK
        DEFAULT
            mDict = "mp_ped_interaction"
            mAnim = "kisses_guy_a"
            fDict = "mp_ped_interaction"
            fAnim = "kisses_guy_a"
            offV = <<0.0, 1.29, 0.07>>
            hdg = 180.0
            isSitting = FALSE
            zOffset = 0.0
            rotX = 0.0
            BREAK
    ENDSWITCH
ENDPROC

// ── 坐/躺 proposition 动画数据 (C# GetStreetPropAnimData 1:1) ───────────
// idx: 0=sitFlag-down 1=prop进入 2=prop循环 3=prop退出 4=站起
PROC CC_PropAnimData(INT idx, TEXT_LABEL_63 &mDict, TEXT_LABEL_63 &mAnim, TEXT_LABEL_63 &fDict, TEXT_LABEL_63 &fAnim, VECTOR &offV, FLOAT &hdg, BOOL &isSitting, FLOAT &zOffset, FLOAT &rotX, FLOAT &rotH)
    rotH = 0.0
    IF idx = 0
        mDict = "amb@world_human_picnic@male@enter"
        mAnim = "enter"
        fDict = "amb@world_human_picnic@female@enter"
        fAnim = "enter"
        offV = <<-0.7, 0.4, 0.0>>
        hdg = 0.0
        isSitting = FALSE
        zOffset = 0.0
        rotX = 0.0
        EXIT
    ENDIF
    IF idx = 4
        mDict = "amb@world_human_picnic@male@exit"
        mAnim = "exit"
        fDict = "amb@world_human_picnic@female@exit"
        fAnim = "exit"
        offV = <<-0.7, 0.4, 0.0>>
        hdg = 185.0
        isSitting = FALSE
        zOffset = 0.0
        rotX = 0.0
        rotH = 180.0
        EXIT
    ENDIF
    // idx 1/2/3 — 全部 sexnorm proposition 系列
    mDict = "mini@prostitutes@sexnorm_veh"
    fDict = "mini@prostitutes@sexnorm_veh"
    offV = <<0.85, -0.01, -0.02>>
    hdg = 0.0
    isSitting = TRUE
    zOffset = -0.855
    rotX = 5.0
    IF idx = 1
        mAnim = "into_proposition_male"
        fAnim = "into_proposition_prostitute"
    ELIF idx = 2
        mAnim = "proposition_loop_male"
        fAnim = "proposition_loop_prostitute"
    ELSE
        mAnim = "prop_to_sit_male"
        fAnim = "prop_to_sit_prostitute"
    ENDIF
ENDPROC

// ── 坐/躺动作↔prop 转场数据 (C# GetStreetPropIntroExitAnimData 1:1) ─────
// idx: 0=prop→动作P1 1=prop→动作P2 2=动作→prop P1 3=动作→prop P2
PROC CC_PropIntroExitData(INT idx, INT actionIdx, TEXT_LABEL_63 &mDict, TEXT_LABEL_63 &mAnim, TEXT_LABEL_63 &fDict, TEXT_LABEL_63 &fAnim, VECTOR &offV, FLOAT &hdg, BOOL &isSitting, FLOAT &zOffset, FLOAT &rotX, FLOAT &rotH)
    rotH = 0.0
    BOOL bjFlag = FALSE
    IF actionIdx >= 2 AND actionIdx <= 4
        bjFlag = TRUE
    ENDIF
    BOOL isFacesit = FALSE
    IF actionIdx = 1
        isFacesit = TRUE
    ENDIF
    IF actionIdx = 8 AND g_sinkraAvail AND g_sinkraLay
        isFacesit = TRUE
    ENDIF
    BOOL isLayBj = FALSE
    IF actionIdx = 4
        isLayBj = TRUE
    ENDIF
    BOOL isLayCow = FALSE
    IF actionIdx = 8
        isLayCow = TRUE
    ENDIF
    BOOL isLayRevCow = FALSE
    IF actionIdx = 9
        isLayRevCow = TRUE
    ENDIF
    BOOL sinkLayCow = FALSE
    IF actionIdx = 8 AND g_sinkraAvail AND g_sinkraLay
        sinkLayCow = TRUE
    ENDIF
    BOOL sinkLayRev = FALSE
    IF actionIdx = 9 AND g_sinkraAvail AND g_sinkraRevLay
        sinkLayRev = TRUE
    ENDIF

    // vanilla Laying Cowgirl — random@drunk_driver_2 全链
    IF isLayCow AND NOT sinkLayCow
        mDict = "random@drunk_driver_2"
        fDict = "random@drunk_driver_2"
        IF idx = 0
            mAnim = "cardrunkflirt_intro_m"
            fAnim = "cardrunkflirt_intro_f"
        ELIF idx = 1
            mAnim = "cardrunksex_intro_m"
            fAnim = "cardrunksex_intro_f"
        ELSE
            mAnim = "cardrunksex_outro_m"
            fAnim = "cardrunksex_outro_f"
        ENDIF
        offV = <<-0.65, 0.0, -0.02>>
        hdg = 0.0
        isSitting = TRUE
        zOffset = -0.95
        rotX = 0.0
        EXIT
    ENDIF
    // vanilla Laying Rev Cowgirl
    IF isLayRevCow AND NOT sinkLayRev
        mDict = "random@drunk_driver_2"
        fDict = "random@drunk_driver_2"
        mAnim = "cardrunksex_intro_m"
        fAnim = "cardrunksex_intro_f"
        IF idx >= 2
            mAnim = "cardrunksex_outro_m"
            fAnim = "cardrunksex_outro_f"
        ENDIF
        offV = <<-0.35, 0.15, -0.02>>
        hdg = 180.0
        isSitting = TRUE
        zOffset = -0.95
        rotX = 0.0
        EXIT
    ENDIF

    // 主表 (Sitting/Laying 同参数 — C# 分支相同)
    offV = <<0.85, -0.01, -0.02>>
    hdg = 0.0
    isSitting = TRUE
    IF isFacesit OR isLayBj
        IF idx = 0
            mDict = "saveveniceb@"
            mAnim = "t_getin_veniceb"
        ELIF idx = 1
            mDict = "saveveniceb@"
            mAnim = "t_sleep_loop_veniceb"
        ELSE
            mDict = "saveveniceb@"
            mAnim = "t_getout_veniceb"
        ENDIF
        fDict = "mini@prostitutes@sexnorm_veh"
        IF bjFlag
            fAnim = "bj_loop_prostitute"
        ELSE
            fAnim = "sex_loop_prostitute"
        ENDIF
        rotX = 0.0
    ELSE
        mDict = "mini@prostitutes@sexnorm_veh"
        fDict = "mini@prostitutes@sexnorm_veh"
        IF idx = 0
            IF bjFlag
                mAnim = "proposition_to_bj_p1_male"
                fAnim = "proposition_to_bj_p1_prostitute"
            ELSE
                mAnim = "proposition_to_sex_p1_male"
                fAnim = "proposition_to_sex_p1_prostitute"
            ENDIF
        ELIF idx = 1
            IF bjFlag
                mAnim = "proposition_to_bj_p2_male"
                fAnim = "proposition_to_bj_p2_prostitute"
            ELSE
                mAnim = "proposition_to_sex_p2_male"
                fAnim = "proposition_to_sex_p2_prostitute"
            ENDIF
        ELIF idx = 2
            IF bjFlag
                mAnim = "bj_to_proposition_p1_male"
                fAnim = "bj_to_proposition_p1_prostitute"
            ELSE
                mAnim = "sex_to_proposition_p1_male"
                fAnim = "sex_to_proposition_p1_prostitute"
            ENDIF
        ELSE
            IF bjFlag
                mAnim = "bj_to_proposition_p2_male"
                fAnim = "bj_to_proposition_p2_prostitute"
            ELSE
                mAnim = "sex_to_proposition_p2_male"
                fAnim = "sex_to_proposition_p2_prostitute"
            ENDIF
        ENDIF
        rotX = 5.0
    ENDIF
    // Sinkra Laying Rev Cowgirl: 男方用 veniceb 系列
    IF sinkLayRev
        IF idx = 0
            mDict = "saveveniceb@"
            mAnim = "t_getin_veniceb"
            rotX = 0.0
        ELIF idx = 1
            mDict = "saveveniceb@"
            mAnim = "t_sleep_loop_veniceb"
            rotX = 0.0
        ELIF idx = 2
            mDict = "saveveniceb@"
            mAnim = "t_getout_veniceb"
            rotX = 0.0
        ENDIF
    ENDIF
    // zOffset 覆盖 (C# 尾段)
    IF idx <= 2
        IF isFacesit OR isLayBj OR sinkLayRev
            zOffset = -0.59
        ELSE
            zOffset = -0.855
        ENDIF
    ELSE
        zOffset = -0.855
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  清理 (C# FullReset / EndAnimation / ReleaseGirl)
// ════════════════════════════════════════════════════════════════════════

PROC CC_ReleaseGirl(BOOL keepTask)
    // ══════════════════════════════════════════════════════════════════════
    // ★★★ v0.62 忠实照抄 C# `ReleaseGirl()`（17153）—— 原来漏了两步，
    //   直接造成用户报的「沙盒模式让她离开，她走一小会儿又自己回来」：
    //
    //   C# 原文（关键三行）：
    //       girl.Task.ClearAll();
    //       // Give her a wander task so AI doesn't re-queue the old vehicle enter
    //       girl.Task.Wander();
    //       girl.IsPersistent = false;
    //       girl.MarkAsNoLongerNeeded();
    //
    //   ① 只 `CLEAR_PED_TASKS` 不留新任务 ⇒ 她身上"上一次的上车/跟随任务"
    //      会被 AI **重新排进队列**（C# 注释写得明明白白）⇒ 她走两步又回来。
    //      补一个 wander（游荡）任务覆盖它，她才真的走开。
    //   ② 认领时我们调了 `SET_ENTITY_AS_MISSION_ENTITY(g_girl, TRUE, TRUE)`
    //      （见 CC_ClaimGirl），释放时**必须清掉**：任务实体不会被
    //      `SET_PED_AS_NO_LONGER_NEEDED` 带走，会一直挂在世界里。
    //      C# 对应 `girl.IsPersistent = false`。
    //
    //   ⚠ 两个分支（keepTask=TRUE 是她正在逃跑/敌对，要保留那个任务）**都要清标记**；
    //     wander 只加在 keepTask=FALSE 的分支（keepTask=TRUE 时保留她原有的任务）。
    // ══════════════════════════════════════════════════════════════════════
    IF CC_EXISTS(g_girl)
        IF keepTask
            IF CC_ALIVE(g_girl)
                CLEAR_FACIAL_IDLE_ANIM_OVERRIDE(g_girl)
            ENDIF
            SET_PED_KEEP_TASK(g_girl, TRUE)
            FREEZE_ENTITY_POSITION(g_girl, FALSE)
            SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
            SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, FALSE)
            SET_ENTITY_AS_MISSION_ENTITY(g_girl, FALSE, FALSE)
            SET_PED_AS_NO_LONGER_NEEDED(g_girl)
        ELSE
            SET_PED_KEEP_TASK(g_girl, FALSE)
            CLEAR_PED_TASKS(g_girl)
            // ★ 补：给她一个"游荡"任务，盖掉 AI 重排的旧上车/跟随任务。
            //   （TASK_WANDER_STANDARD 在本产物里已有多处使用 ⇒ 不是未验证 native。）
            TASK_WANDER_STANDARD(g_girl, 0.0, INT_TO_ENUM(EWDR_SCRIPT_FLAGS, 0))
            SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, FALSE)
            FREEZE_ENTITY_POSITION(g_girl, FALSE)
            SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
            CLEAR_FACIAL_IDLE_ANIM_OVERRIDE(g_girl)
            SET_ENTITY_AS_MISSION_ENTITY(g_girl, FALSE, FALSE)
            SET_PED_AS_NO_LONGER_NEEDED(g_girl)
        ENDIF
    ENDIF
    g_girl = NULL
    g_hasGirl = FALSE
    g_curAlIdx = -1
    g_girlVoice = ""
    // ★ v0.64：新女伴的音频探针重新开始（见 cc_part1.sc g_voiceProbeShown）
    g_voiceProbeShown = FALSE
    g_audioProbeWant = FALSE
    // ★ v0.66：嗓子轮换状态一并重置 —— 换人 = 重新定嗓、重新允许自愈。
    g_voiceIdx = -1
    g_voiceRotate = 0
    g_voiceCheckAt = 0
    g_voiceLocked = FALSE
ENDPROC

PROC CC_FullReset(BOOL keepTask)
    // ══════════════════════════════════════════════════════════════════════
    // ★★★ 2026-09-26 v0.59 修：CC_FullReset 必须先**收掉玩家身上的动作**。
    //
    //   【用户实测（v0.57 / v0.58）】在**动作进行中**去设置菜单关掉「热咖啡模组」：
    //       女伴会立刻离开（对，那是 CC_ReleaseGirl 干的），
    //       但**玩家本人卡在半空中、保持坐/躺那套"车内座"动画姿势**。
    //
    //   【根因】CC_FullReset 以前**只做"放生女伴 + 复位状态量"**，对玩家自己只做了
    //       FREEZE_ENTITY_POSITION(FALSE) + SET_ENTITY_COLLISION(TRUE, TRUE)，
    //       **既不清 TASK_PLAY_ANIM、也不把人拉回动作前的落地点**。
    //     这在它原有的调用场合里是对的（那些场合本来就没有玩家动作在跑），
    //     但 v0.57 新加的"关总开关"路径**可以在动作进行中触发** ⇒ 人就留在原地：
    //       · TASK_PLAY_ANIM 没被清 ⇒ 姿势还停在坐/躺（看着就是"保持开车动作"）
    //       · 位置还是动作摆出来的那个悬浮高度 ⇒ "卡在半空中"
    //     这不是新引入的逻辑错误，是**新调用路径 + 旧函数的既有假设**撞上了。
    //
    //   【修法】补一段玩家侧收尾，顺序与 CC_EndAnimation **完全一致**：
    //       恢复碰撞 → 拉回地面 → 清任务 → 最后解冻
    //     （顺序不能换：先解冻的话，人还沉在地面以下、碰撞又是关的，物理会直接往下拽。
    //       详见 CC_EndAnimation 里那段注释。）
    //
    //   ⚠ 只在"确实有动作/道具在跑"时才做 —— 保证平时那些调用点（单纯放生女伴）
    //     行为一字不变。
    // ══════════════════════════════════════════════════════════════════════
    // ══════════════════════════════════════════════════════════════════════
    // ★★★ v0.62 二次修正：这段"玩家侧硬收尾"**只对街面/徒步动作成立**。
    //
    //   【用户实测】搭讪模式，两人在车里（车震/运输途中）说"再见 / 让她离开"：
    //       玩家**当场被弹出车外、像被传送一样站在车边**，女伴也被弹出车，
    //       然后她受惊尖叫跑开。
    //
    //   【根因】硬收尾里的 `CLEAR_PED_TASKS_IMMEDIATELY(g_player)`：玩家本来
    //     坐在车里、靠 `TASK_PLAY_ANIM` 维持车内姿势。把任务清掉的瞬间，
    //     引擎认为"这个 ped 不该待在这辆车里"⇒ 直接把他**弹出车外**到最近的
    //     合法落脚点（用户看到的"传送一样站在车边"）；女伴随后也被弹出，
    //     变成普通 NPC 后受惊跑开。
    //
    //   【C# 怎么做的】原版 `FullReset` 对玩家**只做三件事**
    //     （CinnamonCoffee.cs 17916 附近）：
    //         player.IsPositionFrozen = false;
    //         SET_ENTITY_COLLISION(player, true, true);
    //         if (player in vehicle) vehicle.IsInteriorLightOn = false;
    //     ——**从不清玩家任务、从不挪玩家位置**。
    //     我们 v0.59 加的硬收尾是端口自己发明的（为了修"关总开关后卡半空"），
    //     那个 bug 只发生在**街面动作**（玩家在地上、位置被负 zOffset 沉到地下）。
    //     车内动作根本不需要它（车内没有负 zOffset 的沉地，见 CC_StartCarAction）。
    //
    //   【修法】把硬收尾收窄到"街面动作 + 玩家确实不在车里"：
    //       · 街面 → 保持 v0.59 行为（恢复碰撞 → 拉回地面 → 清任务 → 解冻）
    //       · 车内/徒步 proposition → 只走温和收尾（清**次级**任务即可，
    //         车内 sex 动画挂在 secondary task 上；CC_EndAnimation 的车内分支同款）。
    //     这样旧调用点（关总开关时卡半空）行为一字不变，车内弹出也消失。
    // ══════════════════════════════════════════════════════════════════════
    IF g_state = ST_ANIMATING OR g_sitPropActive OR g_carPropActive
        SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
        IF g_mode = CCM_STREET AND NOT IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            // 落地点：优先用动作记录点 g_animOrigin；
            //   它还是哨兵（= 动作还没过"记录落地点"那一步，例如坐/躺 proposition 期间）时，
            //   退回坐/躺道具的"坐下前地面位置" g_sitOrigin —— 两者都是"人原本站的地方"。
            VECTOR backTo = g_animOrigin
            FLOAT backH = g_animOriginH
            IF backTo.z <= -8999.0
                IF g_sitPropActive
                    backTo = <<g_sitOrigin.x, g_sitOrigin.y, g_sitOrigin.z>>
                    backH = g_sitOriginH
                ENDIF
            ENDIF
            IF backTo.z > -8999.0
                SET_ENTITY_COORDS(g_player, backTo)
                SET_ENTITY_HEADING(g_player, backH)
            ENDIF
            // ★ 用 IMMEDIATELY 版：这是"中止"语义，要当场脱离姿势；
            //   普通 CLEAR_PED_TASKS 会先让当前任务收尾/混合，人还会在姿势里停一下。
            //   （这个 native 在同一份产物里已被僵尸模组用了 4 处 ⇒ 不是未验证 native。）
            CLEAR_PED_TASKS_IMMEDIATELY(g_player)
        ELSE
            // 车内 / 徒步：温和收尾，**绝不把玩家弹出车**（与 C# FullReset 一致）。
            //   车内 sex 动画挂在 secondary task 上 ⇒ 清次级任务即可脱离姿势。
            //   绝不用 CLEAR_PED_TASKS_IMMEDIATELY：那就是"弹出车外"的元凶。
            CLEAR_PED_SECONDARY_TASK(g_player)
        ENDIF
        FREEZE_ENTITY_POSITION(g_player, FALSE)
    ENDIF
    CC_DetachDildo()
    CC_RemoveGirlBlip()
    CC_ReleaseGirl(keepTask)
    // 硬复位：走统一写点（= 强制 FALSE 并同步影子值）。此时女伴已被释放，
    // g_hasGirl 已在 CC_ReleaseGirl 里置 FALSE，所以 reconcile 不会把它再拉回 TRUE。
    CC_PoliceIgnoreSet(FALSE)
    SET_PLAYER_CAN_DO_DRIVE_BY(PLAYER_ID(), TRUE)
    STOP_CONTROL_SHAKE(PLAYER_CONTROL)
    FREEZE_ENTITY_POSITION(g_player, FALSE)
    SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
    IF DOES_ENTITY_EXIST(g_car)
        SET_VEHICLE_INTERIORLIGHT(g_car, FALSE)
    ENDIF
    g_menuLevel = ML_NONE
    g_menuIndex = 0
    g_state = ST_IDLE
    g_mode = CCM_NONE
    // ★ 2026-09-25晚：诊断/还原状态一并复位。
    //   g_diagPhase = -1 是为了让下一个动作的**首帧**诊断一定会打印
    //   （否则上一次碰巧也是 phase 0 + 同 mode 时，变化驱动的诊断会把它吞掉）。
    //   g_animOrigin 复置成 z = -9000 的哨兵值 = "没有有效落地点"。
    g_diagPhase = -1
    g_diagMode = -1
    g_animOrigin = <<0.0, 0.0, -9000.0>>
    g_animOriginH = 0.0
    g_animSpeed = 1.0
    g_femSpeedScale = 1.0
    g_kissingRev = FALSE
    g_rdoggyRev = FALSE
    g_standBJRev = FALSE
    g_sitCowPullRev = FALSE
    g_carPullRev = FALSE
    g_standBJSwallowAt = 0
    g_backseatEntryPending = FALSE
    g_backseatCarSex = FALSE
    // ★ v0.78：放生/收尾时一并清掉"她已同意上车"（C# 17980 同一处）
    g_vehInvited = FALSE
    g_appPhase = 0
    g_appAnimWait = 0
    g_appSexySwap = 0
    g_jHoldStart = 0
    g_carPropActive = FALSE
    g_carPropPhase = 0
    g_carPropWait = 0
    g_sitPropActive = FALSE
    g_sitPropPhase = 0
    g_sitPropWait = 0
    g_streetSitCat = ML_NONE
    g_curAction = ""
    g_lastPaid = 0
    g_throatLocked = FALSE
    g_throatLockAt = 0
    g_throatEndAt = 0
    g_bjThroatPending = FALSE
    g_finExecPending = FALSE
    g_finInside = FALSE
    g_finForced = FALSE
    g_finBroke = FALSE
    g_finAsked = FALSE
    g_postSexPhase = 0
    g_postSexDeadline = 0
    g_postSexMenu = ML_NONE
    g_girlLastVit = -1.0
    g_aLifeHookerSvc = FALSE
    g_aLifeFinPrice = 0
    g_prostAutoApproachActive = FALSE
    g_approachCand = NULL
    g_streetActionIdx = -1
    g_rumblePulse = FALSE
    g_finBurstFired = FALSE
    // ★ v0.63 新增状态一并复位（否则上一个动作的"返回菜单"会漏到下一个动作）
    g_actionReturnMenu = ML_NONE
    g_navLockUntil = 0
    g_speedImmuneUntil = 0
ENDPROC

// C# EndAnimation — 服务完成收尾
PROC CC_EndAnimation()
    // 震动收尾 (C# XInput burst 等效)
    INT burst = 600
    IF g_mode = CCM_STREET
        burst = 2000
    ENDIF
    IF g_finBurstFired AND GET_GAME_TIMER() - g_lastRumble <= burst
        WAIT(burst - (GET_GAME_TIMER() - g_lastRumble))
    ENDIF
    STOP_CONTROL_SHAKE(PLAYER_CONTROL)
    g_rumblePulse = FALSE
    g_finBurstFired = FALSE
    IF g_mode = CCM_STREET
        // ★★★ 2026-09-25 修复执行顺序：**先恢复碰撞 + 把人拉回地面，最后才解冻**。
        //   原来是「先 FREEZE FALSE → 再 SET_ENTITY_COLLISION(TRUE)」：
        //   解冻那一瞬 ped 还沉在地面以下（zOffset 是负的）、碰撞又是关的 ⇒
        //   物理立刻把它往下拽；掉到一定深度后引擎才把它传送回来 ——
        //   这就是用户看到的"**退出动作的时候往下掉**"（甚至反复掉/被传回）。
        //   正确顺序：恢复碰撞 → 传送回原地面位置 → 清任务 → 最后解冻。
        SET_ENTITY_COLLISION(g_player, TRUE, TRUE)
        IF g_animOrigin.z > -8999.0
            SET_ENTITY_COORDS(g_player, g_animOrigin)
            SET_ENTITY_HEADING(g_player, g_animOriginH)
        ENDIF
        // ★ 用完即弃：把落地点复位成哨兵值（z = -9000）。
        //   否则会留下一个"陈旧的落地点"，下次在别的上下文里被当成有效坐标，
        //   把人一下子瞬移到上一次动作的位置。
        g_animOrigin = <<0.0, 0.0, -9000.0>>
        g_animOriginH = 0.0
        CLEAR_PED_TASKS(g_player)
        FREEZE_ENTITY_POSITION(g_player, FALSE)
        IF CC_EXISTS(g_girl)
            SET_ENTITY_COLLISION(g_girl, TRUE, TRUE)
            FREEZE_ENTITY_POSITION(g_girl, FALSE)
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
    ELSE
        CLEAR_PED_SECONDARY_TASK(g_player)
        IF DOES_ENTITY_EXIST(g_car)
            SET_VEHICLE_INTERIORLIGHT(g_car, FALSE)
        ENDIF
        IF CC_EXISTS(g_girl)
            SET_PED_KEEP_TASK(g_girl, FALSE)
            CLEAR_PED_SECONDARY_TASK(g_girl)
            SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
        ENDIF
    ENDIF
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
    CC_DetachDildo()
    SET_PLAYER_CAN_DO_DRIVE_BY(PLAYER_ID(), TRUE)
    // 街面动作收尾：C# 原版在这里裸调 FALSE（10983/15355）。本工程改为**状态推导**——
    // 此刻 g_state 仍是 ST_ANIMATING、g_hasGirl 仍为 TRUE，所以：
    //   开关=开启 → 保持无视（闲逛跟随期也继续无视，= 用户选的「默认开启」语义）；
    //   开关=关闭 → 当场落到 FALSE（警察可打你）。
    // 绝不裸调 FALSE：那会让 g_policeApplied 残留 1，导致开关开着也不再重设。
    CC_ApplyPoliceIgnore()
    BOOL wasExcluded = g_devExcluded
    BOOL wasForced = g_finForced
    g_state = ST_IDLE
    g_animPhase = 0
    g_animSpeed = 1.0
    g_femSpeedScale = 1.0
    g_speedCooldown = 0
    g_devExcluded = FALSE
    g_kissingRev = FALSE
    g_standBJRev = FALSE
    g_rdoggyRev = FALSE
    g_sitCowPullRev = FALSE
    g_standBJSwallowAt = 0
    g_backseatCarSex = FALSE
    g_sitPropPhase = 0
    g_curAction = ""
    g_lastPaid = 0
    g_throatLocked = FALSE
    g_throatLockAt = 0
    g_throatEndAt = 0
    g_finExecPending = FALSE
    g_finInside = FALSE
    g_finForced = FALSE
    g_finBroke = FALSE
    g_finAsked = FALSE
    // 声望 (C# +1 rep, forced 不给)
    IF NOT wasForced AND g_aLifeMode AND g_curAlIdx >= 0
        IF g_alRep[g_curAlIdx] >= 0
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] + 1
            IF g_alRep[g_curAlIdx] > CC_REP_MAX
                g_alRep[g_curAlIdx] = CC_REP_MAX
            ENDIF
            AL_UpdateRel(g_curAlIdx)
        ENDIF
    ENDIF
    // 敌对 → 立即释放 (C# 尾段)
    IF g_aLifeMode AND CC_EXISTS(g_girl) AND g_curAlIdx >= 0
        IF g_alRep[g_curAlIdx] <= CC_REP_MIN
            g_aLifeHookerSvc = FALSE
            SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, FALSE)
            TASK_WANDER_STANDARD(g_girl, 0.0, INT_TO_ENUM(EWDR_SCRIPT_FLAGS, 0))
            CLEAR_FACIAL_IDLE_ANIM_OVERRIDE(g_girl)
            FREEZE_ENTITY_POSITION(g_girl, FALSE)
            SET_PED_AS_NO_LONGER_NEEDED(g_girl)
            g_girl = NULL
            g_hasGirl = FALSE
            g_curAlIdx = -1
            g_mode = CCM_NONE
            g_menuLevel = ML_NONE
        ENDIF
    ENDIF
    // ══════════════════════════════════════════════════════════════════════
    // ★★★ v0.63：动作结束 → **把菜单还给玩家**（用户要求："别一动作就把菜单关掉"）
    //
    //   C# 原版在服务收尾时是**主动重开服务菜单**的（CinnamonCoffee.cs 15309）：
    //       menuIndex = 0;
    //       if (_postSexPhase == 1) { _postSexPendingMenu = MenuLevel.Services; return; }
    //       menuLevel = MenuLevel.Services;
    //   —— `_postSexPendingMenu` 就是本端口的 `g_postSexMenu`，而本端口**从没给它赋过值**
    //      ⇒ 每次动作后菜单都停在 ML_NONE ⇒ 玩家必须重新打开菜单。这就是"菜单自己关闭"。
    //
    //   现在照抄：动作启动时记下的来源菜单（g_actionReturnMenu）在这里还回去。
    //     · post-sex 语音序列还在跑 ⇒ 交给它收尾时打开（C# 的 _postSexPendingMenu 语义）
    //     · 否则当场打开
    //   ⚠ 只在"她还在"时还菜单；她被放生/敌对释放了就没有可操作的菜单对象，保持关闭。
    // ══════════════════════════════════════════════════════════════════════
    IF g_hasGirl AND CC_EXISTS(g_girl)
        INT retMenu = g_actionReturnMenu
        IF retMenu = ML_NONE
            // 没记到（例如从非菜单路径进来）⇒ 回服务页（C# 默认也是 Services）
            retMenu = ML_SERVICES
        ENDIF
        IF g_postSexPhase = 1
            g_postSexMenu = retMenu
        ELSE
            g_menuLevel = retMenu
            g_menuIndex = 0
        ENDIF
    ENDIF
    g_actionReturnMenu = ML_NONE
    g_keyCooldown = KEY_COOLDOWN_FRAMES * 3
    // ★ v0.67（用户要求）：删掉"完成！"提示 —— 动作结束本来就有画面变化，
    //   再弹一条"完成！"纯属噪音。（原来这里/cc_part2b/cc_part2c 共三处。）
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  敌对反应 (C# TriggerHostileReaction 1:1)
// ════════════════════════════════════════════════════════════════════════
PROC CC_TriggerHostileReaction(PED_INDEX targetPed)
    IF NOT CC_EXISTS(targetPed)
        EXIT
    ENDIF
    INT aggVal = 30
    IF g_curAlIdx >= 0
        aggVal = AGGRESSIVENESS[g_alPers[g_curAlIdx]]
    ENDIF
    // flee 概率 = 1.0 - agg*0.60 (agg 0..0.95)
    INT fleePct = 100 - ((aggVal * 60) / 100)
    IF CC_ROLL(fleePct)
        IF g_curAlIdx >= 0
            g_alRel[g_curAlIdx] = 1
        ENDIF
        CC_Subtitle("~r~她躲着你。", 3000)
        TASK_SMART_FLEE_PED(targetPed, g_player, 150.0, -1, FALSE, FALSE)
    ELSE
        IF g_curAlIdx >= 0
            g_alRel[g_curAlIdx] = 0
        ENDIF
        CC_Subtitle("~r~她不肯再做了！", 3000)
        SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(targetPed, FALSE)
        SET_PED_COMBAT_ATTRIBUTES(targetPed, INT_TO_ENUM(COMBAT_ATTRIBUTE, 2), TRUE)
        SET_PED_COMBAT_ATTRIBUTES(targetPed, INT_TO_ENUM(COMBAT_ATTRIBUTE, 5), TRUE)
        SET_PED_COMBAT_ATTRIBUTES(targetPed, INT_TO_ENUM(COMBAT_ATTRIBUTE, 46), TRUE)
        SET_PED_COMBAT_MOVEMENT(targetPed, CM_WILLADVANCE)
        SET_PED_COMBAT_ABILITY(targetPed, CAL_AVERAGE)
        GIVE_WEAPON_TO_PED(targetPed, INT_TO_ENUM(WEAPON_TYPE, -1576587329), 0, TRUE, TRUE)
        SET_CURRENT_PED_WEAPON(targetPed, INT_TO_ENUM(WEAPON_TYPE, -1576587329), TRUE)
        TASK_COMBAT_PED(targetPed, g_player, INT_TO_ENUM(TASK_COMBAT_PED_FLAGS, 0), INT_TO_ENUM(TASK_THREAT_RESPONSE_FLAGS, 16))
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  搭讪状态机 (C# HandleApproachingState 1:1)
// ════════════════════════════════════════════════════════════════════════
PROC CC_PlayAppAnim(STRING animName, INT flagVal)
    IF NOT CC_ReqDict(CC_APP_DICT_NAME(g_appDict))
        EXIT
    ENDIF
    VECTOR gp = GET_ENTITY_COORDS(g_girl)
    TASK_PLAY_ANIM_ADVANCED(g_girl, g_appDictStr, animName, gp, <<0.0, 0.0, g_appHeading>>, 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, flagVal), 0.0, EULER_YXZ)
ENDPROC

// ── 搭讪状态机 (C# HandleApproachingState — 拆分为 per-phase PROC) ──
PROC CC_AppPhase0()
    VECTOR gpos = GET_ENTITY_COORDS(g_girl)
    VECTOR ppos = GET_ENTITY_COORDS(g_player)
    FLOAT distToTarget = VDIST(gpos, ppos)
    IF g_playerWasInVeh
        distToTarget = VDIST(gpos, g_appTarget)
    ENDIF
    FLOAT dist = VDIST(gpos, ppos)
    IF dist > 10.0
        CC_FullReset(FALSE)
        EXIT
    ENDIF
    FLOAT arrive = 1.7
    IF g_playerWasInVeh
        arrive = 0.8
    ENDIF
    IF distToTarget < arrive
        CLEAR_PED_TASKS(g_girl)
        VECTOR diffV = ppos - gpos
        g_appHeading = GET_HEADING_FROM_VECTOR_2D(diffV.x, diffV.y)
        SET_ENTITY_HEADING(g_girl, g_appHeading)
        g_appDict = CC_RAND(0, 4)
        IF g_curAlIdx >= 0
            g_appDict = g_alDict[g_curAlIdx]
        ENDIF
        IF NOT CC_ReqDict(CC_APP_DICT_NAME(g_appDict))
            CC_FullReset(FALSE)
            EXIT
        ENDIF
        BOOL isProst = FALSE
        IF NOT g_sandboxMode AND g_aLifeMode
            isProst = TRUE
        ENDIF
        IF NOT isProst OR g_prostAutoApproachActive
            CC_AssignVoice()
            CC_PlaySpeech("SOLICIT_GENERIC")
        ENDIF
        CC_PlayAppAnim("idle_intro", 2)
        // ★ 2026-09-25晚：搭讪流程也用同一个夹紧读法 —— 这里是 g_appAnimWait，
        //   读数是垃圾同样会把搭讪卡在 phase 0（人站着不动、菜单不出来）。
        FLOAT introDur = CC_SafeAnimDur(g_appDictStr, "idle_intro", 1.5)
        g_appAnimWait = GET_GAME_TIMER() + ROUND(introDur * 1000.0)
        g_appPhase = 1
    ENDIF
ENDPROC

PROC CC_AppPhase1()
    IF GET_GAME_TIMER() >= g_appAnimWait
        CC_PlayAppAnim("idle_wait", 1)
        g_appSexySwap = GET_GAME_TIMER() + CC_RAND(4000, 8000)
        g_appDecision = GET_GAME_TIMER() + 45000
        g_appPhase = 2
    ENDIF
ENDPROC

// Phase 2/6: idle_wait + sexy pose 打断 (接受/拒绝)
PROC CC_AppPhase26()
    VECTOR gpos = GET_ENTITY_COORDS(g_girl)
    VECTOR ppos = GET_ENTITY_COORDS(g_player)
    FLOAT dist = VDIST(gpos, ppos)
    IF dist > 10.0
        CC_PlayAppAnim("idle_outro", 0)
        FLOAT outroDur = CC_SafeAnimDur(g_appDictStr, "idle_outro", 1.5)
        g_appAnimWait = GET_GAME_TIMER() + ROUND(outroDur * 1000.0)
        g_appPhase = 7
        EXIT
    ENDIF
    IF GET_GAME_TIMER() >= g_appDecision
        STOP_CURRENT_PLAYING_AMBIENT_SPEECH(g_girl)
        CC_PlayAppAnim("idle_reject", 2)
        FLOAT rejDur = CC_SafeAnimDur(g_appDictStr, "idle_reject", 1.5)
        g_appAnimWait = GET_GAME_TIMER() + ROUND(rejDur * 1000.0)
        g_appPhase = 4
        IF NOT g_sandboxMode AND g_aLifeMode AND g_curAlIdx >= 0
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 4
            IF g_alRep[g_curAlIdx] < -1
                g_alRep[g_curAlIdx] = -1
            ENDIF
            AL_UpdateRel(g_curAlIdx)
        ENDIF
        CC_HudStatus("~r~她等得不耐烦，走了！", 2000)
        EXIT
    ENDIF
    // ★ v0.57：按键提示中文化 + 改成本端口的 Switch 键位
    //   （原为 PlayStation 的 `[X/A] Accept [O/B] Decline`；本端口 A=接受 B=拒绝）
    CC_HelpText("~g~[A]~w~ 接受   ~r~[B]~w~ 拒绝")
    IF g_appPhase = 2
        IF GET_GAME_TIMER() >= g_appSexySwap
            TEXT_LABEL_63 sexy = "idle_a"
            INT pickI
            pickI = CC_RAND(0, 4)
            IF pickI = 1
                sexy = "idle_b"
            ELIF pickI = 2
                sexy = "idle_c"
            ELIF pickI = 3
                sexy = "idle_d"
            ENDIF
            CC_PlayAppAnim(sexy, 2)
            FLOAT sexyDur = CC_SafeAnimDur(g_appDictStr, sexy, 1.5)
            g_appAnimWait = GET_GAME_TIMER() + ROUND(sexyDur * 1000.0)
            g_appPhase = 6
        ENDIF
    ENDIF
    IF g_appPhase = 6
        IF GET_GAME_TIMER() >= g_appAnimWait
            CC_PlayAppAnim("idle_wait", 1)
            g_appSexySwap = GET_GAME_TIMER() + CC_RAND(4000, 8000)
            g_appPhase = 2
        ENDIF
    ENDIF
    IF IN_Select()
        CC_PlayAppAnim("idle_outro", 0)
        FLOAT oDur = CC_SafeAnimDur(g_appDictStr, "idle_outro", 1.5)
        g_appAnimWait = GET_GAME_TIMER() + ROUND(oDur * 1000.0)
        g_appPhase = 3
    ELIF IN_Back()
        STOP_CURRENT_PLAYING_AMBIENT_SPEECH(g_girl)
        BOOL casual = FALSE
        IF NOT g_sandboxMode AND g_aLifeMode AND g_curAlIdx >= 0
            INT aggV = AGGRESSIVENESS[g_alPers[g_curAlIdx]]
            INT patV = PATIENCE[g_alPers[g_curAlIdx]]
            INT casualChance = ROUND((100 - aggV) * 0.55 + patV * 0.20)
            IF casualChance < 5
                casualChance = 5
            ENDIF
            IF casualChance > 75
                casualChance = 75
            ENDIF
            casual = CC_ROLL(casualChance)
        ELSE
            IF g_curAlIdx >= 0
                IF g_appRejSet[g_curAlIdx]
                    casual = g_appRejCasual[g_curAlIdx]
                ELSE
                    casual = CC_ROLL(50)
                    g_appRejCasual[g_curAlIdx] = casual
                    g_appRejSet[g_curAlIdx] = TRUE
                ENDIF
            ELSE
                casual = CC_ROLL(50)
            ENDIF
        ENDIF
        IF casual
            CC_PlayAppAnim("idle_outro", 0)
            FLOAT oDur2 = CC_SafeAnimDur(g_appDictStr, "idle_outro", 1.5)
            g_appAnimWait = GET_GAME_TIMER() + ROUND(oDur2 * 1000.0)
            g_appPhase = 7
        ELSE
            CC_PlayAppAnim("idle_reject", 2)
            FLOAT rDur2 = CC_SafeAnimDur(g_appDictStr, "idle_reject", 1.5)
            g_appAnimWait = GET_GAME_TIMER() + ROUND(rDur2 * 1000.0)
            g_appPhase = 4
            IF NOT g_sandboxMode AND g_aLifeMode AND g_curAlIdx >= 0
                g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 4
                IF g_alRep[g_curAlIdx] < -1
                    g_alRep[g_curAlIdx] = -1
                ENDIF
                AL_UpdateRel(g_curAlIdx)
            ENDIF
        ENDIF
        IF casual
            CC_HudStatus("~y~她婉拒了。", 2000)
        ELSE
            CC_HudStatus("~r~她拒绝了！", 2000)
        ENDIF
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════════
//  认领女伴 —— 把她从"环境 NPC"变成"本脚本的剧本实体"
//
//  ★ v0.61 新增。原来只有沙盒路径（CC_TryPickup）做了这一步：
//        IF CC_EXISTS(g_girl)
//            SET_ENTITY_AS_MISSION_ENTITY(g_girl, TRUE, TRUE)
//            SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
//        ENDIF
//    而**搭讪模式的"接受"分支完全没做**（cc_part2d 的 ML_APPROACHLINE →
//    g_hasGirl = TRUE；cc_part2a 的 CC_AppPhase3 同样没有）。
//    后果：她仍被环境 AI / 别的系统管着 ⇒
//      · 该停下来跟你走的时候还在自己走（环境 wander 任务顶掉我们的 TASK_FOLLOW）
//      · 好不容易发的"上车"任务被环境任务打断 ⇒ "要请好几次才上车"
//      · 她被别的系统收回/重排 ⇒ 表现像"莫名其妙走开"
//    统一收口到这一个 PROC，两条路径都调，语义一致、幂等。
//
//  ★ 注意：这一步**挡不住原版 pb_prostitute 脚本**（它自己也会把 ped 标成
//    mission entity，主状态机不查这个标记）。挡它靠的是"选目标时跳过站街女"，
//    见 cc_part1.sc 的 g_hookerHash 长注释。
// ════════════════════════════════════════════════════════════════════════════
PROC CC_ClaimGirl()
    IF NOT CC_EXISTS(g_girl)
        EXIT
    ENDIF
    SET_ENTITY_AS_MISSION_ENTITY(g_girl, TRUE, TRUE)
    SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
    // ★ v0.63：认领时**顺手把嗓子定下来**。
    //   原来 CC_AssignVoice 只在"开始动作 / 要呻吟"时才调（cc_part2b、cc_part1），
    //   中间那段窗口里 g_girlVoice 还是空串；用户报的"这一个女伴全程没声音"，
    //   除了速度=0 那条，这里也是一条可疑窗口。认领即定嗓，成本一次 native。
    CC_AssignVoice()
ENDPROC

PROC CC_AppPhase3()
    VECTOR gpos = GET_ENTITY_COORDS(g_girl)
    VECTOR ppos = GET_ENTITY_COORDS(g_player)
    FLOAT dist = VDIST(gpos, ppos)
    IF dist > 15.0
        IF NOT g_sandboxMode AND g_aLifeMode AND g_curAlIdx >= 0
            g_alRep[g_curAlIdx] = g_alRep[g_curAlIdx] - 6
            IF g_alRep[g_curAlIdx] < -1
                g_alRep[g_curAlIdx] = -1
            ENDIF
            AL_UpdateRel(g_curAlIdx)
        ENDIF
        CC_FullReset(FALSE)
        EXIT
    ENDIF
    IF GET_GAME_TIMER() >= g_appAnimWait
        CLEAR_PED_TASKS(g_girl)
        g_hasGirl = TRUE
        CC_ClaimGirl()
        CC_AttachGirlBlip()
        // ★ 2026-09-25：原来这里无条件 SET_EVERYONE_IGNORE_PLAYER(TRUE)（C# 12425 同）。
        //   现在改为走开关（设置第 13 项「警察无视」，默认开启 = 原行为）。
        CC_ApplyPoliceIgnore()
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            g_playerWasInVeh = TRUE
            g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
            INT seatI = 0
            IF CC_InBackSeat(g_car)
                seatI = 1
            ENDIF
            IF DOES_ENTITY_EXIST(g_car) AND CC_SuitableVeh(g_car)
                TASK_ENTER_VEHICLE(g_girl, g_car, -1, INT_TO_ENUM(VEHICLE_SEAT, seatI), 1.0, INT_TO_ENUM(ENTER_EXIT_VEHICLE_FLAGS, 0))
            ENDIF
        ELSE
            TASK_FOLLOW_TO_OFFSET_OF_ENTITY(g_girl, g_player, <<0.0, -1.0, 0.0>>, 1.0, -1, 1.5, TRUE)
            g_playerWasInVeh = FALSE
        ENDIF
        g_state = ST_IDLE
        g_menuLevel = ML_MAIN
        g_menuIndex = 0
        g_keyCooldown = KEY_COOLDOWN_FRAMES
        CC_HudStatus("~g~她答应了！请选择服务", 3000)
    ENDIF
ENDPROC

PROC CC_AppPhase4()
    IF GET_GAME_TIMER() >= g_appAnimWait
        CC_SyncAppDict(g_appDict)
            REQUEST_ANIM_DICT(g_appDictStr)
        TASK_PLAY_ANIM(g_girl, g_appDictStr, "reject_outro", 8.0, -4.0, -1, INT_TO_ENUM(ANIMATION_FLAGS, 0))
        FLOAT roaDur = CC_SafeAnimDur(g_appDictStr, "reject_outro", 1.5)
        g_appAnimWait = GET_GAME_TIMER() + ROUND(roaDur * 1000.0)
        g_appPhase = 5
    ENDIF
ENDPROC

// ── dispatcher ──
PROC CC_ApproachTick()
    IF NOT CC_ALIVE(g_girl)
        CC_FullReset(FALSE)
        EXIT
    ENDIF
    // 按住主键中止 (phase <= 1)
    IF g_appPhase <= 1
        IF IN_MasterHeld()
            IF g_jHoldStart = 0
                g_jHoldStart = GET_GAME_TIMER()
            ENDIF
            INT heldMs = GET_GAME_TIMER() - g_jHoldStart
            INT pctV = heldMs * 100 / 600
            IF pctV > 100
                pctV = 100
            ENDIF
            // ★ v0.57：原来是 `"~r~ABORTING... %"` —— 那个 `%` 是个**没填进去的
            //   占位符**（上面算出来的 pctV 从没被用上），屏幕上是死字。改成中文提示。
            CC_HudStatus("~r~正在中止…（松手取消）", 100)
            IF heldMs >= 600
                CC_HudStatus("~r~已中止！", 2000)
                CC_FullReset(FALSE)
                EXIT
            ENDIF
        ELSE
            g_jHoldStart = 0
        ENDIF
    ENDIF
    IF g_appPhase = 0
        CC_AppPhase0()
    ELIF g_appPhase = 1
        CC_AppPhase1()
    ELIF g_appPhase = 2 OR g_appPhase = 6
        CC_AppPhase26()
    ELIF g_appPhase = 3
        CC_AppPhase3()
    ELIF g_appPhase = 4
        CC_AppPhase4()
    ELSE
        IF GET_GAME_TIMER() >= g_appAnimWait
            CC_FullReset(FALSE)
        ENDIF
    ENDIF
ENDPROC

// ════════════════════════════════════════════════════════════════════════
//  拾取 (C# TryPickup 核心路径)
// ════════════════════════════════════════════════════════════════════════
PROC CC_TryPickup(PED_INDEX foundPed)
    g_girl = foundPed
    IF NOT g_sandboxMode AND g_aLifeMode AND NOT g_prostAutoApproachActive
        g_prostAutoApproachCd = GET_GAME_TIMER() + 20000
    ENDIF
    // A-Life 记录
    // ★ v0.74 去妓女化：末尾 AND NOT g_alNoHooker（见 cc_part1 的说明）
    g_curAlIdx = AL_GetOrCreate(g_girl, (NOT g_sandboxMode AND g_aLifeMode) AND NOT g_alNoHooker)
    IF g_aLifeMode AND g_curAlIdx >= 0
        IF g_alVoice[g_curAlIdx] < 0
            // ★ v0.72：跳过已知无音频的嗓子（见 cc_part1 的 CC_VoiceRandOk）
            g_alVoice[g_curAlIdx] = CC_VoiceRandOk()
        ENDIF
    ENDIF
    // 定价
    PR_Generate(g_curAlIdx)
    IF NOT g_sandboxMode AND g_aLifeMode AND g_alHooker[g_curAlIdx]
        PR_GenerateProstHooker(g_curAlIdx)
    ENDIF
    IF CC_EXISTS(g_girl)
        SET_ENTITY_AS_MISSION_ENTITY(g_girl, TRUE, TRUE)
        SET_BLOCKING_OF_NON_TEMPORARY_EVENTS(g_girl, TRUE)
    ENDIF
    IF g_aLifeMode AND g_curAlIdx >= 0
        CC_ApplyMoodFace(g_girl, g_alMood[g_curAlIdx])
    ENDIF
    g_girlVoice = ""
    IF NOT g_sandboxMode
        // 她主动走近
        g_menuLevel = ML_NONE
        g_state = ST_APPROACHING
        g_appPhase = 0
        g_jHoldStart = 0
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            g_playerWasInVeh = TRUE
            g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
            VECTOR pp = GET_ENTITY_COORDS(g_player)
            g_appTarget = GET_OFFSET_FROM_ENTITY_IN_WORLD_COORDS(g_player, <<-1.5, 0.5, 0.0>>)
            TASK_GO_STRAIGHT_TO_COORD(g_girl, g_appTarget, 1.0, -1, GET_ENTITY_HEADING(g_player) + 90.0, 0.5)
        ELSE
            g_playerWasInVeh = FALSE
            g_appTarget = GET_ENTITY_COORDS(g_player)
            TASK_GO_TO_ENTITY(g_girl, g_player, -1, 1.5, 1.0, 2.0, INT_TO_ENUM(EGOTO_ENTITY_FLAGS, 0))
        ENDIF
        CC_HudStatus("~y~有小姐正向你走来… ~s~(~b~按住 B+十字键右~s~ 可中止！)", 5000)
    ELSE
        g_hasGirl = TRUE
        CC_AttachGirlBlip()
        // ★★★ v0.70：沙盒路径也**认领即定嗓**。
        //   原来只有 CC_ClaimGirl（搭讪/走近那条路）会调 CC_AssignVoice，
        //   沙盒要等到"第一次要呻吟"才定嗓 ⇒ 在那之前 g_girlVoice 是空串，
        //   服务语音（CC_PlaySpeech）会掉到"没有嗓子"的分支，用她**模型自带**的嗓音，
        //   和后来定下来的嗓子不是同一个 ⇒ 听起来像"这个女伴的声前后不一致"。
        //   放在这里 = 她一加入就有确定的、**单独随机**的嗓子。
        CC_AssignVoice()
        // ★ 2026-09-25：同 CC_AppPhase3 —— 走开关而不是无条件 TRUE。
        CC_ApplyPoliceIgnore()
        IF IS_PED_IN_ANY_VEHICLE(g_player, FALSE)
            g_playerWasInVeh = TRUE
            g_car = GET_VEHICLE_PED_IS_IN(g_player, FALSE)
            INT seat = 0
            IF CC_InBackSeat(g_car)
                seat = 1
            ENDIF
            IF DOES_ENTITY_EXIST(g_car) AND CC_SuitableVeh(g_car)
                TASK_ENTER_VEHICLE(g_girl, g_car, -1, INT_TO_ENUM(VEHICLE_SEAT, seat), 1.0, INT_TO_ENUM(ENTER_EXIT_VEHICLE_FLAGS, 0))
            ENDIF
        ELSE
            FLOAT dist = VDIST(GET_ENTITY_COORDS(g_girl), GET_ENTITY_COORDS(g_player))
            FLOAT spd = 1.0
            IF dist > 7.0
                spd = 3.0
            ENDIF
            TASK_FOLLOW_TO_OFFSET_OF_ENTITY(g_girl, g_player, <<0.0, -1.0, 0.0>>, spd, -1, 1.5, TRUE)
            g_playerWasInVeh = FALSE
        ENDIF
        IF NOT g_aLifeMode
            CC_HudStatus("~g~找到目标了！", 3000)
        ENDIF
    ENDIF
ENDPROC

// 最近女NPC (C# FindNearestFemale)
//  ★ v0.61：新增 skipHooker —— 见 cc_part1.sc 里 g_hookerHash 那段长注释。
//    原版站街女（s_f_y_hooker_*）身上挂着原版的 pb_prostitute 脚本，
//    玩家一上车它就会抢走控制权（剧本相机锁视角 / 尖叫跑走 / 上车被踹）。
//    排除它们 = 从源头消掉那四个现象；代价是搭讪模式下不能搭讪站街女（用户已确认）。
FUNC PED_INDEX CC_FindNearestFemale(FLOAT range, BOOL skipHooker)
    PED_INDEX bestPed = NULL
    FLOAT bestDist = range
    PED_INDEX arr[64]
    INT n = GET_PED_NEARBY_PEDS(g_player, arr, INT_TO_ENUM(PED_TYPE, -1))
    INT i = 0
    VECTOR pp = GET_ENTITY_COORDS(g_player)
    g_lastScanSkippedHooker = FALSE
    WHILE i < n AND i < 64
        PED_INDEX p = arr[i]
        IF p != g_player
            IF DOES_ENTITY_EXIST(p)
                IF CC_ALIVE(p)
                    IF NOT IS_PED_IN_ANY_VEHICLE(p, FALSE)
                        IF NOT IS_PED_MALE(p)
                            IF skipHooker AND CC_IsVanillaHookerModel(p)
                                // 记下来：全是站街女时，调用方要给出解释而不是"附近没有女性"
                                g_lastScanSkippedHooker = TRUE
                            ELSE
                                FLOAT d = VDIST(GET_ENTITY_COORDS(p), pp)
                                IF d < bestDist
                                    bestDist = d
                                    bestPed = p
                                ENDIF
                            ENDIF
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF
        ENDIF
        i = i + 1
    ENDWHILE
    RETURN bestPed
ENDFUNC
