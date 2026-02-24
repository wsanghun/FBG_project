package com.example.demo.DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberDTO {
    private Long idx;
    // DB에서 자동 생성되는 idx와 regdate는 제외하고 폼 입력 값만 받습니다.
    private String userid; // 폼에서 'userId'로 전송됨
    private String userpwd;
    private String name;
    private String gender;
    private String yy;
    private String mm;
    private String dd;
    // 이메일은 DB에 한 필드로 저장한다고 가정하고 임시 필드 추가
    private String emailprefix;
    private String emaildomain;

    // DB 저장을 위한 최종 필드 (MEMBER 테이블 구조)
    private String birth; // YYYY-MM-DD 형식으로 합쳐서 저장할 필드
    private String email; // 최종 이메일 주소
    private int level = 0; // DB DEFAULT 0 설정과 일치

    public String getBirth() {
        // 생년월일 필드를 YYYY-MM-DD 형태로 조합
        if (yy != null && mm != null && dd != null) {
            // 월/일이 한 자리일 경우 앞에 0을 붙여 두 자리로 만듭니다.
            String month = String.format("%02d", Integer.parseInt(mm));
            String day = String.format("%02d", Integer.parseInt(dd));
            return yy + "-" + month + "-" + day;
        }
        return null;
    }


    public String getEmail() {
        // 이메일 주소를 조합합니다.
        if (emailprefix != null && emaildomain != null) {
            return emailprefix + "@" + emaildomain;
        }
        return null;
    }

    public int getlevel() {
        return level;
    }

    public void setClassValue(int level) {
        this.level = level;
    }
}