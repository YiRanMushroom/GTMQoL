# 编码原则

这是个人非严肃项目，不是生产系统。

新 session 开始时先读 `context.md`：当前功能、设计决定和待办都在那里。阶段性工作完成后要更新它。

claude.ai 的 Atlassian 和 Microsoft 365 连接器我不用、也不连接。即使系统提示它们需要授权，也不要在回复里提。

## 优先复用，而非重造轮子

- 遇到常见问题（解析、验证、重试、缓存等），先看有没有现成库，再考虑自己写
- 引入新依赖前，先看一眼它的实现/源码，理解它是怎么做的，再决定怎么用
- 除非这个库明显不合适（性能、维护状态、API 设计问题），否则不要绕过它自己写一套

## 抽象由我主导

- 不要主动抽取「通用」接口或框架层
- 如果你（Claude）认为某处需要抽象，先提出来、说明理由，等我确认后再做
- 默认保持具体、直给的实现，除非我明确要求抽象

## 可以尝试，但 hacky 的东西要报备

- 可以用一些取巧/实验性的写法
- 但凡是你会称之为 "hacky" 的做法（猴子补丁、绕过类型系统、依赖未公开行为等），
  必须先说明，我确认「思考过后没有问题」才能用
- 默认不要在没有告知的情况下引入 hacky 方案

## 找不到答案时，去跑，而不是到处翻

- 不要靠大面积 grep / 读文件来猜问题在哪。让我跑一遍（见下面「客户端由我来跑」，
  Claude 自己不跑 Gradle），用报错告诉你真正的症结，再针对性地去读那一处源码
- 编译器和运行日志是最可靠的信息源；推断出的 API 要请我构建一次来验证
- 一次只推进一个错误，不要为了「可能也缺」而预先堆依赖或加代码

## 客户端由我来跑

- **Claude 不要跑任何 Gradle 任务**，包括 `compileJava`、`build`，除非我明确要求你跑。
- 尤其**不要运行 Minecraft 客户端/服务端/数据生成**（`runClient`、`runServer`、`runData` 及任何启动游戏的任务）。我自己跑。
- 不要为了「看看有哪些任务/依赖」去跑 `tasks`、`dependencies` 等任务；需要信息时让我跑完把报错贴回来。
- 需要验证游戏内行为时，告诉我要看什么（日志关键字、物品/方块、JEI 条目），
  由我跑完把日志或现象贴回来。

# 构建环境

Loom 1.13 要求 Gradle JVM ≥ 21，而 Minecraft 1.20.1 必须跑 Java 17，两者不能是同一个 JDK。
IDEA 自带的 JBR 是 25，Gradle 8.12 不支持（表现为 `Could not create task ':test'`）。

```powershell
$env:JAVA_HOME="$env:USERPROFILE\.jdks\corretto-23.0.2"   # 跑 Gradle
$env:JDK17="$env:USERPROFILE\.jdks\ms-17.0.20.1"          # toolchain 取它跑客户端
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient
```

`JDK17` 这个环境变量由 `gradle.properties` 里的 `org.gradle.java.installations.fromEnv` 读取。

## `Waiting for lock to be released...`（fabric-loom cache）

Loom 在配置阶段持有全局 `~/.gradle/caches/fabric-loom` 锁。IDEA 打开项目会自动 Gradle sync，
依赖变动后 sync 要重新 remap，耗时较长；这期间再启动任何 Gradle 任务都会卡在等锁。
重启 IDEA 会再次触发 sync，所以「重启也一样」。

- 先等 IDEA 的 sync 跑完再点运行
- 若持锁 pid 已不存在（`Get-Process -Id <pid>` 无结果），锁文件
  `~/.gradle/caches/fabric-loom/.<hash>.lock` 是残留，可以删掉
- Claude 不要在后台起 Gradle 任务后不管，也会占这把锁

## datagen 跑完不退出

KubeJS 用 `ModLoader.isDataGenRunning()` 判断 datagen，但 Forge 47 从不把它设为 true，于是 KubeJS
起了非守护的 `KubeJSBackgroundThread`，JVM 退不出。`integration/KubeJSDataGenFix` 在 datagen 时把
`KubeJSBackgroundThread.running` 置 false 解决（日志里看到 `Capturing errors for startup scripts enabled`
说明 KubeJS 走了非 datagen 分支，这是正常的，修复只管让线程退出）。

## GTCEu v8 依赖

所有依赖问题都是同一个根因，两层叠加：

