// ============================================================================
// cc_leave.sc —— 她说"再见"时的回应（**ENGLISH**，休闲/非街女那套）
// ----------------------------------------------------------------------------
// ★ 由 gen_leave_en.py 生成（源: C# `GetLeaveReactionLines`）。
//   中文版里这两张表是手工补进 cc_dialogue_gen.sc 的 —— 生成器抽不出
//   C# 那种 `matched ? A : B` 的二分结构。英文版同样单独生成。
//
// 接口：
//   CC_LEAVEM_H(persIdx) / CC_LEAVEM_T(idx)    matched（她的性格吃你这种道别）
//   CC_LEAVEN_H(persIdx) / CC_LEAVEN_T(idx)    不 matched
//   ★ idx = persIdx*3 + (0..2)，**不是**性格号（见 cc_part2d 的 lvIdx 算法）
// ============================================================================


FUNC STRING CC_LEAVEM_H(INT persIdx)
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_LEAVEN_H(INT persIdx)
    RETURN "~r~"
ENDFUNC

FUNC STRING CC_LEAVEM_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "I'll see you soon?"    CASE 1 RETURN "Come back when you can, okay?"    CASE 2 RETURN "Take care of yourself."
        CASE 3 RETURN "Sure you don't want to stay a little longer?"    CASE 4 RETURN "Come find me again sometime."    CASE 5 RETURN "I'll be thinking about you."
        CASE 6 RETURN "You know where to find me."    CASE 7 RETURN "Come back when you're ready."    CASE 8 RETURN "I'll be here."
        CASE 9 RETURN "Until next time."    CASE 10 RETURN "Later."    CASE 11 RETURN "See you."
        CASE 12 RETURN "Aw, I hope I see you again!"    CASE 13 RETURN "Come back soon, okay?"    CASE 14 RETURN "It was so nice talking to you!"
        CASE 15 RETURN "Let me know if you need anything."    CASE 16 RETURN "I'll be here."    CASE 17 RETURN "Come back anytime."
        CASE 18 RETURN "Stay out of trouble."    CASE 19 RETURN "Watch your back out there."    CASE 20 RETURN "Keep it real."
        CASE 21 RETURN "This was fun! Next time, yeah?"    CASE 22 RETURN "Had a blast! Come find me again!"    CASE 23 RETURN "Don't be a stranger!"
        CASE 24 RETURN "This was wonderful. Come back soon."    CASE 25 RETURN "I'll be thinking about you."    CASE 26 RETURN "Until we meet again."
        CASE 27 RETURN "Try not to think about me too much."    CASE 28 RETURN "Good luck not missing me."    CASE 29 RETURN "I'll be here when you inevitably come back."
        CASE 30 RETURN "Don't stay away too long."    CASE 31 RETURN "Text me when you get home?"    CASE 32 RETURN "Come back soon, promise?"
        CASE 33 RETURN "Take care."    CASE 34 RETURN "Look after yourself."    CASE 35 RETURN "Until next time."
        CASE 36 RETURN "Come back soon, okay?"    CASE 37 RETURN "Don't make me wait too long."    CASE 38 RETURN "I'll be thinking about you."
        CASE 39 RETURN "BYE! Come back when things get boring!"    CASE 40 RETURN "GO GO GO! Come back later!"    CASE 41 RETURN "Bye! This was chaos in the best way!"
        CASE 42 RETURN "You're going to miss me."    CASE 43 RETURN "I knew you'd come around. See you soon."    CASE 44 RETURN "I'll be here. I always am."
        CASE 45 RETURN "Yeah, see you around."    CASE 46 RETURN "Stay out of trouble."    CASE 47 RETURN "Don't take too long."
        CASE 48 RETURN "See ya! Don't miss me too much!"    CASE 49 RETURN "Come back and play later!"    CASE 50 RETURN "Bye! This was fun!"
        CASE 51 RETURN "You'll find me if you want to."    CASE 52 RETURN "I'll be around."    CASE 53 RETURN "Until we cross paths again."
        CASE 54 RETURN "It was a pleasure. Until next time."    CASE 55 RETURN "Do take care."    CASE 56 RETURN "I look forward to seeing you again."
        CASE 57 RETURN "Okay! Text me when you get home!"    CASE 58 RETURN "Come back soon! Like really soon!"    CASE 59 RETURN "Bye! Be safe! Come back!"
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_LEAVEN_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "Oh... okay. Bye."    CASE 1 RETURN "Alright. See you."    CASE 2 RETURN "Okay. Bye."
        CASE 3 RETURN "Aw, already?"    CASE 4 RETURN "Already leaving?"    CASE 5 RETURN "Bye then."
        CASE 6 RETURN "Mmhm. Later."    CASE 7 RETURN "Sure."    CASE 8 RETURN "Fine. Go."
        CASE 9 RETURN "Noted."    CASE 10 RETURN "Sure."    CASE 11 RETURN "Right."
        CASE 12 RETURN "Oh, okay. Bye!"    CASE 13 RETURN "Take care!"    CASE 14 RETURN "Alright, see you around."
        CASE 15 RETURN "Mmhm."    CASE 16 RETURN "Sure."    CASE 17 RETURN "Okay."
        CASE 18 RETURN "Aight."    CASE 19 RETURN "Sure."    CASE 20 RETURN "Later."
        CASE 21 RETURN "Ugh, fine."    CASE 22 RETURN "Whatever, bye."    CASE 23 RETURN "Okay then."
        CASE 24 RETURN "Already leaving?"    CASE 25 RETURN "Oh. Okay then."    CASE 26 RETURN "Take care."
        CASE 27 RETURN "Oh wow, don't make me cry."    CASE 28 RETURN "Devastating. Truly."    CASE 29 RETURN "I'll manage somehow."
        CASE 30 RETURN "Oh. Fine."    CASE 31 RETURN "You're leaving already?"    CASE 32 RETURN "Okay. I guess."
        CASE 33 RETURN "Sure."    CASE 34 RETURN "Okay."    CASE 35 RETURN "Bye."
        CASE 36 RETURN "Fine. Go."    CASE 37 RETURN "Okay then."    CASE 38 RETURN "Sure."
        CASE 39 RETURN "Okay, sure, whatever!"    CASE 40 RETURN "Fine! Bye!"    CASE 41 RETURN "Okay!"
        CASE 42 RETURN "Leaving already? That's a shame."    CASE 43 RETURN "I thought you'd stay longer."    CASE 44 RETURN "Your loss."
        CASE 45 RETURN "Go, then."    CASE 46 RETURN "Whatever."    CASE 47 RETURN "Bye."
        CASE 48 RETURN "Alright, bye."    CASE 49 RETURN "Okay then."    CASE 50 RETURN "Sure."
        CASE 51 RETURN "Sure."    CASE 52 RETURN "Okay."    CASE 53 RETURN "Right."
        CASE 54 RETURN "Of course."    CASE 55 RETURN "Goodbye then."    CASE 56 RETURN "Very well."
        CASE 57 RETURN "Oh. Okay then."    CASE 58 RETURN "Fine. Bye."    CASE 59 RETURN "Okay. I guess."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_LEAVEPROST_1_H(INT relIdx)
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_LEAVEPROST_1_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "Don't rush back."    CASE 1 RETURN "Good."
        CASE 2 RETURN "Finally."    CASE 3 RETURN "Okay."
        CASE 4 RETURN "Right."    CASE 5 RETURN "Sure."
        CASE 6 RETURN "Mmhm."    CASE 7 RETURN "Alright."
        CASE 8 RETURN "Sure."    CASE 9 RETURN "Mmhm."
        CASE 10 RETURN "Alright."    CASE 11 RETURN "Sure."
        CASE 12 RETURN "Mmhm."    CASE 13 RETURN "Alright."
        CASE 14 RETURN "Sure."    CASE 15 RETURN "See you around."
        CASE 16 RETURN "Take care."    CASE 17 RETURN "Later."
        CASE 18 RETURN "See you around."    CASE 19 RETURN "Take care."
        CASE 20 RETURN "Later."    CASE 21 RETURN "Already?"
        CASE 22 RETURN "You just got here..."    CASE 23 RETURN "Fine. Come back soon."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_LEAVEPROST_2_H(INT relIdx)
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_LEAVEPROST_2_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "Don't rush back."    CASE 1 RETURN "Good."
        CASE 2 RETURN "Finally."    CASE 3 RETURN "Okay."
        CASE 4 RETURN "Right."    CASE 5 RETURN "Sure."
        CASE 6 RETURN "Mmhm."    CASE 7 RETURN "Alright."
        CASE 8 RETURN "Sure."    CASE 9 RETURN "Mmhm."
        CASE 10 RETURN "Alright."    CASE 11 RETURN "Sure."
        CASE 12 RETURN "Mmhm."    CASE 13 RETURN "Alright."
        CASE 14 RETURN "Sure."    CASE 15 RETURN "You know where I am."
        CASE 16 RETURN "I'll be around."    CASE 17 RETURN "Same spot."
        CASE 18 RETURN "You know where I am."    CASE 19 RETURN "I'll be around."
        CASE 20 RETURN "Same spot."    CASE 21 RETURN "You better come back."
        CASE 22 RETURN "I'll hold you to that."    CASE 23 RETURN "Don't forget about me."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_LEAVEPROST_3_H(INT relIdx)
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_LEAVEPROST_3_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "Don't rush back."    CASE 1 RETURN "Good."
        CASE 2 RETURN "Finally."    CASE 3 RETURN "Okay."
        CASE 4 RETURN "Right."    CASE 5 RETURN "Sure."
        CASE 6 RETURN "Mmhm."    CASE 7 RETURN "Alright."
        CASE 8 RETURN "Sure."    CASE 9 RETURN "Mmhm."
        CASE 10 RETURN "Alright."    CASE 11 RETURN "Sure."
        CASE 12 RETURN "Mmhm."    CASE 13 RETURN "Alright."
        CASE 14 RETURN "Sure."    CASE 15 RETURN "Appreciated."
        CASE 16 RETURN "Good doing business."    CASE 17 RETURN "Thanks for stopping by."
        CASE 18 RETURN "Appreciated."    CASE 19 RETURN "Good doing business."
        CASE 20 RETURN "Thanks for stopping by."    CASE 21 RETURN "Already?"
        CASE 22 RETURN "You just got here..."    CASE 23 RETURN "Fine. Come back soon."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_LEAVEPROST_4_H(INT relIdx)
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_LEAVEPROST_4_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "Don't rush back."    CASE 1 RETURN "Good."
        CASE 2 RETURN "Finally."    CASE 3 RETURN "Okay."
        CASE 4 RETURN "Right."    CASE 5 RETURN "Sure."
        CASE 6 RETURN "Stay safe out there."    CASE 7 RETURN "Take care of yourself."
        CASE 8 RETURN "Be careful out there."    CASE 9 RETURN "Stay safe out there."
        CASE 10 RETURN "Take care of yourself."    CASE 11 RETURN "Be careful out there."
        CASE 12 RETURN "Stay safe out there."    CASE 13 RETURN "Take care of yourself."
        CASE 14 RETURN "Be careful out there."    CASE 15 RETURN "I'll be here. Take care."
        CASE 16 RETURN "Safe travels."    CASE 17 RETURN "Look after yourself."
        CASE 18 RETURN "I'll be here. Take care."    CASE 19 RETURN "Safe travels."
        CASE 20 RETURN "Look after yourself."    CASE 21 RETURN "Come find me whenever. I mean it."
        CASE 22 RETURN "I'll be thinking about you."    CASE 23 RETURN "Don't make me wait too long."
    ENDSWITCH
    RETURN ""
