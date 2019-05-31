package com.ivy.lib.pdf;


import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class PDFGenerator {

    private static float pageWidth = 720;
    private static float pageHeight = 720;
    private static float marginLeft = 36;
    private static float marginRight = 18;
    private static float marginTop = 72;
    private static float marginBottom = 72;
    private String filePath;
    private String fileName;
    private String imagePath;
    public static final int ALIGNMENT_JUSTIFIED = Element.ALIGN_JUSTIFIED;
    public static final int ALIGNMENT_CENTER = Element.ALIGN_CENTER;
    public static final Chunk NEW_LINE = Chunk.NEWLINE;

    public static Font FONT_BOLD;
    public static Font FONT_NORMAL;
    public static Font FONT_BOLD_SMALL;

    private ArrayList<String> imageList;
    private PdfPCell pdfWordCell;

    public PDFGenerator(String filePath, String fileName, String imagePath) {

        this.filePath = filePath;
        this.fileName = fileName;
        this.imagePath = imagePath;
        FONT_BOLD = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        FONT_NORMAL = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        FONT_BOLD_SMALL = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD);
        PdfPCell pdfWordCell = new PdfPCell();
        pdfWordCell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        pdfWordCell.setPadding(10f);
        setPdfWordCell(pdfWordCell);

    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }

    public PdfPCell getPdfWordCell() {
        return pdfWordCell;
    }

    public void setPdfWordCell(PdfPCell pdfWordCell) {
        this.pdfWordCell = pdfWordCell;
    }

    /**
     * Main method of this class to create PDF.
     */
    public void createPdf() {
        Rectangle pageSize = new Rectangle(pageWidth, pageHeight);
        Document document = new Document(pageSize, marginLeft, marginRight, marginTop, marginBottom);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath + fileName));
            document.open();
            document.add(createPdfTable());
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a table to attach with the pdf
     *
     * @return PDF table
     */
    private PdfPTable createPdfTable() {
        PdfPTable table = new PdfPTable(1);
        table.addCell(getPdfWordCell());
        return table;
    }

    /**
     * Used to add images to the PDF.
     */
    public void addImagesToPdf() {
        try {
            Paragraph paragraph = addParagraph(Element.ALIGN_JUSTIFIED);
            paragraph.add(Chunk.NEWLINE);
            int photoCount = 1;
            float xOffset = 0;
            for (String imagename : getImageList()) {
                Image images = Image.getInstance(imagePath + "/" + imagename);
                images.setAlignment(Image.ALIGN_LEFT);
                images.scalePercent(10);
                paragraph.add(new Chunk(images, xOffset, 0, true));
                xOffset += 10;
                photoCount++;
                if (photoCount == 6) {
                    paragraph.add(Chunk.NEWLINE);
                    paragraph.add(Chunk.NEWLINE);
                    getPdfWordCell().addElement(paragraph);
                    paragraph = new Paragraph();
                    photoCount = 0;
                    xOffset = 0;
                }
            }
            paragraph.add(Chunk.NEWLINE);
            getPdfWordCell().addElement(paragraph);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To add a signature to the PDF
     *
     * @param signaturePath - Path that signature image presents.
     * @param alignment     - Where the signature need to align(center,left or right, etc) in the pdf
     * @param scale         - Size of the image
     */
    public void addSignature(String signaturePath, int alignment, float scale) {
        try {
            Image signature = Image.getInstance(signaturePath);
            signature.setAlignment(alignment);
            signature.scalePercent(scale);
            getPdfWordCell().addElement(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To add a new phrase into the pdf
     *
     * @param content - Content to be present in the phrase.
     * @param font    - Font of the text
     * @return - Phrase with content
     */
    public Phrase addPhrase(String content, Font font) {
        return new Phrase(content, font);
    }

    /**
     * To add a new paragraph into the pdf
     *
     * @param alignment - Where the paragraph need to align(center,left or right, etc) in the pdf
     * @return - Paragraph with content
     */
    public Paragraph addParagraph(int alignment) {
        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(alignment);
        return paragraph;
    }

    /**
     * Add empty spaces between the text
     *
     * @param spaceCount - Number of spaces to be added
     * @return - String with empty spaces
     */
    public static String addSpace(int spaceCount) {
        StringBuilder space = new StringBuilder();
        for (int i = 0; i <= spaceCount + 1; i++) {
            space.append(' ');
        }
        return String.valueOf(space);
    }
}
