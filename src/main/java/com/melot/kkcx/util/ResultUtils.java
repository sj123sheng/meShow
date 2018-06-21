package com.melot.kkcx.util;

import com.melot.kktv.base.Result;

/**
 * Title: ResultUtils
 * <p>
 * Description:
 * </p>
 *
 * @author <a href="mailto:baolin.zhu@melot.cn">朱宝林</a>
 * @version V1.0
 * @since 2018/6/12 14:04
 */
public class ResultUtils {
    private ResultUtils() {}

    public static boolean checkResultNotNull(Result result) {
        return result != null && result.getData() != null;
    }
}
