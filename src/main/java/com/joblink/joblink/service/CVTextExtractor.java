package com.joblink.joblink.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CVTextExtractor {
	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	public String extractFromUrl(String cvFileUrl) {
		if (cvFileUrl == null || cvFileUrl.isBlank()) return "";
		try {
			String normalized = cvFileUrl.startsWith("/") ? cvFileUrl.substring(1) : cvFileUrl;
			// If url like "/cvs/uuid.pdf" and uploadDir points to ".../static/uploads"
			Path path = Paths.get(uploadDir).resolve(normalized).normalize();
			if (!Files.exists(path)) return "";
			String filename = path.getFileName().toString().toLowerCase();
			if (filename.endsWith(".pdf")) return extractPdf(path);
			if (filename.endsWith(".docx")) return extractDocx(path);
			// .doc (binary) không hỗ trợ hiện tại để giữ build đơn giản
			// if (filename.endsWith(".doc")) return extractDoc(path);
			return "";
		} catch (Exception e) {
			return "";
		}
	}

	private String extractPdf(Path path) {
		try (InputStream is = new FileInputStream(path.toFile());
			 PDDocument doc = PDDocument.load(is)) {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(doc);
		} catch (Exception e) {
			return "";
		}
	}

	private String extractDocx(Path path) {
		try (InputStream is = new FileInputStream(path.toFile());
			 XWPFDocument doc = new XWPFDocument(is);
			 XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
			return extractor.getText();
		} catch (Exception e) {
			return "";
		}
	}

}


