# This is the function that Bash will run
_blaze_completions() {
    # 'COMP_WORDS' is an array of words the user has typed
    # We call our Java app, passing a special flag and all those words
    local suggestions
    suggestions=$(blaze --_generate_completion "${COMP_WORDS[@]}")
    
    # 'COMPREPLY' is the special array Bash uses for suggestions
    # We feed the output from our Java app into it
    COMPREPLY=($(compgen -W "${suggestions}" -- "${COMP_WORDS[COMP_CWORD]}"))
}

# This line tells Bash to use our function
# whenever the user tries to complete the 'blaze' command.
# This is why the wrapper script in Step 1 is so important!
complete -F _blaze_completions blaze

# to install it
# source <this-file>
#
# OR
#
# you can inline it to ~/.bashrc and that'll work too
#
# OR
#
# It must be named for the command it completes things for
# cp <this-file> ~/.local/share/bash-completion/completions/blaze
#
