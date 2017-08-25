package com.melot.kktv.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: ConfigService
 * @author: shengjian
 * @date: 2017/8/25
 * @copyright: Copyright (c)2017
 * @company: melot
 * <p>
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2017/8/25           shengjian     1.0
 */
@Service
public class ConfigService {

    @Autowired
    private String disconfHttpdir;

    public String getDisconfHttpdir() {
        return disconfHttpdir;
    }

    public void setDisconfHttpdir(String disconfHttpdir) {
        this.disconfHttpdir = disconfHttpdir;
    }
}
