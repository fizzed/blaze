import static com.fizzed.blaze.Shells.*

def main() {
    println("Finding javac...")
    def javac = which("javac").run()

    println("Using javac " + javac)
    exec("javac").arg("-version").run()
}
