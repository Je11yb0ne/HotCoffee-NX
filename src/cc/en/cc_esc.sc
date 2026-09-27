// ============================================================================
// cc_esc.sc —— 「升级 / 进一步」菜单（C# MenuLevel.Escalate）的台词（**ENGLISH**）
// ----------------------------------------------------------------------------
// ★ 本文件由 gen_esc_en.py 生成（源: esc_en.json <- extract_esc.py <- C# GetEscalate*Line）。
//   与中文版的 cc_esc.sc 函数形状完全一致，只有字面量语言不同。
//
// 接口（与本项目其它家族同形，但这里是 lineIdx 而非"可随机条数"）：
//   CC_ESCACC_H()                    头（"~g~"）
//   CC_ESCACC_T(lineIdx, pIdx)       正文（lineIdx = 菜单第几项 0/1/2）
//   CC_ESCREJ_H() / CC_ESCREJ_T(...) 同上，头是 "~r~"
//   ★ pIdx = **性格**索引 0..19（不是 A-Life 槽位号！）
//
// 菜单三项：0 = "Let's have some fun." / 1 = "Mind if I... come closer?.." / 2 = "Wanna fuck?"
// ============================================================================


FUNC STRING CC_ESCACC_H()
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_ESCREJ_H()
    RETURN "~r~"
ENDFUNC

