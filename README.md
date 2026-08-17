# CobbleBlaze

> 将火属性宝可梦变成 Create 的燃烧动力，让宝可梦工厂真正“燃烧”起来。

[English README](README_EN.md)

## 游戏介绍

CobbleBlaze 是一款基于 **Cobblemon** 与 **Create** 开发的宝可梦烈焰燃烧室模组。模组新增了一个独立的宝可梦烈焰燃烧室，玩家可以从 Cobblemon 队伍中选择火属性宝可梦，将它放入燃烧室，为 Create 的机械设备和锅炉提供热量。

放入的宝可梦会完整保留自身数据，包括种族、形态、个体信息、招式和其他 Cobblemon 数据，并使用 Cobblemon 自己的模型显示在燃烧室内部。破坏一个已放入宝可梦的燃烧室时，宝可梦数据会随燃烧室一起保存；重新放置后可以恢复原来的宝可梦。潜行右键燃烧室即可将宝可梦取回队伍。

本模组是为 **天空宝可梦工厂** 整合包开发的附属模组之一，欢迎各位玩家游玩体验。感谢整合包作者愿意将模组开源并公开，也感谢 **Horrrs** 老大的支持。

## 核心功能

- **专用宝可梦烈焰燃烧室**：使用独立方块，不会改变或占用 Create 原版烈焰燃烧室的宝可梦逻辑。
- **队伍选择界面**：空手右键燃烧室会打开 Cobblemon 队伍选择界面，最多从当前队伍中选择一只符合条件的宝可梦。
- **火属性限制**：默认只允许至少拥有火属性的宝可梦进入燃烧室，并可通过配置文件设置黑名单或白名单。
- **完整数据保存**：宝可梦离开队伍后，其完整 NBT 数据会保存在燃烧室中，取回时恢复为原来的宝可梦。
- **宝可梦模型渲染**：直接调用 Cobblemon 模型显示宝可梦，不替换资源，也不会显示 Create 的烈焰人头部。
- **可配置的持续热量**：高属性宝可梦可以在不消耗普通燃料的情况下持续提供配置的常规热量等级。
- **属性影响锅炉效率**：燃烧室的锅炉热量会根据宝可梦六项战斗属性总和获得额外倍率。
- **可搬运**：破坏已占用燃烧室、将其装入移动结构或重新放置时，宝可梦数据都会随之保留。
- **机械臂支持**：Create 机械臂可以识别宝可梦烈焰燃烧室并向其中插入燃料。
- **Create Crafts & Additions 兼容**：在 NeoForge 且安装 CCA 时，可以使用吸管将已占用的宝可梦烈焰燃烧室转换为带液体储罐的版本。

## 使用方法

### 放入宝可梦

1. 将宝可梦烈焰燃烧室放置在世界中。
2. 确保目标宝可梦在玩家当前队伍中，并且玩家手上没有物品。
3. 空手右键燃烧室，打开 Cobblemon 队伍选择界面。
4. 选择一只符合火属性和配置限制的宝可梦。

宝可梦被放入后会从玩家队伍中移除，并显示在燃烧室内部。一个燃烧室只能容纳一只宝可梦。

### 取回宝可梦

手持空手并按住潜行键，右键已占用的燃烧室即可取回宝可梦。取回时玩家队伍必须有空位；如果队伍已满，宝可梦会继续留在燃烧室中。

### 添加燃料

非空手右键会保留 Create 烈焰燃烧室原本的燃料交互，因此可以继续使用 Create 支持的普通燃料和特殊燃料。属性达到无限燃烧阈值的宝可梦不会消耗普通燃料，但特殊燃料仍可用于进入 `seething`（炽热）等级。

### 携带与移动

已占用的燃烧室可以直接挖掘并作为物品携带。物品提示会显示其中保存的宝可梦名称。将燃烧室安装到 Create 移动结构或列车上时，模型会跟随结构移动；移动结构上的模型位置可能需要通过配置微调。

## 热量规则

