package com.betterdo.app.data.seed

import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.domain.model.Toned
import com.betterdo.app.domain.model.Subtask

/**
 * Sample-day content + curated AI copy, ported from the design mockup's `data.jsx`.
 * Every AI string is authored in all three tones (gentle / coach / savage). The
 * mock agent ([com.betterdo.app.agent.MockAgentService]) reads from here.
 */
object SeedData {

    const val WEEKDAY = "周二"
    const val DATE_LABEL = "6月3日"
    const val AI_NAME = "Dodo"
    const val USER_NAME = "我"

    private fun ai(id: String, body: Toned, time: String, likes: Int = 0) =
        Comment(id = id, fromAi = true, authorName = AI_NAME, body = body, time = time, likes = likes)

    private fun usr(id: String, text: String, time: String, likes: Int = 0) =
        Comment(id = id, fromAi = false, authorName = USER_NAME, body = Toned.plain(text), time = time, likes = likes)

    fun seedTodos(now: Long): List<Todo> = listOf(
        Todo(
            id = "t1", title = "季度 OKR 复盘文档", note = "交给 Lena 之前自己先过一遍",
            time = "14:00", tag = Tag.WORK, icon = TodoIcon.TARGET, priority = true, streak = 0,
            source = TodoSource.USER, createdAt = now, position = 0,
            subtasks = listOf(
                Subtask("s1", "写「关键成果」回顾", done = true, byAi = true),
                Subtask("s2", "补 3 个掉链子的指标 + 原因", done = false, byAi = true),
                Subtask("s3", "下季度 1 个聚焦目标", done = false, byAi = true),
            ),
            comments = listOf(
                ai(
                    "c-t1-1",
                    Toned(
                        gentle = "这份你已经放了两天啦~ 别有压力，先花 15 分钟把框架搭起来就好。我帮你拆成了 3 小步，点开就能填 ☺︎",
                        coach = "今天唯一的高杠杆任务。建议 14:00 前交付：现在开免打扰，用 25 分钟先写完「关键成果」。已替你拆成 3 步。",
                        savage = "OKR 复盘？你「明天再说」已经说了两天了。再拖，周三晚上就是你和这份文档过夜。我拆成 3 步了，别找借口。",
                    ),
                    time = "9:31", likes = 2,
                ),
            ),
        ),
        Todo(
            id = "t2", title = "推日训练 · 公司健身房", note = "胸 + 三头，45 分钟",
            time = "19:00", tag = Tag.HEALTH, icon = TodoIcon.DUMBBELL, streak = 6,
            source = TodoSource.USER, createdAt = now + 1, position = 1,
            comments = listOf(
                ai(
                    "c-t2-1",
                    Toned(
                        gentle = "已经连续 6 天啦，好厉害！今晚也轻轻推你一把，去拉伸一下就当奖励自己~",
                        coach = "连续打卡第 7 天的门槛最危险。19:00 准时去，只做 3 个核心动作也算数。",
                        savage = "健身卡这个月去了 2 次。今天再不去，下个月你又要在朋友圈立 flag 了。",
                    ),
                    time = "9:31",
                ),
            ),
        ),
        Todo(
            id = "t3", title = "给妈回个电话", note = "上周说好的",
            time = null, tag = Tag.LIFE, icon = TodoIcon.PHONE,
            source = TodoSource.USER, createdAt = now + 2, position = 2,
            comments = listOf(
                ai(
                    "c-t3-1",
                    Toned(
                        gentle = "不用很正式，路上走着就能打。她大概率只是想听听你的声音~",
                        coach = "5 分钟的事，往往拖成一周的心理负担。午饭后顺手打掉它。",
                        savage = "“等会儿打” = 又一周没打。现在通话键就在那，按下去会死吗？",
                    ),
                    time = "9:32",
                ),
            ),
        ),
        Todo(
            id = "t4", title = "Review 小杰的 PR #482", note = "feat: 离线缓存",
            time = null, tag = Tag.WORK, icon = TodoIcon.CODE,
            source = TodoSource.USER, createdAt = now + 3, position = 3,
            comments = listOf(
                usr("c-t4-1", "今天有点满，可能得放到明天", "9:18"),
                ai(
                    "c-t4-2",
                    Toned(
                        gentle = "理解的~ 那我帮你标成「明天上午」第一件，并提醒小杰别急着合并，好吗？",
                        coach = "阻塞别人的任务优先级要上调。建议今天花 15 分钟过一遍主干逻辑，细节明天。已置顶。",
                        savage = "“放明天”——小杰的 PR 已经挂 2 天了，你这是在养蘑菇。15 分钟，先扫一遍主逻辑。",
                    ),
                    time = "9:19", likes = 1,
                ),
            ),
        ),
        Todo(
            id = "t5", title = "买猫粮", note = "只剩 2 天的量了",
            time = null, tag = Tag.LIFE, icon = TodoIcon.CART,
            source = TodoSource.AI, sourceNote = "你昨天提到「猫粮快没了」，我帮你立成待办",
            createdAt = now + 4, position = 4,
            comments = listOf(
                ai(
                    "c-t5-1",
                    Toned(
                        gentle = "帮你查了，常买那款今天下单明天就到，要我把链接放上来吗？",
                        coach = "订阅式自动续购能彻底解决这件事，省得每月手忙脚乱。要不要设一下？",
                        savage = "上次断粮主子绝食抗议的样子还记得吗？现在下单，别等它再蹬翻你的水杯。",
                    ),
                    time = "9:30",
                ),
            ),
        ),
        Todo(
            id = "t6", title = "读《思考，快与慢》30 页", note = "睡前习惯",
            time = "22:30", tag = Tag.HABIT, icon = TodoIcon.BOOK, streak = 12,
            source = TodoSource.USER, createdAt = now + 5, position = 5,
        ),
        Todo(
            id = "t7", title = "预约牙医复查", note = "上次说好 3 个月后，已经过去 11 周",
            time = null, tag = Tag.LIFE, icon = TodoIcon.PIN,
            source = TodoSource.AI, sourceNote = "翻你 3 月的待办时发现的，到期啦",
            createdAt = now + 6, position = 6,
            comments = listOf(
                ai(
                    "c-t7-1",
                    Toned(
                        gentle = "不着急，但趁现在好约。我可以帮你拟一条预约短信，你点发送就行~",
                        coach = "小事拖成牙疼就贵了。现在打一个电话，本季度的健康项就清零。",
                        savage = "11 周了。你的牙不会自己长好，但账单会自己变大。打电话。",
                    ),
                    time = "9:30", likes = 3,
                ),
            ),
        ),
    )

