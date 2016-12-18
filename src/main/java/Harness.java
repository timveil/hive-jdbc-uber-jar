import org.apache.hadoop.hive.shims.ShimLoader;
import org.apache.hadoop.util.VersionInfo;


public class Harness {

    public static void main(String[] args) {

        System.out.println(VersionInfo.getVersion());

        System.out.println(ShimLoader.getMajorVersion());

    }
}
