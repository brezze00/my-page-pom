package com.tj.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * author tongjia
 * create_time 2023/3/7
 **/
@Data
@Accessors(chain = true)
public class Page {
    private Integer pageNum;

    private Integer pageSize;

    private Integer pageMix;
}