    /** Pre-completed items shown on the review screen. */
    fun doneItems(now: Long): List<Todo> = listOf(
        Todo(id = "d1", title = "晨间 routine · 喝水 + 拉伸", tag = Tag.HABIT, icon = TodoIcon.SUN,
            done = true, source = TodoSource.USER, createdAt = now - 2, position = 100),
        Todo(id = "d2", title = "回复客户的合同邮件", tag = Tag.WORK, icon = TodoIcon.CODE,
            done = true, source = TodoSource.USER, createdAt = now - 1, position = 101),
    )

    // ---- toned narration ----
    val greeting = Toned(
        gentle = "早安~ 今天 7 件事，咱们慢慢来，我都在",
        coach = "早。今天 7 件待办，1 件高杠杆。先攻最重要的那件。",
        savage = "醒了？昨天拖下来 2 件还瞪着你呢。今天 7 件，少装死。",
    )

    val brief = Toned(
        gentle = "我顺手帮你加了 2 件「别忘了」的小事，还在 OKR 上拆了 3 步~ 不急，挑顺手的先做。",
        coach = "今日扫描完成：新增 2 项到期事务，OKR 已拆成 3 步。建议顺序——OKR → PR → 健身。",
        savage = "帮你揪出 2 件你装看不见的事，OKR 也拆好了。别谢我，去做。",
    )

    val reviewSummary = Toned(
        gentle = "今天你完成了 5 件，已经很棒啦~ 剩下 2 件我帮你轻轻挪到明天，别有负担。明天见 ☺︎",
        coach = "完成率 71%，高于你近 7 天均值。高杠杆任务已清。2 件顺延，明早第一时间处理。",
        savage = "5/7。比昨天强，但 OKR 你又留到明天了——这是第三天。明天它要是还在，我可不客气了。",
    )

    val replyAck = Toned(
        gentle = "收到~ 那我帮你挪到顺手的时段，你按自己的节奏来，我都在 ☺︎",
        coach = "记录了。那就先做能立刻推进的那一步——回来打勾就行。",
        savage = "行行行，借口先给你存着。等下没做完，这段聊天记录我可留着。",
    )

    val quickAddAck = Toned(
        gentle = "记下啦~ 要不要我帮你定个提醒时间？这样就不会忘了 ☺︎",
        coach = "已收。给它配个具体时间点，完成率能翻倍——要现在设吗？",
        savage = "又一条。光记不做等于没记。说个时间，几点干？",
    )

