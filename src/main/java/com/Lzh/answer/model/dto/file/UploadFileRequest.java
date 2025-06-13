package com.Lzh.answer.model.dto.file;

import java.io.Serializable;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求
 *
 *  
 */
@Data
public class UploadFileRequest implements Serializable {
    private MultipartFile file;
    private Integer status;
    private static final long serialVersionUID = 1L;
}