package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

import java.util.List;

public class Post extends Command {

    // Fields
    private String content;
    private List<String> taggedUsersList;

    // Public Constructor
    public Post(String content, List<String> taggedUsersList) {
        this.op_code = 5;
        this.content = content;
        this.taggedUsersList = taggedUsersList;
    }

    public String getContent() {
        return content;
    }

    public List<String> getTaggedUsersList() {
        return taggedUsersList;
    }
}
