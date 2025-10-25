#compdef blaze

# This is the Zsh completion function
_blaze() {
    # 'words' is the Zsh equivalent of 'COMP_WORDS'
    # 'CURRENT' is the index of the current word

    local suggestions

    # 1. Run the SAME Java command to get suggestions
    #    "${(@f)...}" is a Zsh-specific trick to split the
    #    command's output by newline into an array.
    suggestions=("${(@f)$(blaze --generate-completion-with-desc $words)}")

    # 2. 'compadd' is the Zsh command to add suggestions.
    #    -a tells it to treat 'suggestions' as an array.
    compadd -a suggestions
}

#
# mkdir -p ~/.zsh/completion
#
# cp <this-script> ~/.zsh/completion/_blaze
#
# # Add our custom completion directory to the fpath
# (This adds it to the beginning of the path)
# fpath=(~/.zsh/completion $fpath)

# Initialize the completion system
# autoload -U compinit
# compinit