    val cheer = mapOf(
        "gentle" to listOf("做到了！为你开心~", "又划掉一件，轻松一点啦", "你看，没那么难对吧 ☺︎"),
        "coach" to listOf("+1。节奏不错，继续。", "高杠杆任务清零，今天稳了。", "完成。下一件，保持。"),
        "savage" to listOf("哟，居然做了？刮目相看。", "行，这件不啰嗦你了。", "完成一件，剩下的别又装死。"),
    )

    // ---- reminders (notifications) ----
    data class ReminderSeed(
        val id: String, val todoId: String, val icon: TodoIcon,
        val title: String, val time: String, val body: Toned,
    )

    val reminders = listOf(
        ReminderSeed(
            "r1", "t2", TodoIcon.DUMBBELL, "推日训练", "19:00",
            Toned(
                gentle = "19:00 啦，该去撸铁咯 💪 昨天的你超棒，今天也轻轻推你一把~",
                coach = "推日训练 · 19:00。连续 6 天打卡，别在第 7 天掉链子。45 分钟，走起。",
                savage = "19:00。健身卡这月去了 2 次。今天不去，明天镜子里那人会继续埋怨你。",
            ),
        ),
        ReminderSeed(
            "r2", "t1", TodoIcon.TARGET, "OKR 复盘 · 还有 2 小时", "12:00",
            Toned(
                gentle = "离 14:00 还有 2 小时～ 先写「关键成果」那段就好，我陪你。",
                coach = "距 deadline 2 小时。现在开始还来得及，先攻第一块，25 分钟。",
                savage = "2 小时后要交，你还没动笔。再刷手机，14:01 你就懂什么叫真香。",
            ),
        ),
        ReminderSeed(
            "r3", "t3", TodoIcon.PHONE, "给妈回个电话", "13:10",
            Toned(
                gentle = "午饭吃了吗？顺手给妈打个电话吧，她在等你呢~",
                coach = "午休是打这通电话的黄金窗口。5 分钟，现在最合适。",
                savage = "又到午休了，又想假装忘了对吧？通话键，按下去。",
            ),
        ),
    )

    // ---- model-in-the-loop derived suggestions ----
    data class DerivedSeed(
        val id: String, val source: String, val srcIcon: TodoIcon,
        val title: String, val tag: Tag, val why: Toned,
    )

    val derivedBatch1 = listOf(
        DerivedSeed("g1", "季度 OKR 复盘", TodoIcon.TARGET, "把复盘同步给 Lena · 约 15min", Tag.WORK,
            Toned("复盘写完一般要对齐一下，我先帮你占个坑～", "下一步逻辑：自评 → 对齐。复盘后顺手约最省事。", "写完不同步等于白写，又想自我感动？")),
        DerivedSeed("g2", "季度 OKR 复盘", TodoIcon.TARGET, "导出 Q1 指标看板截图", Tag.WORK,
            Toned("写「关键成果」会用到数据，先备着不慌～", "复盘需要数据支撑，提前导出省得临时翻后台。", "到时候找不到数据，又得现翻三个后台。")),
        DerivedSeed("g3", "推日训练", TodoIcon.DUMBBELL, "周四加一次拉伸 / 恢复", Tag.HEALTH,
            Toned("连续推日辛苦啦，给膝盖也放个假～", "连训 7 天需主动恢复，排一次低强度日。", "天天推不拉伸，迟早躺床上后悔。")),
        DerivedSeed("g4", "买猫粮", TodoIcon.CART, "顺手把猫砂也买了", Tag.LIFE,
            Toned("上次它俩一起见底的，一起下单更省事～", "同类耗材合并采购，少一次决策成本。", "只买粮不买砂，三天后你还得再下一单。")),
    )

    val derivedBatch2 = listOf(
        DerivedSeed("g5", "Review PR #482", TodoIcon.CODE, "合并后通知测试同学", Tag.WORK,
            Toned("合完记得喊一声测试，他们好接上～", "PR 合并的收尾动作，别让流程断在你这。", "合完就跑，测试一脸懵，经典甩锅预备。")),
        DerivedSeed("g6", "给妈回电话", TodoIcon.PHONE, "周末订束花寄回家", Tag.LIFE,
            Toned("她下周生日呀，提前订从容些～", "她生日临近，提前下单可选当日达。", "她生日你又想发个红包了事？走点心。")),
    )

    // ---- DeriveHow explainer steps ----
    data class HowStep(val n: String, val title: String, val body: String)

    val howSteps = listOf(
        HowStep("1", "读上下文", "每天看一遍你写的事 + 完成历史，理解意图，而不是只匹配关键词。"),
        HowStep("2", "顺下一步", "为每件事推理“做完它通常还要做什么”，连回来源生成建议。"),
        HowStep("3", "你拍板", "加入或忽略都行。你的选择会调教它，下次更懂你的口味。"),
    )
}
