package com.babu.ms.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SportsMessage {
    private String title;
    private String content;
    private int priority;

    @Override
    public String toString() {
        return "SportsMessage{title='" + title + "', content='" + content + "', priority=" + priority + "}";
    }
}
