import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constant {
    private static Constant constant;

    private Constant(){}

    public static Constant getInstance(){
        if (constant == null){
            constant = new Constant();
        }
        return constant;
    }

    //////
    //ba propertise load kon
    private String nodeAddress;
    private String walletDirectory;


    public String getWalletDirectory() {
        walletDirectory = loadProperties("walletDirectory");
        return walletDirectory;
    }

    public String getNodeAddress() {
        nodeAddress = loadProperties("nodeAddress");
        return nodeAddress;
    }

    private String loadProperties(String key){
        try (InputStream input = Constant.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            return prop.getProperty(key);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
