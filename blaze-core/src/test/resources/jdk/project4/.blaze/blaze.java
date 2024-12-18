import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Config;

public class blaze {
    private final Config config = Contexts.config();

    public void main() {
        String val1 = config.value("project4.val1").get();
        System.out.println("val1 = " + val1);

        String val2 = config.value("project4.val2").get();
        System.out.println("val2 = " + val2);
    }
    
}
