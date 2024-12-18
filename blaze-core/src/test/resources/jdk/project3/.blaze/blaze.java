import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.Config;

public class blaze {
    private final Config config = Contexts.config();

    public void main() {
        String val1 = config.value("project3.val1").get();
        System.out.println(val1);
    }
    
}
