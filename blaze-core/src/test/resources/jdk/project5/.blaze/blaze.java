import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Config;

public class blaze {
    private final Config config = Contexts.config();

    public void test() {
        int arg1 = config.value("project5.arg1", int.class).get();
        System.out.println("arg1 = " + arg1);

        String arg2 = config.value("project5.arg2").get();
        System.out.println("arg2 = " + arg2);

        String arg3 = config.value("project5.arg3").get();
        System.out.println("arg3 = " + arg3);
    }
    
}
