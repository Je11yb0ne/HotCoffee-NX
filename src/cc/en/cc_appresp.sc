// ============================================================================
// cc_appresp.sc — 搭讪「她的回应」表（**ENGLISH**）
// ----------------------------------------------------------------------------
// ★ 由 gen_appresp_en.py 生成（源: C# `GetApproachLineResponseOptions` 的 **warm** 那一组）。
//   形状与中文版严格同形：`CC_AppRespNo(cluster, lineIdx)` / `CC_AppRespYes(cluster, lineIdx)`，
//   每个 (风格, 台词号) 给 a/b 两条随机。
//
//   【返回格式】必须是**整行**（自带 ":~s~ "）—— 调用方是
//       head("~g~"/"~r~") + 名字 + 这句 三段拼，见 cc_part2d 的 ML_APPROACHLINE。
//
//   【组装位置】merge_mods.py 里排在 cc_dlgnav.sc 之后（要用它的 CC_DlgPick）、
//     cc_part2d.sc 之前（调用点在 p2d）。
// ============================================================================



// -- 拒绝 --
FUNC STRING CC_AppRespNo(INT cluster, INT lineIdx)
    STRING a = ":~s~ Not now. You picked the wrong person."
    STRING b = ":~s~ I don't think so. Take it somewhere else."
    INT v = CC_DlgPick(2)

    IF cluster = 0                          // Gentle
        IF lineIdx = 0
            a = ":~s~ Oh, you're sweet, but now's not a good time."
            b = ":~s~ I'm a bit busy right now. Sorry."
        ELIF lineIdx = 1
            a = ":~s~ You're kind, but I'm keeping to myself today."
            b = ":~s~ That's sweet, but I'm not looking for company."
        ELIF lineIdx = 2
            a = ":~s~ That's sweet. But please don't take it as an invitation."
            b = ":~s~ Hi. But I really need to be alone right now."
        ELIF lineIdx = 3
            a = ":~s~ I appreciate the offer, but I'd like some quiet."
            b = ":~s~ That's kind of you. I just need some space."
        ENDIF
    ELIF cluster = 1                          // Confident
        IF lineIdx = 0
            a = ":~s~ Flattered. But not interested, sorry."
            b = ":~s~ Good for you. I'm still busy."
        ELIF lineIdx = 1
            a = ":~s~ I appreciate the honesty. Still not interested."
            b = ":~s~ Bold. I respect it. But still no."
        ELIF lineIdx = 2
            a = ":~s~ I do. And right now I want some space."
            b = ":~s~ True. And what I want is to be left alone. Sorry."
        ELIF lineIdx = 3
            a = ":~s~ I really don't, sorry."
            b = ":~s~ Not right now. Maybe later?"
        ENDIF
    ELIF cluster = 2                          // Cool
        IF lineIdx = 0
            a = ":~s~ Then go do them."
            b = ":~s~ Right. So do I, actually."
        ELIF lineIdx = 1
            a = ":~s~ Good. I'll stay that way then."
            b = ":~s~ I'll take that. Goodbye."
        ELIF lineIdx = 2
            a = ":~s~ Thank you. Short but still no."
            b = ":~s~ Appreciated. Still keeping to myself."
        ELIF lineIdx = 3
            a = ":~s~ Thank you. I like to keep to myself though."
            b = ":~s~ That's kind. Still leaving."
        ENDIF
    ELIF cluster = 3                          // Flattering
        IF lineIdx = 0
            a = ":~s~ Points for bravery. Still no thanks."
            b = ":~s~ I appreciate the effort. Not interested."
        ELIF lineIdx = 1
            a = ":~s~ That's sweet. Types don't always work out though."
            b = ":~s~ Flattered. Still not interested."
        ELIF lineIdx = 2
            a = ":~s~ Aw, that's kind. Still not going anywhere with you."
            b = ":~s~ Thank you genuinely. But no."
        ELIF lineIdx = 3
            a = ":~s~ That's really sweet. I'm just not looking."
            b = ":~s~ I appreciate that. I'm fine right now."
        ENDIF
    ELIF cluster = 4                          // Playful
        IF lineIdx = 0
            a = ":~s~ Ha, you're right, I do. Thanks though!"
            b = ":~s~ I do. And I'm still busy. Funny how that works."
        ELIF lineIdx = 1
            a = ":~s~ Ha! Maybe I am. That's exactly why you should go."
            b = ":~s~ Trouble doesn't mean available. Take care."
        ELIF lineIdx = 2
            a = ":~s~ Ha! I might be. Just not for you right now."
            b = ":~s~ I am fun. Just not with strangers today."
        ELIF lineIdx = 3
            a = ":~s~ Oh, I wish I could. Sorry!"
            b = ":~s~ I'd give them if I could. Not right now."
        ENDIF
    ENDIF

    IF v = 2
        RETURN b
    ENDIF
    RETURN a
