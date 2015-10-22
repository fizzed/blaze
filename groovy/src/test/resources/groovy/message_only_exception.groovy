import static com.fizzed.blaze.Shells.*

def main() {
    requireExec("thisdoesnotexist", "This message should be displayed").run()
}