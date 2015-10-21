import static com.fizzed.blaze.Shells.*

def main() {
    println("Finding mvn...")
    def mvn = which("mvn").run()

    println("Using mvn " + mvn)
    exec("mvn").arg("-v").run()
}
