import com.google.common.base.Joiner

def main() {
    def s = Joiner.on("; ").skipNulls().join("Harry", null, "Ron", "Hermione")
    println(s)
}
