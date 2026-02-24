package com.example.demo.Controller;

import com.example.demo.Entity.ImageEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.ImageRepository;
import com.example.demo.Repository.MemberRepository;
import com.example.demo.ServiceMember.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    @Value("${file.upload-dir}")
    private String uploadPath;

    @ResponseBody
    @PostMapping("/upload/image")
    public Map<String, Object> uploadImage(
            @RequestParam("upload") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws Exception {

        System.out.println("🔥🔥🔥 업로드 컨트롤러 실행됨");

        if (file.isEmpty()) {
            throw new RuntimeException("업로드된 파일이 없습니다.");
        }

        // 날짜 폴더 생성
        String folderPath = makeUploadFolder();
        String fullFolderPath = uploadPath + folderPath + "/";

        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + file.getOriginalFilename();
        String savePath = fullFolderPath + fileName;

        File dest = new File(savePath);

        System.out.println("📁 저장 경로: " + savePath);
        System.out.println("📁 폴더 존재?: " + new File(fullFolderPath).exists());
        System.out.println("📁 파일 저장 시도...");

        try {
            file.transferTo(dest);
            System.out.println("✅ 파일 저장 성공!");
        } catch (Exception e) {
            System.out.println("❌ 파일 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }

        String url = "/upload/" + folderPath + "/" + fileName;

        ImageEntity image = ImageEntity.builder()
                .boardIdx(null)
                .member(userDetails != null ? userDetails.getMemberEntity() : null)  // ⭐ 로그인 안해도 null 저장
                .fileName(fileName)
                .originalName(file.getOriginalFilename())
                .fileUrl(url)
                .fileSize((int) file.getSize())
                .regDate(LocalDateTime.now())
                .type("BOARD")
                .build();

        imageRepository.save(image);


        Map<String, Object> result = new HashMap<>();
        result.put("uploaded", true);  // ⭐ 필수
        result.put("url", url);

        return result;
    }

    /* =============================
       날짜 폴더 자동 생성 메서드
       ============================= */
    private String makeUploadFolder() {
        String folderName = LocalDate.now().toString();  // "2025-01-30"

        File folder = new File(uploadPath + folderName);
        if (!folder.exists()) {
            folder.mkdirs(); // 폴더가 없으면 생성
        }

        return folderName; // "2025-01-30" 반환
    }

    @PostMapping("/user/uploadProfile")
    public String uploadProfile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws Exception {

        MemberEntity member = userDetails.getMemberEntity();

        if (userDetails == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        member = userDetails.getMemberEntity();

        if (file.isEmpty()) {
            throw new RuntimeException("업로드된 파일이 없습니다.");
        }

        // 날짜 폴더
        String folderPath = makeUploadFolder();
        String fullFolderPath = uploadPath + folderPath + "/";

        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + "_" + file.getOriginalFilename();
        String savePath = fullFolderPath + fileName;

        file.transferTo(new File(savePath));

        String url = "/upload/" + folderPath + "/" + fileName;

        // ⭐ 기존 프로필 이미지 삭제(선택)
        // 필요하면 구현: imageRepository.findByType... 해서 삭제

        // ⭐ DB에 이미지 저장(type=PROFILE)
        ImageEntity image = ImageEntity.builder()
                .boardIdx(null)
                .member(member)
                .fileName(fileName)
                .originalName(file.getOriginalFilename())
                .fileUrl(url)
                .fileSize((int) file.getSize())
                .type("PROFILE")
                .build();

        imageRepository.save(image);

        // ⭐ MemberEntity에 프로필 이미지 적용
        member.setProfileImage(url);
        memberRepository.save(member);

        return "redirect:/user/view?idx=" + member.getIdx();
    }
}