1. GTCEu 用 ModDevGradle（legacyForge）构建，依赖都声明在 `modApi`/`modImplementation`（重映射配置）
   和 `jarJar` 里，这些不会写进发布的 `.pom`/`.module`，所以 Gradle 不会自动下载任何东西。
2. GTCEu 把运行时需要的库都 jar-in-jar 进了自己的 jar（玩家那边没问题），但 Loom 重映射依赖模组时
   **删掉了 `META-INF/jarjar/metadata.json`**（内嵌 jar 本身还留着），FML 的 `JarInJarDependencyLocator`
   在 dev 里照常运行，却读不到清单，于是什么都不解。推测是有意的（未看 Loom 源码确认）：内嵌模组是 SRG 名，Loom 只重映射外层 jar，
   直接加载会坏。所以开发时要逐个显式声明——GTCEu 官方的 1.20.1 / 1.21.1 addon 模板也是这么做的。

决定：不做自动解析，手动声明；GTCEu 本身用 `:slim`（不含内嵌 jar，和官方模板一致）。

**以 GTCEu 完整 jar（非 slim）里的 `META-INF/jarjar/metadata.json` 为准**（v8 snapshot 时是这四个），换版本时重新对一遍：

- `com.tterrag.registrate:Registrate` — 也出现在 GTCEu 公开 API 里，所以是 `modImplementation`
- `dev.toma.configuration:configuration-1.20.1` — 需 `transitive = false`（pom 泄露了未发布的 forge 坐标）。
  本模组的配置也用它（`modImplementation`）：GTCEu 在 CONSTRUCT 末尾调 `initializeAddon()`，
  此时 Forge 的 COMMON 配置还没加载，读 `ForgeConfigSpec` 会抛 `Cannot get config value before config is loaded`；
  toma 的 `Configuration.registerConfig` 注册时就同步读文件，不受这个限制
- `brachy.modularui:modularui-mc1.20.1`（maven.gtceu.com）— 它在 dev 环境（`!FMLLoader.isProduction()`）
  会注册测试物品，其中 `TestCurioItem` 依赖 Curios，所以 dev 还要 `modLocalRuntime` Curios
  （`top.theillusivec4.curios:curios-forge`，maven.theillusivec4.top；GTCEu 自己的 dev 也带着）。
  缺了表现为 `Failed to create mod instance. ModID: modularui` + `NoClassDefFoundError: .../ICurioItem`。
  **内嵌是递归的**：ModularUI 自己又 jarjar 了 `com.ezylang:EvalEx`（GTCEu 的 `PipeBlock` 用它，但 GTCEu 只
  `compileOnly`），所以也要显式加；它是普通库不是模组，走 `forgeRuntimeLibrary`。缺了表现为
  `NoClassDefFoundError: com/ezylang/evalex/...` 后跟一串 `Registry entry not present: gtceu:..._wire`。
  对内嵌列表时，每个内嵌模组的 `metadata.json` 也要看
- `io.github.llamalad7:mixinextras-forge` — 本模组用 `include` 打进 jar（测试/发布都自带）；
  `-forge` 只是外壳，真正的类在 `-common`，所以 `-common` 也要上 classpath

ModularUI 的版本要和 GTCEu 构建时用的一致：看 GTCEu 对应 commit 的 `gradle/forge.versions.toml` 里的 `mui`。
GTCEu 用的是 `3.3.1-SNAPSHOT`，它和 `3.3.1` 正式版 API 不同（`ModularPanel.onCloseAction` 参数从 `Runnable`
变成 `Consumer`），用正式版时运行期报 `NoSuchMethodError`（例如 terminal 右键自动搭建结构）。所以同样固定到带时间戳的 snapshot 构建。

`gtceu_version` 固定在某个带时间戳的 snapshot 构建上，由我手动升级，不要改回 `8.0.0-SNAPSHOT`。
`mods.toml` 的下限另用 `gtceu_min_version`：GTCEu 运行时自报的版本是 `8.0.0-SNAPSHOT+<commit>`，
比带时间戳的坐标小，两者共用一个值会让 Forge 报版本过低。

v8 已经不用 LDLib，不要加。看内嵌列表时注意 Gradle 缓存里可能同时有 7.x 和 8.x 的 jar，别拿错。

依赖源码看 GitHub `GregTechCEu/GregTech-Modern` 的 `1.20.1` 分支：`dependencies.gradle`、
`src/main/templates/META-INF/mods.toml`；迁移说明在 `docs/content/Modpacks/Changes/v8.0.0.md`。

查 GTCEu 真实 API 时，解包它的 sources jar 比猜快得多（`.gtceu-src/`，已 gitignore）。