宝可梦烈焰燃烧室使用 Create 的热量等级。默认情况下，满足属性阈值的火属性宝可梦会持续提供 `kindled`（点燃）等级的常规热量；如果配置中将默认等级设为 `seething`，无限燃烧逻辑会自动降为 `kindled`，因为 `seething` 仍然属于需要特殊燃料维持的超热等级。

无限燃烧条件由宝可梦的六项战斗属性总和决定：最大生命值、攻击、防御、特攻、特防和速度的总和必须 **严格大于** `infiniteBurningStatThreshold`。未达到阈值时，燃烧室仍然可以正常使用 Create 燃料，但不会获得无限燃烧效果。

锅炉热量还会按照以下配置获得属性倍率：

```text
热量倍率 = 1 + 六项属性总和 × boilerHeatBonusPercentPerStat ÷ 100
```

## Create Crafts & Additions 兼容

在 **NeoForge 1.21.1** 中安装 Create Crafts & Additions（CCA）后：

- 对一个已经放入宝可梦的宝可梦烈焰燃烧室使用 CCA 吸管，可以将它转换为带液体储罐的“带吸管的宝可梦烈焰燃烧室”。
- 转换过程会保留其中的完整宝可梦数据、属性总和和模型显示信息。
- 转换后的燃烧室同时保留 CCA 的液体处理能力，并可继续作为 Create 的锅炉热源和风扇处理催化剂使用。
- 普通的 CCA 液体烈焰燃烧室不能直接通过右键放入宝可梦；宝可梦只能从已占用的普通燃烧室转换而来。
- 从带吸管版本取回宝可梦后，方块会恢复为普通的宝可梦烈焰燃烧室。

CCA 兼容内容是可选的。未安装 CCA 时，CobbleBlaze 仍可正常使用普通宝可梦烈焰燃烧室；Fabric 版本不会注册 CCA 专属方块。

## 配置文件

首次启动后，配置文件会生成在：

```text
config/cobbleblaze.json
```

修改配置后重新启动游戏或服务器即可生效。物种可以使用完整 ID（例如 `cobblemon:slugma`）或物种路径（例如 `slugma`）填写。

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `defaultHeatLevel` | `kindled` | 宝可梦占用燃烧室时使用的默认常规热量等级。可填写 `none`、`smouldering`、`kindled` 或 `seething`，也支持对应数字索引。 |
| `speciesHeatLevels` | `{}` | 按物种覆盖热量等级的对象，例如 `{ "cobblemon:slugma": "kindled" }`。 |
| `blacklistedSpecies` | `[]` | 永远禁止放入的物种列表，优先级高于其他允许规则。 |
| `allowAnyFireType` | `true` | 为 `true` 时允许所有火属性宝可梦；为 `false` 时仅允许 `whitelistedSpecies` 中的物种。 |
| `whitelistedSpecies` | `[]` | `allowAnyFireType` 为 `false` 时使用的白名单。 |
| `infiniteBurningStatThreshold` | `1000` | 六项战斗属性总和必须严格大于此值，才会触发普通燃料无限燃烧。 |
| `boilerHeatBonusPercentPerStat` | `0.2` | 六项属性总和对锅炉热量的额外加成百分比。每一点属性增加 `0.2%`，默认值下属性总和为 1000 时倍率为 `3.0`。 |
| `modelScale` | `0.5` | 宝可梦模型的全局缩放倍率，会乘以物种自身的基础缩放值。 |
| `modelYOffset` | `0.55` | 宝可梦模型在燃烧室中的垂直偏移量，单位为方块。 |
| `modelRotation` | `0.0` | 宝可梦模型的水平旋转角度，单位为度。 |

示例：只允许 Slugma 和 Magcargo，并让 Slugma 使用 `kindled` 热量：

```json
{
  "defaultHeatLevel": "kindled",
  "speciesHeatLevels": {
    "cobblemon:slugma": "kindled"
  },
  "blacklistedSpecies": [],
  "allowAnyFireType": false,
  "whitelistedSpecies": [
    "cobblemon:slugma",
    "cobblemon:magcargo"
  ],
  "infiniteBurningStatThreshold": 1000,
  "boilerHeatBonusPercentPerStat": 0.2,
  "modelScale": 0.5,
  "modelYOffset": 0.55,
  "modelRotation": 0.0
}
```

