package com.example.demo.ServiceImage;

import com.example.demo.Entity.ImageEntity;
import com.example.demo.Repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService{

    @Value("${file.upload-dir}")
    private String uploadPath;

    private final ImageRepository imageRepository;

    @Override
    public String processBase64Images(String content, String userId) {

        if (content == null) return null;

        Pattern base64Pattern =
                Pattern.compile("<img[^>]+src=['\"]data:(image/[^;]+);base64,([^'\"]+)['\"][^>]*>");

        Matcher matcher = base64Pattern.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String mimeType = matcher.group(1);
            String base64Data = matcher.group(2);

            String ext = mimeType.split("/")[1];

            byte[] fileBytes = Base64.getDecoder().decode(base64Data);

            String today = LocalDate.now().toString();
            File folder = new File(uploadPath + today);
            if (!folder.exists()) folder.mkdirs();

            String fileName = UUID.randomUUID() + "." + ext;
            String savePath = uploadPath + today + "/" + fileName;

            try (FileOutputStream fos = new FileOutputStream(savePath)) {
                fos.write(fileBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ⭐ DB 저장
            ImageEntity img = ImageEntity.builder()
                    .boardIdx(null)
                    .member(null)
                    .fileName(fileName)
                    .originalName(fileName)
                    .fileUrl("/upload/" + today + "/" + fileName)
                    .fileSize(fileBytes.length)
                    .regDate(LocalDateTime.now())
                    .type("BOARD")
                    .build();

            imageRepository.save(img);

            // ⭐ HTML의 img src를 실제 파일 URL로 교체
            matcher.appendReplacement(sb,
                    "<img src=\"" + img.getFileUrl() + "\" />");
        }

        matcher.appendTail(sb);
        return sb.toString();
    }



    /** 날짜 기반 폴더 생성 */
    private String makeUploadFolder() {
        String folderName = LocalDate.now().toString();
        File folder = new File(uploadPath + folderName);
        if (!folder.exists()) folder.mkdirs();
        return folderName;
    }
}
