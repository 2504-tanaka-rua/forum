package com.example.forum.controller.form;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;


public class ReportForm {
    private int id;
    @NotBlank(message = "投稿内容を入力してください")
    private String content;
    private Date createdDate;
    private Date updatedDate;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
