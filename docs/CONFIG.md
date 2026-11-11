> **本文档由 AI 生成。** 项目署名：**By Sunnbt**

# 配置项说明

配置文件：`.minecraft/config/sunnbtmods.cfg`
图形界面：主菜单 → **Mods** → **SunnBT Tools** → **Config**
改完点 Done 即生效（所有项都标为不需要重启游戏）。

---

## 分类 `autologin`（自动登录）

| 键 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enableAutoLogin` | 布尔 | `true` | 自动登录总开关。关闭后完全不监听聊天栏、不发任何指令。 |
| `password` | 字符串 | `sunnbt111` | 注册/登录用的密码。指令模板里的 `%password%` 会替换成它。 |
| `registerCommand` | 字符串 | `/register %password% %password%` | 检测到注册提示时发送的指令，`%password%` 自动替换。 |
| `loginCommand` | 字符串 | `/login %password%` | 检测到登录提示时发送的指令。 |
| `detectWindowSeconds` | 整数 | `40` | 进服后监听聊天多少秒，之后不再动作。 |
| `sendLoginAfterRegister` | 布尔 | `false` | `false` = 只要发过 `/register`，就**不再**发 `/login`；设 `true` 则在注册后约 3 秒补发一次登录。 |
| `maxLoginAttempts` | 整数 | `3` | 时间窗内最多发送几次 `/login`。 |
| `detectRegisterPrompt` | 布尔 | `true` | 是否识别注册提示（`/register`、`/reg`、`请注册`、`please register` 等）。 |
| `detectLoginPrompt` | 布尔 | `true` | 是否识别登录提示（`/login`、`/l`、`请登录`、`please login` 等）。 |
| `showChatMessages` | 布尔 | `true` | 本模组每次发指令时，是否在自己聊天栏打一行提示。 |

### 自动登录的行为细节

1. 时间窗从**进入世界**开始计时；`detectWindowSeconds` 到点后停止一切检测。
2. 聊天出现成功类提示（`successfully registered` / `successfully logged in` / `logged in successfully` / `you are now logged in` / `already logged in` / `注册成功` / `登录成功` / `已登录` 等）→ 立即停止检测。
3. 自己发出去的指令会在聊天栏回显，回显里含密码，会被跳过，不会死循环。
4. 服务器说 `already registered` / `已被注册` / `已被占用` → 直接改发 `/login`。
5. 发送条件：`enableAutoLogin=true`、在时间内、玩家对象存在、未判定成功。

---

## 分类 `autorename`（离线账号改名）

| 键 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enableAutoRename` | 布尔 | `true` | 离线改名相关功能总开关（按钮不显示、改名逻辑不执行）。 |
| `showAccountsButtonInMultiplayer` | 布尔 | `true` | 多人游戏服务器列表左上角是否显示 **Offline Accounts** 按钮。 |
| `showRenameButtonOnDisconnect` | 布尔 | `true` | 断线界面是否显示 **Rename & Rejoin** 按钮。 |
| `reconnectDelayTicks` | 整数 | `15` | 改名后等多少 tick 再重连（20 tick = 1 秒）。 |
| `minNameLength` | 整数 | `3` | 离线用户名最短长度，范围 3–16。 |
| `maxNameLength` | 整数 | `16` | 离线用户名最长长度，范围 3–16，且不会小于最短长度。 |
| `namePrefixes` | 字符串列表 | `Steve`、`Alex`、`Craft`… 共 20 个 | 随机名生成用的前缀，一行一个。随机名格式为 `前缀_5位随机`。 |

### 离线账号名单

- 路径：`.minecraft/config/autorename-accounts.txt`，UTF-8，一行一个名字。
- 写入时会按当前长度限制重新校验，非法名字（字符不在 `a-z A-Z 0-9 _`、长度越界）会被跳过。
- 重复名字（忽略大小写）不会重复添加。

### 改名与 UUID

- 离线 UUID：`UUID.nameUUIDFromBytes("OfflinePlayer:" + name)`（UTF-8），与原版离线模式一致。
- 改名时会替换客户端 `Session`（先尝试换对象，失败则逐字段改写），随后用原服务器的 `ServerData` 重新连接。

---

## 手动编辑示例

```cfg
autologin {
    B:enableAutoLogin=true
    S:password=sunnbt111
    S:registerCommand=/register %password% %password%
    S:loginCommand=/login %password%
    I:detectWindowSeconds=40
    B:sendLoginAfterRegister=false
    I:maxLoginAttempts=3
    B:detectRegisterPrompt=true
    B:detectLoginPrompt=true
    B:showChatMessages=true
}

autorename {
    B:enableAutoRename=true
    B:showAccountsButtonInMultiplayer=true
    B:showRenameButtonOnDisconnect=true
    I:reconnectDelayTicks=15
    I:minNameLength=3
    I:maxNameLength=16
    S:namePrefixes=Steve
    S:namePrefixes=Alex
    ...
}
```

游戏运行中直接改文件不会立即生效，用 Config 界面或重启游戏。
