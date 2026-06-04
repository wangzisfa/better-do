package com.betterdo.app.agent

import com.betterdo.app.data.seed.SeedData
import com.betterdo.app.domain.model.AgentTone
import com.betterdo.app.domain.model.Comment
import com.betterdo.app.domain.model.DerivedSuggestion
import com.betterdo.app.domain.model.ReviewStats
import com.betterdo.app.domain.model.Subtask
import com.betterdo.app.domain.model.Tag
import com.betterdo.app.domain.model.Todo
import com.betterdo.app.domain.model.TodoIcon
import com.betterdo.app.domain.model.TodoSource
import com.betterdo.app.domain.model.Toned
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Deterministic, offline agent for v1. Returns the design's curated copy for the
 * seed day and falls back to simple per-tag/tone templates for user-added todos.
 * Short delays reproduce the design's "Agent 正在读你的清单…" scanning beat.
 */
class MockAgentService : AgentService {

    private val clock = DateTimeFormatter.ofPattern("H:mm")

    override suspend fun parseList(raw: String): List<Todo> {
        delay(1100) // the design's "Agent 正在读你的清单…" scanning beat
        val now = System.currentTimeMillis()
        return cleanLines(raw).mapIndexed { i, line ->
            val (time, rest) = extractTime(line)
            val title = rest.ifBlank { line }
            val (tag, icon) = classify(title)
            Todo(
                id = uid(),
                title = title,
                time = time,
                tag = tag,
                icon = icon,
                priority = looksUrgent(line),
                source = TodoSource.USER,
                createdAt = now + i,
                position = i,
            )
        }
    }

    override suspend fun derive(today: List<Todo>, round: Int): List<DerivedSuggestion> {
        delay(900)
        val seeds = when (round) {
            1 -> SeedData.derivedBatch1
            2 -> SeedData.derivedBatch2
            else -> emptyList()
        }
        return seeds.map {
            DerivedSuggestion(
                id = it.id, sourceTitle = it.source, sourceIcon = it.srcIcon,
                title = it.title, tag = it.tag, why = it.why,
            )
        }
    }

    override suspend fun splitIntoSubtasks(todo: Todo): List<Subtask> {
        delay(700)
        // Faithful for the OKR seed; generic 3-step scaffold otherwise.
        if (todo.id == "t1") {
            return listOf(
                Subtask(uid(), "写「关键成果」回顾", byAi = true),
                Subtask(uid(), "补 3 个掉链子的指标 + 原因", byAi = true),
                Subtask(uid(), "下季度 1 个聚焦目标", byAi = true),
            )
        }
        return listOf(
            Subtask(uid(), "理清目标，先迈出最小的一步", byAi = true),
            Subtask(uid(), "动手做核心的那部分", byAi = true),
            Subtask(uid(), "收尾、检查，划掉它", byAi = true),
        )
    }

    override suspend fun commentOn(todo: Todo): Comment {
        delay(500)
        val title = todo.title
        return Comment(
            id = uid(),
            fromAi = true,
            authorName = SeedData.AI_NAME,
            body = Toned(
                gentle = "「$title」记下啦~ 想先从最小的一步开始吗？我陪你 ☺︎",
                coach = "「$title」已收。给它配个具体时间点，更容易落地。",
                savage = "「$title」？写了就别供着——定个时间，几点干？",
            ),
            time = LocalTime.now().format(clock),
            likes = 0,
        )
    }

    override fun greeting(tone: AgentTone) = SeedData.greeting[tone]
    override fun morningBrief(tone: AgentTone) = SeedData.brief[tone]
    override fun reviewSummary(stats: ReviewStats, tone: AgentTone) = SeedData.reviewSummary[tone]
    override fun replyAck(tone: AgentTone) = SeedData.replyAck[tone]
    override fun quickAddAck(tone: AgentTone) = SeedData.quickAddAck[tone]

    override fun completionReaction(tone: AgentTone, seed: Int): String {
        val list = SeedData.cheer[tone.key] ?: SeedData.cheer.getValue("coach")
        return list[(seed % list.size + list.size) % list.size]
    }

    private fun uid() = "u-" + UUID.randomUUID().toString().take(8)

    // ---- list parsing (deterministic stand-in for a real model) ----

    /** Leading bullet / number / checkbox markers to peel off each line.
     *  Note: the number separators deliberately exclude ":" so a leading time
     *  like "14:00 ..." is kept for [extractTime] rather than eaten as a "14."-style bullet. */
    private val markerRegex =
        Regex("""^\s*(?:[-*•·–—◦▪]+\s*)?(?:\[[ xX✓]?]|[□☐✓✔])?\s*(?:\d+[.)、]\s*)?""")

