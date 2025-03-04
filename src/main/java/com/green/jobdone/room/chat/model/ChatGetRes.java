package com.green.jobdone.room.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ChatGetRes {
    private int flag;
    private long chatId;
    private String userName;
    private String businessName;
    @JsonIgnore
    private long businessId;
    private String logo;
    private String createdAt;
    private String message;
    private List<GetPicDto> pics;
}
