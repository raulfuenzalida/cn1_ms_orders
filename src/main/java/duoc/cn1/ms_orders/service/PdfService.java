package duoc.cn1.ms_orders.service;

import duoc.cn1.ms_orders.exception.OrderNotFoundException;
import duoc.cn1.ms_orders.model.Order;
import duoc.cn1.ms_orders.repository.OrderRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

	private final OrderRepository orderRepository;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public byte[] generateOrderReceipt(Long id) {
		Order order = orderRepository.findById(id)
			.orElseThrow(() -> new OrderNotFoundException(id));

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf);

			PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
			PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

			document.setFont(font);

			document.add(new Paragraph("PrintWorks")
				.setFont(boldFont)
				.setFontSize(18)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginBottom(20));

			document.add(new Paragraph("Comprobante de Pedido")
				.setFont(boldFont)
				.setFontSize(14)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginBottom(10));

			document.add(new Paragraph("Número: " + order.getOrderNumber())
				.setFontSize(12)
				.setMarginBottom(5));

			document.add(new Paragraph("Fecha: " + order.getCreatedAt().format(DATE_FORMATTER))
				.setFontSize(12)
				.setMarginBottom(5));

			document.add(new Paragraph("Estado: " + order.getStatus().name())
				.setFontSize(12)
				.setMarginBottom(20));

			document.add(new Paragraph("Cliente")
				.setFont(boldFont)
				.setFontSize(12)
				.setMarginBottom(5));

			document.add(new Paragraph("Nombre: " + order.getCustomerName())
				.setFontSize(10)
				.setMarginBottom(5));

			document.add(new Paragraph("Email: " + order.getCustomerEmail())
				.setFontSize(10)
				.setMarginBottom(20));

			document.add(new Paragraph("Productos")
				.setFont(boldFont)
				.setFontSize(12)
				.setMarginBottom(10));

			Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2}))
				.useAllAvailableWidth();

			table.addHeaderCell(createCell("ID", boldFont));
			table.addHeaderCell(createCell("Producto", boldFont));
			table.addHeaderCell(createCell("Precio Unit.", boldFont));
			table.addHeaderCell(createCell("Cantidad", boldFont));
			table.addHeaderCell(createCell("Subtotal", boldFont));

			order.getItems().forEach(item -> {
				table.addCell(createCell(String.valueOf(item.getIdProduct()), font));
				table.addCell(createCell(item.getProductName(), font));
				table.addCell(createCell(item.getUnitPrice().toString(), font));
				table.addCell(createCell(String.valueOf(item.getQuantity()), font));
				table.addCell(createCell(item.getSubtotal().toString(), font));
			});

			document.add(table);

			document.add(new Paragraph("Total: $" + order.getTotal())
				.setFont(boldFont)
				.setFontSize(14)
				.setTextAlignment(TextAlignment.RIGHT)
				.setMarginTop(20));

			if (order.getStatus().name().equals("CANCELLED")) {
				document.add(new Paragraph("Pedido Cancelado")
					.setFont(boldFont)
					.setFontSize(12)
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginTop(10));

				if (order.getCancelledAt() != null) {
					document.add(new Paragraph("Fecha de cancelación: " + order.getCancelledAt().format(DATE_FORMATTER))
						.setFontSize(10)
						.setTextAlignment(TextAlignment.CENTER));
				}
			}

			document.add(new Paragraph("Este comprobante no constituye un documento tributario.")
				.setFontSize(8)
				.setTextAlignment(TextAlignment.CENTER)
				.setMarginTop(30));

			document.close();

			log.info("PDF generado exitosamente para pedido: {}", order.getOrderNumber());
			return outputStream.toByteArray();

		} catch (IOException e) {
			log.error("Error al generar PDF para pedido {}: {}", id, e.getMessage());
			throw new RuntimeException("Error al generar el PDF del comprobante", e);
		}
	}

	private com.itextpdf.layout.element.Cell createCell(String text, PdfFont font) {
		return new com.itextpdf.layout.element.Cell()
			.add(new Paragraph(text).setFont(font).setFontSize(10));
	}
}