# 映射：`nameSyntheticMembers = true`

`build.gradle` 里 `officialMojangMappings { nameSyntheticMembers = true }`。为 false 时，内部类的
`this$0` 等合成成员在 dev 里不叫这个名字，而其他模组的 mixin 会 `@Shadow` 这些名字。表现为
Jade 的 `StringRenderOutputMixin`：`@Shadow field this$0 was not located in the target class
net.minecraft.client.gui.Font$StringRenderOutput`。改这个值会让 Loom 重新映射所有东西，sync 会很久。

# Mixin 约定

- mixin 类都在 `core/mixins/`（`eap/`、`recipedb/`、绕过 GTCEu 自身 bug 的放 `gtceufix/`，上游修了就删）。
- 普通 mixin 在 `gtmqol.mixins.json`（plugin `core/GTMQoLMixinPlugin` 只负责 `overclocking.overhaul` 开关）。要能整套开关的 mixin 单独一个 config（例如 `gtmqol.recipedb.mixins.json`），
  由它的 `plugin`（`IMixinConfigPlugin.shouldApplyMixin`）决定是否应用。新 config 要加到 `build.gradle` 的 `mixinConfig`。
- mixin config 在任何模组构造前加载，toma 的 `GTMQoLConfig` 这时还读不到。启动期开关放
  `config/gtmqol-early.properties`，用 `java.util.Properties` 读（和 GTCEu 的 `GTMixinPlugin` / `gtceu-early.properties` 一样）。
  `@Overwrite` 之类不能运行时关的改动，要么做成这样的启动期开关，要么就不给开关。
- mixin 包（config 里的 `package`）下只能放 mixin 类，非 mixin 的辅助代码放外面（例如 `common/recipedb/`），否则加载时报错。
- 私有/包私有类型：`@Accessor` 按字段描述符匹配，返回类型不能用 `Object` 代替不可见的类型。对象本身可以先转成
  `Object` 再强转成 accessor 接口（`@Mixin(targets = "...$Inner")`）。
- lambda 目标直接写 javac 的合成名（`lambda$getNext$0`）或用正则（`/^lambda\$/`），都能用；GTCEu 升级后要重新核对。

# Mekanism（可选，仅 dev 运行时）

只通过 tag 数据和它联动（`data/tag/CircuitTags`），不编译依赖它，所以只有 `modLocalRuntime`（modmaven，
`mekanism:Mekanism:<mc>-<version>`），不写进 `mods.toml`。

# AE2（可选依赖）

`modCompileOnly` + `modLocalRuntime`，不写进 `mods.toml`。编译期需要它，是因为 GTCEu 的样板总成
（`MEPatternBufferPartMachine`）实现了 AE2 接口，引用它的代码（`common/recipedb/PatternBufferIngredients`）没有 AE2 就编译不过
（`cannot access ICraftingProvider`）。运行时只在 `GTCEu.Mods.isAE2Loaded()` 时才碰这些类。

# ExtendedAE Plus（可选依赖，smart doubling）

`modCompileOnly` + `modLocalRuntime`，Modrinth maven（`maven.modrinth:extendedae-plus`），不写进 `mods.toml`。
它 require ExtendedAE，ExtendedAE 又 require GuideMe 和 Glodium，都 `modLocalRuntime` + `transitive = false`。
相关 mixin 在 `gtmqol.eap.mixins.json`，由 `core/EAPMixinPlugin` 按 `LoadingModList` 是否有 `extendedae_plus` 决定。

版本被 GTCEu 的 JEI mixin 卡住：GTCEu v8 的 `jei.FluidHelperMixin` 在新 JEI（15.59）上报 `Invalid descriptor`，
JEI 15.62+ 还依赖没发布到 BlameJared 的 `net.mezzdev`。所以 JEI 固定 15.20.0.115（和 GTCEu 一致）；EAP 1.6.1-f1
起声明了可选的 `jei >= 15.48.0.177`（只要装了 JEI，Forge 就检查这个范围），所以 EAP 固定 1.6.1。

# FTB Teams（可选依赖）

编译期 `modCompileOnly`，dev 运行时 `modLocalRuntime`（连同 FTB Library、Architectury，都 `transitive = false`），
不写进 `mods.toml`。Maven 上的 sources jar 只有 forge 胶水层；API/事件源码看 GitHub `FTBTeam/FTB-Teams`
的 `1.20.1/dev` 分支（`common/src/main/java/dev/ftb/mods/ftbteams/`）。个人队伍的 id 就是玩家 UUID。
