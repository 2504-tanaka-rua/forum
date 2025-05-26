package com.example.forum.service;

import com.example.forum.controller.form.ReportForm;
import com.example.forum.repository.ReportRepository;
import com.example.forum.repository.entity.Report;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ReportService {
    @Autowired
    ReportRepository reportRepository;

    /*
     * レコード全件取得処理
     */
    public List<ReportForm> findAllReport() {
        List<Report> results = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedDate"));
        List<ReportForm> reports = setReportForm(results);
        return reports;
    }

    public List<ReportForm> findDesignatedDayReport(String start, String end) throws ParseException {
        if (StringUtils.isBlank(start)) {
            start = "2020-01-01 00:00:00";
        } else {
            start = start + " 00:00:00";
        }

        if (StringUtils.isBlank(end)) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //現在時刻の情報を取得
            end = sdf.format(calendar.getTime());
        } else {
            end = end + " 59:59:59";
        }

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date startDate = sdFormat.parse(start);
        Date endDate = sdFormat.parse(end);
        List<Report> results = reportRepository.findByCreatedDateBetweenOrderByUpdatedDate(startDate, endDate);
        List<ReportForm> reports = setReportForm(results);
        return reports;
    }

    /*
     * DBから取得したデータをFormに設定
     */
    private List<ReportForm> setReportForm(List<Report> results) {
        List<ReportForm> reports = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            ReportForm report = new ReportForm();
            Report result = results.get(i);
            report.setId(result.getId());
            report.setContent(result.getContent());
            report.setCreatedDate(result.getCreatedDate());
            report.setUpdatedDate(result.getUpdatedDate());
            reports.add(report);
        }
        return reports;
    }

    /*
     *編集したい書き込みを取得
     */
    public ReportForm editReport(Integer id) {

        List<Report> results = new ArrayList<>();
        results.add((Report) reportRepository.findById(id).orElse(null));
        List<ReportForm> reports = setReportForm(results);

        return reports.get(0);
    }

    /*
     * レコード追加
     */
    public void saveReport(ReportForm reqReport) {
        Report saveReport = setReportEntity(reqReport);
        reportRepository.save(saveReport);
    }

    /*
     * リクエストから取得した情報をEntityに設定
     */
    private Report setReportEntity(ReportForm reqReport) {
        Report report = new Report();
        report.setId(reqReport.getId());
        report.setContent(reqReport.getContent());
        Calendar cl = Calendar.getInstance();
        report.setUpdatedDate(cl.getTime());
        return report;
    }

    public void delete(Integer id) {
        reportRepository.deleteById(id);
    }

}