## 物品与方块 ID

| 内容 | ID | 说明 |
| --- | --- | --- |
| 宝可梦烈焰燃烧室 | `cobbleblaze:pokemon_blaze_burner` | 基础版本，所有平台可用。 |
| 带吸管的宝可梦烈焰燃烧室 | `cobbleblaze:pokemon_liquid_blaze_burner` | CCA 兼容版本，仅在 NeoForge 且安装 CCA 时注册。 |

基础燃烧室会加入 Minecraft 的“功能方块”创造标签页。当前版本没有额外的自定义合成配方，生存模式中的获取方式请以整合包配置为准。

## 安装要求

- Minecraft **1.21.1**
- Java **21** 或更高版本
- Cobblemon **1.7.1** 或更高版本
- Create **6.0.10** 或更高版本
- Flywheel（随对应 Create 版本提供）
- Architectury API **13.0.8** 或更高版本
- Fabric：Fabric Loader **0.18.1** 或更高版本及 Fabric API
- NeoForge：NeoForge **21.1** 系列
- 可选：Create Crafts & Additions（仅 NeoForge，用于吸管和液体燃烧室兼容）

请确保 CobbleBlaze、Cobblemon、Create、Flywheel、Ponder 以及加载器版本属于同一 Minecraft 版本。不同平台请使用对应平台的 Cobblemon、Create 和依赖文件。

## 安装方法

1. 安装与 Minecraft 1.21.1 匹配的 Fabric 或 NeoForge 实例。
2. 将 Cobblemon、Create、Architectury API、平台依赖和 CobbleBlaze 放入 `mods` 文件夹。
3. 如果使用 NeoForge 并需要液体燃烧室兼容，再安装 Create Crafts & Additions。
4. 启动游戏，进入“功能方块”创造标签页或使用物品 ID 检查模组是否加载成功。

## 从源码构建

项目使用 Architectury，同时提供 Fabric 和 NeoForge 构建目标。Windows 可以运行：

```powershell
.\gradlew.bat :fabric:remapJar :neoforge:remapJar
```

Linux、macOS 或 Git Bash 可以运行：

```bash
./gradlew :fabric:remapJar :neoforge:remapJar
```

构建产物位于：

```text
fabric/build/libs/cobbleblaze-fabric-1.0.jar
neoforge/build/libs/cobbleblaze-neoforge-1.0.jar
```

编译所需的 Create、Flywheel 和 Ponder 文件位于 `common/libs/`。运行时仍需在实例中安装 Cobblemon、Create 及其对应依赖；构建目录中的依赖文件不等于最终游戏实例的完整运行环境。

## 当前版本限制

- 只能从玩家当前队伍（最多 6 只）选择宝可梦，暂不支持直接从 PC 盒子选择。
- 只有火属性宝可梦可以放入燃烧室；黑名单、白名单和 `allowAnyFireType` 还会进一步限制可选范围。
- `seething` 超热等级仍然需要 Create 的特殊燃料，例如烈焰蛋糕或乙醇；无限燃烧不会永久维持该等级。
- 移动结构和列车上的模型使用独立渲染路径，个别大型或特殊体型宝可梦可能需要调整 `modelScale` 与 `modelYOffset`。
- CCA 兼容功能为 NeoForge 1.21.1 专属，Fabric 版本会跳过相关内容。
- 当前版本没有 PC 选择界面，也没有额外的自定义合成配方。

## 开源与授权说明

CobbleBlaze 是免费公开源码的项目，专为天空宝可梦工厂整合包提供支持。请从正规渠道获取模组；如果某个平台将本模组包装成付费内容，请谨慎辨别，避免受骗。

本项目附带的具体使用、修改和再发布权限以仓库中的 [LICENSE.txt](LICENSE.txt) 为准。

## 致谢

- 感谢 Cobblemon、Create、Architectury、Flywheel、Ponder 和 Create Crafts & Additions 的开发者与维护者。
- 感谢天空宝可梦工厂整合包作者公开并支持本模组。
- 特别感谢 **Horrrs** 老大的支持。

