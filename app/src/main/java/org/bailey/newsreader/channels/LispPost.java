package org.bailey.newsreader.channels;

// LispPost - класс используемый для хранения заголовка и текста поста.
public class LispPost {
    public String title;
    public String text;

    LispPost(String title, String text) {
        this.title = title;
        this.text = text;
    }
}
