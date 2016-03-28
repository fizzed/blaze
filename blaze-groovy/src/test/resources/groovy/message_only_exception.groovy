import static com.fizzed.blaze.Systems.*

def main() {
    requireExec("thisdoesnotexist", "This message should be displayed").run()
}