ENDFUNC

FUNC STRING CC_LEAVEPROST_5_H(INT relIdx)
    RETURN "~g~"
ENDFUNC

FUNC STRING CC_LEAVEPROST_5_T(INT idx)
    SWITCH idx
        CASE 0 RETURN "Don't rush back."    CASE 1 RETURN "Good."
        CASE 2 RETURN "Finally."    CASE 3 RETURN "Okay."
        CASE 4 RETURN "Right."    CASE 5 RETURN "Sure."
        CASE 6 RETURN "Ha. Sure."    CASE 7 RETURN "Sure, yeah."
        CASE 8 RETURN "Okay then."    CASE 9 RETURN "Ha. Sure."
        CASE 10 RETURN "Sure, yeah."    CASE 11 RETURN "Okay then."
        CASE 12 RETURN "Ha. Sure."    CASE 13 RETURN "Sure, yeah."
        CASE 14 RETURN "Okay then."    CASE 15 RETURN "You're not so bad yourself."
        CASE 16 RETURN "Ha. See you."    CASE 17 RETURN "Not bad. Come back sometime."
        CASE 18 RETURN "You're not so bad yourself."    CASE 19 RETURN "Ha. See you."
        CASE 20 RETURN "Not bad. Come back sometime."    CASE 21 RETURN "Stop it. Come back soon."
        CASE 22 RETURN "Ugh, fine. But you better be back."    CASE 23 RETURN "Go then. I want you back soon."
    ENDSWITCH
    RETURN ""
ENDFUNC
