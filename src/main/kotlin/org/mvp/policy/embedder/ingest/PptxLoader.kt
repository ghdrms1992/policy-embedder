package org.mvp.policy.embedder.ingest

import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFShape
import org.apache.poi.xslf.usermodel.XSLFSlide
import org.apache.poi.xslf.usermodel.XSLFTable
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.springframework.ai.document.Document
import java.io.InputStream

class PptxLoader {
    /**
     * PPTX -> [Document] 목록 (슬라이드 본문/표/노트 각각 1개 문서). 메타데이터 포함.
     */
    fun load(input: InputStream, title: String, version: String, sourceName: String): List<Document> {
        XMLSlideShow(input).use { show ->
            val docs = mutableListOf<Document>()
            show.slides.forEachIndexed { idx, slide ->
                val slideName = "slide-${idx + 1}"
                // 본문 + 표
                val body = buildString {
                    slide.shapes.forEach { sh ->
                        when (sh) {
                            is XSLFTable -> append(tableToMarkdown(sh)).appendLine()
                            else -> append(shapeText(sh)).appendLine()
                        }
                    }
                }.trim().replace(Regex("[ \t]+\n"), "\n").replace(Regex("\n{3,}"), "\n\n")
                if (body.isNotBlank()) {
                    docs += Document(
                        body,
                        mapOf(
                            "source_type" to "pptx",
                            "source" to sourceName,
                            "title" to title,
                            "version" to version,
                            "section" to slideName
                        )
                    )
                }
                // 노트
                val notes = extractNotes(slide)
                if (notes.isNotBlank()) {
                    docs += Document(
                        notes,
                        mapOf(
                            "source_type" to "pptx",
                            "source" to sourceName,
                            "title" to title,
                            "version" to version,
                            "section" to "$slideName-notes"
                        )
                    )
                }
            }
            return docs
        }
    }

    private fun shapeText(sh: XSLFShape): String = try {
        val m = sh.javaClass.methods.firstOrNull { it.name == "getText" && it.parameterCount == 0 }
        (m?.invoke(sh) as? String) ?: ""
    } catch (_: Exception) { "" }

    private fun tableToMarkdown(tbl: XSLFTable): String {
        val rows: List<List<String>> = tbl.rows.map { row ->
            row.cells.map { cell ->
                cell.text.trim().replace(Regex("\\s+"), " ")
            }
        }

        if (rows.isEmpty()) return ""

        val header = rows.first().joinToString(" | ")
        val sep = List(rows.first().size) { "---" }.joinToString(" | ")
        val rest = rows.drop(1).map { it.joinToString(" | ") }
        return (listOf(header, sep) + rest).joinToString("\n")
    }

    private fun extractNotes(slide: XSLFSlide): String =
        slide.notes?.let { notes ->
            notes.shapes
                .filterIsInstance<XSLFTextShape>()
                .mapNotNull { it.text }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
                .replace(Regex("[ \\t]+\\n"), "\n")
                .replace(Regex("\\n{3,}"), "\n\n")
                .trim()
        } ?: ""
}