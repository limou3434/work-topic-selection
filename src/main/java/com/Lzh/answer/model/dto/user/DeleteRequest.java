package com.Lzh.answer.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 *  
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private String userAccount;

    private static final long serialVersionUID = 1L;
}