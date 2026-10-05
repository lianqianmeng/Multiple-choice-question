package com.example.forchoiceexam

import com.example.forchoiceexam.data.model.ColumnMapping
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.data.parser.ExcelParser
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ExcelParserTest {

    @Test
    fun testParseExcel_SevenColumnLayout() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sheet1")

        // Row 0: Header
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("题目")
        header.createCell(1).setCellValue("A选项")
        header.createCell(2).setCellValue("B选项")
        header.createCell(3).setCellValue("C选项")
        header.createCell(4).setCellValue("D选项")
        header.createCell(5).setCellValue("参考答案")
        header.createCell(6).setCellValue("解析")

        // Row 1: Single Choice
        val r1 = sheet.createRow(1)
        r1.createCell(0).setCellValue("1. Android的开发语言是？")
        r1.createCell(1).setCellValue("Java")
        r1.createCell(2).setCellValue("Kotlin")
        r1.createCell(3).setCellValue("Swift")
        r1.createCell(4).setCellValue("C#")
        r1.createCell(5).setCellValue("B")
        r1.createCell(6).setCellValue("Kotlin是Android首选语言")

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()

        val inputStream = ByteArrayInputStream(out.toByteArray())
        val previewRows = ExcelParser.readPreviewRows(inputStream)
        val mapping = ExcelParser.suggestDefaultMapping(previewRows)

        val parseStream = ByteArrayInputStream(out.toByteArray())
        val questions = ExcelParser.parseExcelWithMapping(parseStream, bankId = 100L, mapping = mapping)

        assertEquals(1, questions.size)
        val q1 = questions[0]
        assertEquals("1. Android的开发语言是？", q1.questionText)
        assertEquals(QuestionType.SINGLE_CHOICE, q1.type)
        assertEquals("B", q1.normalizedAnswer)
    }

    @Test
    fun testParseExcel_NineColumnLayout() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sheet1")

        // Row 0: Header (序号, 题型, 题干, 选项A, 选项B, 选项C, 选项D, 参考答案, 解析)
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("序号")
        header.createCell(1).setCellValue("题型")
        header.createCell(2).setCellValue("题干")
        header.createCell(3).setCellValue("选项A")
        header.createCell(4).setCellValue("选项B")
        header.createCell(5).setCellValue("选项C")
        header.createCell(6).setCellValue("选项D")
        header.createCell(7).setCellValue("参考答案")
        header.createCell(8).setCellValue("解析")

        // Row 1: Single Choice
        val r1 = sheet.createRow(1)
        r1.createCell(0).setCellValue(1.0)
        r1.createCell(1).setCellValue("单选题")
        r1.createCell(2).setCellValue("商用密码评估标准包括哪些？")
        r1.createCell(3).setCellValue("GB/T 39786")
        r1.createCell(4).setCellValue("ISO 27001")
        r1.createCell(5).setCellValue("IEEE 802.11")
        r1.createCell(6).setCellValue("HTTP/2")
        r1.createCell(7).setCellValue("A")
        r1.createCell(8).setCellValue("详解GB/T 39786")

        // Row 2: Multiple Choice
        val r2 = sheet.createRow(2)
        r2.createCell(0).setCellValue(2.0)
        r2.createCell(1).setCellValue("多选题")
        r2.createCell(2).setCellValue("对称加密算法包括？")
        r2.createCell(3).setCellValue("AES")
        r2.createCell(4).setCellValue("SM4")
        r2.createCell(5).setCellValue("RSA")
        r2.createCell(6).setCellValue("ECC")
        r2.createCell(7).setCellValue("B, A")
        r2.createCell(8).setCellValue("SM4和AES是对称算法")

        // Row 3: Judgment Question
        val r3 = sheet.createRow(3)
        r3.createCell(0).setCellValue(3.0)
        r3.createCell(1).setCellValue("判断题")
        r3.createCell(2).setCellValue("SM2是非对称加密算法。")
        r3.createCell(3).setCellValue("")
        r3.createCell(4).setCellValue("")
        r3.createCell(5).setCellValue("")
        r3.createCell(6).setCellValue("")
        r3.createCell(7).setCellValue("正确")
        r3.createCell(8).setCellValue("SM2基于ECC椭圆曲线")

        val out = ByteArrayOutputStream()
        workbook.write(out)
        workbook.close()

        val inputStream = ByteArrayInputStream(out.toByteArray())
        val previewRows = ExcelParser.readPreviewRows(inputStream)
        val mapping = ExcelParser.suggestDefaultMapping(previewRows)

        val parseStream = ByteArrayInputStream(out.toByteArray())
        val questions = ExcelParser.parseExcelWithMapping(parseStream, bankId = 200L, mapping = mapping)

        assertEquals(3, questions.size)

        // Question 1: Single Choice
        val q1 = questions[0]
        assertEquals("商用密码评估标准包括哪些？", q1.questionText)
        assertEquals(QuestionType.SINGLE_CHOICE, q1.type)
        assertEquals("GB/T 39786", q1.optionA)
        assertEquals("A", q1.normalizedAnswer)
        assertEquals("详解GB/T 39786", q1.explanation)

        // Question 2: Multiple Choice
        val q2 = questions[1]
        assertEquals(QuestionType.MULTIPLE_CHOICE, q2.type)
        assertEquals("AB", q2.normalizedAnswer)

        // Question 3: Judgment
        val q3 = questions[2]
        assertEquals(QuestionType.JUDGMENT, q3.type)
        assertEquals("正确", q3.optionA)
        assertEquals("错误", q3.optionB)
        assertEquals("A", q3.normalizedAnswer)
    }
}