    // 14:00 / 9：30
    private val colonTime = Regex("""(\d{1,2})[:：](\d{2})""")
    // 下午3点 / 晚上8点半 / 早上7点30分 / 9点
    private val cnTime =
        Regex("""(凌晨|清晨|早上|上午|中午|正午|下午|傍晚|晚上|夜里|早|晚|夜)?\s*(\d{1,2})\s*[点时](半|[0-5]?\d\s*分)?""")

    /** Period words that push an hour into the afternoon/evening. */
    private val pmPeriods = setOf("中午", "正午", "下午", "傍晚", "晚上", "晚", "夜里", "夜")

    private data class Rule(val tag: Tag, val icon: TodoIcon, val keys: List<String>)

    /** Ordered keyword → tag/icon rules; the first rule with a hit wins. */
    private val rules = listOf(
        Rule(Tag.HEALTH, TodoIcon.DUMBBELL, listOf("健身", "训练", "撸铁", "跑步", "晨跑", "夜跑", "拉伸", "游泳", "瑜伽", "健身房", "有氧", "深蹲", "卧推", "骑行", "打球", "hiit")),
        Rule(Tag.HEALTH, TodoIcon.PIN, listOf("牙医", "体检", "看病", "医院", "挂号", "复查", "门诊", "吃药", "疫苗", "预约")),
        Rule(Tag.HEALTH, TodoIcon.SUN, listOf("喝水", "早睡", "早起", "冥想", "作息", "泡脚")),
        Rule(Tag.LIFE, TodoIcon.CART, listOf("买", "购", "采购", "下单", "超市", "快递", "取件", "猫粮", "狗粮", "囤")),
        Rule(Tag.LIFE, TodoIcon.PHONE, listOf("打电话", "回电话", "回个电话", "打给", "回电", "致电", "电话", "联系", "妈", "爸", "父母", "家人", "奶奶", "爷爷", "外婆", "外公")),
        Rule(Tag.WORK, TodoIcon.TARGET, listOf("okr", "kpi", "目标", "复盘", "季度", "述职", "绩效", "北极星")),
        Rule(Tag.WORK, TodoIcon.CODE, listOf("pr", "review", "代码", "bug", "部署", "上线", "merge", "提测", "联调", "接口", "重构")),
        Rule(Tag.WORK, TodoIcon.CALENDAR, listOf("会议", "开会", "对齐", "面试", "评审", "汇报", "路演", "周会", "站会", "双周会")),
        Rule(Tag.WORK, TodoIcon.BOOK, listOf("文档", "报告", "方案", "邮件", "ppt", "周报", "日报", "提案", "需求", "总结", "纪要")),
        Rule(Tag.HABIT, TodoIcon.BOOK, listOf("读", "阅读", "看书", "背单词", "写日记", "日记", "打卡", "练琴", "学习", "上课", "笔记", "单词")),
    )

    private val urgentKeys = listOf("!", "！", "重要", "紧急", "必须", "deadline", "ddl", "最重要", "asap", "急")

    /** Split a pasted / hand-written blob into one trimmed task per meaningful line. */
    private fun cleanLines(raw: String): List<String> =
        raw.split('\n', '\r')
            .map { it.replace(markerRegex, "").trim() }
            .filter { it.isNotBlank() }

    /** Pull a time out of a line and return (normalized "H:mm" or null, line without it). */
    private fun extractTime(line: String): Pair<String?, String> {
        colonTime.find(line)?.let { m ->
            val h = m.groupValues[1].toInt()
            val min = m.groupValues[2].toInt()
            if (h in 0..23 && min in 0..59) return fmt(h, min) to without(line, m.range)
        }
        cnTime.find(line)?.let { m ->
            var h = m.groupValues[2].toInt()
            if (h in 0..23) {
                if (m.groupValues[1] in pmPeriods && h < 12) h += 12
                val minPart = m.groupValues[3]
                val min = when {
                    minPart.startsWith("半") -> 30
                    minPart.contains("分") -> minPart.filter { it.isDigit() }.toIntOrNull() ?: 0
                    else -> 0
                }
                if (h in 0..23 && min in 0..59) return fmt(h, min) to without(line, m.range)
            }
        }
        return null to line
    }

    private fun fmt(h: Int, min: Int) = "%d:%02d".format(h, min)

    /** Remove a matched span and tidy any separators it leaves behind. */
    private fun without(line: String, range: IntRange): String =
        line.removeRange(range).trim().trim(' ', '·', '-', '—', ',', '，', '、', ':', '：').trim()

    private fun classify(title: String): Pair<Tag, TodoIcon> {
        val t = title.lowercase()
        val r = rules.firstOrNull { rule -> rule.keys.any { t.contains(it) } }
        return if (r != null) r.tag to r.icon else Tag.LIFE to TodoIcon.SPARKLE
    }

    private fun looksUrgent(line: String): Boolean {
        val t = line.lowercase()
        return urgentKeys.any { t.contains(it) }
    }
}
