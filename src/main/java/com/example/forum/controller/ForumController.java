package com.example.forum.controller;

import com.example.forum.controller.form.CommentForm;
import com.example.forum.controller.form.ReportForm;
import com.example.forum.service.CommentService;
import com.example.forum.service.ReportService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpSession;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
@Validated
public class ForumController {

    private HttpSession session;

    // コンストラクタを作成し、@Autowiredアノテーションを付与する
    @Autowired
    public void SessionController(HttpSession session) {
        // フィールドに代入する
        this.session = session;
    }

    @Autowired
    ReportService reportService;
    @Autowired
    CommentService commentService;

    /*
     * 投稿内容表示処理
     */
    @GetMapping
    public ModelAndView top(@ModelAttribute("startDate") String start, @ModelAttribute("endDate") String end) throws ParseException {

        //sessionに格納しておいたエラーメッセージを取得
        List<String> errorMessages = (List<String>) session.getAttribute("errorMessages");

        ModelAndView mav = new ModelAndView();
        ReportForm reportForm = new ReportForm();
        CommentForm commentForm = new CommentForm();

        if(!StringUtils.isBlank(start) || !StringUtils.isBlank(end)){
            //日付指定して絞り込んだ投稿のみ取得
            List<ReportForm> selectContent = reportService.findDesignatedDayReport(start, end);

            mav.addObject("contents", selectContent);
        }else{
            // 投稿を全件取得
            List<ReportForm> contentData = reportService.findAllReport();
            // 投稿データオブジェクトを保管
            mav.addObject("contents", contentData);
        }
        // コメントを全件取得
        List<CommentForm> commentData = commentService.findAllComment();
        // コメントデータオブジェクトを保管
        mav.addObject("comments", commentData);
        // 画面遷移先を指定
        mav.setViewName("/top");
        // 空のフォームを渡しておく（コメント入力は画面遷移しないから）
        mav.addObject("commentForm", commentForm);

        //エラーメッセージがnullじゃない＝コメントの入力が不適切だったということ
        //nullではなかった場合、入力されたcommentFormの内容とエラーメッセージをModelAndViewに格納
        //Viewでcfに入っているreportIdに一致する投稿でエラーメッセージを表示する
        if(!(errorMessages == null)){
            //入力されたcommentFormの内容をsessionから取り出す
            CommentForm cf = (CommentForm) session.getAttribute("commentForm");
            mav.addObject("errorMessages", errorMessages);
            mav.addObject("commentForm", cf);
        }
        session.removeAttribute("errorMessages");
        session.removeAttribute("commentForm");

        return mav;
    }

    /*
     * 新規投稿画面表示
     */
    @GetMapping("/new")
    public ModelAndView newContent() {
        ModelAndView mav = new ModelAndView();
        // form用の空のentityを準備
        ReportForm reportForm = new ReportForm();
        // 画面遷移先を指定
        mav.setViewName("/new");
        // 準備した空のFormを保管
        mav.addObject("formModel", reportForm);
        return mav;
    }

    /*
     * 新規投稿処理
     */
    @PostMapping("/add")
    public ModelAndView addContent(@ModelAttribute("formModel") @Validated ReportForm reportForm, BindingResult result){

        if(result.hasErrors()){
            ModelAndView mav = new ModelAndView();
            mav.addObject("formModel", reportForm);
            mav.setViewName("/new");
            return mav;
        }else {
            // 投稿をテーブルに格納
            reportService.saveReport(reportForm);
            // rootへリダイレクト
            return new ModelAndView("redirect:/");
        }
    }

