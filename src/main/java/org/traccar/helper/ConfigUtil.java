package org.traccar.helper;

        import org.traccar.Context;

        import java.net.URISyntaxException;
        import java.net.URL;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.nio.file.Paths;


/**
 * Created by niko on 2/6/16.
 */
public enum ConfigUtil {

    USER_HOME("user.home"){
        public boolean findAndLoad(){
            String property = System.getProperty(this.dir);
            if (property != null && Files.exists(Paths.get(property, "traccar.xml"))) {
                load(Paths.get(property, "traccar.xml"));
                return true;
            }
            return false;
        }

    }, CATALINA_HOME("catalina.home"){
        public boolean findAndLoad(){
            String property = System.getProperty(this.dir);
            if (property != null && Files.exists(Paths.get(property, "traccar.xml"))) {
                load(Paths.get(property, "traccar.xml"));
                return true;
            }
            return false;
        }
    }, DEFAULT("DEFAULT"){
        public boolean findAndLoad(){
            URL resource = ConfigUtil.class.getResource("/debug.xml");
            try {
                load(Paths.get(resource.toURI()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    };
    String dir;
    private ConfigUtil(String dir){
        this.dir = dir;
    }

    public abstract boolean findAndLoad();

    public void load(Path path) {
        try {
            System.out.println("Config file loading from ...." + path.toAbsolutePath());
            Context.init(path);
            System.out.println("Config file loaded" + path.toAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
