import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLab {

    //Slf4J
    final static Logger logger = LoggerFactory.getLogger(TLab.class);

    private XMLStreamReader parser;
    XMLInputFactory factory = XMLInputFactory.newInstance();

    /*
     *  Количество правок внесенных каждым пользователем (пользователь – кол-во правок)
     * отсортированное в обратном порядке по количеству правок (атрибут user в тегах node).
     * */

    private Map<String, Map.Entry<String,Long>> userToChangeset = new HashMap<>();

    private Map<String, Long> keyToCount = new HashMap<>();

    private String getAttributeValue(String name) {
        return parser.getAttributeValue(null, name);
    }

    private void treatElementNode() {
        String userName = getAttributeValue("user");
        String key = getAttributeValue("uid");
        Long changeset = Long.parseLong(getAttributeValue("changeset"));
        userToChangeset.put(key, Map.entry(userName, changeset));
        //
        Long countKey = keyToCount.getOrDefault(key, 0L);
        keyToCount.put(key, countKey + 1);
    }

    private void printSortedUserToChangeset() {
        logger.info("start sort entries");
        userToChangeset.entrySet().stream()
                .sorted(new Comparator<Map.Entry<String, Map.Entry<String, Long>>>() {
                    @Override
                    public int compare(Map.Entry<String, Map.Entry<String, Long>> o1, Map.Entry<String, Map.Entry<String, Long>> o2) {
                        long changesetFirst = o1.getValue().getValue();
                        long changesetSecond = o2.getValue().getValue();
                        if (changesetFirst == changesetSecond)
                            return 0;
                        return changesetFirst > changesetSecond ? 1 : -1;
                    }
                })
                .forEach(entry -> System.out.println(entry.getValue().getKey() + " : " + entry.getValue().getValue()));
        logger.info("end sorting");
    }

    private void printKeyToCount() {
        keyToCount.entrySet().stream().forEach(System.out::println);
    }

    public void run() {
        logger.info("start application");

        try (InputStream in = new FileInputStream("RU-NVS.osm")) {
            logger.info("start treat xml file");
            parser = factory.createXMLStreamReader(in);
            while(parser.hasNext()) {
                int event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT && parser.getLocalName().equals("node")) {
                    treatElementNode();
                }
            }
        } catch (IOException e) {
            logger.warn("ioexception ", e);
        } catch (XMLStreamException ee) {
            logger.warn("xmll", ee);
        } finally {
            logger.info("end treat xml file");
            printSortedUserToChangeset();
            System.out.println("//////////////////");
            printKeyToCount();
        }
        logger.info("end application");
    }
}
