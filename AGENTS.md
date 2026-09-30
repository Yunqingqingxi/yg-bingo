# AGENTS.md — Bingo 集卡（yg-bingo）开发规范

> 本包是 yunxigames 系列的玩法包之一。系列总览、公共约定与全系列踩坑速查见
> [yunxigames 文档仓库](https://github.com/Yunqingqingxi/yunxigames) 的 AGENTS.md（必读）。
> 本文件是本仓库开发者（人类与 AI）的入口，开工前通读。

## 1. 本包是什么

**Bingo 集卡**：物品 / 击杀双板集卡，5×5 板画在地图上，连线发奖。

- mod id：`yg_bingo`，jar：`yg-bingo-<版本>.jar`，配置：`config/yg-bingo.json`，入口 `YunxiGamesBingo`

### 类地图

| 类 | 职责 |
| --- | --- |
| `BingoConfig` | 本包全部配置项 + `validate()` 钳制 |
| `Bingos` | 双板集卡核心（目标抽取 / 打勾判定 / 地图绘制 / 连线发奖） |
| `BingoSelfTest` | 本包自检 |

### 三条设计底线 / 向后兼容承诺

1. 只在服务端做判定；2. 一局制、零持久化（板面状态全在内存，重启即清零）；
3. 物品不凭空消失。
mod id / jar 名 / 配置文件名 / lang key 永不改；配置字段只增不删；删字段 / 改默认行为升 major；
语义化版本 + GitHub Release 附 jar。

## 2. 环境（硬性）

| 组件 | 版本 |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.159.0+26.2 |
| **JDK** | **25**（本机 `D:\Java\jdk-25`，runServer/build 必须显式指定） |

一切 gradle 命令加 `--offline`。

## 3. 常用命令

```bash
./gradlew compileJava --offline            # 开发期每个功能写完就跑
./gradlew test --offline                   # 三层 JUnit 测试
./gradlew smokeTest --offline              # 只跑冒烟
JAVA_HOME='D:\Java\jdk-25' ./gradlew runServer --offline > selftest-<版本>.log 2>&1
JAVA_HOME='D:\Java\jdk-25' ./gradlew build --offline
```

- runServer 工作目录是本仓库自己的 `run/`（首次跑改 `run/eula.txt` 为 `eula=true`）；
- 自检前把 `run/config/yg-bingo.json` 的 `selfTestRolls` 改成 `200`，跑完**改回 `0`**；
- 自检完 runServer 不自退，手动结束 java 进程，否则 `run/` 被锁。

## 4. 代码规范

1. 一个功能一个类，类头 javadoc 写「是什么 + 为什么」；
2. 一切数值进本包 `BingoConfig`，带中文注释，每个功能独立开关（「爽但不劝退」）；
3. 新配置项必须在 `validate()` 钳制：`!(x >= lo && x <= hi)` 顺带治 NaN；
4. 面向 `ServerLevel` / `LivingEntity` 写逻辑，泛化签名（自检要在无玩家服务器复用）；
5. 中文注释 / 文案（§ 颜色码）/ lang 键值；
6. 26.2 API 不确定：**先查反混淆 jar，别猜**。

## 5. 测试节奏

- 三层 JUnit（Smoke / Unit / Regression）+ runServer 自检；批量开发期只跑 `compileJava`；
- **新增玩法功能必须同步新增自检项**并更新本包 README 的自检表；
- 配置测试基建：`YgConfig.configDirOverride`、`mergeMissingFields`（`raw.has` 判缺项补回）、
  `orDefaultIfNaN`；构造器与 `validate()` 包内可见是测试前提，别改回 private。

## 6. 本包专属坑（全系列公共坑见系列仓库 AGENTS §7）

- **地图绘制是数据驱动**：改的是 `MapItemSavedData` / `MapDecoration` 层，不动客户端渲染器；
  绘制逻辑要容忍「地图被重生成」（`MapItemSavedData` 失效时重建而非崩溃）；
- **目标抽取**复用本包 `LootSupply`（动态扫 `BuiltInRegistries`，mod 物品自动进池；
  抽样要排除空气 / 技术性物品，黑名单支持 `命名空间:*` 通配）；
- **击杀板**挂实体死亡事件，过滤 `instanceof ServerPlayer` 后再记功
  （击杀者可能不是玩家，如苦力怕炸死目标）；
- 连线判定是纯内存逻辑：行列 / 对角线扫描写成可单测的纯函数，别混进地图绘制代码；
- 板面 / 进度状态全在内存，**不要写任何存档**（一局制底线）。
