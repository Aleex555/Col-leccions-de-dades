package cat.iesesteveterradas.exemples;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;

public class Informe2 {

    public static void main(String[] args) {
        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Preguntas");
            MongoCollection<Document> collection = database.getCollection("PreguntasMasVistas");

            // Crear el filtro para buscar preguntas con palabras clave en el título
            Bson regexFilter = Filters.regex("title", ".*(pug|wig|yak|nap|jig|mug|zap|gag|oaf|elf).*", "i");

            // Buscar documentos que cumplan con el filtro
            FindIterable<Document> results = collection.find(regexFilter);
            createPdfFromResults(results, "informe2.pdf");
               
            
    }
}

    private static String cleanText(String text) {
        // Elimina los caracteres que no son ASCII o convierte ciertos caracteres especiales
        return text.replaceAll("[^\\x00-\\x7F]", ""); // Elimina caracteres no ASCII
    }
    
    private static void createPdfFromResults(FindIterable<Document> results, String filename) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
    
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
    
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(25, 750);
    
            int lineCount = 0;
            for (Document doc : results) {
                if (lineCount % 45 == 0 && lineCount != 0) {
                    contentStream.endText();
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                    contentStream.setLeading(14.5f);
                    contentStream.newLineAtOffset(25, 750);
                }
                String line = cleanText(doc.toJson());
                contentStream.showText(line);
                contentStream.newLine();
                lineCount++;
            }
            contentStream.endText();
            contentStream.close();
    
            document.save(System.getProperty("user.dir") + "/data/output/" + filename);
            System.out.println("PDF creado en: " + System.getProperty("user.dir") + "/data/output/" + filename);
        } catch (IOException e) {
            System.err.println("Error al crear el PDF: " + e.getMessage());
        }
    }
    
}