ENDFUNC

// -- 接受 --
FUNC STRING CC_AppRespYes(INT cluster, INT lineIdx)
    STRING a = ":~s~ Not now. You picked the wrong person."
    STRING b = ":~s~ I don't think so. Take it somewhere else."
    INT v = CC_DlgPick(2)

    IF cluster = 0                          // Gentle
        IF lineIdx = 0
            a = ":~s~ Not at all, how can I help you?"
            b = ":~s~ Of course not. What's on your mind?"
        ELIF lineIdx = 1
            a = ":~s~ Aw, thank you! That really made my day."
            b = ":~s~ You just made me smile even harder."
        ELIF lineIdx = 2
            a = ":~s~ I'm glad you didn't! Hi back."
            b = ":~s~ Well hi! What's up?"
        ELIF lineIdx = 3
            a = ":~s~ I'd like that, actually."
            b = ":~s~ Sure, why not? I could use the company."
        ENDIF
    ELIF cluster = 1                          // Confident
        IF lineIdx = 0
            a = ":~s~ Did I? Well, you've got mine too."
            b = ":~s~ Good. Come on over."
        ELIF lineIdx = 1
            a = ":~s~ Good, because I noticed you too."
            b = ":~s~ Honesty. I like that. Come here."
        ELIF lineIdx = 2
            a = ":~s~ I do. And right now I'm curious about you."
            b = ":~s~ Ha, you have no idea. Let's go."
        ELIF lineIdx = 3
            a = ":~s~ Sure. What's going on?"
            b = ":~s~ For you? Yeah, I think so."
        ENDIF
    ELIF cluster = 2                          // Cool
        IF lineIdx = 0
            a = ":~s~ Ha. Then why'd you stop?"
            b = ":~s~ Fair point. But here we are."
        ELIF lineIdx = 1
            a = ":~s~ Most people say that. You seem like you might figure it out."
            b = ":~s~ Good observation. Most give up. Come on."
        ELIF lineIdx = 2
            a = ":~s~ Short and effective. I appreciate that."
            b = ":~s~ You're not bad yourself. Come on."
        ELIF lineIdx = 3
            a = ":~s~ Ha, you noticed. I like you already."
            b = ":~s~ Good eyes. Come on then."
        ENDIF
    ELIF cluster = 3                          // Flattering
        IF lineIdx = 0
            a = ":~s~ Ha! I like that. Come on then."
            b = ":~s~ Smart man. Let's talk."
        ELIF lineIdx = 1
            a = ":~s~ Well that's convenient, because I don't hate your face either."
            b = ":~s~ Is that so? Come here and let's find out."
        ELIF lineIdx = 2
            a = ":~s~ I'll take it exactly the right way. Thank you."
            b = ":~s~ Ha, I never take that the wrong way. Come on."
        ELIF lineIdx = 3
            a = ":~s~ Damn right I do. And that's you, right now."
            b = ":~s~ You're not wrong. Let's see where this goes."
        ENDIF
    ELIF cluster = 4                          // Playful
        IF lineIdx = 0
            a = ":~s~ Ha! Honestly, you're not wrong. Come on."
            b = ":~s~ I do get it a lot. But I liked how you said it."
        ELIF lineIdx = 1
            a = ":~s~ Ha! You have no idea. Come on then."
            b = ":~s~ I've been called worse. Let's see what you've got."
        ELIF lineIdx = 2
            a = ":~s~ Oh, I'm more fun than I look. You in?"
            b = ":~s~ I absolutely am. Come find out."
        ELIF lineIdx = 3
            a = ":~s~ Ha, five minutes? Sure, let's see if you're worth it."
            b = ":~s~ Fine! Five minutes. Starting now."
        ENDIF
    ENDIF

    IF v = 2
        RETURN b
    ENDIF
    RETURN a
ENDFUNC
