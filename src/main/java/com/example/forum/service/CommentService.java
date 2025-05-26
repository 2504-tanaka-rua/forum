package com.example.forum.service;

import com.example.forum.controller.form.CommentForm;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;

    /*
     *コメントの追加、更新
     */
    public void saveComment(CommentForm reqComment) {
        Comment saveComment = setCommentEntity(reqComment);
        commentRepository.save(saveComment);
    }

    /*
     *送られてきたFormの内容をEntityに詰め替える
     */
    private Comment setCommentEntity(CommentForm reqComment) {
        Comment comment = new Comment();
        comment.setId(reqComment.getId());
        comment.setContent(reqComment.getContent());
        comment.setReportId(reqComment.getReportId());
        return comment;
    }

    /*
     *コメント全件取得
     */
    public List<CommentForm> findAllComment() {
        List<Comment> results = commentRepository.findAll();
        List<CommentForm> comments = setCommentForm(results);
        return comments;
    }

    /*
     *編集対象のコメントをコメントのidでselect
     */
    public CommentForm editComment(Integer id){

        List<Comment> results = new ArrayList<>();
        results.add((Comment) commentRepository.findById(id).orElse(null));
        List<CommentForm> comments = setCommentForm(results);

        return comments.get(0);
    }

    /*
     *selectしたデータをEntityからFormに詰め替える（表示用への切り替え）
     */
    private List<CommentForm> setCommentForm(List<Comment> results) {
        List<CommentForm> comments = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            CommentForm comment = new CommentForm();
            Comment result = results.get(i);
            comment.setId(result.getId());
            comment.setContent(result.getContent());
            comment.setReportId(result.getReportId());
            comments.add(comment);
        }
        return comments;
    }

    public void delete(Integer id){
        commentRepository.deleteById(id);
    }
}
