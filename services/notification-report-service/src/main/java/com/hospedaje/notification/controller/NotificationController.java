package com.hospedaje.notification.controller;
import com.hospedaje.notification.document.NotificationLog;
import com.hospedaje.notification.dto.ApiResponse;
import com.hospedaje.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<NotificationLog>>> history(){
        return ResponseEntity.ok(ApiResponse.success("OK",notificationService.getHistory()));}
}
