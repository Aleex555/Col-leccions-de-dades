package cat.iesesteveterradas.exemples;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class Informe1 {

    public static void main(String[] args) {
        String mongoUri = "mongodb://root:example@localhost:27017";
        String databaseName = "Preguntas";
        String collectionName = "PreguntasMasVistas";
        
        try (var mongoClient = MongoClients.create(mongoUri)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Consultar los documentos
            double averageViewCount = calculateAverageViewCount(collection);
            System.out.println(averageViewCount);
            Bson filter = Filters.gt("views", averageViewCount);
            FindIterable<Document> results = collection.find(filter);

            createPdfFromResults(results, "informe1.pdf");
        } catch (Exception e) {
            System.err.println("Error de MongoDB: " + e.getMessage());
        }
    }

    private static double calculateAverageViewCount(MongoCollection<Document> collection) {
        // Crear un pipeline de agregación para calcular el promedio
        AggregateIterable<Document> aggResult = collection.aggregate(
                Arrays.asList(
                        Aggregates.group(null, Accumulators.avg("averageViewCount", "$views"))
                )
        );

        // Obtener el resultado de la agregación
        Document result = aggResult.first();
        if (result != null) {
            return result.getDouble("averageViewCount");
        } else {
            return 0.0;
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
