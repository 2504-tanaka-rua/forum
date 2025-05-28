package com.example.forum.controller.form;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;

public class CommentForm {
    private int id;
    @NotBlank(message = "コメントを入力してください")
    private String content;
    private int reportId;
    private Date createdDate;
    private Date updatedDate;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getReportId() {
        return reportId;
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
