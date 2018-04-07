package com.cout970.magneticraft.gui.client.guide

import com.cout970.magneticraft.IVector2
import com.cout970.magneticraft.util.vector.vec2Of
import net.minecraft.util.text.TextFormatting

object MdRenderer {

    fun render(doc: MarkdownDocument, pageSize: IVector2, fontHeight: Int, fontWidth: (String) -> Int): List<Page> {
        val ctx = Context(pageSize, fontHeight, fontWidth)
        val txt = doc.root.flatMap { renderTag(ctx, it) }

        return txt.groupBy { it.page }.map {
            Page(
                    text = it.value.filterIsInstance<NormalTextBox>(),
                    links = it.value.filterIsInstance<LinkTextBox>(),
                    index = it.key
            )
        }
    }

    fun renderTag(ctx: Context, tag: MdTag): List<TextBox> = tag.run {
        when (this) {
            is MdText -> {
                if (txt.isEmpty()) {
                    emptyList()
                } else {
                    renderText(ctx, txt)
                }
            }
            is MdLink -> {
                val (linkSection, page) = parseUrl(url)
                listOf(LinkTextBox(childs.flatMap { renderTag(ctx, it) }, linkSection.removeSuffix(".md"), page))
            }
            is MdItalic -> {
                ctx.prefix += TextFormatting.ITALIC
                val ret = childs.flatMap { renderTag(ctx, it) }
                ctx.prefix = ctx.prefix.substring(0, ctx.prefix.length - 2)
                ret
            }
            is MdBold -> {
                ctx.prefix += TextFormatting.BOLD
                val ret = childs.flatMap { renderTag(ctx, it) }
                ctx.prefix = ctx.prefix.substring(0, ctx.prefix.length - 2)
                ret
            }
            is MdHeader -> {
                ctx.prefix += TextFormatting.BOLD
                val ret = childs.flatMap { renderTag(ctx, it) }
                ctx.prefix = ctx.prefix.substring(0, ctx.prefix.length - 2)
                ret
            }
            is MdNewLine -> {
                ctx.newLine()
                emptyList()
            }
            else -> emptyList()
        }
    }

    private fun renderText(ctx: Context, txt: String): List<TextBox> {
        val list = mutableListOf<TextBox>()

        if (txt.length != 1 || txt != "\n") {
            txt.split(" ", "\n").filter { it.isNotEmpty() }.forEach {

                val size = ctx.fontWidth(ctx.prefix + it + " ")

                if (ctx.lastPosX + size > ctx.pageSize.xi) {
                    ctx.newLine()
                }

                list += NormalTextBox(ctx.prefix + it, vec2Of(ctx.lastPosX, ctx.lastPosY), ctx.page)
                ctx.lastPosX += size
            }
        }

        if (txt.endsWith("\n")) {
            ctx.newLine()
        }
        return list
    }

    fun parseUrl(url: String): Pair<String, Int> {
        val separator = url.indexOfLast { it == '#' }

        val page = if (separator != -1) {
            url.substringAfterLast('#').toIntOrNull() ?: 0
        } else 0

        val urlWithoutPage = if (separator != -1) {
            url.substringBeforeLast('#')
        } else url

        val slashIndex = urlWithoutPage.indexOfLast { it == '/' }

        val section = if (slashIndex == -1) {
            urlWithoutPage
        } else urlWithoutPage.substringAfterLast('/')

        return section to page
    }

    data class Context(
            val pageSize: IVector2,
            val fontHeight: Int,
            val fontWidth: (String) -> Int,
            var lastPosX: Int = 0,
            var lastPosY: Int = 0,
            var prefix: String = "",
            var page: Int = 0
    ) {

        fun newLine() {
            lastPosY += fontHeight + 2
            lastPosX = 0
            if (lastPosY > pageSize.yi) {
                lastPosY = 0
                page++
            }
        }
    }
}
