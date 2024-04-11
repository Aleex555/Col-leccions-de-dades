package cat.iesesteveterradas.exemples;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class MongoInsertExample {
    private static final Logger logger = LoggerFactory.getLogger(MongoInsertExample.class);
    public static void main(String[] args) {

        try (var mongoClient = MongoClients.create("mongodb://root:example@localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("Preguntas");
            MongoCollection<Document> collection = database.getCollection("PreguntasMasVistas");

            // Parsear el archivo XML
            File xmlFile = new File("data/1.xml");
            var dbFactory = DocumentBuilderFactory.newInstance();
            var dBuilder = dbFactory.newDocumentBuilder();
            var doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("post");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                    String views = eElement.getElementsByTagName("views").item(0).getTextContent();
                    
                    // Crear y insertar el documento en MongoDB
                    Document postDocument = new Document("title", title)
                                            .append("views", Integer.parseInt(views));
                    collection.insertOne(postDocument);
                    logger.info("Documento insertado: Title = {}, Views = {}", title, views);
                }
            }

            logger.info("Todos los documentos han sido insertados con éxito.");
        } catch (Exception e) {
            logger.error("Ocurrió un error al insertar los documentos", e);
        }
    }
}
