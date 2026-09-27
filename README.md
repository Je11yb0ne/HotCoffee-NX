# HotCoffee-NX

CinnamonCoffee 的 GTA V Switch 版移植。用 SanScript 重写，中英双语两套文本。

English: **[README.en.md](README.en.md)**

---

## 下载

在 [Releases](https://github.com/Je11yb0ne/HotCoffee-NX/releases) 页面，四个压缩包二选一：

| 压缩包 | 语言 | 内容 |
|---|---|---|
| `HotCoffee-NX_cinnamon_zh.zip` | 中文 | 只有本模组 |
| `HotCoffee-NX_cinnamon_en.zip` | English | 只有本模组 |
| `HotCoffee-NX_combo_zh.zip` | 中文 | 本模组 + 僵尸模组「脏比洛圣都」 |
| `HotCoffee-NX_combo_en.zip` | English | 本模组 + 僵尸模组「脏比洛圣都」 |

每个压缩包里是**两个文件**（名字已经是最终的，不用改）：

```
simple_zombies.nsc
error_listener.nsc
```

## 安装

1. **备份** RPF 里原版的 `error_listener.nsc`。
2. 用 RPF 工具（CodeWalker / RPF Explorer 等）打开
   `<SD>:/atmosphere/contents/<title id>/romfs/update/update2.rpf` 内层的
   `switch/levels/gta5/script/script_rel.rpf`。
3. 把压缩包里的 `error_listener.nsc` **替换**原版那个。
4. 把压缩包里的 `simple_zombies.nsc` **新增**为条目。
5. 两个条目都必须是**普通压缩文件条目**（不要做成 RPF 资源条目、不要加 RSC7 外壳）。

文件名不能改：脚本名在编译时已经烙进 `.nsc`，入口脚本按这个名字去找它，名字对不上游戏里不会有反应。

## 使用

| 按键 | 作用 |
|---|---|
| **`B` + 十字键右** | 打开 / 关闭主菜单 |
| 十字键 ↑↓ | 上下移动 |
| `A` 或 十字键 ←→ | 确认 |
| `B` | 返回上一级 |

**互动**

1. 主菜单 →「互动」。
2. 搭讪模式（`设置 → 游戏模式: 搭讪`）下先搭讪：选一种风格 → 挑一句台词。她接受后才会跟着你。
   「沙盒」模式跳过这一步，直接可互动。
3. 互动菜单：聊天（近况 / 心情 / 目标…）、问她的偏好、试探一下、邀请上车、给钱、说再见、结束关系。
4. 好感度与关系等级决定她能接受什么；反复问同一个问题会掉好感，掉多了她会直接走人。
5. 主菜单 →「设置」：模组总开关、游戏模式、AI 模式、各类语音与效果开关。

**互动过程中**

| 按键 | 作用 |
|---|---|
| 十字键 ←→（长按） | 调速 |
| `X` | 速度复位 |
| **`B` + 十字键右** | 中止 |

菜单开启期间会临时屏蔽本模组的按键，避免和别的模组抢输入；镜头控制不受影响。

## 注意

- `combo` 版本需要先装「脏比洛圣都」（本仓库不含）；`cinnamon` 版本不需要。
- 两个版本的 `simple_zombies.nsc` 是同一个位置，只能装一个。
- 不要动这两个条目以外的文件。
- 产物结构、脚本名与 native 表都做过校验；游戏内表现未实机验证。

## 授权

本项目自己写的部分（移植码、文档）：**MIT**，见 [LICENSE](LICENSE)。

## Credit

- **CinnamonCoffee** —— 原始 GTA V PC 端（ScriptHookVDotNet）模组。
  本项目的玩法逻辑与大量台词来源于它（英文台词多为其原文，中文由本项目翻译/重写）。
  原作者与授权条件见其发布页，再分发前请先核对。
- **「脏比洛圣都」** —— 配套的僵尸模组，不包含在本仓库中。