FUNC STRING CC_ESCACC_L0_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN "...Okay. Just... stay close."    CASE 1 RETURN "I thought you'd never ask."    CASE 2 RETURN "Lead the way. But I'm in charge."    CASE 3 RETURN "Fine. Don't make it a whole thing."
        CASE 4 RETURN "I'd really like that."    CASE 5 RETURN "Sure. Make it worth the night."    CASE 6 RETURN "Alright. Keep it discreet."    CASE 7 RETURN "Hell yeah, let's get out of here."
        CASE 8 RETURN "I've been waiting for you to say that."    CASE 9 RETURN "Sure, beats standing here."    CASE 10 RETURN "Yes... please don't let go."    CASE 11 RETURN "On my terms. Understood?"
        CASE 12 RETURN "Only if it's just us."    CASE 13 RETURN "Yes! Let's go right now!"    CASE 14 RETURN "I'll allow it. For now."    CASE 15 RETURN "Finally. Move."
        CASE 16 RETURN "Ooh, yes. Let's go."    CASE 17 RETURN "...Come on then."    CASE 18 RETURN "Alright. But I have expectations."    CASE 19 RETURN "Yes! Yes! Don't change your mind!"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCACC_L1_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN "...I don't mind."    CASE 1 RETURN "I was wondering when you'd close the gap."    CASE 2 RETURN "You're asking? Good. Come here."    CASE 3 RETURN "...Sure. Just don't hover."
        CASE 4 RETURN "Of course. Come here."    CASE 5 RETURN "Mmm. Make it interesting."    CASE 6 RETURN "Go ahead. But read the room."    CASE 7 RETURN "Yeah, get in here."
        CASE 8 RETURN "I was hoping you would."    CASE 9 RETURN "Oh, look who finally made a move."    CASE 10 RETURN "Please. I want you close."    CASE 11 RETURN "Sure. Just don't crowd me."
        CASE 12 RETURN "Only you. Nobody else."    CASE 13 RETURN "Yes! Get over here!"    CASE 14 RETURN "I'll let you. Don't waste it."    CASE 15 RETURN "Stop asking and do it."
        CASE 16 RETURN "Took you long enough."    CASE 17 RETURN "...Yeah."    CASE 18 RETURN "You may."    CASE 19 RETURN "YES. Come here right now."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCACC_L2_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN "That's... really blunt. But... okay."    CASE 1 RETURN "God, finally someone who just says it. Yes."    CASE 2 RETURN "Straight to it. I respect that. Come on."    CASE 3 RETURN "Efficient. Fine."
        CASE 4 RETURN "You could've asked nicer but... yes."    CASE 5 RETURN "Bold. Make it worth my time."    CASE 6 RETURN "Blunt. Alright. But we're doing this my way."    CASE 7 RETURN "Yes! That's what I'm talking about!"
        CASE 8 RETURN "Not how I imagined it, but... yes."    CASE 9 RETURN "Real smooth. Yeah, sure."    CASE 10 RETURN "...Yes. Don't make me wait."    CASE 11 RETURN "At least you're honest. Yeah."
        CASE 12 RETURN "You better only be asking me that."    CASE 13 RETURN "YES. Let's go let's go let's go!"    CASE 14 RETURN "Direct. I can work with that."    CASE 15 RETURN "Now we're talking."
        CASE 16 RETURN "Ha! Yes. Absolutely yes."    CASE 17 RETURN "...Yeah."    CASE 18 RETURN "No subtlety whatsoever. ...Fine."    CASE 19 RETURN "YES. NOW. Let's GO."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCREJ_L0_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN "I... I'm not ready for that."    CASE 1 RETURN "Maybe later. Not yet."    CASE 2 RETURN "You haven't earned a night with me."    CASE 3 RETURN "That's not happening tonight."
        CASE 4 RETURN "I like you, but I'm not there yet."    CASE 5 RETURN "A night costs more than just asking."    CASE 6 RETURN "I don't know you well enough for that."    CASE 7 RETURN "Not right now. Ask me after another round."
        CASE 8 RETURN "I need more between us before that."    CASE 9 RETURN "Charming offer. Hard pass."    CASE 10 RETURN "I want to... but I need to know you mean it."    CASE 11 RETURN "I don't do that on your timeline."
        CASE 12 RETURN "You better not be asking anyone else this."    CASE 13 RETURN "Wrong vibe right now."    CASE 14 RETURN "Earn the night first."    CASE 15 RETURN "Not a chance."
        CASE 16 RETURN "Mmm... not quite yet."    CASE 17 RETURN "Not yet."    CASE 18 RETURN "Not how this works."    CASE 19 RETURN "No! Don't push me!"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCREJ_L1_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN "I'd rather keep some space right now."    CASE 1 RETURN "Not right now."    CASE 2 RETURN "You can want. Doesn't mean you get."    CASE 3 RETURN "Yes, I mind. Keep your distance."
        CASE 4 RETURN "I'm not quite comfortable with that yet."    CASE 5 RETURN "Closeness like that costs something."    CASE 6 RETURN "Keep your distance."    CASE 7 RETURN "Maybe later."
        CASE 8 RETURN "Not yet. We're not there."    CASE 9 RETURN "Yes, I do mind. Nice question though."    CASE 10 RETURN "I want to but... I need to trust you first."    CASE 11 RETURN "I prefer my space, thanks."
        CASE 12 RETURN "I don't want that right now."    CASE 13 RETURN "No! Bad timing!"    CASE 14 RETURN "Closer isn't a right. It's earned."    CASE 15 RETURN "Back up."
        CASE 16 RETURN "Hmm... not yet."    CASE 17 RETURN "No."    CASE 18 RETURN "That's a bit forward."    CASE 19 RETURN "Don't come near me right now!"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_ESCREJ_L2_T(INT pIdx)
    SWITCH pIdx
        CASE 0 RETURN "You can't just... say that. No."    CASE 1 RETURN "You could've said that better. Try again."    CASE 2 RETURN "You haven't earned that."    CASE 3 RETURN "No."
        CASE 4 RETURN "That's... a lot. No thank you."    CASE 5 RETURN "Not for free."    CASE 6 RETURN "That how you talk to women?"    CASE 7 RETURN "Even I have standards. No."
        CASE 8 RETURN "That's not how I want this to go."    CASE 9 RETURN "Wow. No."    CASE 10 RETURN "Not like this. Ask me properly."    CASE 11 RETURN "That's not how this goes."
        CASE 12 RETURN "Don't talk to me like that."    CASE 13 RETURN "No! Bad! Wrong!"    CASE 14 RETURN "That's not how you get what you want."    CASE 15 RETURN "Watch your mouth."
        CASE 16 RETURN "Points for boldness. Still no."    CASE 17 RETURN "No."    CASE 18 RETURN "Absolutely not."    CASE 19 RETURN "HOW DARE YOU. ...maybe later. BUT NO."
    ENDSWITCH
    RETURN ""
ENDFUNC

// lineIdx 分发：0 / 1 / 2 -> _L0 / _L1 / _L2 的正文。
FUNC STRING CC_ESCACC_T(INT lineIdx, INT pIdx)
    IF lineIdx = 0
        RETURN CC_ESCACC_L0_T(pIdx)
    ELIF lineIdx = 1
        RETURN CC_ESCACC_L1_T(pIdx)
    ENDIF
    RETURN CC_ESCACC_L2_T(pIdx)
ENDFUNC

// lineIdx 分发：0 / 1 / 2 -> _L0 / _L1 / _L2 的正文。
FUNC STRING CC_ESCREJ_T(INT lineIdx, INT pIdx)
    IF lineIdx = 0
        RETURN CC_ESCREJ_L0_T(pIdx)
    ELIF lineIdx = 1
        RETURN CC_ESCREJ_L1_T(pIdx)
    ENDIF
    RETURN CC_ESCREJ_L2_T(pIdx)
ENDFUNC
