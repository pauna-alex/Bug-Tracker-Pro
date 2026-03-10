package main.tickets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * */
public final class Comments {

    private String author;
    private String content;
    private String timestamp;

    /**
     * @param author
     * @param content
     * @param timestamp
     */
    public Comments(final String author, final String content, final String timestamp) {
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * @return author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @return timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Converteste datele comentariului in format JSON.
     * @param mapper
     * @return
     */
    public ObjectNode toObjectNode(final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        node.put("author", author);
        node.put("content", content);
        node.put("createdAt", timestamp);
        return node;
    }
}