    /*
     *コメントの登録
     */
    @PostMapping("/comment/add/{reportId}")
    public ModelAndView addComment(@PathVariable("reportId") Integer reportId,
                                   @ModelAttribute("commentForm") @Validated CommentForm commentForm, BindingResult result, HttpServletRequest request){

        if(result.hasErrors()){
            HttpSession session = request.getSession();
            List<String> errorMessages = new ArrayList<String>();
            //resultからデフォルトのエラーメッセージを取得
            //入っているerrorsの分だけ回してListに詰める
            for(ObjectError error : result.getAllErrors()){
                errorMessages.add(error.getDefaultMessage());
            }
            //リダイレクト先でも使えるようにsession領域に格納
            this.session.setAttribute("errorMessages", errorMessages);
            this.session.setAttribute("commentForm", commentForm);

            ModelAndView mav = new ModelAndView();
            mav.setViewName("redirect:/");
            return mav;
        }else {
            // コメントした投稿のcontent.idをhtmlから受け取り、フォームにセット
            // contentはhtmlですでに入力内容が詰められてる（idは後から自動で付与される）
            commentForm.setReportId(reportId);
            commentService.saveComment(commentForm);
            //reportIdでコメントを追加した投稿の情報を取得
            //コメントの追加とともに、コメントした投稿の更新日時も更新したい
            ReportForm reportForm = new ReportForm();
            reportForm.setId(reportId);
            ReportForm updateData = reportService.editReport(reportId);
            // 取得した投稿のデータを更新しに行く(実際updatedDateだけが更新される)
            reportService.saveReport(updateData);

            return new ModelAndView("redirect:/");
        }
    }

    /*
     *編集画面表示
     */
    @GetMapping("/edit/{id}")
    public ModelAndView editContent(@PathVariable("id") Integer id){
        ModelAndView mav = new ModelAndView();

        ReportForm reportForm = new ReportForm();

        ReportForm editData = reportService.editReport(id);

        mav.setViewName("/edit");
        // selectしてきた編集対象の投稿データを保管
        mav.addObject("formModel", editData);

        return mav;
    }

    /*
     *編集対象のコメントを探す（htmlから受け取ったidで）
     * 見つかったらそのままコメント用の編集画面を表示
     */
    @GetMapping("/comment/edit/{id}")
    public ModelAndView editComment(@PathVariable("id") Integer id){
        ModelAndView mav = new ModelAndView();

        CommentForm commentForm = new CommentForm();

        CommentForm editData = commentService.editComment(id);

        mav.setViewName("/editComment");
        // selectしてきた編集対象のコメントデータを保管
        mav.addObject("commentForm", editData);

        return mav;

    }

    /*
     *編集内容の更新（投稿用）
     */
    @PutMapping("/update/{id}")
    public ModelAndView editContent(@PathVariable("id") Integer id,
                                    @ModelAttribute("formModel") @Validated ReportForm report, BindingResult result){

        if(result.hasErrors()){
            ModelAndView mav = new ModelAndView();
            mav.addObject("formModel", report);
            mav.setViewName("/edit");
            return mav;
        }else {
            report.setId(id);
            reportService.saveReport(report);

            return new ModelAndView("redirect:/");
        }
    }

    /*
     *編集内容を更新（コメント用）
     */
    @PutMapping("/comment/update/{id}")
    public ModelAndView editComment(@PathVariable("id") Integer id,
                                    @ModelAttribute("commentForm") @Validated CommentForm comment, BindingResult result){

        if(result.hasErrors()){
            ModelAndView mav = new ModelAndView();
            mav.addObject("commentForm", comment);
            mav.setViewName("/editComment");
            return mav;
        }else {
            comment.setId(id);
            // setしたidに紐づくコメントを更新しに行く
            commentService.saveComment(comment);

            return new ModelAndView("redirect:/");
        }
    }



    /*
     *書き込み削除機能
     */

    @DeleteMapping("/delete/{id}")
    // urlに含まれる{id}がid変数にマッピングされる
    // 変数に{id}の情報が渡された状態でコントローラー側で使えるようになる
    public ModelAndView deleteUserOne(@PathVariable("id") Integer id) {

        // 特定の書き込みの削除
        reportService.delete(id);
        return new ModelAndView("redirect:/");
    }

    @DeleteMapping("/comment/delete/{id}")
    public ModelAndView deleteComment(@PathVariable("id") Integer id){

        commentService.delete(id);
        return new ModelAndView("redirect:/");
    }

}

