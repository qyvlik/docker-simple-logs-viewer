package io.github.qyvlik.dockersimplelogsviewer.entity;

import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;

public class LogRow {
    private StreamType streamType;
    private String content;

    public LogRow() {

    }

    public LogRow(Frame frame) {
        this.streamType = frame.getStreamType();
        this.content = new String(frame.getPayload());
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
