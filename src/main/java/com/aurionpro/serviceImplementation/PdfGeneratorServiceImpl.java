package com.aurionpro.serviceImplementation;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.aurionpro.entity.EmployeeEntity;
import com.aurionpro.entity.SalaryDisbursementEntity;
import com.aurionpro.service.PdfGeneratorService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

	// Define color scheme
	private static final BaseColor HEADER_BG = new BaseColor(41, 128, 185); // Professional Blue
	private static final BaseColor SECTION_BG = new BaseColor(236, 240, 241); // Light Gray
	private static final BaseColor TEXT_DARK = new BaseColor(44, 62, 80);
	private static final BaseColor BORDER_COLOR = new BaseColor(189, 195, 199);
	private static final BaseColor NET_SALARY_BG = new BaseColor(46, 204, 113); // Green

	@Override
	public byte[] generateSalarySlip(EmployeeEntity emp, SalaryDisbursementEntity disb) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Document document = new Document(PageSize.A4, 40, 40, 50, 50);
			PdfWriter.getInstance(document, out);
			document.open();

			// -------------------------------
			// Header Section with Company Branding
			// -------------------------------
			PdfPTable headerTable = new PdfPTable(1);
			headerTable.setWidthPercentage(100);
			headerTable.setSpacingAfter(20);

			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE);
			PdfPCell headerCell = new PdfPCell(new Phrase("SALARY SLIP", titleFont));
			headerCell.setBackgroundColor(HEADER_BG);
			headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			headerCell.setPadding(15);
			headerCell.setBorder(Rectangle.NO_BORDER);
			headerTable.addCell(headerCell);

			document.add(headerTable);

			// -------------------------------
			// Employee Information Section
			// -------------------------------
			PdfPTable empInfoTable = new PdfPTable(2);
			empInfoTable.setWidthPercentage(100);
			empInfoTable.setWidths(new float[] { 1, 1 });
			empInfoTable.setSpacingAfter(20);

			Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, HEADER_BG);
			Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);

			// Left column
			addInfoCell(empInfoTable, "Employee Name", emp.getFirstName() + " " + emp.getLastName(), sectionFont,
					infoFont, true);
			addInfoCell(empInfoTable, "Employee ID", String.valueOf(emp.getEmployeeId()), sectionFont, infoFont, true);

			// Right column
			addInfoCell(empInfoTable, "Email", emp.getUser().getEmail(), sectionFont, infoFont, false);
			addInfoCell(empInfoTable, "Salary Month", disb.getSalaryMonth().toString(), sectionFont, infoFont, false);

			document.add(empInfoTable);

			// -------------------------------
			// Salary Breakdown Section
			// -------------------------------
			Font breakdownTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TEXT_DARK);
			Paragraph breakdownTitle = new Paragraph("SALARY BREAKDOWN", breakdownTitleFont);
			breakdownTitle.setSpacingBefore(10);
			breakdownTitle.setSpacingAfter(15);
			document.add(breakdownTitle);

			// Earnings Table
			PdfPTable earningsTable = new PdfPTable(2);
			earningsTable.setWidthPercentage(100);
			earningsTable.setWidths(new float[] { 3, 2 });

			Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
			Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);
			Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_DARK);

			// Earnings Header
			addTableHeader(earningsTable, "EARNINGS", tableHeaderFont);
			addTableHeader(earningsTable, "AMOUNT (₹)", tableHeaderFont);

			// Earnings rows
			addStyledRow(earningsTable, "Basic Salary", String.format("%.2f", disb.getBasicSalary()), tableCellFont,
					amountFont, false);
			addStyledRow(earningsTable, "House Rent Allowance (HRA)", String.format("%.2f", disb.getHra()),
					tableCellFont, amountFont, false);
			addStyledRow(earningsTable, "Other Allowances", String.format("%.2f", disb.getAllowances()), tableCellFont,
					amountFont, false);

			// Gross Salary (subtotal)
			double grossSalary = disb.getBasicSalary() + disb.getHra() + disb.getAllowances();
			addStyledRow(earningsTable, "Gross Salary", String.format("%.2f", grossSalary), tableCellFont, amountFont,
					true);

			document.add(earningsTable);
			document.add(new Paragraph(" "));

			// Deductions Table
			PdfPTable deductionsTable = new PdfPTable(2);
			deductionsTable.setWidthPercentage(100);
			deductionsTable.setWidths(new float[] { 3, 2 });

			addTableHeader(deductionsTable, "DEDUCTIONS", tableHeaderFont);
			addTableHeader(deductionsTable, "AMOUNT (₹)", tableHeaderFont);

			addStyledRow(deductionsTable, "Total Deductions", String.format("%.2f", disb.getDeductions()),
					tableCellFont, amountFont, true);

			document.add(deductionsTable);

			// -------------------------------
			// Net Salary Section (Highlighted)
			// -------------------------------
			PdfPTable netSalaryTable = new PdfPTable(2);
			netSalaryTable.setWidthPercentage(100);
			netSalaryTable.setWidths(new float[] { 3, 2 });
			netSalaryTable.setSpacingBefore(15);
			netSalaryTable.setSpacingAfter(20);

			Font netSalaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.WHITE);

			PdfPCell netLabelCell = new PdfPCell(new Phrase("NET SALARY (Take Home)", netSalaryFont));
			netLabelCell.setBackgroundColor(NET_SALARY_BG);
			netLabelCell.setPadding(12);
			netLabelCell.setBorder(Rectangle.NO_BORDER);
			netLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);

			PdfPCell netAmountCell = new PdfPCell(
					new Phrase("₹ " + String.format("%.2f", disb.getNetSalary()), netSalaryFont));
			netAmountCell.setBackgroundColor(NET_SALARY_BG);
			netAmountCell.setPadding(12);
			netAmountCell.setBorder(Rectangle.NO_BORDER);
			netAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

			netSalaryTable.addCell(netLabelCell);
			netSalaryTable.addCell(netAmountCell);

			document.add(netSalaryTable);

			// -------------------------------
			// Transaction Details
			// -------------------------------
			PdfPTable transactionTable = new PdfPTable(1);
			transactionTable.setWidthPercentage(100);
			transactionTable.setSpacingBefore(15);

			Font transactionFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_DARK);

			PdfPCell transCell = new PdfPCell();
			transCell.setBorder(Rectangle.TOP);
			transCell.setBorderColor(BORDER_COLOR);
			transCell.setPaddingTop(15);
			transCell.setPaddingBottom(5);
			transCell.addElement(new Phrase(
					"Transaction Date: "
							+ disb.getTransactionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
					transactionFont));
			transCell.addElement(new Phrase("Transaction Reference: " + disb.getPaymentRefNo(), transactionFont));
			transCell.addElement(new Phrase("Payment Status: " + disb.getStatus(), transactionFont));

			transactionTable.addCell(transCell);
			document.add(transactionTable);

			// -------------------------------
			// Footer
			// -------------------------------
			Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, BaseColor.GRAY);
			Paragraph footer = new Paragraph("This is a system-generated payslip. No signature is required.",
					footerFont);
			footer.setAlignment(Element.ALIGN_CENTER);
			footer.setSpacingBefore(20);
			document.add(footer);

			document.close();
			return out.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Error generating salary slip PDF", e);
		}
	}

	// Helper method to add info cells with labels
	private void addInfoCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont,
			boolean isLeftColumn) {
		PdfPCell cell = new PdfPCell();
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setBackgroundColor(SECTION_BG);
		cell.setPadding(10);

		Paragraph p = new Paragraph();
		p.add(new Phrase(label + "\n", labelFont));
		p.add(new Phrase(value, valueFont));
		cell.addElement(p);

		table.addCell(cell);
	}

	// Helper method for table headers
	private void addTableHeader(PdfPTable table, String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBackgroundColor(HEADER_BG);
		cell.setPadding(10);
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.addCell(cell);
	}

	// Helper method for styled table rows
	private void addStyledRow(PdfPTable table, String label, String amount, Font labelFont, Font amountFont,
			boolean isBold) {
		// Label cell
		PdfPCell labelCell = new PdfPCell(
				new Phrase(label, isBold ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_DARK) : labelFont));
		labelCell.setPadding(8);
		labelCell.setBorder(Rectangle.BOTTOM);
		labelCell.setBorderColor(BORDER_COLOR);
		labelCell.setBackgroundColor(isBold ? SECTION_BG : BaseColor.WHITE);
		table.addCell(labelCell);

		// Amount cell
		PdfPCell amountCell = new PdfPCell(new Phrase("₹ " + amount,
				isBold ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, TEXT_DARK) : amountFont));
		amountCell.setPadding(8);
		amountCell.setBorder(Rectangle.BOTTOM);
		amountCell.setBorderColor(BORDER_COLOR);
		amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		amountCell.setBackgroundColor(isBold ? SECTION_BG : BaseColor.WHITE);
		table.addCell(amountCell);
	}
}